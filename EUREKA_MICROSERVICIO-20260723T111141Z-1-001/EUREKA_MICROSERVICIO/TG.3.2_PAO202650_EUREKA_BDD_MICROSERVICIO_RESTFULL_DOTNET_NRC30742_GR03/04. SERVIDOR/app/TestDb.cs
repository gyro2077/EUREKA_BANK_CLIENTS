using EurekaBankSOAP.Data;
using Microsoft.AspNetCore.Http;
using System;

public static class TestDb
{
    public static string Test()
    {
        try
        {
            using var cn = AccesoDB.GetConnection();
            return "OK";
        }
        catch (Exception ex)
        {
            return "Error: " + ex.Message + "\nInner: " + ex.InnerException?.Message;
        }
    }
}
