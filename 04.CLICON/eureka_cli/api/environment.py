from enum import Enum


class ServerType(Enum):
    JAVA_REST = {
        "label": "Java REST (8090)",
        "base_url": "http://209.145.48.25:8090",
        "soap_path": "",
        "is_soap": False,
        "is_dotnet": False,
    }
    JAVA_SOAP = {
        "label": "Java SOAP (8091)",
        "base_url": "http://209.145.48.25:8091",
        "soap_path": "ROOT/CoreBancarioWS",
        "is_soap": True,
        "is_dotnet": False,
    }
    DOTNET_SOAP = {
        "label": ".NET SOAP (8092)",
        "base_url": "http://209.145.48.25:8092",
        "soap_path": "CoreBancarioWS",
        "is_soap": True,
        "is_dotnet": True,
    }
    DOTNET_REST = {
        "label": ".NET REST (8093)",
        "base_url": "http://209.145.48.25:8093",
        "soap_path": "",
        "is_soap": False,
        "is_dotnet": True,
    }

    @property
    def label(self) -> str:
        return self.value["label"]

    @property
    def base_url(self) -> str:
        return self.value["base_url"]

    @property
    def soap_path(self) -> str:
        return self.value["soap_path"]

    @property
    def is_soap(self) -> bool:
        return self.value["is_soap"]

    @property
    def is_dotnet(self) -> bool:
        return self.value["is_dotnet"]

    @property
    def endpoint_url(self) -> str:
        if self.is_soap:
            return f"{self.base_url}/{self.soap_path}"
        return self.base_url


class EnvironmentManager:
    current_server: ServerType = ServerType.JAVA_REST
