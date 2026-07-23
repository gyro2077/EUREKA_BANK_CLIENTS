from abc import ABC, abstractmethod
import requests
from eureka_cli.api.models import LoginResponse, OperacionCuentaResponse, Movimiento
from eureka_cli.api.environment import EnvironmentManager
from eureka_cli.api.soap_builder import (
    build_login, build_deposito, build_retiro, build_transferencia,
    build_movimientos, build_ping,
    parse_login_response, parse_operacion_response, parse_movimientos_response, parse_ping_response
)


class IEurekaBankRepository(ABC):
    @abstractmethod
    def ping(self) -> str:
        pass

    @abstractmethod
    def login(self, usuario: str, password: str) -> LoginResponse:
        pass

    @abstractmethod
    def deposito(self, cuenta: str, importe: float) -> OperacionCuentaResponse:
        pass

    @abstractmethod
    def retiro(self, cuenta: str, importe: float) -> OperacionCuentaResponse:
        pass

    @abstractmethod
    def transferencia(self, cuenta_origen: str, cuenta_destino: str, importe: float) -> OperacionCuentaResponse:
        pass

    @abstractmethod
    def movimientos(self, cuenta: str) -> list[Movimiento]:
        pass


class RestRepository(IEurekaBankRepository):
    def ping(self) -> str:
        url = EnvironmentManager.current_server.get_url("ping")
        resp = requests.get(url, timeout=10)
        if resp.status_code == 200:
            return resp.json().get("msg", "Sin respuesta")
        raise Exception(f"Error del servidor: {resp.status_code}")

    def login(self, usuario: str, password: str) -> LoginResponse:
        url = EnvironmentManager.current_server.get_url("login")
        resp = requests.post(
            url,
            json={"usuario": usuario, "password": password},
            timeout=10
        )
        if resp.status_code == 200:
            body = resp.json()
            resultado = body.get("resultado", "Error")
            if resultado.lower() == "exitoso":
                return LoginResponse("Exitoso")
            raise Exception(f"Login fallido: {resultado}")
        raise Exception(resp.text or "Error de autenticacion")

    def deposito(self, cuenta: str, importe: float) -> OperacionCuentaResponse:
        url = EnvironmentManager.current_server.get_url("deposito")
        resp = requests.post(
            url,
            json={"cuenta": cuenta, "importe": importe},
            timeout=10
        )
        if resp.status_code == 200:
            body = resp.json()
            codigo = body.get("estado", body.get("codigo", 0))
            saldo = body.get("saldo", 0.0)
            if codigo == -1:
                raise Exception("Cuenta no encontrada o error en deposito")
            return OperacionCuentaResponse(codigo=codigo, mensaje="Operacion exitosa", saldo=saldo)
        raise Exception(resp.text or "Error en deposito")

    def retiro(self, cuenta: str, importe: float) -> OperacionCuentaResponse:
        url = EnvironmentManager.current_server.get_url("retiro")
        resp = requests.post(
            url,
            json={"cuenta": cuenta, "importe": importe},
            timeout=10
        )
        if resp.status_code == 200:
            body = resp.json()
            codigo = body.get("estado", body.get("codigo", 0))
            saldo = body.get("saldo", 0.0)
            if codigo == -1:
                raise Exception("Cuenta no encontrada o fondos insuficientes")
            return OperacionCuentaResponse(codigo=codigo, mensaje="Operacion exitosa", saldo=saldo)
        raise Exception(resp.text or "Error en retiro")

    def transferencia(self, cuenta_origen: str, cuenta_destino: str, importe: float) -> OperacionCuentaResponse:
        url = EnvironmentManager.current_server.get_url("transferencia")
        resp = requests.post(
            url,
            json={"cuentaOrigen": cuenta_origen, "cuentaDestino": cuenta_destino, "importe": importe},
            timeout=10
        )
        if resp.status_code == 200:
            body = resp.json()
            codigo = body.get("estado", body.get("codigo", 0))
            saldo = body.get("saldo", 0.0)
            if codigo == -1:
                raise Exception("Error en transferencia")
            return OperacionCuentaResponse(codigo=codigo, mensaje="Operacion exitosa", saldo=saldo)
        raise Exception(resp.text or "Error en transferencia")

    def movimientos(self, cuenta: str) -> list[Movimiento]:
        base_mov = EnvironmentManager.current_server.get_url("movimientos")
        url = f"{base_mov}/{cuenta}" if not base_mov.endswith(f"/{cuenta}") else base_mov
        resp = requests.get(url, timeout=10)
        if resp.status_code == 200:
            data = resp.json()
            if isinstance(data, list):
                return [
                    Movimiento(
                        cuenta=m.get("cuenta", ""),
                        nromov=m.get("nromov", 0),
                        fecha=m.get("fecha", ""),
                        tipo=m.get("tipo", ""),
                        accion=m.get("accion", ""),
                        importe=m.get("importe", 0.0),
                        referencia=m.get("referencia") or None,
                    )
                    for m in data
                ]
            return []
        raise Exception(resp.text or "Error al obtener movimientos")


