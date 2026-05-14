package com.example.test.ui.screens

import android.net.Uri
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
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
    noteViewModel: NoteViewModel,
    onBack: () -> Unit
) {
    val colorScheme = MaterialTheme.colorScheme
    val notes by noteViewModel.notes.collectAsState()
    val existingNote = remember(noteId, notes) { notes.find { it.id == noteId } }
    val type = existingNote?.type ?: noteType

    var title by remember { mutableStateOf(existingNote?.title ?: "") }
    var content by remember { mutableStateOf(existingNote?.content ?: "") }
    var color by remember { mutableStateOf(existingNote?.color ?: "DEFAULT") }
    var isPinned by remember { mutableStateOf(existingNote?.isPinned ?: false) }
    var listItems by remember {
        mutableStateOf(
            if (existingNote != null) noteViewModel.parseListItems(existingNote.listItemsJson)
            else emptyList()
        )
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
    var isSaved by remember { mutableStateOf(false) }

    val isColored = color != "DEFAULT"
    val bgColor = if (isColored) NoteColorMap[color] ?: colorScheme.background
    else colorScheme.background
    val onBgColor = if (isColored) Color(0xFF1A1A1A) else colorScheme.onBackground
    val onBgVariant = if (isColored) Color(0xFF555555) else colorScheme.onSurfaceVariant

    val photoPicker = rememberLauncherForActivityResult(
        ActivityResultContracts.GetMultipleContents()
    ) { uris -> photoUris = photoUris + uris.map { it.toString() } }

    fun save() {
        val note = Note(
            id = if (noteId != -1L) noteId else 0L,
            title = title,
            content = content,
            type = type,
            color = color,
            listItemsJson = noteViewModel.serializeListItems(listItems),
            photoUris = photoUris.joinToString(","),
            stickers = stickers.joinToString(","),
            isPinned = isPinned
        )
        if (noteId == -1L) noteViewModel.addNote(note)
        else noteViewModel.updateNote(note)
        isSaved = true
    }

    BackHandler { save(); onBack() }

    Scaffold(
        topBar = {
            TopAppBar(
                navigationIcon = {
                    IconButton(onClick = { save(); onBack() }) {
                        Icon(Icons.Default.ArrowBack, "Back", tint = onBgColor)
                    }
                },
                title = {
                    Text(
                        when (type) { "JOURNAL" -> "📔 Journal"; "LIST" -> "✅ List"; else -> "📝 Note" },
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Medium,
                        color = colorScheme.primary
                    )
                },
                actions = {
                    IconButton(onClick = { save() }) {
                        Icon(
                            Icons.Default.Save,
                            contentDescription = "Save",
                            tint = if (isSaved) colorScheme.primary else onBgColor
                        )
                    }
                    IconButton(onClick = { isPinned = !isPinned; isSaved = false }) {
                        Icon(
                            Icons.Default.PushPin,
                            null,
                            tint = if (isPinned) colorScheme.primary else onBgVariant
                        )
                    }
                    IconButton(onClick = { showColorPicker = !showColorPicker }) {
                        Icon(Icons.Default.Palette, null, tint = onBgColor)
                    }
                    if (type == "JOURNAL") {
                        IconButton(onClick = { photoPicker.launch("image/*") }) {
                            Icon(Icons.Default.Image, null, tint = onBgColor)
                        }
                        IconButton(onClick = { showStickerPicker = !showStickerPicker }) {
                            Icon(Icons.Default.Mood, null, tint = onBgColor)
                        }
                    }
                    if (noteId != -1L) {
                        IconButton(onClick = {
                            existingNote?.let { noteViewModel.deleteNote(it) }
                            onBack()
                        }) {
                            Icon(Icons.Default.Delete, null, tint = colorScheme.error)
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
            if (showColorPicker) {
                Row(
                    modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    NoteColorMap.forEach { (key, clr) ->
                        val displayColor = if (key == "DEFAULT") colorScheme.surface else clr
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(displayColor)
                                .border(
                                    if (color == key) 3.dp else 1.dp,
                                    if (color == key) colorScheme.primary else colorScheme.outline,
                                    CircleShape
                                )
                                .clickable { color = key; isSaved = false }
                        )
                    }
                }
                Spacer(Modifier.height(12.dp))
            }

            if (showStickerPicker && type == "JOURNAL") {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = colorScheme.surface),
                    elevation = CardDefaults.cardElevation(4.dp)
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text("Tap a sticker to add", fontSize = 12.sp, color = colorScheme.onSurfaceVariant)
                        Spacer(Modifier.height(8.dp))
                        LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            items(Stickers) { sticker ->
                                Text(sticker, fontSize = 28.sp, modifier = Modifier.clickable {
                                    stickers = stickers + sticker
                                    showStickerPicker = false
                                    isSaved = false
                                }.padding(4.dp))
                            }
                        }
                    }
                }
                Spacer(Modifier.height(12.dp))
            }

            if (stickers.isNotEmpty() && type == "JOURNAL") {
                Row(modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    stickers.forEachIndexed { i, s ->
                        Text(s, fontSize = 28.sp, modifier = Modifier.clickable {
                            stickers = stickers.toMutableList().also { it.removeAt(i) }
                            isSaved = false
                        })
                    }
                }
                Spacer(Modifier.height(4.dp))
                Text("(tap sticker to remove)", fontSize = 11.sp, color = onBgVariant)
                Spacer(Modifier.height(8.dp))
            }

            BasicTextField(
                value = title,
                onValueChange = { title = it; isSaved = false },
                textStyle = TextStyle(fontSize = 24.sp, fontWeight = FontWeight.Bold, color = onBgColor),
                modifier = Modifier.fillMaxWidth(),
                decorationBox = { inner ->
                    if (title.isEmpty()) Text("Title", fontSize = 24.sp, fontWeight = FontWeight.Bold, color = onBgVariant)
                    inner()
                }
            )

            Spacer(Modifier.height(16.dp))
            HorizontalDivider(color = colorScheme.outline.copy(alpha = 0.3f))
            Spacer(Modifier.height(16.dp))

            when (type) {
                "LIST" -> {
                    listItems.forEachIndexed { index, item ->
                        Row(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp), verticalAlignment = Alignment.CenterVertically) {
                            Checkbox(checked = item.isChecked, onCheckedChange = { checked ->
                                val updated = listItems.toMutableList()
                                updated[index] = item.copy(isChecked = checked)
                                listItems = updated
                                isSaved = false
                            }, colors = CheckboxDefaults.colors(checkedColor = colorScheme.primary, uncheckedColor = onBgVariant))
                            Spacer(Modifier.width(8.dp))
                            BasicTextField(value = item.text, onValueChange = { newText ->
                                val updated = listItems.toMutableList()
                                updated[index] = item.copy(text = newText)
                                listItems = updated
                                isSaved = false
                            }, textStyle = TextStyle(fontSize = 16.sp, color = if (item.isChecked) onBgVariant else onBgColor, textDecoration = if (item.isChecked) TextDecoration.LineThrough else TextDecoration.None), modifier = Modifier.weight(1f), decorationBox = { inner ->
                                if (item.text.isEmpty()) Text("Item", color = onBgVariant, fontSize = 16.sp)
                                inner()
                            })
                            IconButton(onClick = { listItems = listItems.toMutableList().also { it.removeAt(index) }; isSaved = false }, modifier = Modifier.size(32.dp)) {
                                Icon(Icons.Default.Close, null, modifier = Modifier.size(16.dp), tint = onBgVariant)
                            }
                        }
                        HorizontalDivider(color = colorScheme.outline.copy(alpha = 0.1f))
                    }
                    Spacer(Modifier.height(8.dp))
                    Row(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp), verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Add, null, tint = colorScheme.primary, modifier = Modifier.size(24.dp))
                        Spacer(Modifier.width(8.dp))
                        BasicTextField(value = newItemText, onValueChange = { newItemText = it }, modifier = Modifier.weight(1f), textStyle = TextStyle(fontSize = 16.sp, color = onBgColor), keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done), keyboardActions = KeyboardActions(onDone = { if (newItemText.isNotBlank()) { listItems = listItems + ListItem(text = newItemText); newItemText = ""; isSaved = false } }), decorationBox = { inner ->
                            if (newItemText.isEmpty()) Text("Add item and press Done...", color = onBgVariant, fontSize = 16.sp)
                            inner()
                        })
                        if (newItemText.isNotBlank()) IconButton(onClick = { listItems = listItems + ListItem(text = newItemText); newItemText = ""; isSaved = false }, modifier = Modifier.size(36.dp)) { Icon(Icons.Default.Check, null, tint = colorScheme.primary) }
                    }
                }
                else -> {
                    BasicTextField(value = content, onValueChange = { content = it; isSaved = false }, textStyle = TextStyle(fontSize = 16.sp, color = onBgColor, lineHeight = 26.sp), modifier = Modifier.fillMaxWidth().defaultMinSize(minHeight = 200.dp), decorationBox = { inner ->
                        if (content.isEmpty()) Text(if (type == "JOURNAL") "What's on your mind today..." else "Start writing...", color = onBgVariant, fontSize = 16.sp)
                        inner()
                    })
                }
            }

            if (photoUris.isNotEmpty() && type == "JOURNAL") {
                Spacer(Modifier.height(16.dp))
                Text("Photos", fontWeight = FontWeight.SemiBold, fontSize = 14.sp, color = onBgVariant)
                Spacer(Modifier.height(8.dp))
                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(photoUris) { uri ->
                        Box {
                            AsyncImage(model = Uri.parse(uri), contentDescription = null, modifier = Modifier.size(120.dp).clip(RoundedCornerShape(12.dp)), contentScale = ContentScale.Crop)
                            IconButton(onClick = { photoUris = photoUris.toMutableList().also { it.remove(uri) }; isSaved = false }, modifier = Modifier.align(Alignment.TopEnd).size(28.dp)) {
                                Icon(Icons.Default.Close, null, tint = Color.White, modifier = Modifier.size(16.dp))
                            }
                        }
                    }
                }
            }
            Spacer(Modifier.height(80.dp))
        }
    }
}
