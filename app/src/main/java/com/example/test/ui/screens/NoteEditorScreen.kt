package com.example.test.ui.screens

import android.net.Uri
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.*
import coil.compose.AsyncImage
import com.example.test.data.models.ListItem
import com.example.test.data.models.Note
import com.example.test.ui.viewmodels.NoteViewModel

val Stickers = listOf(
    "😊","❤️","🌟","🎉","🌸","☀️","🌙","🦋",
    "🌈","✨","🔥","💫","🎵","🍀","🎨","💭",
    "🏆","🎯","💡","📚","🌺","🦄","🌊","🍓"
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NoteEditorScreen(
    noteId: Long,
    noteType: String,
    userId: Long,
    noteViewModel: NoteViewModel,
    onBack: () -> Unit
) {
    val notes by noteViewModel.notes.collectAsState()
    val existingNote = remember(noteId, notes) { notes.find { it.id == noteId } }
    val type = existingNote?.type ?: noteType

    var title by remember { mutableStateOf(existingNote?.title ?: "") }
    var content by remember { mutableStateOf(existingNote?.content ?: "") }
    var color by remember { mutableStateOf(existingNote?.color ?: "DEFAULT") }
    var isPinned by remember { mutableStateOf(existingNote?.isPinned ?: false) }
    var listItems by remember {
        mutableStateOf(if (existingNote != null) noteViewModel.parseListItems(existingNote.listItemsJson) else emptyList())
    }
    var photoUris by remember {
        mutableStateOf(existingNote?.photoUris?.split(",")?.filter { it.isNotEmpty() } ?: emptyList())
    }
    var stickers by remember {
        mutableStateOf(existingNote?.stickers?.split(",")?.filter { it.isNotEmpty() } ?: emptyList())
    }
    var showColorPicker by remember { mutableStateOf(false) }
    var showStickerPicker by remember { mutableStateOf(false) }
    var newItemText by remember { mutableStateOf("") }

    val bgColor = NoteColorMap[color] ?: Color.White

    val photoPicker = rememberLauncherForActivityResult(ActivityResultContracts.GetMultipleContents()) { uris ->
        photoUris = photoUris + uris.map { it.toString() }
    }

    fun save() {
        val note = Note(
            id = if (noteId != -1L) noteId else 0L,
            userId = userId,
            title = title, content = content, type = type, color = color,
            listItemsJson = noteViewModel.serializeListItems(listItems),
            photoUris = photoUris.joinToString(","),
            stickers = stickers.joinToString(","),
            isPinned = isPinned
        )
        if (noteId == -1L) noteViewModel.addNote(note) else noteViewModel.updateNote(note)
    }

    BackHandler { save(); onBack() }

    Scaffold(
        topBar = {
            TopAppBar(
                navigationIcon = {
                    IconButton(onClick = { save(); onBack() }) {
                        Icon(Icons.Default.ArrowBack, "Back")
                    }
                },
                title = {},
                actions = {
                    IconButton(onClick = { isPinned = !isPinned }) {
                        Icon(Icons.Default.PushPin, null, tint = if (isPinned) MaterialTheme.colorScheme.primary else Color.Gray)
                    }
                    IconButton(onClick = { showColorPicker = !showColorPicker }) {
                        Icon(Icons.Default.Palette, null)
                    }
                    if (type == "JOURNAL") {
                        IconButton(onClick = { photoPicker.launch("image/*") }) {
                            Icon(Icons.Default.Image, null)
                        }
                        IconButton(onClick = { showStickerPicker = !showStickerPicker }) {
                            Icon(Icons.Default.Mood, null)
                        }
                    }
                    if (noteId != -1L) {
                        IconButton(onClick = { existingNote?.let { noteViewModel.deleteNote(it) }; onBack() }) {
                            Icon(Icons.Default.Delete, null, tint = MaterialTheme.colorScheme.error)
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = bgColor)
            )
        },
        containerColor = bgColor
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp)
        ) {
            Surface(shape = RoundedCornerShape(8.dp), color = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)) {
                Text(
                    when (type) { "JOURNAL" -> "📔 Journal"; "LIST" -> "✅ List"; else -> "📝 Note" },
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                    fontSize = 12.sp, fontWeight = FontWeight.Medium, color = MaterialTheme.colorScheme.primary
                )
            }
            Spacer(Modifier.height(12.dp))

            if (showColorPicker) {
                Row(
                    modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    NoteColorMap.forEach { (key, clr) ->
                        Box(
                            modifier = Modifier
                                .size(36.dp).clip(CircleShape).background(clr)
                                .border(if (color == key) 3.dp else 1.dp,
                                    if (color == key) MaterialTheme.colorScheme.primary else Color.Gray, CircleShape)
                                .clickable { color = key }
                        )
                    }
                }
                Spacer(Modifier.height(12.dp))
            }

            if (showStickerPicker && type == "JOURNAL") {
                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                    items(Stickers.size) { i ->
                        Text(Stickers[i], fontSize = 28.sp, modifier = Modifier.clickable {
                            stickers = stickers + Stickers[i]; showStickerPicker = false
                        }.padding(4.dp))
                    }
                }
                Spacer(Modifier.height(12.dp))
            }

            if (stickers.isNotEmpty() && type == "JOURNAL") {
                Row(modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    stickers.forEachIndexed { i, s ->
                        Text(s, fontSize = 28.sp, modifier = Modifier.clickable {
                            stickers = stickers.toMutableList().also { it.removeAt(i) }
                        })
                    }
                }
                Spacer(Modifier.height(8.dp))
            }

            BasicTextField(
                value = title, onValueChange = { title = it },
                textStyle = TextStyle(fontSize = 24.sp, fontWeight = FontWeight.Bold, color = Color(0xFF1A1A1A)),
                modifier = Modifier.fillMaxWidth(),
                decorationBox = { inner ->
                    if (title.isEmpty()) Text("Title", fontSize = 24.sp, fontWeight = FontWeight.Bold, color = Color.LightGray)
                    inner()
                }
            )

            Spacer(Modifier.height(16.dp))
            HorizontalDivider(color = Color.Gray.copy(alpha = 0.2f))
            Spacer(Modifier.height(16.dp))

            when (type) {
                "LIST" -> {
                    listItems.forEachIndexed { index, item ->
                        Row(Modifier.fillMaxWidth().padding(vertical = 4.dp), verticalAlignment = Alignment.CenterVertically) {
                            Checkbox(
                                checked = item.isChecked,
                                onCheckedChange = {
                                    listItems = listItems.toMutableList().also { l -> l[index] = item.copy(isChecked = it) }
                                }
                            )
                            Spacer(Modifier.width(8.dp))
                            BasicTextField(
                                value = item.text,
                                onValueChange = { newText ->
                                    listItems = listItems.toMutableList().also { l -> l[index] = item.copy(text = newText) }
                                },
                                textStyle = TextStyle(
                                    fontSize = 16.sp,
                                    color = if (item.isChecked) Color.Gray else Color(0xFF1A1A1A),
                                    textDecoration = if (item.isChecked) TextDecoration.LineThrough else null
                                ),
                                modifier = Modifier.weight(1f),
                                decorationBox = { inner ->
                                    if (item.text.isEmpty()) Text("Item", color = Color.LightGray, fontSize = 16.sp)
                                    inner()
                                }
                            )
                            IconButton(onClick = { listItems = listItems.toMutableList().also { it.removeAt(index) } }, modifier = Modifier.size(32.dp)) {
                                Icon(Icons.Default.Close, null, modifier = Modifier.size(16.dp), tint = Color.Gray)
                            }
                        }
                    }
                    Row(Modifier.fillMaxWidth().padding(vertical = 4.dp), verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Add, null, tint = MaterialTheme.colorScheme.primary)
                        Spacer(Modifier.width(8.dp))
                        BasicTextField(
                            value = newItemText, onValueChange = { newItemText = it },
                            modifier = Modifier.weight(1f),
                            textStyle = TextStyle(fontSize = 16.sp, color = Color(0xFF1A1A1A)),
                            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                            keyboardActions = KeyboardActions(onDone = {
                                if (newItemText.isNotBlank()) {
                                    listItems = listItems + ListItem(text = newItemText)
                                    newItemText = ""
                                }
                            }),
                            decorationBox = { inner ->
                                if (newItemText.isEmpty()) Text("Add item...", color = Color.LightGray, fontSize = 16.sp)
                                inner()
                            }
                        )
                    }
                }
                else -> {
                    BasicTextField(
                        value = content, onValueChange = { content = it },
                        textStyle = TextStyle(fontSize = 16.sp, color = Color(0xFF333333), lineHeight = 24.sp),
                        modifier = Modifier.fillMaxWidth().defaultMinSize(minHeight = 200.dp),
                        decorationBox = { inner ->
                            if (content.isEmpty()) Text(
                                if (type == "JOURNAL") "What's on your mind today?" else "Start writing...",
                                color = Color.LightGray, fontSize = 16.sp
                            )
                            inner()
                        }
                    )
                }
            }

            if (photoUris.isNotEmpty() && type == "JOURNAL") {
                Spacer(Modifier.height(16.dp))
                Text("Photos", fontWeight = FontWeight.SemiBold, fontSize = 14.sp, color = Color.Gray)
                Spacer(Modifier.height(8.dp))
                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(photoUris.size) { i ->
                        Box {
                            AsyncImage(
                                model = Uri.parse(photoUris[i]),
                                contentDescription = null,
                                modifier = Modifier.size(120.dp).clip(RoundedCornerShape(12.dp)),
                                contentScale = ContentScale.Crop
                            )
                            IconButton(
                                onClick = { photoUris = photoUris.toMutableList().also { it.removeAt(i) } },
                                modifier = Modifier.align(Alignment.TopEnd).size(28.dp)
                            ) {
                                Icon(Icons.Default.Close, null, tint = Color.White, modifier = Modifier.size(16.dp))
                            }
                        }
                    }
                }
            }
        }
    }
}