using System.ServiceModel;
using EurekaBankSOAP.Models;

namespace EurekaBankSOAP.Services;

[ServiceContract(Namespace = "http://ws.monster.edu.ec/",
                 Name = "CoreBancarioWS")]
public interface ICoreBancarioService
{
    [OperationContract] string Login(string usuario, string password);
    [OperationContract] OperacionCuentaResponse RegistrarDeposito(string cuenta, double importe);
    [OperationContract] OperacionCuentaResponse RegistrarRetiro(string cuenta, double importe);
    [OperationContract] OperacionCuentaResponse RegistrarTransferencia(
        string cuentaOrigen, string cuentaDestino, double importe);
    [OperationContract] List<Movimiento> ObtenerMovimientos(string cuenta);
    [OperationContract] string Ping();
}
