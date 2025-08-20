package com.example.kalkulatorsederhana // Ganti ini dengan package Anda

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.EditText

class MainActivity : AppCompatActivity() {
    private lateinit var etDisplay: EditText
    private var canAddOperation = false
    private var canAddDecimal = true

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        etDisplay = findViewById(R.id.et_display)

        // Temukan dan atur onClickListener untuk tombol angka dan titik
        val btn0 = findViewById<Button>(R.id.btn0)
        val btn1 = findViewById<Button>(R.id.btn1)
        val btn2 = findViewById<Button>(R.id.btn2)
        val btn3 = findViewById<Button>(R.id.btn3)
        val btn4 = findViewById<Button>(R.id.btn4)
        val btn5 = findViewById<Button>(R.id.btn5)
        val btn6 = findViewById<Button>(R.id.btn6)
        val btn7 = findViewById<Button>(R.id.btn7)
        val btn8 = findViewById<Button>(R.id.btn8)
        val btn9 = findViewById<Button>(R.id.btn9)
        val btnDot = findViewById<Button>(R.id.btn_dot)

        // Temukan dan atur onClickListener untuk tombol operasi
        val btnAdd = findViewById<Button>(R.id.btn_add)
        val btnSubtract = findViewById<Button>(R.id.btn_subtract)
        val btnMultiply = findViewById<Button>(R.id.btn_multiply)
        val btnDivide = findViewById<Button>(R.id.btn_divide)
        val btnClear = findViewById<Button>(R.id.btn_clear)
        val btnEqual = findViewById<Button>(R.id.btn_equal)
        val btnPercent = findViewById<Button>(R.id.btn_percent)

        // Pasang listeners
        val numberButtons = listOf(btn0, btn1, btn2, btn3, btn4, btn5, btn6, btn7, btn8, btn9)
        for (button in numberButtons) {
            button.setOnClickListener {
                onNumberClick(button)
            }
        }

        val operatorButtons = listOf(btnAdd, btnSubtract, btnMultiply, btnDivide)
        for (button in operatorButtons) {
            button.setOnClickListener {
                onOperatorClick(button)
            }
        }

        btnDot.setOnClickListener { onDecimalClick() }
        btnClear.setOnClickListener { onClearClick() }
        btnEqual.setOnClickListener { onEqualClick() }
        btnPercent.setOnClickListener { onPercentClick() }
    }

    private fun onNumberClick(button: Button) {
        if (etDisplay.text.toString() == "0") {
            etDisplay.text.clear()
        }
        etDisplay.append(button.text)
        canAddOperation = true
    }

    private fun onOperatorClick(button: Button) {
        if (canAddOperation) {
            etDisplay.append(button.text)
            canAddOperation = false
            canAddDecimal = true
        }
    }

    private fun onDecimalClick() {
        if (canAddDecimal) {
            etDisplay.append(".")
            canAddDecimal = false
            canAddOperation = false
        }
    }

    private fun onClearClick() {
        etDisplay.setText("0")
        canAddOperation = false
        canAddDecimal = true
    }

    private fun onEqualClick() {
        // Logika evaluasi ekspresi kalkulator akan diletakkan di sini
        // Untuk saat ini, kita akan buat sederhana saja
        try {
            val expression = etDisplay.text.toString().replace("×", "*").replace("÷", "/")
            val result = eval(expression)
            etDisplay.setText(result.toString())
        } catch (e: Exception) {
            etDisplay.setText("Error")
        }
    }

    private fun onPercentClick() {
        // Logika untuk operasi persentase
        val expression = etDisplay.text.toString()
        try {
            val currentValue = eval(expression.replace("%", ""))
            val result = currentValue / 100
            etDisplay.setText(result.toString())
        } catch (e: Exception) {
            etDisplay.setText("Error")
        }
    }

    // Fungsi sederhana untuk mengevaluasi string ekspresi (menggunakan skrip engine)
    private fun eval(expression: String): Double {
        return object : Any() {
            var pos = -1
            var ch = 0

            fun nextChar() {
                ch = if (++pos < expression.length) expression[pos].code else -1
            }

            fun eat(charToEat: Int): Boolean {
                while (ch == ' '.code) nextChar()
                if (ch == charToEat) {
                    nextChar()
                    return true
                }
                return false
            }

            fun parse(): Double {
                nextChar()
                val x = parseExpression()
                if (pos < expression.length) throw RuntimeException("Unexpected: " + ch.toChar())
                return x
            }

            fun parseExpression(): Double {
                var x = parseTerm()
                while (true) {
                    if (eat('+'.code)) x += parseTerm() // addition
                    else if (eat('-'.code)) x -= parseTerm() // subtraction
                    else return x
                }
            }

            fun parseTerm(): Double {
                var x = parseFactor()
                while (true) {
                    if (eat('*'.code)) x *= parseFactor() // multiplication
                    else if (eat('/'.code)) x /= parseFactor() // division
                    else return x
                }
            }

            fun parseFactor(): Double {
                if (eat('+'.code)) return parseFactor() // unary plus
                if (eat('-'.code)) return -parseFactor() // unary minus

                var x: Double
                val startPos = this.pos
                if (eat('('.code)) { // parentheses
                    x = parseExpression()
                    eat(')'.code)
                } else if (ch >= '0'.code && ch <= '9'.code || ch == '.'.code) { // numbers
                    while (ch >= '0'.code && ch <= '9'.code || ch == '.'.code) nextChar()
                    x = expression.substring(startPos, this.pos).toDouble()
                } else if (ch >= 'a'.code && ch <= 'z'.code) { // functions
                    while (ch >= 'a'.code && ch <= 'z'.code) nextChar()
                    val func = expression.substring(startPos, this.pos)
                    x = parseFactor()
                    if (func == "sqrt") x = Math.sqrt(x)
                    else if (func == "sin") x = Math.sin(Math.toRadians(x))
                    else if (func == "cos") x = Math.cos(Math.toRadians(x))
                    else if (func == "tan") x = Math.tan(Math.toRadians(x))
                    else throw RuntimeException("Unknown function: " + func)
                } else {
                    throw RuntimeException("Unexpected: " + ch.toChar())
                }

                if (eat('^'.code)) x = Math.pow(x, parseFactor()) // exponentiation

                return x
            }
        }.parse()
    }
}