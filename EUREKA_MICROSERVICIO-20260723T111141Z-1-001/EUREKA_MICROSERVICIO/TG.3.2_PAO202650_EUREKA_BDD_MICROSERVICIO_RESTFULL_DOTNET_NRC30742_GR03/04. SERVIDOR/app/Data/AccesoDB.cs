using MySqlConnector;

namespace EurekaBankSOAP.Data;

public static class AccesoDB
{
    private static string GetConnectionString()
    {
        return Environment.GetEnvironmentVariable("ConnectionStrings__MySQL")
            ?? "Server=db_dotnet_soap;Port=3306;Database=eurekabank;User=eureka;Password=admin;SslMode=none;";
    }

    public static MySqlConnection GetConnection()
    {
        var cn = new MySqlConnection(GetConnectionString());
        cn.Open();
        return cn;
    }
}
