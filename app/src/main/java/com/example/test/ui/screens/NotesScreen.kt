package com.example.test.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.staggeredgrid.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.*
import com.example.test.data.models.Note
import com.example.test.ui.viewmodels.NoteViewModel
import java.text.SimpleDateFormat
import java.util.*

val NoteColorMap = mapOf(
    "DEFAULT" to Color(0xFFFFFFFF),
    "RED"     to Color(0xFFFFCDD2),
    "ORANGE"  to Color(0xFFFFE0B2),
    "YELLOW"  to Color(0xFFFFF9C4),
    "GREEN"   to Color(0xFFC8E6C9),
    "BLUE"    to Color(0xFFBBDEFB),
    "PURPLE"  to Color(0xFFE1BEE7),
    "PINK"    to Color(0xFFF8BBD9),
    "TEAL"    to Color(0xFFB2EBF2)
)

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun NotesScreen(
    userId: Long,
    noteViewModel: NoteViewModel,
    onNoteClick: (Long) -> Unit,
    onNewNote: (String) -> Unit
) {
    val notes by noteViewModel.notes.collectAsState()
    val searchQuery by noteViewModel.searchQuery.collectAsState()
    val filterType by noteViewModel.filterType.collectAsState()
    var showSearch by remember { mutableStateOf(false) }
    var showFabMenu by remember { mutableStateOf(false) }
    val colorScheme = MaterialTheme.colorScheme

    LaunchedEffect(userId) { noteViewModel.setUserId(userId) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    if (showSearch) {
                        OutlinedTextField(
                            value = searchQuery,
                            onValueChange = { noteViewModel.setSearchQuery(it) },
                            placeholder = { Text("Search notes...") },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth(),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = colorScheme.primary,
                                unfocusedBorderColor = Color.Transparent
                            )
                        )
                    } else {
                        Text("Notes", fontWeight = FontWeight.Black, fontSize = 28.sp)
                    }
                },
                actions = {
                    IconButton(onClick = {
                        showSearch = !showSearch
                        if (!showSearch) noteViewModel.setSearchQuery("")
                    }) {
                        Icon(if (showSearch) Icons.Default.Close else Icons.Default.Search, null)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = colorScheme.background)
            )
        },
        floatingActionButton = {
            Column(horizontalAlignment = Alignment.End, verticalArrangement = Arrangement.spacedBy(8.dp)) {
                AnimatedVisibility(visible = showFabMenu) {
                    Column(
                        horizontalAlignment = Alignment.End,
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        FabOption("List") { showFabMenu = false; onNewNote("LIST") }
                        FabOption("Journal") { showFabMenu = false; onNewNote("JOURNAL") }
                        FabOption("Note") { showFabMenu = false; onNewNote("NOTE") }
                    }
                }
                FloatingActionButton(
                    onClick = { showFabMenu = !showFabMenu },
                    containerColor = colorScheme.primary
                ) {
                    Icon(
                        if (showFabMenu) Icons.Default.Close else Icons.Default.Add,
                        null, tint = colorScheme.onPrimary
                    )
                }
            }
        },
        containerColor = colorScheme.background
    ) { padding ->
        Column(Modifier.fillMaxSize().padding(padding)) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState())
                    .padding(horizontal = 16.dp, vertical = 4.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                listOf("All" to "ALL", "Notes" to "NOTE", "Journal" to "JOURNAL", "Lists" to "LIST")
                    .forEach { (label, type) ->
                        FilterChip(
                            selected = filterType == type,
                            onClick = { noteViewModel.setFilterType(type) },
                            label = { Text(label) }
                        )
                    }
            }

            if (notes.isEmpty()) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("No notes yet. Tap + to create one.", color = colorScheme.onSurfaceVariant)
                }
            } else {
                LazyVerticalStaggeredGrid(
                    columns = StaggeredGridCells.Fixed(2),
                    modifier = Modifier.fillMaxSize().padding(horizontal = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalItemSpacing = 8.dp,
                    contentPadding = PaddingValues(bottom = 80.dp, top = 8.dp)
                ) {
                    items(notes.size) { index ->
                        NoteCard(
                            note = notes[index],
                            onClick = { onNoteClick(notes[index].id) },
                            onPin = { noteViewModel.togglePin(notes[index]) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun FabOption(label: String, onClick: () -> Unit) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Surface(shape = RoundedCornerShape(8.dp), color = MaterialTheme.colorScheme.surface, shadowElevation = 4.dp) {
            Text(label, modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp), fontWeight = FontWeight.Medium)
        }
        Spacer(Modifier.width(8.dp))
        SmallFloatingActionButton(onClick = onClick, containerColor = MaterialTheme.colorScheme.secondaryContainer) {
            Icon(Icons.Default.Edit, null)
        }
    }
}

@Composable
private fun NoteCard(note: Note, onClick: () -> Unit, onPin: () -> Unit) {
    val bgColor = NoteColorMap[note.color] ?: Color.White
    val dateStr = remember(note.dateModified) {
        SimpleDateFormat("MMM d", Locale.getDefault()).format(Date(note.dateModified))
    }
    val typeEmoji = when (note.type) { "JOURNAL" -> "📔"; "LIST" -> "✅"; else -> "📝" }

    Card(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = bgColor),
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text(typeEmoji, fontSize = 14.sp)
                if (note.isPinned) Icon(Icons.Default.PushPin, null, modifier = Modifier.size(14.dp), tint = Color.Gray)
            }
            if (note.title.isNotEmpty()) {
                Spacer(Modifier.height(4.dp))
                Text(note.title, fontWeight = FontWeight.Bold, fontSize = 15.sp, maxLines = 2, overflow = TextOverflow.Ellipsis, color = Color(0xFF1A1A1A))
            }
            if (note.content.isNotEmpty()) {
                Spacer(Modifier.height(4.dp))
                Text(note.content, fontSize = 13.sp, maxLines = 6, overflow = TextOverflow.Ellipsis, color = Color(0xFF444444))
            }
            if (note.stickers.isNotEmpty()) {
                Spacer(Modifier.height(4.dp))
                Text(note.stickers.split(",").take(5).joinToString(" "), fontSize = 16.sp)
            }
            Spacer(Modifier.height(8.dp))
            Text(dateStr, fontSize = 11.sp, color = Color.Gray)
        }
    }
}