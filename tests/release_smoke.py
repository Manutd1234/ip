"""Verify one copied release JAR without using the source tree or the user's saved tasks."""

import os
from pathlib import Path
import shutil
import subprocess
import sys
import tempfile
from zipfile import ZipFile


def run(command, directory, environment, input_text=None):
    result = subprocess.run(command, cwd=directory, env=environment, input=input_text,
                            text=True, capture_output=True, timeout=60)
    print(result.stdout, end="")
    if result.returncode:
        print(result.stderr, file=sys.stderr)
        raise RuntimeError(f"Release check failed: {command[0]}")
    return result.stdout


def verify_contents(jar):
    with ZipFile(jar) as archive:
        names = set(archive.namelist())
        for platform in ("win", "linux", "mac", "mac-aarch64"):
            folder = f"javafx-natives/{platform}/"
            libraries = archive.read(folder + "libraries.list").decode().splitlines()
            assert libraries, f"Missing libraries for {platform}"
            assert all(folder + library in names for library in libraries)
        assert "duke/gui/main.css" in names
        assert "duke/gui/assets/ash.jpeg" in names
        assert "duke/gui/assets/charizard.jpg" in names
        assert not any("/" not in name and name.endswith((".dll", ".so", ".dylib")) for name in names)
    print("PASS: all four JavaFX platforms and GUI assets are packaged")


def main():
    repository = Path(__file__).resolve().parents[1]
    source_jar = Path(sys.argv[1]).resolve() if len(sys.argv) > 1 else repository / "build/libs/Wangsa.jar"
    verify_contents(source_jar)
    environment = dict(os.environ)
    environment.pop("LLM_API_KEY", None)
    environment.pop("LLM_MODEL", None)
    with tempfile.TemporaryDirectory(prefix="wangsa-release-") as temporary:
        folder = Path(temporary)
        jar = folder / "Wangsa.jar"
        shutil.copy2(source_jar, jar)
        classes = folder / "smoke-classes"
        classes.mkdir()
        run(["javac", "-cp", str(jar), "-d", str(classes),
             str(repository / "src/test/java/duke/gui/ReleaseSmoke.java")], folder, environment)
        command = ["java", "--enable-native-access=ALL-UNNAMED", "-ea", "-cp",
                   os.pathsep.join((str(classes), str(jar))), "duke.gui.ReleaseSmoke"]
        run(command + ["normal"], folder, environment)
        run(command + ["reload"], folder, environment)
        output = run(["java", "--enable-native-access=ALL-UNNAMED", "-cp", str(jar), "duke.Wangsa"],
                     folder, environment, "list\n@ai How do I add a task?\nbye\n")
        assert "[D][X] submit report" in output and "[E][ ] team meeting" in output
        assert "Offline help:" in output and "todo DESCRIPTION" in output
        print("PASS: CLI shares saved tasks and optional AI falls back offline")
        blocked = folder / "blocked"
        (blocked / "data").mkdir(parents=True)
        database = blocked / "data/wangsa.db"
        invalid_data = b"deliberately invalid database for release verification"
        database.write_bytes(invalid_data)
        run(command + ["blocked"], blocked, environment)
        assert database.read_bytes() == invalid_data
        print("PASS: startup errors preserve the original data")


if __name__ == "__main__":
    main()
