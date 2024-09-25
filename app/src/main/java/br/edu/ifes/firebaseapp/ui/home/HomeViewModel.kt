package br.edu.ifes.firebaseapp.ui.home

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

import com.google.firebase.auth.FirebaseAuth

import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.ListenerRegistration

class HomeViewModel : ViewModel() {

   private val _messages = MutableLiveData<List<Message>>()
    val messages: LiveData<List<Message>> get() = _messages

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> get() = _isLoading

    private val _isEmpty = MutableLiveData<Boolean>()
    val isEmpty: LiveData<Boolean> get() = _isEmpty

    private val auth: FirebaseAuth = FirebaseAuth.getInstance()
    private val db: FirebaseFirestore = FirebaseFirestore.getInstance()
    private var pageSize = 10
    private lateinit var messagesRef: Query

    private var listenerRegistration: ListenerRegistration? = null

    init {
        fetchMessages()
    }

    private fun fetchMessages() {
        _isLoading.value = true

        // Obtém o ID do usuário autenticado
        val currentUser = auth.currentUser

        if (currentUser != null) {

            val userId = currentUser.uid

            messagesRef = db.collection("users")
                .document(userId)
                .collection("messages")
                .orderBy("date", Query.Direction.DESCENDING)
                .limit(pageSize.toLong())

            listenerRegistration?.remove()

            listenerRegistration = messagesRef.addSnapshotListener { snapshot, e ->
                _isLoading.value = false

                if (e != null) {
                    return@addSnapshotListener
                }

                if (snapshot != null && !snapshot.isEmpty) {
                    val messagesList = mutableListOf<Message>()
                    for (document in snapshot.documents) {
                        val message = document.toObject(Message::class.java)
                        // Recupera o ID e adiciona ao objeto Message
                        message?.id = document.id
                        messagesList.add(message!!)
                    }

                    _messages.value = messagesList
                    _isEmpty.value = messagesList.isEmpty()

                } else {
                    _isEmpty.value = true
                }
            }
        } else {
            _isLoading.value = false
            _isEmpty.value = true
        }
    }

    override fun onCleared() {
        super.onCleared()
        listenerRegistration?.remove()
    }

    fun loadMoreMessages() {
        pageSize += 10
        fetchMessages()
    }

}