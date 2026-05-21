# Delta Agent — Professional Marketing Video Generator

Delta Agent מייצר אוטומטית וידאו שיווקי מקצועי (1080x1920, 30fps, ~11s) עם:
- רקעים דינמיים מ-Unsplash
- טקסט שיווקי בעברית עם אנימציית fade-in/slide-up
- מוזיקת רקע בעוצמה נמוכה כדי לשמור עדיפות לקריינות
- בדיקות איכות קשיחות (fail-fast)

## Script אוטומטי לכל קמפיין עתידי

אפשר להגדיר תוכן משתנה בלי לגעת בקוד דרך `DELTA_VIDEO_SCRIPT`:

```json
[
  {"title":"כותרת 1", "subtitle":"שורת משנה 1", "query":"business,success"},
  {"title":"כותרת 2", "subtitle":"שורת משנה 2", "query":"executive,office"},
  {"title":"כותרת 3", "subtitle":"שורת משנה 3", "query":"smartphone,marketing"}
]
```

אם לא מוגדר script, המערכת משתמשת בתסריט מקצועי ברירת מחדל.

## Run

```bash
python delta_agent.py
```

Output: `output/final_marketing_video.mp4`
