using System.Runtime.Serialization;

namespace EurekaBankSOAP.Models;

[DataContract]
public class Movimiento
{
    [DataMember] public string Cuenta { get; set; } = "";
    [DataMember] public int Nromov { get; set; }
    [DataMember] public DateTime Fecha { get; set; }
    [DataMember] public string Tipo { get; set; } = "";
    [DataMember] public string Accion { get; set; } = "";
    [DataMember] public double Importe { get; set; }
    [DataMember] public string? Referencia { get; set; }
}
