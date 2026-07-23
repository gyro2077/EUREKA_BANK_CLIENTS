import xml.etree.ElementTree as ET
from eureka_cli.api.models import LoginResponse, OperacionCuentaResponse, Movimiento

SOAP_NAMESPACE = "http://schemas.xmlsoap.org/soap/envelope/"
WS_NAMESPACE = "http://ws.monster.edu.ec/"


def _dotnet_envelope(body: str) -> str:
    return f"""<?xml version="1.0" encoding="utf-8"?>
<soapenv:Envelope xmlns:soapenv="{SOAP_NAMESPACE}" xmlns:ws="{WS_NAMESPACE}">
  <soapenv:Body>
    {body}
  </soapenv:Body>
</soapenv:Envelope>"""


def _java_envelope(body: str) -> str:
    return f"""<soapenv:Envelope xmlns:soapenv="{SOAP_NAMESPACE}" xmlns:ws="{WS_NAMESPACE}"><soapenv:Header/><soapenv:Body>{body}</soapenv:Body></soapenv:Envelope>"""


def build_login(usuario: str, password: str, is_dotnet: bool) -> str:
    if is_dotnet:
        body = f"<ws:Login><ws:usuario>{usuario}</ws:usuario><ws:password>{password}</ws:password></ws:Login>"
    else:
        body = f"<ws:login><usuario>{usuario}</usuario><password>{password}</password></ws:login>"
    return _dotnet_envelope(body) if is_dotnet else _java_envelope(body)


def build_deposito(cuenta: str, importe: float, is_dotnet: bool) -> str:
    if is_dotnet:
        body = f"<ws:RegistrarDeposito><ws:cuenta>{cuenta}</ws:cuenta><ws:importe>{importe}</ws:importe></ws:RegistrarDeposito>"
    else:
        body = f"<ws:registrarDeposito><cuenta>{cuenta}</cuenta><importe>{importe}</importe></ws:registrarDeposito>"
    return _dotnet_envelope(body) if is_dotnet else _java_envelope(body)


def build_retiro(cuenta: str, importe: float, is_dotnet: bool) -> str:
    if is_dotnet:
        body = f"<ws:RegistrarRetiro><ws:cuenta>{cuenta}</ws:cuenta><ws:importe>{importe}</ws:importe></ws:RegistrarRetiro>"
    else:
        body = f"<ws:registrarRetiro><cuenta>{cuenta}</cuenta><importe>{importe}</importe></ws:registrarRetiro>"
    return _dotnet_envelope(body) if is_dotnet else _java_envelope(body)


def build_transferencia(cuenta_origen: str, cuenta_destino: str, importe: float, is_dotnet: bool) -> str:
    if is_dotnet:
        body = f"<ws:RegistrarTransferencia><ws:cuentaOrigen>{cuenta_origen}</ws:cuentaOrigen><ws:cuentaDestino>{cuenta_destino}</ws:cuentaDestino><ws:importe>{importe}</ws:importe></ws:RegistrarTransferencia>"
    else:
        body = f"<ws:registrarTransferencia><cuentaOrigen>{cuenta_origen}</cuentaOrigen><cuentaDestino>{cuenta_destino}</cuentaDestino><importe>{importe}</importe></ws:registrarTransferencia>"
    return _dotnet_envelope(body) if is_dotnet else _java_envelope(body)


def build_movimientos(cuenta: str, is_dotnet: bool) -> str:
    if is_dotnet:
        body = f"<ws:ObtenerMovimientos><ws:cuenta>{cuenta}</ws:cuenta></ws:ObtenerMovimientos>"
    else:
        body = f"<ws:obtenerMovimientos><cuenta>{cuenta}</cuenta></ws:obtenerMovimientos>"
    return _dotnet_envelope(body) if is_dotnet else _java_envelope(body)


