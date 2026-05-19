from eureka_cli.api.environment import EnvironmentManager
from eureka_cli.api.repository import IEurekaBankRepository, RestRepository, SoapRepository


class RepositoryFactory:
    _instance: IEurekaBankRepository | None = None
    _last_server = None

    @classmethod
    def get_repository(cls) -> IEurekaBankRepository:
        current = EnvironmentManager.current_server
        if cls._instance is None or cls._last_server != current:
            if current.is_soap:
                cls._instance = SoapRepository()
            else:
                cls._instance = RestRepository()
            cls._last_server = current
        return cls._instance
