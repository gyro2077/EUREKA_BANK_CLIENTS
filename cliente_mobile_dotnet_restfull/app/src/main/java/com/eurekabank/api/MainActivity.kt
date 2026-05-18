package com.eurekabank.api

import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.lifecycle.lifecycleScope
import com.eurekabank.api.data.repository.EurekaBankRepository
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {

    private val repository = EurekaBankRepository()

    private lateinit var tvPing: TextView
    private lateinit var tvLogin: TextView
    private lateinit var tvResultado: TextView
    private lateinit var etUsuario: EditText
    private lateinit var etPassword: EditText
    private lateinit var etCuenta: EditText
    private lateinit var etImporte: EditText
    private lateinit var btnLogin: Button
    private lateinit var btnDeposito: Button
    private lateinit var btnRetiro: Button
    private lateinit var btnMovimientos: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        tvPing = findViewById(R.id.tvPing)
        tvLogin = findViewById(R.id.tvLogin)
        tvResultado = findViewById(R.id.tvResultado)
        etUsuario = findViewById(R.id.etUsuario)
        etPassword = findViewById(R.id.etPassword)
        etCuenta = findViewById(R.id.etCuenta)
        etImporte = findViewById(R.id.etImporte)
        btnLogin = findViewById(R.id.btnLogin)
        btnDeposito = findViewById(R.id.btnDeposito)
        btnRetiro = findViewById(R.id.btnRetiro)
        btnMovimientos = findViewById(R.id.btnMovimientos)

        etCuenta.setText("00100001")

        btnLogin.setOnClickListener {
            val usuario = etUsuario.text.toString().trim()
            val password = etPassword.text.toString().trim()
            if (usuario.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "Ingresa usuario y password", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            doLogin(usuario, password)
        }

        btnDeposito.setOnClickListener {
            val cuenta = etCuenta.text.toString().trim()
            val importe = etImporte.text.toString().trim().toDoubleOrNull()
            if (cuenta.isEmpty() || importe == null || importe <= 0) {
                Toast.makeText(this, "Ingresa cuenta e importe válido", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            doDeposito(cuenta, importe)
        }

        btnRetiro.setOnClickListener {
            val cuenta = etCuenta.text.toString().trim()
            val importe = etImporte.text.toString().trim().toDoubleOrNull()
            if (cuenta.isEmpty() || importe == null || importe <= 0) {
                Toast.makeText(this, "Ingresa cuenta e importe válido", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            doRetiro(cuenta, importe)
        }

        btnMovimientos.setOnClickListener {
            val cuenta = etCuenta.text.toString().trim()
            if (cuenta.isEmpty()) {
                Toast.makeText(this, "Ingresa numero de cuenta", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            doMovimientos(cuenta)
        }

        testPing()
    }

    private fun testPing() {
        lifecycleScope.launch {
            tvPing.text = "Ping: Conectando..."
            val result = repository.ping()
            result.onSuccess {
                tvPing.text = "Ping: OK - $it"
            }.onFailure {
                tvPing.text = "Ping: ERROR - ${it.message}"
            }
        }
    }

    private fun doLogin(usuario: String, password: String) {
        lifecycleScope.launch {
            tvLogin.text = "Login: Conectando..."
            val result = repository.login(usuario, password)
            result.onSuccess { response ->
                tvLogin.text = "Login: OK - ${response.resultado}"
            }.onFailure {
                tvLogin.text = "Login: ERROR - ${it.message}"
            }
        }
    }

    private fun doDeposito(cuenta: String, importe: Double) {
        lifecycleScope.launch {
            tvResultado.text = "Procesando depósito..."
            val result = repository.deposito(cuenta, importe)
            result.onSuccess { response ->
                tvResultado.text = "Depósito exitoso\nNuevo saldo: ${response.saldo}"
            }.onFailure {
                tvResultado.text = "Error: ${it.message}"
            }
        }
    }

    private fun doRetiro(cuenta: String, importe: Double) {
        lifecycleScope.launch {
            tvResultado.text = "Procesando retiro..."
            val result = repository.retiro(cuenta, importe)
            result.onSuccess { response ->
                tvResultado.text = "Retiro exitoso\nNuevo saldo: ${response.saldo}"
            }.onFailure {
                tvResultado.text = "Error: ${it.message}"
            }
        }
    }

    private fun doMovimientos(cuenta: String) {
        lifecycleScope.launch {
            tvResultado.text = "Cargando movimientos..."
            val result = repository.movimientos(cuenta)
            result.onSuccess { movimientos ->
                val sb = StringBuilder("Movimientos cuenta $cuenta:\n\n")
                if (movimientos.isEmpty()) {
                    sb.append("Sin movimientos")
                } else {
                    movimientos.forEach { m ->
                        sb.append("#${m.nromov} | ${m.fecha.substring(0, 10)} | ${m.tipo}\n")
                        sb.append("  ${m.accion}: ${m.importe}\n")
                        sb.append("---\n")
                    }
                }
                tvResultado.text = sb.toString()
            }.onFailure {
                tvResultado.text = "Error: ${it.message}"
            }
        }
    }
}
