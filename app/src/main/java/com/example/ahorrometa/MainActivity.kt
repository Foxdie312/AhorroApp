package com.example.ahorrometa

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Savings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardOptions
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent { AhorroMetaApp() }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AhorroMetaApp() {
    val context = androidx.compose.ui.platform.LocalContext.current
    var goals by remember { mutableStateOf(Store.loadGoals(context)) }
    var movements by remember { mutableStateOf(Store.loadMovements(context)) }
    var selectedGoal by remember { mutableStateOf<Goal?>(null) }
    var showAddGoal by remember { mutableStateOf(false) }

    fun persist() = Store.save(context, goals, movements)

    fun addGoal(name: String, target: Double, deadline: String) {
        goals = goals + Goal(name = name.trim(), targetAmount = target, deadline = deadline)
        persist()
    }

    fun deleteGoal(goal: Goal) {
        goals = goals.filter { it.id != goal.id }
        movements = movements.filter { it.goalId != goal.id }
        persist()
    }

    fun addMovement(goal: Goal?, amount: Double, isDeposit: Boolean, note: String) {
        val date = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault()).format(Date())
        movements = movements + Movement(
            goalId = goal?.id, amount = amount, isDeposit = isDeposit, note = note, date = date
        )
        if (goal != null) {
            goals = goals.map {
                if (it.id == goal.id) it.copy(
                    savedAmount = (it.savedAmount + if (isDeposit) amount else -amount)
                        .coerceAtLeast(0.0)
                ) else it
            }
        }
        persist()
    }

    fun deleteMovement(m: Movement) {
        movements = movements.filter { it.id != m.id }
        if (m.goalId != null) {
            goals = goals.map {
                if (it.id == m.goalId) it.copy(
                    savedAmount = (it.savedAmount + if (m.isDeposit) -m.amount else m.amount)
                        .coerceAtLeast(0.0)
                ) else it
            }
        }
        persist()
    }

    MaterialTheme(colorScheme = lightColorScheme(primary = Color(0xFF2E7D32))) {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = {
                        Text(
                            if (selectedGoal == null) "AhorroMeta" else selectedGoal!!.name,
                            fontWeight = FontWeight.Bold
                        )
                    },
                    navigationIcon = {
                        if (selectedGoal != null) {
                            IconButton(onClick = { selectedGoal = null }) {
                                Icon(Icons.Default.ArrowBack, contentDescription = "Volver")
                            }
                        }
                    },
                    actions = {
                        if (selectedGoal == null) {
                            IconButton(onClick = { showAddGoal = true }) {
                                Icon(Icons.Default.Add, contentDescription = "Nueva meta")
                            }
                        }
                    }
                )
            },
            floatingActionButton = {
                if (selectedGoal != null) {
                    val goal = selectedGoal!!
                    FloatingActionButton(onClick = { MovementDialog(goal = goal, onConfirm = { amt, dep, note -> addMovement(goal, amt, dep, note) }) }) {
                        Icon(Icons.Default.Add, contentDescription = "Nuevo movimiento")
                    }
                }
            }
        ) { padding ->
            if (selectedGoal == null) {
                HomeScreen(
                    goals = goals,
                    movements = movements,
                    modifier = Modifier.padding(padding),
                    onOpenGoal = { selectedGoal = it },
                    onDeleteGoal = { deleteGoal(it) },
                    onAddGeneralMovement = { amt, dep, note -> addMovement(null, amt, dep, note) }
                )
            } else {
                GoalDetailScreen(
                    goal = goals.first { it.id == selectedGoal!!.id },
                    movements = movements.filter { it.goalId == selectedGoal!!.id },
                    modifier = Modifier.padding(padding),
                    onDeleteMovement = { deleteMovement(it) }
                )
            }
        }
    }

    if (showAddGoal) {
        AddGoalDialog(
            onDismiss = { showAddGoal = false },
            onConfirm = { name, target, deadline ->
                addGoal(name, target, deadline)
                showAddGoal = false
            }
        )
    }
}

