using System.Runtime.Serialization;

namespace EurekaBankSOAP.Models;

[DataContract]
public class TransferenciaRequest
{
    [DataMember] public string CuentaOrigen { get; set; } = "";
    [DataMember] public string CuentaDestino { get; set; } = "";
    [DataMember] public double Importe { get; set; }
}
