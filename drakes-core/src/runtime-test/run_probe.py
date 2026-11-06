#!/usr/bin/env python3
"""Run CommandGateProbe on a disposable loopback-only Paper server."""
import argparse
from pathlib import Path
import shutil
import subprocess
import tempfile
import xml.etree.ElementTree as ET
import zipfile

parser = argparse.ArgumentParser(description=__doc__)
parser.add_argument("--paper-home", type=Path, required=True,
                    help="Existing cache/libraries/versions and paper-26.2-133.jar; read only")
args = parser.parse_args()
repo = Path(__file__).resolve().parents[3]
reports = repo / "drakes-core/target/surefire-reports"
report = ET.parse(next(reports.glob("TEST-*.xml"))).getroot()
classpath = next(p.get("value") for p in report.findall("./properties/property")
                 if p.get("name") == "java.class.path")
state = Path.home() / ".local/state/saori"
state.mkdir(parents=True, exist_ok=True)
work = Path(tempfile.mkdtemp(prefix="commandgate-probe-", dir=state))
print(f"Fixture: {work}", flush=True)
classes = work / "classes"
classes.mkdir()
with (work / "compile.log").open("w") as log:
    subprocess.run(["bash", str(Path.home() / "ai-hub/scripts/build_seguro.sh"),
                    "25", str(repo), "javac", "-cp", classpath, "-d", str(classes),
                    str(Path(__file__).with_name("CommandGateProbe.java"))],
                   stdout=log, stderr=subprocess.STDOUT, check=True)
server = work / "server"
plugins = server / "plugins"
plugins.mkdir(parents=True)
for folder in ("libraries", "cache", "versions"):
    shutil.copytree(args.paper_home / folder, server / folder)
shutil.copy2(args.paper_home / "paper-26.2-133.jar", server / "paper.jar")
shutil.copy2(repo / "drakes-core/target/DrakesCore-v2.0.0-26.X-SNAPSHOT.jar", plugins)
with zipfile.ZipFile(plugins / "CommandGateProbe.jar", "w") as jar:
    for source in classes.rglob("*.class"):
        jar.write(source, source.relative_to(classes))
    jar.write(Path(__file__).with_name("plugin.yml"), "plugin.yml")
    jar.writestr("META-INF/MANIFEST.MF",
                 "Manifest-Version: 1.0\npaperweight-mappings-namespace: mojang\n\n")
(server / "eula.txt").write_text("eula=true\n")
(server / "server.properties").write_text(
    "server-ip=127.0.0.1\nserver-port=0\nonline-mode=false\n"
    "level-name=probe\nlevel-type=minecraft:flat\ngenerate-structures=false\n"
    'generator-settings={"layers":[{"block":"minecraft:bedrock","height":1},'
    '{"block":"minecraft:dirt","height":2},{"block":"minecraft:grass_block","height":1}],'
    '"biome":"minecraft:plains"}\n'
    "view-distance=2\nsimulation-distance=2\nmax-players=1\n"
    "enable-query=false\nenable-rcon=false\nspawn-protection=0\n")
with (work / "runtime.log").open("w") as log:
    result = subprocess.run([
        "nice", "-n", "15", "timeout", "--signal=TERM", "--kill-after=30s", "240s",
        "/usr/lib/jvm/java-25-openjdk-amd64/bin/java", "-Xms512m", "-Xmx2g",
        "-XX:ActiveProcessorCount=2", "-Ddrakes.probe.commandgate=true",
        "-jar", "paper.jar", "--nogui"], cwd=server, stdout=log, stderr=subprocess.STDOUT)
output = (work / "runtime.log").read_text(errors="replace")
markers = [line for line in output.splitlines() if "COMMAND_GATE_PROBE" in line]
print("\n".join(markers))
if result.returncode or "COMMAND_GATE_PROBE PASS checks=38" not in output or " ERROR" in output:
    raise SystemExit(f"Probe failed; inspect {work / 'runtime.log'}")
