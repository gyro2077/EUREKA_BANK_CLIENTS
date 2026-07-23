using System.Security.Cryptography;
using System.Text;
using EurekaBankSOAP.Data;
using EurekaBankSOAP.Models;
using MySqlConnector;

namespace EurekaBankSOAP.Services;

public class CoreBancarioService : ICoreBancarioService
{
    private const string USUARIO = "MONSTER";
    private static readonly string PASSWORD = GenerarHash("MONSTER9");

    public string Login(string usuario, string password)
    {
        string hashIngresado = GenerarHash(password);
        return (USUARIO == usuario && PASSWORD == hashIngresado) ? "Exitoso" : "Denegado";
    }

    public OperacionCuentaResponse RegistrarDeposito(string cuenta, double importe)
    {
        string codEmp = "0001";
        var resp = new OperacionCuentaResponse();
        using var cn = AccesoDB.GetConnection();
        using var tx = cn.BeginTransaction();
        try
        {
            using var cmd = new MySqlCommand(
                "SELECT dec_cuensaldo, int_cuencontmov FROM cuenta " +
                "WHERE chr_cuencodigo = @cuenta AND vch_cuenestado = 'ACTIVO' FOR UPDATE", cn, tx);
            cmd.Parameters.AddWithValue("@cuenta", cuenta);
            using var reader = cmd.ExecuteReader();
            if (!reader.Read())
                throw new Exception("ERROR, cuenta no existe o no esta activa");
            double saldo = reader.GetDouble("dec_cuensaldo");
            int cont = reader.GetInt32("int_cuencontmov");
            reader.Close();

            saldo += importe;
            cont++;

            using var cmdUpd = new MySqlCommand(
                "UPDATE cuenta SET dec_cuensaldo = @saldo, int_cuencontmov = @cont " +
                "WHERE chr_cuencodigo = @cuenta AND vch_cuenestado = 'ACTIVO'", cn, tx);
            cmdUpd.Parameters.AddWithValue("@saldo", saldo);
            cmdUpd.Parameters.AddWithValue("@cont", cont);
            cmdUpd.Parameters.AddWithValue("@cuenta", cuenta);
            cmdUpd.ExecuteNonQuery();

            using var cmdIns = new MySqlCommand(
                "INSERT INTO movimiento(chr_cuencodigo, int_movinumero, dtt_movifecha, " +
                "chr_emplcodigo, chr_tipocodigo, dec_moviimporte) " +
                "VALUES(@cuenta, @cont, SYSDATE(), @emp, '003', @importe)", cn, tx);
            cmdIns.Parameters.AddWithValue("@cuenta", cuenta);
            cmdIns.Parameters.AddWithValue("@cont", cont);
            cmdIns.Parameters.AddWithValue("@emp", codEmp);
            cmdIns.Parameters.AddWithValue("@importe", importe);
            cmdIns.ExecuteNonQuery();

            tx.Commit();
            resp.Estado = 1;
            resp.Saldo = saldo;
        }
        catch
        {
            tx.Rollback();
            resp.Estado = -1;
            resp.Saldo = -1;
            throw;
        }
        return resp;
    }

    public OperacionCuentaResponse RegistrarRetiro(string cuenta, double importe)
    {
        string codEmp = "0004";
        var resp = new OperacionCuentaResponse();
        using var cn = AccesoDB.GetConnection();
        using var tx = cn.BeginTransaction();
        try
        {
            using var cmd = new MySqlCommand(
                "SELECT dec_cuensaldo, int_cuencontmov FROM cuenta " +
                "WHERE chr_cuencodigo = @cuenta AND vch_cuenestado = 'ACTIVO' FOR UPDATE", cn, tx);
            cmd.Parameters.AddWithValue("@cuenta", cuenta);
            using var reader = cmd.ExecuteReader();
            if (!reader.Read())
                throw new Exception("ERROR: La cuenta no existe o no esta activa.");
            double saldo = reader.GetDouble("dec_cuensaldo");
            int cont = reader.GetInt32("int_cuencontmov");
            reader.Close();

            if (saldo < importe)
                throw new Exception("ERROR: Saldo insuficiente.");

            saldo -= importe;
            cont++;

            using var cmdUpd = new MySqlCommand(
                "UPDATE cuenta SET dec_cuensaldo = @saldo, int_cuencontmov = @cont " +
                "WHERE chr_cuencodigo = @cuenta AND vch_cuenestado = 'ACTIVO'", cn, tx);
            cmdUpd.Parameters.AddWithValue("@saldo", saldo);
            cmdUpd.Parameters.AddWithValue("@cont", cont);
            cmdUpd.Parameters.AddWithValue("@cuenta", cuenta);
            cmdUpd.ExecuteNonQuery();

            using var cmdIns = new MySqlCommand(
                "INSERT INTO movimiento(chr_cuencodigo, int_movinumero, dtt_movifecha, " +
                "chr_emplcodigo, chr_tipocodigo, dec_moviimporte) " +
                "VALUES(@cuenta, @cont, SYSDATE(), @emp, '004', @importe)", cn, tx);
            cmdIns.Parameters.AddWithValue("@cuenta", cuenta);
            cmdIns.Parameters.AddWithValue("@cont", cont);
            cmdIns.Parameters.AddWithValue("@emp", codEmp);
            cmdIns.Parameters.AddWithValue("@importe", importe);
            cmdIns.ExecuteNonQuery();

            tx.Commit();
            resp.Estado = 1;
            resp.Saldo = saldo;
        }
        catch
        {
            tx.Rollback();
            resp.Estado = -1;
            resp.Saldo = -1;
            throw;
        }
        return resp;
    }

