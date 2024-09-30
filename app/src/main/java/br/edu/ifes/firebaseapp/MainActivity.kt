package br.edu.ifes.firebaseapp

import android.os.Bundle
import android.view.Menu
import android.widget.TextView
import android.content.Intent
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.navigation.NavigationView
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.navigateUp
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import androidx.navigation.ui.onNavDestinationSelected
import androidx.drawerlayout.widget.DrawerLayout
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AlertDialog
import br.edu.ifes.firebaseapp.databinding.ActivityMainBinding

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class MainActivity : AppCompatActivity() {

    private lateinit var appBarConfiguration: AppBarConfiguration
    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.appBarMain.toolbar)

        binding.appBarMain.fab.setOnClickListener { view ->
            val addIntent = Intent(this, AddActivity::class.java)
        	startActivity(addIntent)
        }
        val drawerLayout: DrawerLayout = binding.drawerLayout
        val navView: NavigationView = binding.navView
        val navController = findNavController(R.id.nav_host_fragment_content_main)
        // Passing each menu ID as a set of Ids because each
        // menu should be considered as top level destinations.
        appBarConfiguration = AppBarConfiguration(
            setOf(
                R.id.nav_home,
            ), drawerLayout
        )
        setupActionBarWithNavController(navController, appBarConfiguration)
        navView.setupWithNavController(navController)

        // Interceptar o clique apenas para o item de logout
        navView.setNavigationItemSelectedListener { menuItem ->
            if (menuItem.itemId == R.id.nav_logout) {
                showLogoutConfirmationDialog()
                true
            } else {
                // Para os outros itens, deixar o navController lidar com a navegação
                menuItem.onNavDestinationSelected(navController) || super.onOptionsItemSelected(menuItem)
            }
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.main, menu)

        // Verifique se o usuário está autenticado
        val user = FirebaseAuth.getInstance().currentUser
        if (user != null) {
            val userId = user.uid
            val messagesRef = FirebaseFirestore.getInstance()
                .collection("users")
                .document(userId)
                .collection("messages")
            messagesRef.addSnapshotListener { querySnapshot, error ->
                if (error != null) {
                    return@addSnapshotListener
                }
                if (querySnapshot != null) {
                    val menuItem = menu?.findItem(R.id.badge_messages)
                    val actionView = menuItem?.actionView
                    val badgeMessages = actionView?.findViewById<TextView>(R.id.count_badge)!!
                    badgeMessages.text = querySnapshot.size().toString()
                }
            }
        }

        return true
    }

    override fun onSupportNavigateUp(): Boolean {
        val navController = findNavController(R.id.nav_host_fragment_content_main)
        return navController.navigateUp(appBarConfiguration) || super.onSupportNavigateUp()
    }

    override fun onStart() {
    		super.onStart()

    		// Verifique se o usuário está autenticado
    		val user = FirebaseAuth.getInstance().currentUser
    		if (user == null) {
        			// Se não estiver logado, redirecione para a LoginActivity
        			val loginIntent = Intent(this, LoginActivity::class.java)
        			startActivity(loginIntent)
        			finish() // Finaliza a MainActivity para que o usuário não possa voltar a ela sem fazer login
    		}
	}

    // Função para mostrar a Popup de confirmação
    private fun showLogoutConfirmationDialog() {
        val builder = AlertDialog.Builder(this)
        builder.setTitle("Confirmar Logout")
        builder.setMessage("Você deseja sair da sua conta?")

        // Botão "Sair"
        builder.setPositiveButton("Sair") { dialog, _ ->
            logoutFromFirebase()
            dialog.dismiss()
        }

        // Botão "Cancelar"
        builder.setNegativeButton("Cancelar") { dialog, _ ->
            dialog.dismiss()
        }

        // Mostrar o diálogo
        builder.create().show()
    }

    // Função para deslogar do Firebase
    private fun logoutFromFirebase() {
        FirebaseAuth.getInstance().signOut()
        // Redirecionar para a tela de login (LoginActivity)
        val loginIntent = Intent(this, LoginActivity::class.java)
        startActivity(loginIntent)
        finish() // Finaliza a MainActivity
    }

}