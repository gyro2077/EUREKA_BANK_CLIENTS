package com.eurekabank.api

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.Spinner
import android.widget.TextView
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.lifecycle.lifecycleScope
import com.eurekabank.api.data.network.EnvironmentManager
import com.eurekabank.api.data.network.ServerType
import com.eurekabank.api.data.repository.IEurekaBankRepository
import com.eurekabank.api.data.repository.RepositoryFactory
import kotlinx.coroutines.launch

import com.eurekabank.api.data.network.HostType

class LoginActivity : ComponentActivity() {

    private lateinit var repository: IEurekaBankRepository

    private lateinit var spnServidor: Spinner
    private lateinit var etUsuario: EditText
    private lateinit var etPassword: EditText
    private lateinit var btnLogin: Button
    private lateinit var tvError: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        spnServidor = findViewById(R.id.spnServidor)
        etUsuario = findViewById(R.id.etUsuario)
        etPassword = findViewById(R.id.etPassword)
        btnLogin = findViewById(R.id.btnLogin)
        tvError = findViewById(R.id.tvError)

        setupServerSpinner()

        repository = RepositoryFactory.getRepository()

        btnLogin.setOnClickListener {
            val usuario = etUsuario.text.toString().trim()
            val password = etPassword.text.toString().trim()
            if (usuario.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "Completa todos los campos", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            doLogin(usuario, password)
        }
    }

    private fun setupServerSpinner() {
        val servers = ServerType.values().map { it.label }
        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, servers)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spnServidor.adapter = adapter
        spnServidor.setSelection(ServerType.values().indexOf(EnvironmentManager.currentServer))

        spnServidor.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                EnvironmentManager.currentServer = ServerType.values()[position]
                repository = RepositoryFactory.getRepository()
            }
            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }
    }

    private fun doLogin(usuario: String, password: String) {
        btnLogin.isEnabled = false
        btnLogin.text = "Conectando..."
        tvError.visibility = View.GONE

        lifecycleScope.launch {
            val result = repository.login(usuario, password)
            result.onSuccess {
                val intent = Intent(this@LoginActivity, HomeActivity::class.java)
                intent.putExtra("usuario", usuario)
                startActivity(intent)
                finish()
            }.onFailure {
                tvError.text = it.message
                tvError.visibility = View.VISIBLE
                btnLogin.isEnabled = true
                btnLogin.text = "Iniciar Sesion"
            }
        }
    }
}