class SoapRepository(IEurekaBankRepository):
    def __init__(self):
        self.headers = {
            "Content-Type": "text/xml; charset=utf-8",
            "SOAPAction": "",
        }

    @property
    def is_dotnet(self) -> bool:
        return EnvironmentManager.current_server.is_dotnet

    def _send_soap(self, target_url: str, envelope: str) -> str:
        resp = requests.post(target_url, data=envelope.encode("utf-8"), headers=self.headers, timeout=10)
        if resp.status_code == 200 and resp.text:
            return resp.text
        raise Exception(f"Error del servidor ({target_url}): {resp.status_code}")

    def ping(self) -> str:
        url = EnvironmentManager.current_server.get_url("ping")
        envelope = build_ping(self.is_dotnet)
        xml = self._send_soap(url, envelope)
        return parse_ping_response(xml)

    def login(self, usuario: str, password: str) -> LoginResponse:
        url = EnvironmentManager.current_server.get_url("login")
        envelope = build_login(usuario, password, self.is_dotnet)
        xml = self._send_soap(url, envelope)
        body = parse_login_response(xml)
        if body.resultado.lower() != "exitoso":
            raise Exception(f"Login fallido: {body.resultado}")
        return body

    def deposito(self, cuenta: str, importe: float) -> OperacionCuentaResponse:
        url = EnvironmentManager.current_server.get_url("cuentas")
        envelope = build_deposito(cuenta, importe, self.is_dotnet)
        xml = self._send_soap(url, envelope)
        body = parse_operacion_response(xml)
        if body.codigo == -1:
            raise Exception(body.mensaje)
        return body

    def retiro(self, cuenta: str, importe: float) -> OperacionCuentaResponse:
        url = EnvironmentManager.current_server.get_url("cuentas")
        envelope = build_retiro(cuenta, importe, self.is_dotnet)
        xml = self._send_soap(url, envelope)
        body = parse_operacion_response(xml)
        if body.codigo == -1:
            raise Exception(body.mensaje)
        return body

    def transferencia(self, cuenta_origen: str, cuenta_destino: str, importe: float) -> OperacionCuentaResponse:
        url = EnvironmentManager.current_server.get_url("transferencia")
        envelope = build_transferencia(cuenta_origen, cuenta_destino, importe, self.is_dotnet)
        xml = self._send_soap(url, envelope)
        body = parse_operacion_response(xml)
        if body.codigo == -1:
            raise Exception(body.mensaje)
        return body

    def movimientos(self, cuenta: str) -> list[Movimiento]:
        url = EnvironmentManager.current_server.get_url("movimientos")
        envelope = build_movimientos(cuenta, self.is_dotnet)
        xml = self._send_soap(url, envelope)
        return parse_movimientos_response(xml)
