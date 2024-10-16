package br.edu.ifes.firebaseapp

import android.Manifest
import android.content.Intent
import android.content.Context
import android.content.pm.PackageManager
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.location.Location
import android.location.LocationManager
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import br.edu.ifes.firebaseapp.databinding.ActivityAddBinding

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.GeoPoint

import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.tasks.Task

class AddActivity : AppCompatActivity() {

	private lateinit var binding: ActivityAddBinding

	private lateinit var auth: FirebaseAuth

    private lateinit var db: FirebaseFirestore

    private lateinit var sensorManager: SensorManager
    private lateinit var sensorEventListener: SensorEventListener
    private var lightSensor: Sensor? = null
    private var light: Float? = null

    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private var location: Location? = null
    private val LOCATION_PERMISSION_REQUEST_CODE = 1000

	override fun onCreate(savedInstanceState: Bundle?) {
    	    	super.onCreate(savedInstanceState)

    	    	// Inicialize o View Binding
    	    	binding = ActivityAddBinding.inflate(layoutInflater)
    	    	setContentView(binding.root)

    	    	// Inicialize o FirebaseAuth
    	    	auth = FirebaseAuth.getInstance()

                // Inicializa o SensorManager e o sensor de luz
                sensorManager = getSystemService(Context.SENSOR_SERVICE) as SensorManager
                lightSensor = sensorManager.getDefaultSensor(Sensor.TYPE_LIGHT)

                if (lightSensor != null) {
                    sensorEventListener = object : SensorEventListener {
                        override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}

                        override fun onSensorChanged(event: SensorEvent?) {
                            event?.let {
                                // Obtém o valor único de luz
                                light = it.values[0]
                                // Atualiza o TextView com o valor do sensor de luz
                                binding.lightTextView.text = "$light"
                            }
                        }
                    }

                    // Registrar o listener com o sensor de luz
                    sensorManager.registerListener(sensorEventListener, lightSensor, SensorManager.SENSOR_DELAY_NORMAL)
                }

                // Inicializar o cliente de localização
                fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

                val locationManager = getSystemService(Context.LOCATION_SERVICE) as LocationManager
                if (!locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
                    binding.locationButton.isEnabled = false
                    binding.locationTextView.text = "Ative o GPS"
                }

                // Botão para obter localização
    	    	binding.locationButton.setOnClickListener {
                    // Verificar permissão
                    if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                        != PackageManager.PERMISSION_GRANTED) {
                        // Se não tiver permissão, solicitá-la
                        ActivityCompat.requestPermissions(
                            this,
                            arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                            LOCATION_PERMISSION_REQUEST_CODE
                        )
                    } else {
                        // Se já tiver permissão, obter localização
                        getLastKnownLocation()
                    }
                }

    	    	// Configurar cliques nos botões usando binding
    	    	binding.addButton.setOnClickListener {
                        val title = binding.titleEditText.text.toString().trim()
                        val currentUser = auth.currentUser

                        if (title.isEmpty()) {
                            Toast.makeText(this, "Preencha todos os campos", Toast.LENGTH_SHORT).show()
                            return@setOnClickListener
                        }

                        if (currentUser == null) {
                            Toast.makeText(this, "Usuário não logado", Toast.LENGTH_SHORT).show()
                            return@setOnClickListener
                        }

                        // Obtém o ID do usuário autenticado
                        val userId = currentUser.uid
                        
                        // Inicialize o FirebaseFirestore
                        db = FirebaseFirestore.getInstance()

                        // Cria o objeto mensagem
                        val message = if (light != null) {
                            if (location != null) { 
                                hashMapOf(
                                "title" to title,
                                "light" to light,
                                "location" to GeoPoint(location!!.latitude, location!!.longitude),
                                "date" to FieldValue.serverTimestamp()
                                )
                            } else {
                                hashMapOf(
                                "title" to title,
                                "light" to light,
                                "date" to FieldValue.serverTimestamp()
                                )
                            }
                        } else {
                            if (location != null) {
                                hashMapOf(
                                    "title" to title,
                                    "location" to GeoPoint(location!!.latitude, location!!.longitude),
                                    "date" to FieldValue.serverTimestamp()
                                )
                            } else {
                                hashMapOf(
                                    "title" to title,
                                    "date" to FieldValue.serverTimestamp()
                                )
                            }
                        }

                        // Adiciona a tarefa à subcoleção 'messages' do usuário
                        db.collection("users").document(userId).collection("messages")
                            .add(message)
                            .addOnSuccessListener {
                                Toast.makeText(this, "Mensagem adicionada com sucesso!", Toast.LENGTH_SHORT).show()
                                finish() // Fecha a atividade após adicionar
                            }
                            .addOnFailureListener { event ->
                                Toast.makeText(this, "Erro ao adicionar mensagem: ${event.message}", Toast.LENGTH_SHORT).show()
                            }
        	    	    
    	    	}
	}

    override fun onPause() {
        super.onPause()
        if (lightSensor != null) {
            sensorManager.unregisterListener(sensorEventListener)
        }
    }

    private fun getLastKnownLocation() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
            == PackageManager.PERMISSION_GRANTED) {
            fusedLocationClient.lastLocation.addOnSuccessListener { lastKnownLocation: Location? ->
                lastKnownLocation?.let {
                    // Guarda na variável global
                    location = it
                    // Exibir a localização no TextView
                    binding.locationTextView.text = "Latitude: ${it.latitude}, Longitude: ${it.longitude}"
                } ?: run {
                    binding.locationTextView.text = "Localização não encontrada"
                }
            }
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int, permissions: Array<out String>, grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE) {
            if ((grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED)) {
                // Permissão concedida, obter localização
                getLastKnownLocation()
            } else {
                // Permissão negada
                binding.locationTextView.text = "Permissão negada"
            }
        }
    }
}