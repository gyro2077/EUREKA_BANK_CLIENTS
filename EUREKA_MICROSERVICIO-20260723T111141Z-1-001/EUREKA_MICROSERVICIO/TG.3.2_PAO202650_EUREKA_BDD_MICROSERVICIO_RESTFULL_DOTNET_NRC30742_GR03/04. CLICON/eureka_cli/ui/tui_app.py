import sys
from rich.console import Console
from rich.panel import Panel
from rich.table import Table
from rich.prompt import Prompt
from rich.align import Align
from questionary import select, text

from eureka_cli.api.environment import ServerType, HostType, EnvironmentManager
from eureka_cli.api.repository_factory import RepositoryFactory


def select_server():
    choices = [s.label for s in ServerType]
    answer = select("Seleccione el Backend:", choices=choices).ask()
    for server in ServerType:
        if server.label == answer:
            EnvironmentManager.current_server = server
            return server
    return ServerType.DOTNET_REST


def do_login(repo) -> str | None:
    console.print(Panel("Inicio de Sesion", style="bold yellow"))
    usuario = Prompt.ask("Usuario")
    password = Prompt.ask("Password", password=True)
    try:
        result = repo.login(usuario, password)
        console.print(f"[green]Login exitoso como {usuario}[/green]")
        return usuario
    except Exception as e:
        console.print(f"[red]Error: {e}[/red]")
        return None


def do_ping(repo):
    try:
        msg = repo.ping()
        console.print(f"[green]Pong: {msg}[/green]")
    except Exception as e:
        console.print(f"[red]Error: {e}[/red]")


def do_deposito(repo):
    console.print(Panel("Deposito", style="bold yellow"))
    cuenta = Prompt.ask("Numero de cuenta")
    importe_str = Prompt.ask("Importe")
    try:
        importe = float(importe_str)
        result = repo.deposito(cuenta, importe)
        console.print(f"[green]Deposito exitoso. Nuevo saldo: ${result.saldo:.2f}[/green]")
    except ValueError:
        console.print("[red]Importe invalido[/red]")
    except Exception as e:
        console.print(f"[red]Error: {e}[/red]")


def do_retiro(repo):
    console.print(Panel("Retiro", style="bold yellow"))
    cuenta = Prompt.ask("Numero de cuenta")
    importe_str = Prompt.ask("Importe")
    try:
        importe = float(importe_str)
        result = repo.retiro(cuenta, importe)
        console.print(f"[green]Retiro exitoso. Nuevo saldo: ${result.saldo:.2f}[/green]")
    except ValueError:
        console.print("[red]Importe invalido[/red]")
    except Exception as e:
        console.print(f"[red]Error: {e}[/red]")


def do_transferencia(repo):
    console.print(Panel("Transferencia", style="bold yellow"))
    cuenta_origen = Prompt.ask("Cuenta origen")
    cuenta_destino = Prompt.ask("Cuenta destino")
    importe_str = Prompt.ask("Importe")
    try:
        importe = float(importe_str)
        result = repo.transferencia(cuenta_origen, cuenta_destino, importe)
        console.print(f"[green]Transferencia exitosa. Nuevo saldo: ${result.saldo:.2f}[/green]")
    except ValueError:
        console.print("[red]Importe invalido[/red]")
    except Exception as e:
        console.print(f"[red]Error: {e}[/red]")


def do_movimientos(repo):
    console.print(Panel("Movimientos", style="bold yellow"))
    cuenta = Prompt.ask("Numero de cuenta")
    try:
        movs = repo.movimientos(cuenta)
        if not movs:
            console.print("[yellow]Sin movimientos[/yellow]")
            return
        table = Table(title=f"Movimientos - Cuenta {cuenta}")
        table.add_column("#", style="cyan")
        table.add_column("Fecha", style="white")
        table.add_column("Tipo", style="magenta")
        table.add_column("Accion", style="green")
        table.add_column("Importe", style="yellow", justify="right")
        table.add_column("Referencia", style="dim")
        for i, m in enumerate(movs, 1):
            table.add_row(
                str(i),
                m.fecha,
                m.tipo,
                m.accion,
                f"${m.importe:.2f}",
                m.referencia or "-",
            )
        console.print(table)
    except Exception as e:
        console.print(f"[red]Error: {e}[/red]")


def run():
    show_banner()
    EnvironmentManager.current_host = HostType.NUBE
    server = select_server()
    console.print(f"[cyan]Conectando a {EnvironmentManager.current_host.label} -> {server.label}...[/cyan]")

    repo = RepositoryFactory.get_repository()

    do_ping(repo)
    console.print()

    usuario = do_login(repo)
    if not usuario:
        console.print("[red]No se pudo iniciar sesion. Saliendo...[/red]")
        return

    while True:
        console.print()
        console.print(Panel(f"Menu Principal - Usuario: {usuario}", style="bold cyan"))
        choice = select(
            "Operacion:",
            choices=[
                "Ping",
                "Deposito",
                "Retiro",
                "Transferencia",
                "Movimientos",
                "Cambiar Backend",
                "Salir",
            ],
        ).ask()

        if choice == "Ping":
            do_ping(repo)
        elif choice == "Deposito":
            do_deposito(repo)
        elif choice == "Retiro":
            do_retiro(repo)
        elif choice == "Transferencia":
            do_transferencia(repo)
        elif choice == "Movimientos":
            do_movimientos(repo)
        elif choice == "Cambiar Backend":
            server = select_server()
            console.print(f"[cyan]Conectando a {EnvironmentManager.current_host.label} -> {server.label}...[/cyan]")
            repo = RepositoryFactory.get_repository()
            do_ping(repo)
        elif choice == "Salir":
            console.print("[yellow]Saliendo...[/yellow]")
            break
