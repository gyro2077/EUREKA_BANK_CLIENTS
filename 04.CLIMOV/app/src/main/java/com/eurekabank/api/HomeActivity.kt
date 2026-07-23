package com.eurekabank.api

import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.Spinner
import android.widget.TextView
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.eurekabank.api.adapter.MovimientosAdapter
import com.eurekabank.api.data.repository.IEurekaBankRepository
import com.eurekabank.api.data.repository.RepositoryFactory
import kotlinx.coroutines.launch

class HomeActivity : ComponentActivity() {

    private lateinit var repository: IEurekaBankRepository

    private lateinit var tvUsuario: TextView
    private lateinit var btnLogout: Button
    private lateinit var spCuentaOrigen: Spinner
    private lateinit var spCuentaDestino: Spinner
    private lateinit var etImporte: EditText
    private lateinit var btnDeposito: Button
    private lateinit var btnRetiro: Button
    private lateinit var btnTransferir: Button
    private lateinit var btnCargarMovimientos: Button
    private lateinit var tvStatus: TextView
    private lateinit var rvMovimientos: RecyclerView

    private val cuentas = listOf("00100001", "00200001")

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home)

        repository = RepositoryFactory.getRepository()

        val usuario = intent.getStringExtra("usuario") ?: "Usuario"
        tvUsuario = findViewById(R.id.tvUsuario)
        tvUsuario.text = "Hola, $usuario"

        btnLogout = findViewById(R.id.btnLogout)
        spCuentaOrigen = findViewById(R.id.spCuentaOrigen)
        spCuentaDestino = findViewById(R.id.spCuentaDestino)
        etImporte = findViewById(R.id.etImporte)
        btnDeposito = findViewById(R.id.btnDeposito)
        btnRetiro = findViewById(R.id.btnRetiro)
        btnTransferir = findViewById(R.id.btnTransferir)
        btnCargarMovimientos = findViewById(R.id.btnCargarMovimientos)
        tvStatus = findViewById(R.id.tvStatus)
        rvMovimientos = findViewById(R.id.rvMovimientos)
        rvMovimientos.layoutManager = LinearLayoutManager(this)

        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, cuentas)
        spCuentaOrigen.adapter = adapter
        spCuentaDestino.adapter = adapter

        btnLogout.setOnClickListener {
            finish()
        }

        btnDeposito.setOnClickListener {
            val cuenta = spCuentaOrigen.selectedItem.toString()
            val importe = etImporte.text.toString().trim().toDoubleOrNull()
            if (importe == null || importe <= 0) {
                Toast.makeText(this, "Ingresa un importe valido", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            doDeposito(cuenta, importe)
        }

        btnRetiro.setOnClickListener {
            val cuenta = spCuentaOrigen.selectedItem.toString()
            val importe = etImporte.text.toString().trim().toDoubleOrNull()
            if (importe == null || importe <= 0) {
                Toast.makeText(this, "Ingresa un importe valido", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            doRetiro(cuenta, importe)
        }

        btnTransferir.setOnClickListener {
            val cuentaOrigen = spCuentaOrigen.selectedItem.toString()
            val cuentaDestino = spCuentaDestino.selectedItem.toString()
            val importe = etImporte.text.toString().trim().toDoubleOrNull()
            if (importe == null || importe <= 0) {
                Toast.makeText(this, "Ingresa un importe valido", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            if (cuentaOrigen == cuentaDestino) {
                Toast.makeText(this, "Las cuentas deben ser diferentes", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            doTransferencia(cuentaOrigen, cuentaDestino, importe)
        }

        btnCargarMovimientos.setOnClickListener {
            val cuenta = spCuentaOrigen.selectedItem.toString()
            cargarMovimientos(cuenta)
        }
    }

    private fun doDeposito(cuenta: String, importe: Double) {
        btnDeposito.isEnabled = false
        btnDeposito.text = "Procesando..."

        lifecycleScope.launch {
            val result = repository.deposito(cuenta, importe)
            result.onSuccess { response ->
                Toast.makeText(this@HomeActivity, "Deposito exitoso. Saldo: ${response.saldo}", Toast.LENGTH_LONG).show()
                cargarMovimientos(cuenta)
            }.onFailure {
                Toast.makeText(this@HomeActivity, "Error: ${it.message}", Toast.LENGTH_LONG).show()
            }
            btnDeposito.isEnabled = true
            btnDeposito.text = "Deposito"
        }
    }

    private fun doRetiro(cuenta: String, importe: Double) {
        btnRetiro.isEnabled = false
        btnRetiro.text = "Procesando..."

        lifecycleScope.launch {
            val result = repository.retiro(cuenta, importe)
            result.onSuccess { response ->
                Toast.makeText(this@HomeActivity, "Retiro exitoso. Saldo: ${response.saldo}", Toast.LENGTH_LONG).show()
                cargarMovimientos(cuenta)
            }.onFailure {
                Toast.makeText(this@HomeActivity, "Error: ${it.message}", Toast.LENGTH_LONG).show()
            }
            btnRetiro.isEnabled = true
            btnRetiro.text = "Retiro"
        }
    }

    private fun doTransferencia(cuentaOrigen: String, cuentaDestino: String, importe: Double) {
        btnTransferir.isEnabled = false
        btnTransferir.text = "Procesando..."

        lifecycleScope.launch {
            val result = repository.transferencia(cuentaOrigen, cuentaDestino, importe)
            result.onSuccess { response ->
                Toast.makeText(this@HomeActivity, "Transferencia exitosa. Saldo: ${response.saldo}", Toast.LENGTH_LONG).show()
                cargarMovimientos(cuentaOrigen)
            }.onFailure {
                Toast.makeText(this@HomeActivity, "Error: ${it.message}", Toast.LENGTH_LONG).show()
            }
            btnTransferir.isEnabled = true
            btnTransferir.text = "Transferir"
        }
    }

    private fun cargarMovimientos(cuenta: String) {
        tvStatus.text = "Cargando movimientos..."
        btnCargarMovimientos.isEnabled = false

        lifecycleScope.launch {
            val result = repository.movimientos(cuenta)
            result.onSuccess { movimientos ->
                if (movimientos.isEmpty()) {
                    tvStatus.text = "Sin movimientos para la cuenta $cuenta"
                    rvMovimientos.adapter = null
                } else {
                    tvStatus.text = "${movimientos.size} movimientos encontrados"
                    rvMovimientos.adapter = MovimientosAdapter(movimientos)
                }
            }.onFailure {
                tvStatus.text = "Error: ${it.message}"
            }
            btnCargarMovimientos.isEnabled = true
        }
    }
}
