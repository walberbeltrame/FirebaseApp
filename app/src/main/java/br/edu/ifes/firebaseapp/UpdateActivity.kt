package br.edu.ifes.firebaseapp

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AlertDialog
import br.edu.ifes.firebaseapp.databinding.ActivityUpdateBinding

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FieldValue

class UpdateActivity : AppCompatActivity() {

	private lateinit var binding: ActivityUpdateBinding

	private lateinit var auth: FirebaseAuth

    private lateinit var db: FirebaseFirestore

	override fun onCreate(savedInstanceState: Bundle?) {
    	    	super.onCreate(savedInstanceState)

    	    	// Inicialize o View Binding
    	    	binding = ActivityUpdateBinding.inflate(layoutInflater)
    	    	setContentView(binding.root)

    	    	// Inicialize o FirebaseAuth
    	    	auth = FirebaseAuth.getInstance()

                // Recuperar o ID da mensagem passada pela Intent
                val messageId = intent.getStringExtra("MESSAGE_ID")
                val messageTitle = intent.getStringExtra("MESSAGE_TITLE")
                
                // Exibir a mensagem atual no EditText
                binding.titleEditText.setText(messageTitle)

    	    	// Configurar cliques nos botões usando binding
    	    	binding.updateButton.setOnClickListener {
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

                        // Atualiza a tarefa à subcoleção 'messages' do usuário
                        db.collection("users").document(userId).collection("messages")
                            .document(messageId!!)
                            .update("title", title)
                            .addOnSuccessListener {
                                Toast.makeText(this, "Mensagem atualizada com sucesso!", Toast.LENGTH_SHORT).show()
                                finish() // Fecha a atividade após adicionar
                            }
                            .addOnFailureListener { event ->
                                Toast.makeText(this, "Erro ao atualizar mensagem: ${event.message}", Toast.LENGTH_SHORT).show()
                            }
        	    	    
    	    	}

                // Configura o botão de excluir com confirmação
                binding.deleteButton.setOnClickListener {
                    val currentUser = auth.currentUser

                    if (currentUser == null) {
                        Toast.makeText(this, "Usuário não logado", Toast.LENGTH_SHORT).show()
                        return@setOnClickListener
                    }

                    // Obtém o ID do usuário autenticado
                        val userId = currentUser.uid
                        
                    // Inicialize o FirebaseFirestore
                    db = FirebaseFirestore.getInstance()

                    AlertDialog.Builder(this)
                    .setTitle("Excluir tarefa")
                    .setMessage("Você tem certeza que deseja excluir esta mensagem?")
                    .setPositiveButton("Sim") { dialog, _ ->
                        db.collection("users").document(userId).collection("messages")
                            .document(messageId!!)
                            .delete()
                            .addOnSuccessListener {
                                Toast.makeText(this, "Mensagem excluída com sucesso", Toast.LENGTH_SHORT).show()
                                finish()  // Fecha a atividade após excluir
                            }
                            .addOnFailureListener { e ->
                                Toast.makeText(this, "Erro ao excluir a mensagem", Toast.LENGTH_SHORT).show()
                            }
                        dialog.dismiss()
                    }
                    .setNegativeButton("Cancelar") { dialog, _ ->
                        dialog.dismiss()
                    }
                    .create()
                    .show()
                }
	}
}