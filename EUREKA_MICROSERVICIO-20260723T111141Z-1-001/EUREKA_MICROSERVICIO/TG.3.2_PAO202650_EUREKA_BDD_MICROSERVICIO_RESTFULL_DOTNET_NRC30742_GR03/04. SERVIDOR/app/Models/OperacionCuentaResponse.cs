using System.Runtime.Serialization;

namespace EurekaBankSOAP.Models;

[DataContract]
public class OperacionCuentaResponse
{
    [DataMember] public int Estado { get; set; }
    [DataMember] public double Saldo { get; set; }
}
