package br.edu.ifes.firebaseapp

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import br.edu.ifes.firebaseapp.databinding.ActivityLoginBinding

import com.google.firebase.auth.FirebaseAuth

class LoginActivity : AppCompatActivity() {

	private lateinit var binding: ActivityLoginBinding

	private lateinit var auth: FirebaseAuth

	override fun onCreate(savedInstanceState: Bundle?) {
    	    	super.onCreate(savedInstanceState)

    	    	// Inicialize o View Binding
    	    	binding = ActivityLoginBinding.inflate(layoutInflater)
    	    	setContentView(binding.root)

    	    	// Inicialize o FirebaseAuth
    	    	auth = FirebaseAuth.getInstance()

    	    	// Configurar cliques nos botões usando binding
    	    	binding.loginButton.setOnClickListener {
        	    	    	val email = binding.emailEditText.text.toString().trim()
        	    	    	val password = binding.passwordEditText.text.toString().trim()

        	    	    	if (email.isEmpty() || password.isEmpty()) {
            	    	    	Toast.makeText(this, "Preencha todos os campos", Toast.LENGTH_SHORT).show()
            	    	    	return@setOnClickListener
        	    	    	}

        	    	    auth.signInWithEmailAndPassword(email, password)
            	    	    .addOnCompleteListener(this) { task ->
                	    	    	if (task.isSuccessful) {
                    	    	    	    	// Entrada bem-sucedida
                    	    	    	    	val user = auth.currentUser
                    	    	    	    	startActivity(Intent(this, MainActivity::class.java))
                    	    	    	    	finish()
                	    	    	} else {
                    	    	    	    	// Falha na entrada
                    	    	    	    	Toast.makeText(this, "Autenticação falhou.", Toast.LENGTH_SHORT).show()
                	    	    	}
            	    	}
    	    	}

    	    	binding.signButton.setOnClickListener {
        	    	    	startActivity(Intent(this, SignActivity::class.java))
    	    	}
	}
}