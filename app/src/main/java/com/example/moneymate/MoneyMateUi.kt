package com.example.moneymate

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import java.text.NumberFormat
import java.util.Locale

@Composable
fun DashboardScreen(navController: NavController, viewModel: MoneyMateViewModel) {
    var salaryInput by remember { mutableStateOf(viewModel.salary.toInt().toString()) }
    val monthlyExpense = viewModel.totalExpenses
    val remaining = viewModel.remainingBalance
    val suggestion = viewModel.budgetInsight()

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Text("MoneyMate", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
        }
        item {
            Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)) {
                Column(Modifier.padding(16.dp)) {
                    Text("Salary", style = MaterialTheme.typography.labelMedium)
                    OutlinedTextField(
                        value = salaryInput,
                        onValueChange = {
                            salaryInput = it
                            viewModel.updateSalary(it)
                        },
                        label = { Text("Monthly salary") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        }
        item {
            Card {
                Column(Modifier.padding(16.dp)) {
                    Text("Budget summary", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                    Spacer(Modifier.height(8.dp))
                    Text("Monthly expenses: ${formatCurrency(monthlyExpense)}")
                    Text("Remaining after expenses: ${formatCurrency(remaining)}")
                    Text("AI tip: $suggestion")
                }
            }
        }
        item {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text("Expense categories", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                Button(onClick = { navController.navigate("expense") }) {
                    Text("Add expense")
                }
            }
        }
        items(viewModel.expenseItems) { item ->
            Card {
                Column(Modifier.padding(16.dp)) {
                    Text(item.name, fontWeight = FontWeight.Medium)
                    Text("Amount: ${formatCurrency(item.amount)}")
                    Text("AI tip: ${item.note}")
                }
            }
        }
    }
}

@Composable
fun ExpenseScreen(navController: NavController, viewModel: MoneyMateViewModel) {
    var title by remember { mutableStateOf("") }
    var amount by remember { mutableStateOf("") }
    var note by remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text("Log an expense", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
        OutlinedTextField(value = title, onValueChange = { title = it }, label = { Text("Expense type") }, modifier = Modifier.fillMaxWidth())
        OutlinedTextField(value = amount, onValueChange = { amount = it }, label = { Text("Amount") }, modifier = Modifier.fillMaxWidth())
        OutlinedTextField(value = note, onValueChange = { note = it }, label = { Text("How to save next time") }, modifier = Modifier.fillMaxWidth())
        OutlinedTextField(
            value = viewModel.apiKey,
            onValueChange = { viewModel.updateApiKey(it) },
            label = { Text("OpenAI API key") },
            modifier = Modifier.fillMaxWidth()
        )
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Button(onClick = {
                viewModel.askOpenAIForSuggestion(title, amount.toDoubleOrNull() ?: 0.0, note)
            }) {
                Text("Ask AI")
            }
            Button(onClick = {
                viewModel.addExpense(title, amount, note)
                viewModel.askOpenAIForSuggestion(title, amount.toDoubleOrNull() ?: 0.0, note)
                navController.popBackStack()
            }) {
                Text("Save expense")
            }
        }
        Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer)) {
            Column(Modifier.padding(16.dp)) {
                Text("AI assistance", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                if (viewModel.isLoading) {
                    Text("Thinking with OpenAI...")
                } else {
                    Text(viewModel.aiAdvice)
                }
            }
        }
    }
}

private fun formatCurrency(value: Number): String =
    NumberFormat.getCurrencyInstance(Locale.US).format(value)
