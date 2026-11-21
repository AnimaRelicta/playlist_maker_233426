"""Dependency-free development commands; Android is built by Gradle."""
import argparse
import os
from pathlib import Path
import shutil
import subprocess
import sys
import xml.etree.ElementTree as ET

ROOT = Path(__file__).resolve().parents[1]


def check() -> int:
    for path in (ROOT / "app/src/main").rglob("*.xml"):
        ET.parse(path)
    for path in (ROOT / "app/src/main/java/ru/student/playlistmaker/ui").rglob("*.kt"):
        source = path.read_text(encoding="utf-8")
        if "import ru.student.playlistmaker.data" in source:
            raise RuntimeError(f"UI depends on Data: {path}")
    manifest = ET.parse(ROOT / "app/src/main/AndroidManifest.xml").getroot()
    namespace = "{http://schemas.android.com/apk/res/android}"
    names = {item.attrib[namespace + "name"] for item in manifest.findall("application/activity")}
    for name in (".MainActivity",):
        assert name in names, f"Missing Activity declaration: {name}"
    resources = ET.parse(ROOT / "app/src/main/res/values/strings.xml").getroot()
    strings = {item.attrib["name"] for item in resources.findall("string")}
    assert {"app_name", "search", "settings", "playlists", "favorites", "back"} <= strings
    print("XML, Activity declarations, strings and UI dependency checks passed.")
    print("This does not replace Android compilation or the Robolectric tests.")
    return 0


def main() -> int:
    parser = argparse.ArgumentParser()
    parser.add_argument("command", choices=["check", "doctor", "build", "test", "lint"])
    args = parser.parse_args()
    if args.command == "check":
        return check()
    if args.command == "doctor":
        print("JAVA_HOME:", os.environ.get("JAVA_HOME", "not set"))
        print("Java:", shutil.which("java") or "not on PATH")
        print("ANDROID_HOME:", os.environ.get("ANDROID_HOME", "not set"))
        print("local.properties:", (ROOT / "local.properties").exists())
        return 0
    task = {"build": "assembleDebug", "test": "testDebugUnitTest", "lint": "lintDebug"}[args.command]
    wrapper = ROOT / ("gradlew.bat" if sys.platform == "win32" else "gradlew")
    if not wrapper.exists():
        parser.error("Gradle Wrapper is missing")
    return subprocess.call([str(wrapper), task], cwd=ROOT)


if __name__ == "__main__":
    raise SystemExit(main())