@Composable
fun HomeScreen(
    goals: List<Goal>,
    movements: List<Movement>,
    modifier: Modifier = Modifier,
    onOpenGoal: (Goal) -> Unit,
    onDeleteGoal: (Goal) -> Unit,
    onAddGeneralMovement: (Double, Boolean, String) -> Unit
) {
    val totalSaved = goals.sumOf { it.savedAmount }
    var showMovement by remember { mutableStateOf(false) }

    LazyColumn(modifier = modifier.fillMaxSize().padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        item {
            Card(colors = CardDefaults.cardColors(containerColor = Color(0xFF2E7D32))) {
                Column(Modifier.padding(16.dp)) {
                    Text("Total ahorrado", color = Color.White, fontSize = 14.sp)
                    Text("$totalSaved €", color = Color.White, fontSize = 32.sp, fontWeight = FontWeight.Bold)
                    Text("${goals.size} metas activas", color = Color(0xFFC8E6C9), fontSize = 13.sp)
                }
            }
        }
        item {
            OutlinedButton(
                onClick = { showMovement = true },
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(Icons.Default.Savings, contentDescription = null)
                Spacer(Modifier.width(8.dp))
                Text("Registrar ingreso / gasto general")
            }
        }
        item { Text("Mis metas de ahorro", fontSize = 18.sp, fontWeight = FontWeight.Bold) }
        if (goals.isEmpty()) {
            item {
                Text(
                    "Aún no tienes metas. Pulsa + para crear una, por ejemplo: \"Viaje a Japón: 2000 €\".",
                    color = Color.Gray
                )
            }
        }
        items(goals, key = { it.id }) { goal ->
            Card(onClick = { onOpenGoal(goal) }) {
                Row(Modifier.padding(16.dp), horizontalArrangement = Arrangement.SpaceBetween) {
                    Column(Modifier.weight(1f)) {
                        Text(goal.name, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                        if (goal.deadline.isNotEmpty()) {
                            Text("Fecha límite: ${goal.deadline}", fontSize = 12.sp, color = Color.Gray)
                        }
                        Spacer(Modifier.height(8.dp))
                        LinearProgressIndicator(
                            progress = { goal.progress },
                            modifier = Modifier.fillMaxWidth().height(8.dp)
                        )
                        Spacer(Modifier.height(4.dp))
                        Text(
                            "${goal.savedAmount} / ${goal.targetAmount} €",
                            fontSize = 13.sp
                        )
                    }
                    IconButton(onClick = { onDeleteGoal(goal) }) {
                        Icon(Icons.Default.Delete, contentDescription = "Eliminar", tint = Color.Gray)
                    }
                }
            }
        }
        item { Spacer(Modifier.height(8.dp)) }
        item { Text("Movimientos generales", fontSize = 18.sp, fontWeight = FontWeight.Bold) }
        val general = movements.filter { it.goalId == null }.take(10)
        if (general.isEmpty()) {
            item { Text("Sin movimientos generales.", color = Color.Gray, fontSize = 13.sp) }
        }
        items(general, key = { it.id }) { m ->
            MovementRow(m, onDelete = {})
        }
    }

    if (showMovement) {
        MovementDialog(goal = null, onConfirm = { amt, dep, note ->
            onAddGeneralMovement(amt, dep, note)
            showMovement = false
        }, onDismiss = { showMovement = false })
    }
}

@Composable
fun GoalDetailScreen(
    goal: Goal,
    movements: List<Movement>,
    modifier: Modifier = Modifier,
    onDeleteMovement: (Movement) -> Unit
) {
    val missing = (goal.targetAmount - goal.savedAmount).coerceAtLeast(0.0)
    LazyColumn(modifier = modifier.fillMaxSize().padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        item {
            Card {
                Column(Modifier.padding(16.dp)) {
                    Text("Objetivo: ${goal.targetAmount} €", fontSize = 14.sp, color = Color.Gray)
                    Text("${goal.savedAmount} €", fontSize = 32.sp, fontWeight = FontWeight.Bold)
                    Text(
                        if (missing <= 0.0) "¡Meta conseguida! 🎉" else "Faltan $missing €",
                        fontSize = 14.sp,
                        color = if (missing <= 0.0) Color(0xFF2E7D32) else Color.Gray
                    )
                    Spacer(Modifier.height(8.dp))
                    LinearProgressIndicator(
                        progress = { goal.progress },
                        modifier = Modifier.fillMaxWidth().height(10.dp)
                    )
                    Spacer(Modifier.height(4.dp))
                    Text("${(goal.progress * 100).toInt()}%", fontSize = 13.sp)
                }
            }
        }
        item { Text("Historial de movimientos", fontSize = 18.sp, fontWeight = FontWeight.Bold) }
        if (movements.isEmpty()) {
            item { Text("Sin movimientos todavía. Usa el botón + para añadir un depósito.", color = Color.Gray, fontSize = 13.sp) }
        }
        items(movements.asReversed(), key = { it.id }) { m ->
            MovementRow(m, onDelete = { onDeleteMovement(m) })
        }
    }
}

@Composable
fun MovementRow(m: Movement, onDelete: () -> Unit) {
    Card {
        Row(
            Modifier.padding(12.dp).fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(Modifier.weight(1f)) {
                Text(
                    (if (m.isDeposit) "+ " else "- ") + "${m.amount} €",
                    color = if (m.isDeposit) Color(0xFF2E7D32) else Color(0xFFC62828),
                    fontWeight = FontWeight.Bold
                )
                if (m.note.isNotEmpty()) Text(m.note, fontSize = 13.sp)
                Text(m.date, fontSize = 11.sp, color = Color.Gray)
            }
            IconButton(onClick = onDelete) {
                Icon(Icons.Default.Delete, contentDescription = "Eliminar", tint = Color.Gray)
            }
        }
    }
}

@Composable
fun AddGoalDialog(onDismiss: () -> Unit, onConfirm: (String, Double, String) -> Unit) {
    var name by remember { mutableStateOf("") }
    var target by remember { mutableStateOf("") }
    var deadline by remember { mutableStateOf("") }
    var error by remember { mutableStateOf<String?>(null) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Nueva meta de ahorro") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Nombre (ej. Viaje, Emergencias)") },
                    singleLine = true
                )
                OutlinedTextField(
                    value = target,
                    onValueChange = { target = it },
                    label = { Text("Cantidad objetivo (€)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    singleLine = true
                )
                OutlinedTextField(
                    value = deadline,
                    onValueChange = { deadline = it },
                    label = { Text("Fecha límite (opcional, ej. 31/12/2025)") },
                    singleLine = true
                )
                error?.let { Text(it, color = Color(0xFFC62828), fontSize = 13.sp) }
            }
        },
        confirmButton = {
            TextButton(onClick = {
                val t = target.replace(",", ".").toDoubleOrNull()
                when {
                    name.isBlank() -> error = "Escribe un nombre"
                    t == null || t <= 0 -> error = "Cantidad inválida"
                    else -> onConfirm(name, t, deadline.trim())
                }
            }) { Text("Crear") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancelar") } }
    )
}

