package com.example.moneymate

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.net.HttpURLConnection
import java.net.URL
import java.nio.charset.StandardCharsets

data class ExpenseItem(
    val name: String,
    val amount: Double,
    val note: String
)

class MoneyMateViewModel : ViewModel() {
    var salary by mutableStateOf(5000.0)
        private set

    var expenseItems by mutableStateOf(listOf(
        ExpenseItem("Rent", 1200.0, "Try to split costs with a roommate"),
        ExpenseItem("Food", 400.0, "Cook at home twice a week")
    ))
        private set

    var apiKey by mutableStateOf("")
        private set

    var aiAdvice by mutableStateOf("Add your OpenAI API key to get a personal money-saving tip.")
        private set

    var isLoading by mutableStateOf(false)
        private set

    val totalExpenses: Double
        get() = expenseItems.sumOf { it.amount }

    val remainingBalance: Double
        get() = salary - totalExpenses

    fun updateSalary(value: String) {
        salary = value.toDoubleOrNull() ?: 0.0
    }

    fun updateApiKey(value: String) {
        apiKey = value
    }

    fun addExpense(name: String, amount: String, note: String) {
        val parsedAmount = amount.toDoubleOrNull() ?: 0.0
        if (name.isBlank() || parsedAmount <= 0.0) return

        expenseItems = expenseItems + ExpenseItem(name.trim(), parsedAmount, note.trim())
    }

    fun askOpenAIForSuggestion(expenseName: String, amount: Double, note: String) {
        if (apiKey.isBlank()) {
            aiAdvice = "Add your OpenAI API key to get a personalized saving suggestion."
            return
        }

        if (amount <= 0.0) {
            aiAdvice = "Enter an amount before asking for AI advice."
            return
        }

        viewModelScope.launch {
            isLoading = true
            aiAdvice = withContext(Dispatchers.IO) {
                try {
                    fetchAdviceFromOpenAI(expenseName, amount, note)
                } catch (e: Exception) {
                    "Unable to reach OpenAI right now: ${e.message ?: "unknown error"}"
                }
            }
            isLoading = false
        }
    }

    private fun fetchAdviceFromOpenAI(expenseName: String, amount: Double, note: String): String {
        val requestBody = """
            {
              "model": "gpt-4o-mini",
              "messages": [
                {
                  "role": "system",
                  "content": "You are a helpful budgeting assistant. Give a short, practical savings suggestion in plain English."
                },
                {
                  "role": "user",
                  "content": "My salary is ${salary.toInt()} and I just spent ${amount.toInt()} on $expenseName. My note is: $note. Give me one concise money-saving tip."
                }
              ],
              "temperature": 0.7
            }
        """.trimIndent()

        val connection = (URL("https://api.openai.com/v1/chat/completions").openConnection() as HttpURLConnection).apply {
            requestMethod = "POST"
            doOutput = true
            setRequestProperty("Authorization", "Bearer $apiKey")
            setRequestProperty("Content-Type", "application/json")
            setRequestProperty("Accept", "application/json")
            connectTimeout = 15000
            readTimeout = 20000
        }

        connection.outputStream.use { stream ->
            stream.write(requestBody.toByteArray(StandardCharsets.UTF_8))
        }

        val responseCode = connection.responseCode
        val responseText = if (responseCode in 200..299) {
            connection.inputStream.bufferedReader().use { it.readText() }
        } else {
            connection.errorStream?.bufferedReader()?.use { it.readText() } ?: ""
        }

        val contentMatch = Regex("\\\"content\\\"\\s*:\\s*\\\"((?:\\\\.|[^\\\"])*)\\\"").find(responseText)
        val content = contentMatch?.groupValues?.get(1)?.replace("\\n", "\n")?.replace("\\\"", "\"")

        return content?.takeIf { it.isNotBlank() }
            ?: "AI suggestion is unavailable right now. Try again in a moment."
    }

    fun aiSuggestionForExpense(amount: Double): String {
        return when {
            amount > 500 -> "This is a large expense. Reduce it by choosing a cheaper plan or delaying a non-essential upgrade."
            amount > 200 -> "This expense is moderate. Try to cut one convenience spend this week to stay on target."
            else -> "This is a manageable spend. Keep a weekly cap to protect your savings habit."
        }
    }

    fun budgetInsight(): String {
        return when {
            remainingBalance >= 1500 -> "You are in a strong position. Automate a transfer to savings on payday."
            remainingBalance >= 800 -> "You still have room to save. Trim one flexible category this month."
            else -> "Your spending is close to your income. Focus on one recurring expense to improve cash flow."
        }
    }
}
