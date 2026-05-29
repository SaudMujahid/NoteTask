package com.example.test.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.staggeredgrid.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.*
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.*
import androidx.compose.ui.platform.LocalContext
import com.example.test.data.models.Note
import com.example.test.ui.viewmodels.NoteViewModel
import java.text.SimpleDateFormat
import java.util.*
import com.example.test.data.repository.ProfileRepository

val NoteColorMap = mapOf(
    "DEFAULT" to Color.Transparent,
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
    noteViewModel: NoteViewModel,
    onNoteClick: (Long) -> Unit,
    onNewNote: (String) -> Unit,
    onBack: () -> Unit = {}
) {
    val notes by noteViewModel.notes.collectAsState()
    val searchQuery by noteViewModel.searchQuery.collectAsState()
    val filterType by noteViewModel.filterType.collectAsState()
    var showSearch by rememberSaveable { mutableStateOf(false) }
    var showFabMenu by rememberSaveable { mutableStateOf(false) }
    val colorScheme = MaterialTheme.colorScheme
    val isDark = isSystemInDarkTheme()
    var lockedNoteId by rememberSaveable { mutableStateOf<Long?>(null) }
    val context = LocalContext.current
    val profileRepo = remember { ProfileRepository.getInstance(context) }

    if (lockedNoteId != null) {
        NoteAuthDialog(
            profileRepository = profileRepo,
            onDismiss = { lockedNoteId = null },
            onSuccess = {
                val id = lockedNoteId!!
                lockedNoteId = null
                onNoteClick(id)
            }
        )
    }

    Scaffold(
        topBar = {
            Column {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(start = 6.dp, end = 16.dp, top = 16.dp, bottom = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back"
                        )
                    }

                    if (showSearch) {
                        OutlinedTextField(
                            value = searchQuery,
                            onValueChange = { noteViewModel.setSearchQuery(it) },
                            placeholder = { Text("Search notes...") },
                            singleLine = true,
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(12.dp)
                        )
                    } else {
                        Text(
                            text = "Notes",
                            fontWeight = FontWeight.Black,
                            fontSize = 28.sp,
                            modifier = Modifier.weight(1f)
                        )
                    }

                    Spacer(Modifier.width(8.dp))

                    IconButton(onClick = {
                        showSearch = !showSearch
                        if (!showSearch) noteViewModel.setSearchQuery("")
                    }) {
                        Icon(
                            imageVector = if (showSearch) Icons.Default.Close else Icons.Default.Search,
                            contentDescription = if (showSearch) "Close search" else "Search"
                        )
                    }
                }

                // The colored divider — exact same style as StatsScreen
                Box(
                    modifier = Modifier
                        .padding(horizontal = 16.dp)
                        .fillMaxWidth()
                        .height(3.dp)
                        .clip(RoundedCornerShape(50))
                        .background(MaterialTheme.colorScheme.primary)
                )
            }
        },
        floatingActionButton = {
            Column(horizontalAlignment = Alignment.End, verticalArrangement = Arrangement.spacedBy(8.dp)) {
                AnimatedVisibility(visible = showFabMenu) {
                    Column(horizontalAlignment = Alignment.End, verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        FabOption("List") { showFabMenu = false; onNewNote("LIST") }
                        FabOption("Journal") { showFabMenu = false; onNewNote("JOURNAL") }
                        FabOption("Note") { showFabMenu = false; onNewNote("NOTE") }
                    }
                }
                FloatingActionButton(onClick = { showFabMenu = !showFabMenu }, containerColor = colorScheme.primary) {
                    Icon(if (showFabMenu) Icons.Default.Close else Icons.Default.Add, null, tint = if (isDark) Color.White else colorScheme.onPrimary)
                }
            }
        }
    ) { padding ->
        Column(Modifier.fillMaxSize().padding(padding)) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState())
                    .padding(horizontal = 16.dp, vertical = 4.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                listOf("All" to "ALL", "Notes" to "NOTE", "Journal" to "JOURNAL", "Lists" to "LIST").forEach { (label, type) ->
                    FilterChip(selected = filterType == type, onClick = { noteViewModel.setFilterType(type) }, label = { Text(label) })
                }
            }
            if (notes.isEmpty()) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("No notes yet. Tap + to create one.")
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
                            onClick = {
                                if (notes[index].isLocked) {
                                    lockedNoteId = notes[index].id
                                } else {
                                    onNoteClick(notes[index].id)
                                }
                            },
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
        Surface(shape = RoundedCornerShape(8.dp), shadowElevation = 4.dp, onClick = onClick) {
            Text(label, modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp), fontWeight = FontWeight.Medium, color = if (isSystemInDarkTheme()) Color.White else MaterialTheme.colorScheme.onSurface)
        }
        Spacer(Modifier.width(8.dp))
        SmallFloatingActionButton(onClick = onClick) { Icon(Icons.Default.Edit, null, tint = if (isSystemInDarkTheme()) Color.White else MaterialTheme.colorScheme.primary) }
    }
}

@Composable
private fun NoteCard(note: Note, onClick: () -> Unit, onPin: () -> Unit) {
    val colorScheme = MaterialTheme.colorScheme
    val isDark = isSystemInDarkTheme()
    val bgColor = if (note.color != "DEFAULT") NoteColorMap[note.color] ?: colorScheme.surface else colorScheme.surface
    val isColored = note.color != "DEFAULT"
    val contentColor = if (isColored) Color(0xFF1A1A1A) else (if (isDark) Color.White else colorScheme.onSurface)
    
    val dateStr = SimpleDateFormat("MMM d", Locale.getDefault()).format(Date(note.dateModified))
    val typeEmoji = when (note.type) { "JOURNAL" -> "📔"; "LIST" -> "✅"; else -> "📝" }
    Card(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = bgColor)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                if (note.isLocked) {
                    Icon(
                        imageVector = Icons.Default.Lock,
                        contentDescription = "Locked",
                        modifier = Modifier.size(14.dp),
                        tint = if (isDark && !isColored) Color.White else colorScheme.error
                    )
                }
                Text(typeEmoji, fontSize = 14.sp)
                if (note.isPinned) Icon(Icons.Default.PushPin, null, modifier = Modifier.size(14.dp), tint = if (isDark && !isColored) Color.White else colorScheme.primary)
            }
            if (note.title.isNotEmpty()) Text(note.title, fontWeight = FontWeight.Bold, fontSize = 15.sp, maxLines = 2, overflow = TextOverflow.Ellipsis, color = contentColor)
            if (note.content.isNotEmpty()) Text(note.content, fontSize = 13.sp, maxLines = 6, overflow = TextOverflow.Ellipsis, color = contentColor.copy(alpha = 0.8f))
            if (note.stickers.isNotEmpty()) Text(note.stickers.split(",").take(5).joinToString(" "), fontSize = 16.sp)
            Spacer(Modifier.height(8.dp))
            Text(dateStr, fontSize = 11.sp, color = contentColor.copy(alpha = 0.6f))
        }
    }
}
