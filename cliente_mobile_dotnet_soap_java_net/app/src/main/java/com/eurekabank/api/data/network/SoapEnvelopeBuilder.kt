package com.eurekabank.api.data.network

import com.eurekabank.api.data.models.LoginResponse
import com.eurekabank.api.data.models.Movimiento
import com.eurekabank.api.data.models.OperacionCuentaResponse
import org.w3c.dom.Element
import javax.xml.parsers.DocumentBuilderFactory

object SoapEnvelopeBuilder {

    private const val SOAP_NAMESPACE = "http://schemas.xmlsoap.org/soap/envelope/"
    private const val WS_NAMESPACE = "http://ws.monster.edu.ec/"

    private fun dotNetEnvelope(body: String): String =
        """<?xml version="1.0" encoding="utf-8"?>
        |<soapenv:Envelope xmlns:soapenv="$SOAP_NAMESPACE" xmlns:ws="$WS_NAMESPACE">
        |  <soapenv:Body>
        |    $body
        |  </soapenv:Body>
        |</soapenv:Envelope>""".trimMargin()

    private fun javaEnvelope(body: String): String =
        """<soapenv:Envelope xmlns:soapenv="$SOAP_NAMESPACE" xmlns:ws="$WS_NAMESPACE"><soapenv:Header/><soapenv:Body>$body</soapenv:Body></soapenv:Envelope>"""

    fun buildLogin(usuario: String, password: String, isDotNet: Boolean): String {
        val body = if (isDotNet) {
            "<ws:Login><ws:usuario>$usuario</ws:usuario><ws:password>$password</ws:password></ws:Login>"
        } else {
            "<ws:login><usuario>$usuario</usuario><password>$password</password></ws:login>"
        }
        return if (isDotNet) dotNetEnvelope(body) else javaEnvelope(body)
    }

    fun buildDeposito(cuenta: String, importe: Double, isDotNet: Boolean): String {
        val body = if (isDotNet) {
            "<ws:RegistrarDeposito><ws:cuenta>$cuenta</ws:cuenta><ws:importe>$importe</ws:importe></ws:RegistrarDeposito>"
        } else {
            "<ws:registrarDeposito><cuenta>$cuenta</cuenta><importe>$importe</importe></ws:registrarDeposito>"
        }
        return if (isDotNet) dotNetEnvelope(body) else javaEnvelope(body)
    }

    fun buildRetiro(cuenta: String, importe: Double, isDotNet: Boolean): String {
        val body = if (isDotNet) {
            "<ws:RegistrarRetiro><ws:cuenta>$cuenta</ws:cuenta><ws:importe>$importe</ws:importe></ws:RegistrarRetiro>"
        } else {
            "<ws:registrarRetiro><cuenta>$cuenta</cuenta><importe>$importe</importe></ws:registrarRetiro>"
        }
        return if (isDotNet) dotNetEnvelope(body) else javaEnvelope(body)
    }

    fun buildTransferencia(cuentaOrigen: String, cuentaDestino: String, importe: Double, isDotNet: Boolean): String {
        val body = if (isDotNet) {
            "<ws:RegistrarTransferencia><ws:cuentaOrigen>$cuentaOrigen</ws:cuentaOrigen><ws:cuentaDestino>$cuentaDestino</ws:cuentaDestino><ws:importe>$importe</ws:importe></ws:RegistrarTransferencia>"
        } else {
            "<ws:registrarTransferencia><cuentaOrigen>$cuentaOrigen</cuentaOrigen><cuentaDestino>$cuentaDestino</cuentaDestino><importe>$importe</importe></ws:registrarTransferencia>"
        }
        return if (isDotNet) dotNetEnvelope(body) else javaEnvelope(body)
    }

    fun buildMovimientos(cuenta: String, isDotNet: Boolean): String {
        val body = if (isDotNet) {
            "<ws:ObtenerMovimientos><ws:cuenta>$cuenta</ws:cuenta></ws:ObtenerMovimientos>"
        } else {
            "<ws:obtenerMovimientos><cuenta>$cuenta</cuenta></ws:obtenerMovimientos>"
        }
        return if (isDotNet) dotNetEnvelope(body) else javaEnvelope(body)
    }

    fun buildPing(isDotNet: Boolean): String {
        val body = if (isDotNet) "<ws:Ping/>" else "<ws:ping/>"
        return if (isDotNet) dotNetEnvelope(body) else javaEnvelope(body)
    }

