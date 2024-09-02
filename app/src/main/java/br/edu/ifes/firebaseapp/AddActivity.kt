package br.edu.ifes.firebaseapp

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import br.edu.ifes.firebaseapp.databinding.ActivityAddBinding

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FieldValue

class AddActivity : AppCompatActivity() {

	private lateinit var binding: ActivityAddBinding

	private lateinit var auth: FirebaseAuth

    private lateinit var db: FirebaseFirestore

	override fun onCreate(savedInstanceState: Bundle?) {
    	    	super.onCreate(savedInstanceState)

    	    	// Inicialize o View Binding
    	    	binding = ActivityAddBinding.inflate(layoutInflater)
    	    	setContentView(binding.root)

    	    	// Inicialize o FirebaseAuth
    	    	auth = FirebaseAuth.getInstance()

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
                        val message = hashMapOf(
                            "title" to title,
                            "date" to FieldValue.serverTimestamp()
                        )

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
}