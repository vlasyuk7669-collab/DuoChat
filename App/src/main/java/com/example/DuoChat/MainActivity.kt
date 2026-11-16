package com.example.duochat

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Send
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.Query

private val TelegramBlue = Color(0xFF229ED9)
private val ChatBackground = Color(0xFFE7EBF0)

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent { DuoChatApp() }
    }
}

data class ChatMessage(
    val id: String = "",
    val text: String = "",
    val senderId: String = "",
    val createdAt: Long = 0L
)

@Composable
fun DuoChatApp() {
    val auth = remember { FirebaseAuth.getInstance() }
    var user by remember { mutableStateOf(auth.currentUser) }

    MaterialTheme(
        colorScheme = lightColorScheme(
            primary = TelegramBlue,
            background = ChatBackground,
            surface = Color.White
        )
    ) {
        LaunchedEffect(Unit) {
            auth.addAuthStateListener { user = it.currentUser }
        }
        if (user == null) {
            AuthScreen(
                onLogin = { email, password, create, result ->
                    val task = if (create) {
                        auth.createUserWithEmailAndPassword(email, password)
                    } else {
                        auth.signInWithEmailAndPassword(email, password)
                    }
                    task.addOnCompleteListener { result(it.exception?.localizedMessage) }
                }
            )
        } else {
            ChatScreen(onLogout = { auth.signOut() })
        }
    }
}

@Composable
fun AuthScreen(
    onLogin: (String, String, Boolean, (String?) -> Unit) -> Unit
) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var creating by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf<String?>(null) }
    var busy by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier.fillMaxSize().background(ChatBackground).padding(24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("DuoChat", style = MaterialTheme.typography.headlineLarge,
            color = TelegramBlue, fontWeight = FontWeight.Bold)
        Text("Личный чат для двоих", color = Color.DarkGray)
        Spacer(Modifier.height(28.dp))
        OutlinedTextField(email, { email = it }, label = { Text("Электронная почта") },
            singleLine = true, modifier = Modifier.fillMaxWidth())
        Spacer(Modifier.height(10.dp))
        OutlinedTextField(password, { password = it }, label = { Text("Пароль") },
            singleLine = true, visualTransformation = PasswordVisualTransformation(),
            modifier = Modifier.fillMaxWidth())
        Spacer(Modifier.height(12.dp))
        error?.let { Text(it, color = MaterialTheme.colorScheme.error) }
        Button(
            enabled = !busy && email.isNotBlank() && password.length >= 6,
            modifier = Modifier.fillMaxWidth(),
            onClick = {
                busy = true
                error = null
                onLogin(email.trim(), password, creating) {
                    busy = false
                    error = it
                }
            }
        ) {
            Text(if (creating) "Создать аккаунт" else "Войти")
        }
        TextButton(onClick = { creating = !creating; error = null }) {
            Text(if (creating) "Уже есть аккаунт? Войти" else "Нет аккаунта? Зарегистрироваться")
        }
        Text("Пароль должен содержать минимум 6 символов",
            style = MaterialTheme.typography.bodySmall, color = Color.Gray)
    }
}

@Composable
fun ChatScreen(onLogout: () -> Unit) {
    val db = remember { FirebaseFirestore.getInstance() }
    val currentUid = FirebaseAuth.getInstance().currentUser?.uid.orEmpty()
    var messages by remember { mutableStateOf(listOf<ChatMessage>()) }
    var draft by remember { mutableStateOf("") }
    var error by remember { mutableStateOf<String?>(null) }

    DisposableEffect(Unit) {
        val listener: ListenerRegistration = db.collection("chats")
            .document("private")
            .collection("messages")
            .orderBy("createdAt", Query.Direction.ASCENDING)
            .addSnapshotListener { snapshot, exception ->
                if (exception != null) {
                    error = exception.localizedMessage ?: "Не удалось загрузить сообщения"
                } else {
                    messages = snapshot?.documents?.map { doc ->
                        ChatMessage(
                            id = doc.id,
                            text = doc.getString("text") ?: "",
                            senderId = doc.getString("senderId") ?: "",
                            createdAt = doc.getLong("createdAt") ?: 0L
                        )
                    } ?: emptyList()
                }
            }
        onDispose { listener.remove() }
    }

    Column(Modifier.fillMaxSize().background(ChatBackground)) {
        Row(
            Modifier.fillMaxWidth().background(TelegramBlue).padding(horizontal = 16.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(Modifier.weight(1f)) {
                Text("DuoChat", color = Color.White, fontWeight = FontWeight.Bold,
                    style = MaterialTheme.typography.titleLarge)
                Text("Личный чат", color = Color.White.copy(alpha = 0.85f),
                    style = MaterialTheme.typography.bodySmall)
            }
            TextButton(onClick = onLogout) {
                Text("Выйти", color = Color.White)
            }
        }

        error?.let {
            Text(it, color = Color.Red, modifier = Modifier.padding(8.dp))
        }

        LazyColumn(
            modifier = Modifier.weight(1f).fillMaxWidth().padding(horizontal = 10.dp),
            contentPadding = PaddingValues(vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(7.dp)
        ) {
            items(messages, key = { it.id }) { message ->
                val mine = message.senderId == currentUid
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = if (mine) Arrangement.End else Arrangement.Start
                ) {
                    Surface(
                        color = if (mine) Color(0xFFD8FDD2) else Color.White,
                        shape = RoundedCornerShape(
                            topStart = 16.dp, topEnd = 16.dp,
                            bottomStart = if (mine) 16.dp else 4.dp,
                            bottomEnd = if (mine) 4.dp else 16.dp
                        ),
                        shadowElevation = 1.dp,
                        modifier = Modifier.widthIn(max = 300.dp)
                    ) {
                        Text(message.text, modifier = Modifier.padding(horizontal = 12.dp, vertical = 9.dp))
                    }
                }
            }
        }

        Row(
            Modifier.fillMaxWidth().background(Color.White).padding(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            OutlinedTextField(
                value = draft,
                onValueChange = { draft = it },
                modifier = Modifier.weight(1f),
                placeholder = { Text("Сообщение...") },
                maxLines = 4,
                shape = RoundedCornerShape(24.dp)
            )
            Spacer(Modifier.width(8.dp))
            IconButton(
                enabled = draft.isNotBlank(),
                onClick = {
                    val textToSend = draft.trim()
                    if (textToSend.isNotEmpty()) {
                        val data = hashMapOf(
                            "text" to textToSend,
                            "senderId" to currentUid,
                            "createdAt" to System.currentTimeMillis()
                        )
                        db.collection("chats").document("private")
                            .collection("messages").add(data)
                            .addOnFailureListener { error = it.localizedMessage }
                        draft = ""
                    }
                },
                modifier = Modifier.size(48.dp).background(TelegramBlue, CircleShape)
            ) {
                Icon(Icons.Filled.Send, contentDescription = "Отправить", tint = Color.White)
            }
        }
    }
}
