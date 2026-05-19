from dataclasses import dataclass
from typing import Optional


@dataclass
class LoginResponse:
    resultado: str


@dataclass
class OperacionCuentaResponse:
    codigo: int
    mensaje: str = ""
    saldo: float = 0.0


@dataclass
class Movimiento:
    cuenta: str
    nromov: int
    fecha: str
    tipo: str
    accion: str
    importe: float
    referencia: Optional[str] = None
