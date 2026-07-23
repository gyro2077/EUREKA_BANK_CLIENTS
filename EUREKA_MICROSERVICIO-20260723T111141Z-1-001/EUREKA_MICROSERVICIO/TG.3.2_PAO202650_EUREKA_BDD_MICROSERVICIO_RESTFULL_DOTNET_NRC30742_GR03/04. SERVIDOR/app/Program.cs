using SoapCore;
using EurekaBankSOAP.Services;

var builder = WebApplication.CreateBuilder(args);

builder.Services.AddScoped<ICoreBancarioService, CoreBancarioService>();
builder.Services.AddSoapCore();

var app = builder.Build();

app.UseRouting();

app.UseEndpoints(endpoints =>
{
    endpoints.UseSoapEndpoint<ICoreBancarioService>(
        "/CoreBancarioWS",
        new SoapEncoderOptions(),
        SoapSerializer.XmlSerializer
    );
});

app.MapGet("/", () => "EurekaBank SOAP .NET Service - Activo");
app.MapGet("/testdb", () => TestDb.Test());

app.Run();
