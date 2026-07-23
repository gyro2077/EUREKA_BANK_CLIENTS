from enum import Enum


class HostType(Enum):
    NUBE = ("Nube (209.145.48.25)", "http://209.145.48.25")
    LOCAL = ("Localhost (127.0.0.1)", "http://127.0.0.1")

    @property
    def label(self) -> str:
        return self.value[0]

    @property
    def base_url(self) -> str:
        return self.value[1]


class ServerType(Enum):
    DOTNET_REST = {
        "label": "REST .NET (Microservicios)",
        "is_soap": False,
        "is_dotnet": True,
        "is_microservice": True,
        "ports": {
            "login": 8093,
            "cuentas": 8097,
            "movimientos": 8098,
            "transferencia": 8099,
            "ping": 8093
        }
    }
    JAVA_SOAP = {
        "label": "SOAP Java (Microservicios)",
        "is_soap": True,
        "is_dotnet": False,
        "is_microservice": True,
        "soap_path": "ROOT/CoreBancarioWS",
        "ports": {
            "login": 8091,
            "cuentas": 8094,
            "movimientos": 8095,
            "transferencia": 8096,
            "ping": 8091
        }
    }
    JAVA_REST = {
        "label": "REST Java (Monolito)",
        "is_soap": False,
        "is_dotnet": False,
        "is_microservice": False,
        "port": 8090
    }
    DOTNET_SOAP = {
        "label": "SOAP .NET (Monolito)",
        "is_soap": True,
        "is_dotnet": True,
        "is_microservice": False,
        "soap_path": "CoreBancarioWS",
        "port": 8092
    }

    @property
    def label(self) -> str:
        return self.value["label"]

    @property
    def is_soap(self) -> bool:
        return self.value["is_soap"]

    @property
    def is_dotnet(self) -> bool:
        return self.value["is_dotnet"]

    def get_url(self, service: str) -> str:
        host_base = EnvironmentManager.current_host.base_url
        clean_host = host_base.rstrip("/")

        if self.value.get("is_microservice"):
            port = self.value["ports"].get(service, 8093)
            if self.is_soap:
                path = self.value.get("soap_path", "ROOT/CoreBancarioWS")
                return f"{clean_host}:{port}/{path}"
            else:
                if service == "login":
                    return f"{clean_host}:{port}/resources/corebancario/login"
                elif service == "deposito":
                    return f"{clean_host}:{port}/resources/corebancario/deposito"
                elif service == "retiro":
                    return f"{clean_host}:{port}/resources/corebancario/retiro"
                elif service == "movimientos":
                    return f"{clean_host}:{port}/resources/corebancario/movimientos"
                elif service == "transferencia":
                    return f"{clean_host}:{port}/resources/corebancario/transferencia"
                elif service == "ping":
                    return f"{clean_host}:{port}/resources/corebancario/ping"
                return f"{clean_host}:{port}/resources/corebancario"
        else:
            port = self.value.get("port", 8090)
            if self.is_soap:
                path = self.value.get("soap_path", "CoreBancarioWS")
                return f"{clean_host}:{port}/{path}"
            else:
                return f"{clean_host}:{port}/resources/corebancario"


class EnvironmentManager:
    current_host: HostType = HostType.NUBE
    current_server: ServerType = ServerType.DOTNET_REST

