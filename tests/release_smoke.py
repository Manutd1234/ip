"""Verify one copied release JAR without using the source tree or the user's saved tasks."""

import os
from pathlib import Path
import shutil
import subprocess
import sys
import tempfile
from zipfile import ZipFile


def run(command, directory, environment, input_text=None):
    try:
        result = subprocess.run(command, cwd=directory, env=environment, input=input_text,
                                text=True, capture_output=True, timeout=60)
    except subprocess.TimeoutExpired as error:
        print(error.stdout or "", file=sys.stderr)
        print(error.stderr or "", file=sys.stderr)
        raise RuntimeError("Release check timed out; ensure a graphical desktop is available") from error
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
        assert "duke/gui/QuestIcon.class" in names
        assert "duke/gui/MessageIcon.class" in names
        media_extensions = (".png", ".jpg", ".jpeg", ".gif", ".svg", ".webp", ".ico",
                            ".mp3", ".wav", ".ogg", ".m4a", ".aac", ".flac", ".mp4")
        assert not any(name.startswith("duke/") and name.lower().endswith(media_extensions) for name in names)
        assert "META-INF/wangsa/CREDITS.md" in names
        assert "duke/Storage.class" in names
        assert not any(name.startswith("org/sqlite/") for name in names)
        assert "duke/SqliteTaskRepository.class" not in names
        assert "duke/gui/CharacterAvatar.class" not in names
        assert not any("/" not in name and name.endswith((".dll", ".so", ".dylib")) for name in names)
    print("PASS: four JavaFX platforms and code-drawn symbols; no app image/audio assets or SQLite dependency")


def main():
    repository = Path(__file__).resolve().parents[1]
    source_jar = Path(sys.argv[1]).resolve() if len(sys.argv) > 1 else repository / "build/libs/Wangsa.jar"
    verify_contents(source_jar)
    source_zip = Path(sys.argv[2]).resolve() if len(sys.argv) > 2 else repository / "build/distributions/Wangsa.zip"
    if source_zip.exists():
        with ZipFile(source_zip) as archive:
            assert archive.read("Wangsa/Wangsa.jar") == source_jar.read_bytes()
            assert "Java 25" in archive.read("Wangsa/QUICK-START.txt").decode()
            assert b"%~dp0Wangsa.jar" in archive.read("Wangsa/Start-Wangsa.bat")
            assert (archive.getinfo("Wangsa/Start-Wangsa.command").external_attr >> 16) & 0o111
            assert not any("/data/" in name for name in archive.namelist())
        print("PASS: easy-start ZIP matches the JAR and contains no personal data")
    environment = dict(os.environ)
    environment.pop("LLM_API_KEY", None)
    environment.pop("LLM_MODEL", None)
    with tempfile.TemporaryDirectory(prefix="wangsa-release-") as temporary:
        folder = Path(temporary)
        app_folder = folder / "My Wangsa app"
        app_folder.mkdir()
        jar = app_folder / "Wangsa.jar"
        shutil.copy2(source_jar, jar)
        old_database = app_folder / "data/wangsa.db"
        old_database.parent.mkdir()
        old_database.write_bytes(b"old database must not block the text-file app")
        classes = folder / "smoke-classes"
        classes.mkdir()
        run(["javac", "-cp", str(jar), "-d", str(classes),
             str(repository / "src/test/java/duke/gui/ReleaseSmoke.java")], folder, environment)
        command = ["java", "--enable-native-access=ALL-UNNAMED", "-ea", "-cp",
                   os.pathsep.join((str(classes), str(jar))), "duke.gui.ReleaseSmoke"]
        run(command + ["normal"], folder, environment)
        assert (app_folder / "data/wangsa.txt").is_file()
        assert not (folder / "data").exists()
        assert old_database.read_bytes() == b"old database must not block the text-file app"
        run(command + ["reload"], folder, environment)
        output = run(["java", "--enable-native-access=ALL-UNNAMED", "-cp", str(jar), "duke.Wangsa"],
                     folder, environment, "list\n@ai How do I add a task?\nbye\n")
        assert "[D][X] submit report" in output and "[E][ ] team meeting" in output
        assert "Offline help:" in output and "todo DESCRIPTION" in output
        print("PASS: GUI/CLI share text saves beside the JAR, even from a different launch folder")
        print("PASS: old databases remain untouched and optional AI falls back offline")
        blocked = folder / "blocked"
        (blocked / "data").mkdir(parents=True)
        save_file = blocked / "data/wangsa.txt"
        invalid_data = b"T | 2 | deliberately invalid task for release verification\n"
        save_file.write_bytes(invalid_data)
        blocked_jar = blocked / "Wangsa.jar"
        shutil.copy2(source_jar, blocked_jar)
        blocked_command = ["java", "--enable-native-access=ALL-UNNAMED", "-ea", "-cp",
                           os.pathsep.join((str(classes), str(blocked_jar))), "duke.gui.ReleaseSmoke"]
        run(blocked_command + ["blocked"], folder, environment)
        assert save_file.read_bytes() == invalid_data
        print("PASS: startup errors preserve the original data")


if __name__ == "__main__":
    main()
