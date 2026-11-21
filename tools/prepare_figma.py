"""Curate supplied Figma exports; preserve SVG path geometry in Android vectors."""
from pathlib import Path
import hashlib
import json
import shutil
import struct
import xml.etree.ElementTree as ET
from PIL import Image

ROOT = Path(__file__).resolve().parents[1]
EXPORT = ROOT / "figma/Playlist-Maker"
DRAWABLE = ROOT / "app/src/main/res/drawable"
ANDROID = "http://schemas.android.com/apk/res/android"
SVG = "{http://www.w3.org/2000/svg}"
ET.register_namespace("android", ANDROID)


def vector(source: str, name: str) -> None:
    svg = ET.parse(EXPORT / source).getroot()
    _, _, width, height = svg.attrib["viewBox"].split()
    root = ET.Element("vector", {
        f"{{{ANDROID}}}width": width + "dp",
        f"{{{ANDROID}}}height": height + "dp",
        f"{{{ANDROID}}}viewportWidth": width,
        f"{{{ANDROID}}}viewportHeight": height,
    })
    for path in svg.iter(SVG + "path"):
        attrs = {
            f"{{{ANDROID}}}pathData": path.attrib["d"],
            f"{{{ANDROID}}}fillColor": path.attrib.get("fill", "#1A1B22").replace("white", "#FFFFFF"),
        }
        if path.attrib.get("fill-rule") == "evenodd":
            attrs[f"{{{ANDROID}}}fillType"] = "evenOdd"
        ET.SubElement(root, "path", attrs)
    ET.indent(root)
    ET.ElementTree(root).write(DRAWABLE / f"{name}.xml", encoding="utf-8", xml_declaration=True)


def main() -> None:
    DRAWABLE.mkdir(parents=True, exist_ok=True)
    for source, name in [
        ("ic/24x24/Search/Inactive/Light Mode.svg", "ic_search"),
        ("ic/24x24/Library/Inactive/Light Mode.svg", "ic_playlists"),
        ("ic/24x24/Settings/Inactive/Light Mode.svg", "ic_settings"),
        ("Vector.svg", "ic_favorite"),
        ("Button/Search/Light Mode.svg", "ic_back"),
    ]:
        vector(source, name)
    references = ROOT / "figma/reference"
    references.mkdir(parents=True, exist_ok=True)
    shutil.copyfile(EXPORT / "1. Главная.png", references / "main-light@2x.png")
    overview = Image.open(EXPORT / "Main screen.png")
    overview.crop((100, 1280, 460, 2080)).save(references / "main-dark.png")
    shutil.copyfile(EXPORT / "Main screen.png", references / "main-and-settings.png")
    Image.open(EXPORT / "ic/store/512x512.png").save(DRAWABLE / "ic_app.png")
    icons = ROOT / "figma/icons"
    icons.mkdir(exist_ok=True)
    for source, name in [
        ("ic/24x24/Search/Inactive/Light Mode.svg", "search.svg"),
        ("ic/24x24/Library/Inactive/Light Mode.svg", "playlists.svg"),
        ("ic/24x24/Settings/Inactive/Light Mode.svg", "settings.svg"),
        ("Vector.svg", "favorite.svg"),
        ("Button/Search/Light Mode.svg", "back-button.svg"),
    ]:
        shutil.copyfile(EXPORT / source, icons / name)
    for source, name in [
        ("Settings.png", "settings.png"),
        ("Search default.png", "search-default.png"),
        ("Search result.png", "search-result.png"),
        ("Search error.png", "search-empty.png"),
        ("Search no results.png", "search-network-error.png"),
        ("Track Details.png", "track-details.png"),
    ]:
        shutil.copyfile(EXPORT / source, references / name)
    manifest = []
    for path in sorted(EXPORT.rglob("*")):
        if path.is_file():
            record = {"path": path.relative_to(EXPORT).as_posix(), "bytes": path.stat().st_size,
                      "sha256": hashlib.sha256(path.read_bytes()).hexdigest()}
            if path.suffix.lower() == ".png":
                # Read dimensions without decoding the huge overview image.
                with path.open("rb") as png:
                    header = png.read(24)
                record["size"] = list(struct.unpack(">II", header[16:24]))
            manifest.append(record)
    (ROOT / "figma/export-manifest.json").write_text(
        json.dumps(manifest, ensure_ascii=False, indent=2) + "\n", encoding="utf-8")


if __name__ == "__main__":
    main()