def build_ping(is_dotnet: bool) -> str:
    body = "<ws:Ping/>" if is_dotnet else "<ws:ping/>"
    return _dotnet_envelope(body) if is_dotnet else _java_envelope(body)


def _get_tag_text(root: ET.Element, tag_name: str) -> str | None:
    for elem in root.iter():
        if elem.tag.endswith(tag_name) or elem.tag == tag_name:
            if elem.text and elem.text.strip():
                return elem.text.strip()
    return None


def _find_any_tag_text(root: ET.Element, *tag_names: str) -> str | None:
    for tag in tag_names:
        value = _get_tag_text(root, tag)
        if value is not None:
            return value
    return None


def _parse_xml(xml: str) -> ET.Element:
    root = ET.fromstring(xml)
    return root


def parse_ping_response(xml: str) -> str:
    root = _parse_xml(xml)
    return _find_any_tag_text(root, "PingResult", "pingResult", "msg", "resultado", "return") or "Sin respuesta"


def parse_login_response(xml: str) -> LoginResponse:
    root = _parse_xml(xml)
    login_result = _find_any_tag_text(root, "LoginResult", "loginResult", "return", "resultado")
    is_exitoso = login_result and login_result.lower() in ("exitoso", "t", "true")
    return LoginResponse("Exitoso" if is_exitoso else (login_result or "Error"))


def parse_operacion_response(xml: str) -> OperacionCuentaResponse:
    root = _parse_xml(xml)
    estado = _find_any_tag_text(root, "Estado", "estado", "codigo", "Codigo", "EstadoResult") or "0"
    saldo = _find_any_tag_text(root, "Saldo", "saldo", "saldoActual", "SaldoActual") or "0.0"
    codigo = int(estado) if estado.lstrip('-').isdigit() else 0
    mensaje = "Error en operacion" if codigo == -1 else "Operacion exitosa"
    saldo_val = float(saldo) if saldo.replace('.', '').replace('-', '').isdigit() else 0.0
    return OperacionCuentaResponse(codigo=codigo, mensaje=mensaje, saldo=saldo_val)


def parse_movimientos_response(xml: str) -> list[Movimiento]:
    root = _parse_xml(xml)
    movimientos = []

    def child_text(node: ET.Element, *tags: str) -> str:
        for tag in tags:
            for child in node.iter():
                if child.tag.endswith(tag) or child.tag == tag:
                    if child.text and child.text.strip():
                        return child.text.strip()
        return "0"

    for elem in root.iter():
        tag = elem.tag
        if tag.endswith("Movimiento") or tag == "Movimiento":
            movimientos.append(Movimiento(
                cuenta=child_text(elem, "Cuenta", "cuenta"),
                nromov=int(child_text(elem, "Nromov", "nromov")) if child_text(elem, "Nromov", "nromov").isdigit() else 0,
                fecha=child_text(elem, "Fecha", "fecha"),
                tipo=child_text(elem, "Tipo", "tipo"),
                accion=child_text(elem, "Accion", "accion"),
                importe=float(child_text(elem, "Importe", "importe")) if child_text(elem, "Importe", "importe").replace('.', '').isdigit() else 0.0,
                referencia=child_text(elem, "Referencia", "referencia") or None,
            ))
        elif tag == "return":
            if child_text(elem, "cuenta", "Cuenta") != "0":
                movimientos.append(Movimiento(
                    cuenta=child_text(elem, "cuenta", "Cuenta"),
                    nromov=int(child_text(elem, "nromov", "Nromov")) if child_text(elem, "nromov", "Nromov").isdigit() else 0,
                    fecha=child_text(elem, "fecha", "Fecha"),
                    tipo=child_text(elem, "tipo", "Tipo"),
                    accion=child_text(elem, "accion", "Accion"),
                    importe=float(child_text(elem, "importe", "Importe")) if child_text(elem, "importe", "Importe").replace('.', '').isdigit() else 0.0,
                    referencia=child_text(elem, "referencia", "Referencia") or None,
                ))

    return movimientos
