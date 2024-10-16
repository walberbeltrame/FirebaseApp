package br.edu.ifes.firebaseapp.ui.home

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import br.edu.ifes.firebaseapp.databinding.ItemMessageBinding

import com.google.firebase.Timestamp
import com.google.firebase.firestore.GeoPoint
import java.text.SimpleDateFormat
import java.util.Locale

data class Message(
    var id: String? = null,
    val title: String = "",
    val light: Float? = null,
    val location: GeoPoint? = null,
    val date: Timestamp? = null
)

class MessagesAdapter(private val messagesList: MutableList<Message>, private val onMessageClick: (Message) -> Unit) :
    RecyclerView.Adapter<MessagesAdapter.MessageViewHolder>() {

    // ViewHolder que representa a visualização de cada item da lista usando View Binding
    class MessageViewHolder(val binding: ItemMessageBinding) : RecyclerView.ViewHolder(binding.root)

    // Cria novas visualizações (chamado pelo layout manager)
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MessageViewHolder {
        val binding = ItemMessageBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return MessageViewHolder(binding)
    }

    // Vincula os dados da tarefa à visualização
    override fun onBindViewHolder(holder: MessageViewHolder, position: Int) {
        val message = messagesList[position]
        holder.binding.messageTitle.text = message.title
        holder.binding.messageLight.text = if (message.light != null) {
            "Luminosidade: ${message.light}"
        } else {
            "Luminosidade não disponível"
        }
        val location = message.location as? GeoPoint
        holder.binding.messageLocation.text = if (location != null) {
            "Latitude: ${location.latitude}, Longitude: ${location.longitude}"
        } else {
            "Localização não disponível"
        }
        val date = message.date?.toDate()
        if (date != null) {
            val formatter = SimpleDateFormat("dd MMMM yyyy HH:mm:ss", Locale.getDefault())
            holder.binding.messageDate.text = formatter.format(date)
        }
        // Configura o clique no item da lista
        holder.binding.root.setOnClickListener {
            onMessageClick(message)
        }
    }

    // Retorna o número de itens na lista
    override fun getItemCount(): Int {
        return messagesList.size
    }

    // Método para atualizar mensagens à lista e notificar o RecyclerView
    fun updateMessages(messages: List<Message>) {
        messagesList.clear();
        messagesList.addAll(messages)
        notifyDataSetChanged()
    }
}
