import io
import subprocess
import tempfile
from pathlib import Path

import requests
from gtts import gTTS
from PIL import Image, ImageDraw, ImageFont

WIDTH, HEIGHT = 1080, 1920
FPS = 30
OUT_PATH = Path("output/final_marketing_video.mp4")
FONT_PATH = "/usr/share/fonts/truetype/noto/NotoSansHebrew-Regular.ttf"
MUSIC_URLS = [
    "https://cdn.pixabay.com/download/audio/2022/01/18/audio_d0c6ff1bab.mp3",
    "https://cdn.pixabay.com/download/audio/2021/11/25/audio_5baf6b9d0e.mp3",
    "https://www.soundhelix.com/examples/mp3/SoundHelix-Song-1.mp3",
]

SCENES = [
    {
        "image": "https://images.unsplash.com/photo-1611532736597-de2d4265fba3?w=1080&h=1920&fit=crop",
        "title": "למה הלקוחות שלכם הולכים למתחרים?",
        "subtitle": "",
        "duration": 2,
    },
    {
        "image": "https://images.unsplash.com/photo-1556742049-0cfed4f6a45d?w=1080&h=1920&fit=crop",
        "title": "המערכת של טל הטיל מביאה לכם לקוחות חמים",
        "subtitle": "בזמן שאתם רודפים אחרי לידים",
        "duration": 2,
    },
    {
        "image": "https://images.unsplash.com/photo-1460925895917-afdab827c52f?w=1080&h=1920&fit=crop",
        "title": "פגישות נכנסות אליכם באופן רציף",
        "subtitle": "בלי לבזבז זמן על קהל לא רלוונטי",
        "duration": 2,
    },
    {
        "image": "https://images.unsplash.com/photo-1520607162513-77705c0f0d4a?w=1080&h=1920&fit=crop",
        "title": "כל קמפיין נראה כמו מותג פרימיום",
        "subtitle": "עם מסר חד שמניע לפעולה",
        "duration": 2,
    },
    {
        "image": "https://images.unsplash.com/photo-1556740738-b6a63e27c4df?w=1080&h=1920&fit=crop",
        "title": "תלחצו על הקישור עכשיו",
        "subtitle": "ותראו איך זה עובד",
        "duration": 3,
    },
]


def run_cmd(cmd: list[str]) -> None:
    proc = subprocess.run(cmd, capture_output=True, text=True)
    if proc.returncode != 0:
        raise RuntimeError(f"Failed: {' '.join(cmd)}\n{proc.stderr}")


def draw_scene(image_bytes: bytes, title: str, subtitle: str, out_path: Path) -> None:
    base = Image.open(io.BytesIO(image_bytes)).convert("RGBA").resize((WIDTH, HEIGHT))
    overlay = Image.new("RGBA", (WIDTH, HEIGHT), (0, 0, 0, 128))
    img = Image.alpha_composite(base, overlay)

    draw = ImageDraw.Draw(img)
    title_font = ImageFont.truetype(FONT_PATH, 72)
    subtitle_font = ImageFont.truetype(FONT_PATH, 48)

    title_bbox = draw.textbbox((0, 0), title, font=title_font)
    title_w = title_bbox[2] - title_bbox[0]
    title_h = title_bbox[3] - title_bbox[1]

    subtitle_h = 0
    subtitle_w = 0
    if subtitle:
        sub_bbox = draw.textbbox((0, 0), subtitle, font=subtitle_font)
        subtitle_w = sub_bbox[2] - sub_bbox[0]
        subtitle_h = sub_bbox[3] - sub_bbox[1]

    gap = 28 if subtitle else 0
    block_h = title_h + subtitle_h + gap
    start_y = (HEIGHT - block_h) // 2

    tx = (WIDTH - title_w) // 2
    ty = start_y
    draw.text((tx + 3, ty + 3), title, font=title_font, fill=(0, 0, 0, 255))
    draw.text((tx, ty), title, font=title_font, fill=(255, 255, 255, 255))

    if subtitle:
        sx = (WIDTH - subtitle_w) // 2
        sy = ty + title_h + gap
        draw.text((sx + 3, sy + 3), subtitle, font=subtitle_font, fill=(0, 0, 0, 255))
        draw.text((sx, sy), subtitle, font=subtitle_font, fill=(255, 255, 255, 255))

    img.convert("RGB").save(out_path, "JPEG", quality=95)


def download_music_with_fallback(target: Path) -> None:
    for url in MUSIC_URLS:
        try:
            response = requests.get(url, stream=True, timeout=60)
            response.raise_for_status()
            chunks = []
            total_size = 0
            for chunk in response.iter_content(chunk_size=16384):
                if not chunk:
                    continue
                chunks.append(chunk)
                total_size += len(chunk)
            if total_size > 50 * 1024:
                target.write_bytes(b"".join(chunks))
                return
        except Exception:
            continue
    raise RuntimeError("Could not download valid background music from fallback URLs")


def create_marketing_video() -> Path:
    OUT_PATH.parent.mkdir(parents=True, exist_ok=True)

    with tempfile.TemporaryDirectory(prefix="delta-pillow-") as td:
        tmp = Path(td)
        scene_mp4s = []

        for idx, scene in enumerate(SCENES, start=1):
            img_bytes = requests.get(scene["image"], timeout=40).content
            jpg = tmp / f"scene_{idx}.jpg"
            draw_scene(img_bytes, scene["title"], scene["subtitle"], jpg)

            voice = tmp / f"voice_{idx}.mp3"
            speech_text = f"{scene['title']} {scene['subtitle']}".strip()
            gTTS(text=speech_text, lang="iw").save(str(voice))

            clip = tmp / f"scene_{idx}.mp4"
            duration = scene["duration"]
            frames = duration * FPS
            vf = (
                "scale=8000:-1,"
                f"zoompan=z='min(zoom+0.0015,1.5)':x='iw/2-(iw/zoom/2)':y='ih/2-(ih/zoom/2)':d={frames}:s={WIDTH}x{HEIGHT}:fps={FPS}"
            )
            run_cmd(
                [
                    "ffmpeg",
                    "-y",
                    "-loop",
                    "1",
                    "-i",
                    str(jpg),
                    "-i",
                    str(voice),
                    "-vf",
                    vf,
                    "-t",
                    str(duration),
                    "-r",
                    str(FPS),
                    "-c:v",
                    "libx264",
                    "-b:v",
                    "2000k",
                    "-minrate",
                    "1500k",
                    "-maxrate",
                    "2500k",
                    "-bufsize",
                    "5000k",
                    "-pix_fmt",
                    "yuv420p",
                    "-c:a",
                    "aac",
                    str(clip),
                ]
            )
            scene_mp4s.append(clip)

        concat_list = tmp / "list.txt"
        concat_list.write_text(
            "\n".join([f"file '{p.as_posix()}'" for p in scene_mp4s]), encoding="utf-8"
        )
        combined = tmp / "combined.mp4"
        run_cmd(
            [
                "ffmpeg",
                "-y",
                "-f",
                "concat",
                "-safe",
                "0",
                "-i",
                str(concat_list),
                "-c",
                "copy",
                str(combined),
            ]
        )

        music = tmp / "music.mp3"
        download_music_with_fallback(music)

        run_cmd(
            [
                "ffmpeg",
                "-y",
                "-i",
                str(combined),
                "-i",
                str(music),
                "-filter_complex",
                "[1:a]volume=0.12[bg];[0:a][bg]amix=inputs=2:duration=first",
                "-c:v",
                "copy",
                str(OUT_PATH),
            ]
        )

    return OUT_PATH


if __name__ == "__main__":
    print(create_marketing_video())
