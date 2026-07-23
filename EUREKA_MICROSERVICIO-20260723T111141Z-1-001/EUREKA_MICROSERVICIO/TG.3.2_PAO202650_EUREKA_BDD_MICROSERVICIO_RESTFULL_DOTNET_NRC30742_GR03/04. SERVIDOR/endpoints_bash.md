### Verificar WSDL
```bash
curl http://localhost:8092/CoreBancarioWS?wsdl
```
### Test SOAP - Ping
```bash
curl -X POST http://localhost:8092/CoreBancarioWS \
  -H "Content-Type: text/xml" \
  -d '<soapenv:Envelope xmlns:soapenv="http://schemas.xmlsoap.org/soap/envelope/" xmlns:ws="http://ws.monster.edu.ec/">
  <soapenv:Body><ws:Ping/></soapenv:Body></soapenv:Envelope>'
```
### Test SOAP - Login
```bash
curl -X POST http://localhost:8092/CoreBancarioWS \
  -H "Content-Type: text/xml" \
  -d '<soapenv:Envelope xmlns:soapenv="http://schemas.xmlsoap.org/soap/envelope/" xmlns:ws="http://ws.monster.edu.ec/">
  <soapenv:Body><ws:Login><ws:usuario>MONSTER</ws:usuario><ws:password>MONSTER9</ws:password></ws:Login></soapenv:Body></soapenv:Envelope>'
```
### Test SOAP - ObtenerMovimientos
```bash
curl -X POST http://localhost:8092/CoreBancarioWS \
  -H "Content-Type: text/xml" \
  -d '<soapenv:Envelope xmlns:soapenv="http://schemas.xmlsoap.org/soap/envelope/" xmlns:ws="http://ws.monster.edu.ec/">
  <soapenv:Body><ws:ObtenerMovimientos><ws:cuenta>00100001</ws:cuenta></ws:ObtenerMovimientos></soapenv:Body></soapenv:Envelope>'
```