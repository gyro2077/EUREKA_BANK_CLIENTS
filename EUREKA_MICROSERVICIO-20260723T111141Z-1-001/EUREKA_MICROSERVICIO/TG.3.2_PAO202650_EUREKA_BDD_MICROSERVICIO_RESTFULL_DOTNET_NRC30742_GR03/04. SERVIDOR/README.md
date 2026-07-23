# EurekaBank - Servidor SOAP .NET 8

Servidor SOAP de banca core (.NET 8 + SoapCore + MySQL 5.7) con operaciones de login, depositos, retiros, transferencias y consulta de movimientos.

## Arquitectura

- **API**: .NET 8 con SoapCore (SOAP 1.1)
- **Base de datos**: MySQL 5.7
- **Contenedores**: Docker + Docker Compose

## Requisitos

- Docker y Docker Compose instalados
- Puerto **8092** disponible en el host

## Inicio rapido

```bash
docker compose up --build -d
```

El servicio estara disponible en `http://<TU_IP_PUBLICA>:8092`

## Endpoints

| Ruta | Descripcion |
|---|---|
| `GET /` | Health check simple |
| `GET /testdb` | Prueba de conexion a BD |
| `POST /CoreBancarioWS` | Endpoint SOAP |
| `GET /CoreBancarioWS?wsdl` | WSDL del servicio |

## APIs SOAP disponibles

Namespace: `http://ws.monster.edu.ec/`

### 1. Ping

Verifica que el servicio esta activo.

```bash
curl -X POST http://localhost:8092/CoreBancarioWS \
  -H "Content-Type: text/xml" \
  -d '<soapenv:Envelope xmlns:soapenv="http://schemas.xmlsoap.org/soap/envelope/" xmlns:ws="http://ws.monster.edu.ec/">
  <soapenv:Body><ws:Ping/></soapenv:Body></soapenv:Envelope>'
```

### 2. Login

Autenticacion del sistema.

**Credenciales**: `MONSTER` / `MONSTER9`

```bash
curl -X POST http://localhost:8092/CoreBancarioWS \
  -H "Content-Type: text/xml" \
  -d '<soapenv:Envelope xmlns:soapenv="http://schemas.xmlsoap.org/soap/envelope/" xmlns:ws="http://ws.monster.edu.ec/">
  <soapenv:Body>
    <ws:Login>
      <ws:usuario>MONSTER</ws:usuario>
      <ws:password>MONSTER9</ws:password>
    </ws:Login>
  </soapenv:Body></soapenv:Envelope>'
```

### 3. Registrar Deposito

Deposita un importe en una cuenta.

```bash
curl -X POST http://localhost:8092/CoreBancarioWS \
  -H "Content-Type: text/xml" \
  -d '<soapenv:Envelope xmlns:soapenv="http://schemas.xmlsoap.org/soap/envelope/" xmlns:ws="http://ws.monster.edu.ec/">
  <soapenv:Body>
    <ws:RegistrarDeposito>
      <ws:cuenta>00100001</ws:cuenta>
      <ws:importe>100.00</ws:importe>
    </ws:RegistrarDeposito>
  </soapenv:Body></soapenv:Envelope>'
```

### 4. Registrar Retiro

Retira un importe de una cuenta.

```bash
curl -X POST http://localhost:8092/CoreBancarioWS \
  -H "Content-Type: text/xml" \
  -d '<soapenv:Envelope xmlns:soapenv="http://schemas.xmlsoap.org/soap/envelope/" xmlns:ws="http://ws.monster.edu.ec/">
  <soapenv:Body>
    <ws:RegistrarRetiro>
      <ws:cuenta>00100001</ws:cuenta>
      <ws:importe>50.00</ws:importe>
    </ws:RegistrarRetiro>
  </soapenv:Body></soapenv:Envelope>'
```

### 5. Registrar Transferencia

Transfiere importe entre dos cuentas.

```bash
curl -X POST http://localhost:8092/CoreBancarioWS \
  -H "Content-Type: text/xml" \
  -d '<soapenv:Envelope xmlns:soapenv="http://schemas.xmlsoap.org/soap/envelope/" xmlns:ws="http://ws.monster.edu.ec/">
  <soapenv:Body>
    <ws:RegistrarTransferencia>
      <ws:cuentaOrigen>00100001</ws:cuentaOrigen>
      <ws:cuentaDestino>00100002</ws:cuentaDestino>
      <ws:importe>25.00</ws:importe>
    </ws:RegistrarTransferencia>
  </soapenv:Body></soapenv:Envelope>'
```

### 6. Obtener Movimientos

Consulta el historial de movimientos de una cuenta.

```bash
curl -X POST http://localhost:8092/CoreBancarioWS \
  -H "Content-Type: text/xml" \
  -d '<soapenv:Envelope xmlns:soapenv="http://schemas.xmlsoap.org/soap/envelope/" xmlns:ws="http://ws.monster.edu.ec/">
  <soapenv:Body>
    <ws:ObtenerMovimientos>
      <ws:cuenta>00100001</ws:cuenta>
    </ws:ObtenerMovimientos>
  </soapenv:Body></soapenv:Envelope>'
```

## Consumo desde internet

Para consumir el servicio desde internet, reemplaza `localhost` por tu IP publica o dominio:

```bash
curl -X POST http://<TU_IP_PUBLICA>:8092/CoreBancarioWS \
  -H "Content-Type: text/xml" \
  -d '<soapenv:Envelope xmlns:soapenv="http://schemas.xmlsoap.org/soap/envelope/" xmlns:ws="http://ws.monster.edu.ec/">
  <soapenv:Body><ws:Ping/></soapenv:Body></soapenv:Envelope>'
```

### Ver WSDL desde internet

```bash
curl http://<TU_IP_PUBLICA>:8092/CoreBancarioWS?wsdl
```

## Cuentas disponibles

| Cuenta | Saldo Inicial |
|---|---|
| 00100001 | 1000.00 |
| 00100002 | 500.00 |
| 00100003 | 2500.00 |
| 00100004 | 750.00 |
| 00100005 | 3000.00 |

## Estructura del proyecto

```
.
├── app/
│   ├── Data/            # Acceso a datos (MySQL)
│   ├── Models/          # Modelos SOAP
│   ├── Services/        # Implementacion del servicio SOAP
│   ├── Program.cs       # Entry point
│   └── EurekaBankSOAP.csproj
├── db/
│   ├── 01_Crea_BD.sql   # Schema de la BD
│   └── 02_Carga_Datos.sql # Datos iniciales
├── docker-compose.yml
├── Dockerfile
└── README.md
```

## Comandos utiles

```bash
# Ver logs
docker compose logs -f api_dotnet_soap

# Detener
docker compose down

# Reconstruir
docker compose up --build -d

# Verificar WSDL
curl http://localhost:8092/CoreBancarioWS?wsdl
```

## Configuracion de red

El servicio escucha en `0.0.0.0:8080` dentro del contenedor y se mapea al puerto `8092` del host, accesible desde cualquier interfaz de red (incluida internet si el firewall lo permite).