    public OperacionCuentaResponse RegistrarTransferencia(
        string cuentaOrigen, string cuentaDestino, double importe)
    {
        string codEmp = "0004";
        var resp = new OperacionCuentaResponse();
        using var cn = AccesoDB.GetConnection();
        using var tx = cn.BeginTransaction();
        try
        {
            double saldoOri; int contOri;
            using (var cmd = new MySqlCommand(
                "SELECT dec_cuensaldo, int_cuencontmov FROM cuenta " +
                "WHERE chr_cuencodigo = @c AND vch_cuenestado = 'ACTIVO' FOR UPDATE", cn, tx))
            {
                cmd.Parameters.AddWithValue("@c", cuentaOrigen);
                using var r = cmd.ExecuteReader();
                if (!r.Read()) throw new Exception("ERROR: Cuenta origen no existe o no esta activa.");
                saldoOri = r.GetDouble("dec_cuensaldo");
                contOri = r.GetInt32("int_cuencontmov");
            }
            if (saldoOri < importe)
                throw new Exception("ERROR: Saldo insuficiente en cuenta origen.");

            double saldoDes; int contDes;
            using (var cmd = new MySqlCommand(
                "SELECT dec_cuensaldo, int_cuencontmov FROM cuenta " +
                "WHERE chr_cuencodigo = @c AND vch_cuenestado = 'ACTIVO' FOR UPDATE", cn, tx))
            {
                cmd.Parameters.AddWithValue("@c", cuentaDestino);
                using var r = cmd.ExecuteReader();
                if (!r.Read()) throw new Exception("ERROR: Cuenta destino no existe o no esta activa.");
                saldoDes = r.GetDouble("dec_cuensaldo");
                contDes = r.GetInt32("int_cuencontmov");
            }

            saldoOri -= importe; contOri++;
            saldoDes += importe; contDes++;

            using (var cmd = new MySqlCommand(
                "UPDATE cuenta SET dec_cuensaldo=@s, int_cuencontmov=@c WHERE chr_cuencodigo=@id AND vch_cuenestado='ACTIVO'", cn, tx))
            { cmd.Parameters.AddWithValue("@s", saldoOri); cmd.Parameters.AddWithValue("@c", contOri); cmd.Parameters.AddWithValue("@id", cuentaOrigen); cmd.ExecuteNonQuery(); }

            using (var cmd = new MySqlCommand(
                "UPDATE cuenta SET dec_cuensaldo=@s, int_cuencontmov=@c WHERE chr_cuencodigo=@id AND vch_cuenestado='ACTIVO'", cn, tx))
            { cmd.Parameters.AddWithValue("@s", saldoDes); cmd.Parameters.AddWithValue("@c", contDes); cmd.Parameters.AddWithValue("@id", cuentaDestino); cmd.ExecuteNonQuery(); }

            using (var cmd = new MySqlCommand(
                "INSERT INTO movimiento(chr_cuencodigo,int_movinumero,dtt_movifecha,chr_emplcodigo,chr_tipocodigo,dec_moviimporte) VALUES(@a,@b,SYSDATE(),@c,'009',@d)", cn, tx))
            { cmd.Parameters.AddWithValue("@a", cuentaOrigen); cmd.Parameters.AddWithValue("@b", contOri); cmd.Parameters.AddWithValue("@c", codEmp); cmd.Parameters.AddWithValue("@d", importe); cmd.ExecuteNonQuery(); }

            using (var cmd = new MySqlCommand(
                "INSERT INTO movimiento(chr_cuencodigo,int_movinumero,dtt_movifecha,chr_emplcodigo,chr_tipocodigo,dec_moviimporte) VALUES(@a,@b,SYSDATE(),@c,'008',@d)", cn, tx))
            { cmd.Parameters.AddWithValue("@a", cuentaDestino); cmd.Parameters.AddWithValue("@b", contDes); cmd.Parameters.AddWithValue("@c", codEmp); cmd.Parameters.AddWithValue("@d", importe); cmd.ExecuteNonQuery(); }

            tx.Commit();
            resp.Estado = 1;
            resp.Saldo = saldoOri;
        }
        catch
        {
            tx.Rollback();
            resp.Estado = -1;
            resp.Saldo = -1;
            throw;
        }
        return resp;
    }

    public List<Movimiento> ObtenerMovimientos(string cuenta)
    {
        var lista = new List<Movimiento>();
        using var cn = AccesoDB.GetConnection();
        using var cmd = new MySqlCommand(
            "SELECT m.chr_cuencodigo cuenta, m.int_movinumero nromov, " +
            "m.dtt_movifecha fecha, t.vch_tipodescripcion tipo, " +
            "t.vch_tipoaccion accion, m.dec_moviimporte importe " +
            "FROM tipomovimiento t INNER JOIN movimiento m " +
            "ON t.chr_tipocodigo = m.chr_tipocodigo " +
            "WHERE m.chr_cuencodigo = @cuenta " +
            "ORDER BY m.dtt_movifecha DESC, m.int_movinumero DESC", cn);
        cmd.Parameters.AddWithValue("@cuenta", cuenta);
        using var reader = cmd.ExecuteReader();
        while (reader.Read())
        {
            lista.Add(new Movimiento
            {
                Cuenta = reader.GetString("cuenta"),
                Nromov = reader.GetInt32("nromov"),
                Fecha = reader.GetDateTime("fecha"),
                Tipo = reader.GetString("tipo"),
                Accion = reader.GetString("accion"),
                Importe = reader.GetDouble("importe")
            });
        }
        return lista;
    }

    public string Ping() => "ok";

    private static string GenerarHash(string password)
    {
        byte[] hashBytes = SHA256.HashData(Encoding.UTF8.GetBytes(password));
        return Convert.ToBase64String(hashBytes);
    }
}
