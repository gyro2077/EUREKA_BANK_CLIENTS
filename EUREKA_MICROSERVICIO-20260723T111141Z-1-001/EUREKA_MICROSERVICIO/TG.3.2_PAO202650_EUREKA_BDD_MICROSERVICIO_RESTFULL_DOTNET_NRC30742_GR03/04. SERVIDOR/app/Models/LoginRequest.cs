using System.Runtime.Serialization;

namespace EurekaBankSOAP.Models;

[DataContract]
public class LoginRequest
{
    [DataMember] public string Usuario { get; set; } = "";
    [DataMember] public string Password { get; set; } = "";
}
