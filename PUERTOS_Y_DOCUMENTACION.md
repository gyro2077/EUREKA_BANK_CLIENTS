# 📘 Documentación Técnica Oficial — Microservicios EurekaBank

Este documento contiene la especificación técnica completa, mapas de puertos, arquitectura de microservicios y la guía práctica con comandos `curl` para la integración de clientes con las APIS de **EurekaBank** (tanto en **.NET REST** como en **Java SOAP**).

---

## 📑 Tabla de Contenidos
1. [Arquitectura General](#1-arquitectura-general)
2. [Tabla Maestra de Puertos y Servicios](#2-tabla-maestra-de-puertos-y-servicios)
3. [Documentación de Microservicios .NET REST](#3-documentación-de-microservicios-net-rest)
   - [3.1. ms-login (Puerto 8093)](#31-ms-login-puerto-8093)
   - [3.2. ms-cuentas (Puerto 8097)](#32-ms-cuentas-puerto-8097)
   - [3.3. ms-movimientos (Puerto 8098)](#33-ms-movimientos-puerto-8098)
   - [3.4. ms-transferencias (Puerto 8099)](#34-ms-transferencias-puerto-8099)
4. [Documentación de Microservicios Java SOAP](#4-documentación-de-microservicios-java-soap)
   - [4.1. ms-login (Puerto 8091)](#41-ms-login-puerto-8091)
   - [4.2. ms-cuentas (Puerto 8094)](#42-ms-cuentas-puerto-8094)
   - [4.3. ms-movimientos (Puerto 8095)](#43-ms-movimientos-puerto-8095)
   - [4.4. ms-transferencias (Puerto 8096)](#44-ms-transferencias-puerto-8096)
5. [Datos y Credenciales de Prueba](#5-datos-y-credenciales-de-prueba)
6. [Guía de Administración Docker](#6-guía-de-administración-docker)

---

## 1. Arquitectura General

EurekaBank ha sido descompuesto de sus monolitos originales hacia **8 microservicios independientes** totalmente containerizados en Docker:

- **Aislamiento Total:** Si un microservicio (ej. Depósitos/Retiros) se detiene o falla, los demás microservicios (Login, Movimientos) permanecen 100% operativos.
- **Orquestación de Transferencias (Patrón Saga):** El microservicio de Transferencias no accede a la base de datos de forma directa; invoca internamente los endpoints del microservicio de Cuentas para debitar la cuenta origen y acreditar la cuenta destino. En caso de fallo en el destino, se ejecuta una compensación automática.

---

## 2. Tabla Maestra de Puertos y Servicios

### 🌐 Microservicios .NET REST (`04.ServidorDOTNETEurekaBankRESTFULL`)

| Servicio | Puerto Externo | Contenedor Docker | Tecnología / Base de Datos |
| :--- | :--- | :--- | :--- |
| **ms-login** | `8093` | `eurekabank_rest_ms_login` | .NET 8 / En Memoria (SHA-256) |
| **ms-cuentas** | `8097` | `eurekabank_rest_ms_cuentas` | .NET 8 / MySQL 5.7 (`eurekabank`) |
| **ms-movimientos** | `8098` | `eurekabank_rest_ms_movimientos` | .NET 8 / MySQL 5.7 (`eurekabank`) |
| **ms-transferencias**| `8099` | `eurekabank_rest_ms_transferencias` | .NET 8 / HttpClient a `ms-cuentas` |
| **db** | `3306` (int) | `eurekabank_rest_db` | MySQL 5.7 Container |

### ☕ Microservicios Java SOAP (`04.ServidorJAVAEurekaBankSOAP`)

| Servicio | Puerto Externo | Contenedor Docker | App Server / Base de Datos |
| :--- | :--- | :--- | :--- |
| **ms-login** | `8091` | `eurekabank_soap_ms_login` | GlassFish 7 / En Memoria (SHA-256) |
| **ms-cuentas** | `8094` | `eurekabank_soap_ms_cuentas` | GlassFish 7 / MySQL 5.7 (`eurekabank`) |
| **ms-movimientos** | `8095` | `eurekabank_soap_ms_movimientos` | GlassFish 7 / MySQL 5.7 (`eurekabank`) |
| **ms-transferencias**| `8096` | `eurekabank_soap_ms_transferencias` | GlassFish 7 / HTTP XML a `ms-cuentas` |
| **db** | `3306` (int) | `eurekabank_soap_db` | MySQL 5.7 Container |

---

## 3. Documentación de Microservicios .NET REST

Base URL común: `http://localhost:{PUERTO}/resources/corebancario`

---

### 3.1. ms-login (Puerto 8093)

#### A. Verificación de Salud (Ping)
- **Método:** `GET`
- **URL:** `http://localhost:8093/resources/corebancario/ping`

**Ejemplo `curl`:**
```bash
curl -s http://localhost:8093/resources/corebancario/ping
```
**Respuesta:**
```json
{"msg":"ok"}
```

#### B. Autenticación de Usuario
- **Método:** `POST`
- **URL:** `http://localhost:8093/resources/corebancario/login`
- **Header:** `Content-Type: application/json`

**Ejemplo `curl`:**
```bash
curl -s -X POST http://localhost:8093/resources/corebancario/login \
  -H "Content-Type: application/json" \
  -d '{
    "usuario": "MONSTER",
    "password": "MONSTER9"
  }'
```

**Respuesta Exitosa (200 OK):**
```json
{
  "resultado": "Exitoso"
}
```

**Respuesta Denegada (200 OK):**
```json
{
  "resultado": "Denegado"
}
```

---

### 3.2. ms-cuentas (Puerto 8097)

#### A. Depósito en Cuenta
- **Método:** `POST`
- **URL:** `http://localhost:8097/resources/corebancario/deposito`
- **Header:** `Content-Type: application/json`

**Ejemplo `curl`:**
```bash
curl -s -X POST http://localhost:8097/resources/corebancario/deposito \
  -H "Content-Type: application/json" \
  -d '{
    "cuenta": "00100001",
    "importe": 100.0
  }'
```

**Respuesta Exitosa (200 OK):**
```json
{
  "estado": 1,
  "saldo": 7100.0
}
```

**Respuesta de Error (500 Internal Server Error):**
```json
{
  "estado": -1,
  "saldo": -1.0
}
```

#### B. Retiro de Cuenta
- **Método:** `POST`
- **URL:** `http://localhost:8097/resources/corebancario/retiro`
- **Header:** `Content-Type: application/json`

**Ejemplo `curl`:**
```bash
curl -s -X POST http://localhost:8097/resources/corebancario/retiro \
  -H "Content-Type: application/json" \
  -d '{
    "cuenta": "00100001",
    "importe": 50.0
  }'
```

**Respuesta Exitosa (200 OK):**
```json
{
  "estado": 1,
  "saldo": 7050.0
}
```

---

### 3.3. ms-movimientos (Puerto 8098)

#### A. Consulta de Historial de Movimientos
- **Método:** `GET`
- **URL:** `http://localhost:8098/resources/corebancario/movimientos/{cuenta}`

**Ejemplo `curl`:**
```bash
curl -s http://localhost:8098/resources/corebancario/movimientos/00100001
```

**Respuesta Exitosa (200 OK):**
```json
[
  {
    "cuenta": "00100001",
    "nromov": 12,
    "fecha": "2026-07-23T00:00:00",
    "tipo": "Retiro",
    "accion": "SALIDA",
    "importe": 50.0,
    "referencia": null
  },
  {
    "cuenta": "00100001",
    "nromov": 11,
    "fecha": "2026-07-23T00:00:00",
    "tipo": "Depósito",
    "accion": "INGRESO",
    "importe": 100.0,
    "referencia": null
  }
]
```

---

### 3.4. ms-transferencias (Puerto 8099)

#### A. Transferencia Entre Cuentas
- **Método:** `POST`
- **URL:** `http://localhost:8099/resources/corebancario/transferencia`
- **Header:** `Content-Type: application/json`

**Ejemplo `curl`:**
```bash
curl -s -X POST http://localhost:8099/resources/corebancario/transferencia \
  -H "Content-Type: application/json" \
  -d '{
    "cuentaOrigen": "00100001",
    "cuentaDestino": "00200001",
    "importe": 25.0
  }'
```

**Respuesta Exitosa (200 OK):**
```json
{
  "estado": 1,
  "saldo": 7025.0
}
```

---

## 4. Documentación de Microservicios Java SOAP

Los microservicios SOAP se comunican mediante protocolos XML SOAP 1.1 sobre HTTP.

- **WSDL Base:** `http://localhost:{PUERTO}/ROOT/CoreBancarioWS?wsdl`
- **Target Namespace:** `http://ws.monster.edu.ec/`

---

### 4.1. ms-login (Puerto 8091)

- **WSDL:** `http://localhost:8091/ROOT/CoreBancarioWS?wsdl`
- **Endpoint:** `http://localhost:8091/ROOT/CoreBancarioWS`

#### Operación `login` (SOAP XML)

**Ejemplo `curl`:**
```bash
curl -s -X POST http://localhost:8091/ROOT/CoreBancarioWS \
  -H "Content-Type: text/xml;charset=UTF-8" \
  -H "SOAPAction: \"\"" \
  -d '<?xml version="1.0" encoding="UTF-8"?>
<soapenv:Envelope xmlns:soapenv="http://schemas.xmlsoap.org/soap/envelope/" xmlns:ws="http://ws.monster.edu.ec/">
   <soapenv:Header/>
   <soapenv:Body>
      <ws:login>
         <usuario>MONSTER</usuario>
         <password>MONSTER9</password>
      </ws:login>
   </soapenv:Body>
</soapenv:Envelope>'
```

**Respuesta XML:**
```xml
<?xml version='1.0' encoding='UTF-8'?>
<S:Envelope xmlns:S="http://schemas.xmlsoap.org/soap/envelope/">
    <S:Body>
        <ns2:loginResponse xmlns:ns2="http://ws.monster.edu.ec/">
            <return>Exitoso</return>
        </ns2:loginResponse>
    </S:Body>
</S:Envelope>
```

---

### 4.2. ms-cuentas (Puerto 8094)

- **WSDL:** `http://localhost:8094/ROOT/CoreBancarioWS?wsdl`
- **Endpoint:** `http://localhost:8094/ROOT/CoreBancarioWS`

#### A. Operación `registrarDeposito`

**Ejemplo `curl`:**
```bash
curl -s -X POST http://localhost:8094/ROOT/CoreBancarioWS \
  -H "Content-Type: text/xml;charset=UTF-8" \
  -H "SOAPAction: \"\"" \
  -d '<?xml version="1.0" encoding="UTF-8"?>
<soapenv:Envelope xmlns:soapenv="http://schemas.xmlsoap.org/soap/envelope/" xmlns:ws="http://ws.monster.edu.ec/">
   <soapenv:Header/>
   <soapenv:Body>
      <ws:registrarDeposito>
         <cuenta>00100001</cuenta>
         <importe>100.0</importe>
      </ws:registrarDeposito>
   </soapenv:Body>
</soapenv:Envelope>'
```

**Respuesta XML:**
```xml
<?xml version='1.0' encoding='UTF-8'?>
<S:Envelope xmlns:S="http://schemas.xmlsoap.org/soap/envelope/">
    <S:Body>
        <ns2:registrarDepositoResponse xmlns:ns2="http://ws.monster.edu.ec/">
            <return>
                <estado>1</estado>
                <saldo>7125.0</saldo>
            </return>
        </ns2:registrarDepositoResponse>
    </S:Body>
</S:Envelope>
```

#### B. Operación `registrarRetiro`

**Ejemplo `curl`:**
```bash
curl -s -X POST http://localhost:8094/ROOT/CoreBancarioWS \
  -H "Content-Type: text/xml;charset=UTF-8" \
  -H "SOAPAction: \"\"" \
  -d '<?xml version="1.0" encoding="UTF-8"?>
<soapenv:Envelope xmlns:soapenv="http://schemas.xmlsoap.org/soap/envelope/" xmlns:ws="http://ws.monster.edu.ec/">
   <soapenv:Header/>
   <soapenv:Body>
      <ws:registrarRetiro>
         <cuenta>00100001</cuenta>
         <importe>50.0</importe>
      </ws:registrarRetiro>
   </soapenv:Body>
</soapenv:Envelope>'
```

**Respuesta XML:**
```xml
<?xml version='1.0' encoding='UTF-8'?>
<S:Envelope xmlns:S="http://schemas.xmlsoap.org/soap/envelope/">
    <S:Body>
        <ns2:registrarRetiroResponse xmlns:ns2="http://ws.monster.edu.ec/">
            <return>
                <estado>1</estado>
                <saldo>7075.0</saldo>
            </return>
        </ns2:registrarRetiroResponse>
    </S:Body>
</S:Envelope>
```

---

### 4.3. ms-movimientos (Puerto 8095)

- **WSDL:** `http://localhost:8095/ROOT/CoreBancarioWS?wsdl`
- **Endpoint:** `http://localhost:8095/ROOT/CoreBancarioWS`

#### Operación `obtenerMovimientos`

**Ejemplo `curl`:**
```bash
curl -s -X POST http://localhost:8095/ROOT/CoreBancarioWS \
  -H "Content-Type: text/xml;charset=UTF-8" \
  -H "SOAPAction: \"\"" \
  -d '<?xml version="1.0" encoding="UTF-8"?>
<soapenv:Envelope xmlns:soapenv="http://schemas.xmlsoap.org/soap/envelope/" xmlns:ws="http://ws.monster.edu.ec/">
   <soapenv:Header/>
   <soapenv:Body>
      <ws:obtenerMovimientos>
         <cuenta>00100001</cuenta>
      </ws:obtenerMovimientos>
   </soapenv:Body>
</soapenv:Envelope>'
```

**Respuesta XML:**
```xml
<?xml version='1.0' encoding='UTF-8'?>
<S:Envelope xmlns:S="http://schemas.xmlsoap.org/soap/envelope/">
    <S:Body>
        <ns2:obtenerMovimientosResponse xmlns:ns2="http://ws.monster.edu.ec/">
            <return>
                <accion>SALIDA</accion>
                <cuenta>00100001</cuenta>
                <fecha>2026-07-23T00:00:00+02:00</fecha>
                <importe>50.0</importe>
                <nromov>12</nromov>
                <tipo>Retiro</tipo>
            </return>
        </ns2:obtenerMovimientosResponse>
    </S:Body>
</S:Envelope>
```

---

### 4.4. ms-transferencias (Puerto 8096)

- **WSDL:** `http://localhost:8096/ROOT/CoreBancarioWS?wsdl`
- **Endpoint:** `http://localhost:8096/ROOT/CoreBancarioWS`

#### Operación `registrarTransferencia`

**Ejemplo `curl`:**
```bash
curl -s -X POST http://localhost:8096/ROOT/CoreBancarioWS \
  -H "Content-Type: text/xml;charset=UTF-8" \
  -H "SOAPAction: \"\"" \
  -d '<?xml version="1.0" encoding="UTF-8"?>
<soapenv:Envelope xmlns:soapenv="http://schemas.xmlsoap.org/soap/envelope/" xmlns:ws="http://ws.monster.edu.ec/">
   <soapenv:Header/>
   <soapenv:Body>
      <ws:registrarTransferencia>
         <cuentaOrigen>00100001</cuentaOrigen>
         <cuentaDestino>00200001</cuentaDestino>
         <importe>25.0</importe>
      </ws:registrarTransferencia>
   </soapenv:Body>
</soapenv:Envelope>'
```

**Respuesta XML:**
```xml
<?xml version='1.0' encoding='UTF-8'?>
<S:Envelope xmlns:S="http://schemas.xmlsoap.org/soap/envelope/">
    <S:Body>
        <ns2:registrarTransferenciaResponse xmlns:ns2="http://ws.monster.edu.ec/">
            <return>
                <estado>1</estado>
                <saldo>7050.0</saldo>
            </return>
        </ns2:registrarTransferenciaResponse>
    </S:Body>
</S:Envelope>
```

---

## 5. Datos y Credenciales de Prueba

| Parámetro | Valor de Prueba |
| :--- | :--- |
| **Usuario Autenticación** | `MONSTER` |
| **Contraseña Autenticación** | `MONSTER9` |
| **Cuenta Principal (Origen)** | `00100001` |
| **Cuenta Secundaria (Destino)** | `00200001` |
| **Código de Empleado (Default)** | `0001` / `0004` |

---

## 6. Guía de Administración Docker

### A. Ver Estado de los Microservicios

**.NET REST:**
```bash
cd /home/arquitectura/04.ServidorDOTNETEurekaBankRESTFULL
sudo docker compose ps
```

**Java SOAP:**
```bash
cd /home/arquitectura/04.ServidorJAVAEurekaBankSOAP
sudo docker compose ps
```

### B. Reiniciar o Iniciar un Microservicio Específico

Si deseas detener o reiniciar solo **un microservicio en particular** sin afectar los demás:

```bash
# Ejemplo: Reiniciar solo Cuentas en .NET REST
cd /home/arquitectura/04.ServidorDOTNETEurekaBankRESTFULL
sudo docker compose restart ms-cuentas

# Ejemplo: Detener solo Login en Java SOAP
cd /home/arquitectura/04.ServidorJAVAEurekaBankSOAP
sudo docker compose stop ms-login
```

### C. Ver Logs en Tiempo Real

```bash
# Logs de un microservicio específico
sudo docker compose logs -f ms-cuentas
```