    private fun getTagText(doc: Element, tagName: String): String? {
        val nodes = doc.getElementsByTagName(tagName)
        return if (nodes.length > 0) nodes.item(0)?.textContent else null
    }

    private fun findAnyTagText(doc: Element, vararg tagNames: String): String? {
        for (tag in tagNames) {
            val value = getTagText(doc, tag)
            if (value != null) return value
        }
        return null
    }

    fun parsePingResponse(xml: String): String {
        val doc = parseXml(xml)
        return findAnyTagText(doc, "PingResult", "pingResult", "msg", "resultado", "return") ?: "Sin respuesta"
    }

    fun parseLoginResponse(xml: String): LoginResponse {
        val doc = parseXml(xml)
        val loginResult = findAnyTagText(doc, "LoginResult", "loginResult", "return", "resultado")
        val isExitoso = loginResult.equals("Exitoso", ignoreCase = true) ||
                loginResult.equals("T", ignoreCase = true) ||
                loginResult.equals("true", ignoreCase = true)
        return if (isExitoso) LoginResponse("Exitoso") else LoginResponse(loginResult ?: "Error")
    }

    fun parseOperacionResponse(xml: String): OperacionCuentaResponse {
        val doc = parseXml(xml)

        val estado = findAnyTagText(doc, "Estado", "estado", "codigo", "Codigo", "EstadoResult")
            ?: "0"
        val saldo = findAnyTagText(doc, "Saldo", "saldo", "saldoActual", "SaldoActual")
            ?: "0.0"

        return OperacionCuentaResponse(
            codigo = estado.toIntOrNull() ?: 0,
            mensaje = if (estado.toIntOrNull() == -1) "Error en operacion" else "Operacion exitosa",
            saldo = saldo.toDoubleOrNull() ?: 0.0
        )
    }

    fun parseMovimientosResponse(xml: String): List<Movimiento> {
        val doc = parseXml(xml)
        val movimientos = mutableListOf<Movimiento>()

        val returnNodes = doc.getElementsByTagName("return")
        val movimientoNodes = doc.getElementsByTagName("Movimiento")

        if (movimientoNodes.length > 0) {
            for (i in 0 until movimientoNodes.length) {
                val node = movimientoNodes.item(i) as Element
                movimientos.add(parseMovimientoNode(node, isDotNet = true))
            }
        } else if (returnNodes.length > 0) {
            for (i in 0 until returnNodes.length) {
                val node = returnNodes.item(i) as Element
                if (node.getElementsByTagName("cuenta").length > 0 ||
                    node.getElementsByTagName("Cuenta").length > 0) {
                    movimientos.add(parseMovimientoNode(node, isDotNet = false))
                }
            }
        }

        return movimientos
    }

    private fun parseMovimientoNode(node: Element, isDotNet: Boolean): Movimiento {
        fun childText(vararg tags: String): String {
            for (tag in tags) {
                val nodes = node.getElementsByTagName(tag)
                if (nodes.length > 0) return nodes.item(0)?.textContent ?: "0"
            }
            return "0"
        }

        return if (isDotNet) {
            Movimiento(
                cuenta = childText("Cuenta", "cuenta"),
                nromov = childText("Nromov", "nromov").toIntOrNull() ?: 0,
                fecha = childText("Fecha", "fecha"),
                tipo = childText("Tipo", "tipo"),
                accion = childText("Accion", "accion"),
                importe = childText("Importe", "importe").toDoubleOrNull() ?: 0.0,
                referencia = childText("Referencia", "referencia").takeIf { it.isNotEmpty() }
            )
        } else {
            Movimiento(
                cuenta = childText("cuenta", "Cuenta"),
                nromov = childText("nromov", "Nromov").toIntOrNull() ?: 0,
                fecha = childText("fecha", "Fecha"),
                tipo = childText("tipo", "Tipo"),
                accion = childText("accion", "Accion"),
                importe = childText("importe", "Importe").toDoubleOrNull() ?: 0.0,
                referencia = childText("referencia", "Referencia").takeIf { it.isNotEmpty() }
            )
        }
    }

    private fun parseXml(xml: String): Element {
        val factory = DocumentBuilderFactory.newInstance()
        factory.isNamespaceAware = false
        factory.isValidating = false
        val builder = factory.newDocumentBuilder()
        val doc = builder.parse(xml.byteInputStream())
        doc.documentElement.normalize()
        return doc.documentElement
    }
}
