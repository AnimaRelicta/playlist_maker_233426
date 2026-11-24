"""Compare real Compose renders with supplied Figma pixels, excluding accepted font differences."""
import argparse
import json
from pathlib import Path
from PIL import Image, ImageChops, ImageDraw, ImageStat

ROOT = Path(__file__).resolve().parents[1]
REPORT = ROOT / "app/build/reports/screenshots"


def compare(dark: bool) -> dict:
    name = "main-dark" if dark else "main-light"
    reference = ROOT / "figma/reference" / ("main-dark.png" if dark else "main-light@2x.png")
    expected = Image.open(reference).convert("RGB")
    actual = Image.open(REPORT / f"{name}.png").convert("RGB")
    # Compare at the reference scale without upscaling a rasterized icon.
    assert actual.width * 800 == actual.height * 360, f"Unexpected frame dimensions: {actual.size}"
    if actual.size != expected.size:
        actual = actual.resize(expected.size, Image.Resampling.BOX)
    scale = actual.width / 360

    mask = Image.new("L", actual.size, 255)
    draw = ImageDraw.Draw(mask)
    def box(x1, y1, x2, y2):
        return tuple(round(value * scale) for value in (x1, y1, x2, y2))
    # System font was explicitly accepted. Geometry, icons, curves and colors remain checked.
    draw.rectangle(box(12, 10, 350, 50), fill=0)
    for center in (111, 177, 243, 309):
        draw.rectangle(box(58, center - 22, 302, center + 22), fill=0)
    difference = ImageChops.difference(expected, actual)
    stats = ImageStat.Stat(difference, mask)
    mean_error = sum(stats.mean) / 3
    # Per-slot checks prevent a missing or wrong icon hiding in a large white background.
    slots = {}
    for name_part, center in zip(("search", "playlists", "favorites", "settings"), (111, 177, 243, 309)):
        for side, horizontal in (("icon", (28, 52)), ("chevron", (308, 332))):
            region = box(horizontal[0], center - 12, horizontal[1], center + 12)
            slot_error = sum(ImageStat.Stat(difference.crop(region)).mean) / 3
            slots[f"{name_part}_{side}"] = round(slot_error, 4)
    overlay = Image.blend(expected, actual, 0.5)
    overlay.save(REPORT / f"{name}-overlay.png")
    difference.point(lambda value: min(value * 4, 255)).save(REPORT / f"{name}-diff.png")
    result = {"screen": name, "size": actual.size, "masked_mean_rgb_error": round(mean_error, 4),
              "slot_mean_rgb_errors": slots}
    # Modest per-pixel tolerance accommodates Figma/Skia antialiasing, not layout differences.
    assert mean_error < 1.0, result
    assert max(slots.values()) < 15.0, result
    return result


def compare_sprint4() -> list[dict]:
    results = []
    for screen, source, x in (
        ("search", "search-default.png", 100),
        ("settings", "main-and-settings.png", 560),
    ):
        ref = Image.open(ROOT / "figma/reference" / source).convert("RGB")
        for theme, y in (("light", 240), ("dark", 1280)):
            name = f"{screen}-{theme}"
            expected = ref.crop((x, y, x + 360, y + 800))
            actual = Image.open(REPORT / f"{name}.png").convert("RGB")
            if actual.size not in ((360, 800), (720, 1600)):
                raise ValueError(f"Unexpected frame size for {name}: {actual.size}")
            actual = actual.resize(expected.size, Image.Resampling.BOX)
            mask = Image.new("L", expected.size, 255)
            draw = ImageDraw.Draw(mask)
            draw.rectangle((62, 12, 340, 44), fill=0)
            if screen == "search":
                draw.rectangle((50, 68, 336, 96), fill=0)
            else:
                for center in (110, 171, 232, 293):
                    draw.rectangle((14, center - 14, 290, center + 14), fill=0)
            delta = ImageChops.difference(expected, actual)
            error = sum(ImageStat.Stat(delta, mask).mean) / 3
            Image.blend(expected, actual, 0.5).save(REPORT / f"{name}-overlay.png")
            delta.point(lambda value: min(value * 4, 255)).save(REPORT / f"{name}-diff.png")
            results.append({"screen": name, "masked_mean_rgb_error": round(error, 4)})
    return results


def compare_sprint5() -> list[dict]:
    frames = (
        ("search-results", "search-result.png", 240, 798),
        ("search-empty", "search-empty.png", 1280, 800),
        ("search-failure", "search-network-error.png", 244, 800),
        ("track-details", "track-details.png", 1312, 832),
    )
    results = []
    for name, source, top, height in frames:
        with Image.open(ROOT / "figma/reference" / source) as sheet:
            expected = sheet.crop((100, top, 460, top + height)).convert("RGB")
        with Image.open(REPORT / f"{name}.png") as screenshot:
            actual = screenshot.convert("RGB")
        if actual.size != (720, 1600):
            raise ValueError(f"Unexpected frame size for {name}: {actual.size}")
        actual = actual.resize((360, 800), Image.Resampling.BOX)
        expected = expected.crop((0, 0, 360, min(height, 800)))
        actual = actual.crop((0, 0, 360, expected.height))
        delta = ImageChops.difference(expected, actual)
        Image.blend(expected, actual, 0.5).save(REPORT / f"{name}-overlay.png")
        delta.save(REPORT / f"{name}-diff.png")
        results.append({"screen": name, "mean_rgb_error": round(sum(ImageStat.Stat(delta).mean) / 3, 4)})
    return results


def main() -> None:
    parser = argparse.ArgumentParser()
    parser.add_argument("--sprint", type=int, choices=(3, 4, 5), default=3)
    sprint = parser.parse_args().sprint
    if sprint == 5:
        results = compare_sprint5()
    elif sprint == 4:
        results = compare_sprint4()
    else:
        results = [compare(False), compare(True)]
    (REPORT / "figma-comparison.json").write_text(json.dumps(results, indent=2) + "\n")
    print(json.dumps(results, indent=2))


if __name__ == "__main__":
    main()
