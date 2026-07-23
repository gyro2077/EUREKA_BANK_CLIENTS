FROM mcr.microsoft.com/dotnet/sdk:8.0 AS build
WORKDIR /src
COPY app/EurekaBankSOAP.csproj .
RUN dotnet restore
COPY app/ .
RUN dotnet publish -c Release -o /app/publish

FROM mcr.microsoft.com/dotnet/aspnet:8.0
WORKDIR /app
COPY --from=build /app/publish .
EXPOSE 8080
ENV ASPNETCORE_URLS=http://+:8080
ENTRYPOINT ["dotnet", "EurekaBankSOAP.dll"]
