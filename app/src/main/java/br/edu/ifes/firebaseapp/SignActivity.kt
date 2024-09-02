package br.edu.ifes.firebaseapp

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import br.edu.ifes.firebaseapp.databinding.ActivitySignBinding

import com.google.firebase.auth.FirebaseAuth

class SignActivity : AppCompatActivity() {

	private lateinit var binding: ActivitySignBinding

	private lateinit var auth: FirebaseAuth

	override fun onCreate(savedInstanceState: Bundle?) {
    	    	super.onCreate(savedInstanceState)

    	    	// Inicialize o View Binding
    	    	binding = ActivitySignBinding.inflate(layoutInflater)
    	    	setContentView(binding.root)

    	    	// Inicialize o FirebaseAuth
    	    	auth = FirebaseAuth.getInstance()

    	    	// Configurar cliques nos botões usando binding
    	    	binding.signButton.setOnClickListener {
        	    	    	val email = binding.emailEditText.text.toString().trim()
        	    	    	val password = binding.passwordEditText.text.toString().trim()
        	    	    	val confirm = binding.confirmPasswordEditText.text.toString().trim()

        	    	    	if (email.isEmpty() || password.isEmpty() || confirm.isEmpty()) {
            	    	    	Toast.makeText(this, "Preencha todos os campos", Toast.LENGTH_SHORT).show()
            	    	    	return@setOnClickListener
        	    	    	}

        	    	    	if(!confirm.equals(password)) {
            	    	    	Toast.makeText(this, "Confirme a mesma senha", Toast.LENGTH_SHORT).show()
            	    	    	return@setOnClickListener
        	    	    	}

        	    	    	auth.createUserWithEmailAndPassword(email, password)
            	    	    	.addOnCompleteListener(this) { task ->
                	    	    	if (task.isSuccessful) {
                    	    	    	    	// Registro bem-sucedido
                    	    	    	    	val user = auth.currentUser
                    	    	    	    	startActivity(Intent(this, MainActivity::class.java))
                    	    	    	    	finish()
                	    	    	} else {
                    	    	    	    	// Falha no registro
                    	    	    	    	Toast.makeText(this, "Criação falhou.", Toast.LENGTH_SHORT).show()
                	    	    	}
            	    	}
    	    	}
	}
}