@Composable
fun MovementDialog(
    goal: Goal?,
    onConfirm: (Double, Boolean, String) -> Unit,
    onDismiss: () -> Unit = {}
) {
    var amount by remember { mutableStateOf("") }
    var isDeposit by remember { mutableStateOf(true) }
    var note by remember { mutableStateOf("") }
    var error by remember { mutableStateOf<String?>(null) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (goal == null) "Movimiento general" else "Movimiento: ${goal.name}") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    FilterChip(selected = isDeposit, onClick = { isDeposit = true }, label = { Text("Depósito") })
                    FilterChip(selected = !isDeposit, onClick = { isDeposit = false }, label = { Text("Retirada") })
                }
                OutlinedTextField(
                    value = amount,
                    onValueChange = { amount = it },
                    label = { Text("Cantidad (€)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    singleLine = true
                )
                OutlinedTextField(
                    value = note,
                    onValueChange = { note = it },
                    label = { Text("Nota (opcional)") },
                    singleLine = true
                )
                error?.let { Text(it, color = Color(0xFFC62828), fontSize = 13.sp) }
            }
        },
        confirmButton = {
            TextButton(onClick = {
                val a = amount.replace(",", ".").toDoubleOrNull()
                if (a == null || a <= 0) error = "Cantidad inválida" else onConfirm(a, isDeposit, note.trim())
            }) { Text("Guardar") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancelar") } }
    )
}
