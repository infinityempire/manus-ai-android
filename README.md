# 🤖 Manus-Free AI - Android App

[![Build Android APK](https://github.com/username/manus-free-android/actions/workflows/build-apk.yml/badge.svg)](https://github.com/username/manus-free-android/actions/workflows/build-apk.yml)

**Manus-Free AI** הוא עוזר דיגיטלי חכם עם יכולות AI מתקדמות לאנדרואיד. האפליקציה עובדת במצב offline ו-online ומספקת ממשק צ'אט בעברית עם כלים מתקדמים.

## ✨ תכונות עיקריות

- 🧠 **מודל AI מובנה** - עובד ללא חיבור לאינטרנט
- 💬 **ממשק צ'אט בעברית** - שיחה טבעית ואינטואיטיבית
- 🔍 **חיפוש ברשת** - אינטגרציה עם DuckDuckGo API
- 📁 **ניהול קבצים** - קריאה וכתיבה של קבצים
- 🎨 **עיצוב Material Design** - ממשק מודרני ונוח
- ⚡ **ביצועים מהירים** - תגובה מיידית
- 🔒 **פרטיות מלאה** - כל הנתונים נשמרים במכשיר

## 📱 דרישות מערכת

- **Android 5.0** (API 21) ומעלה
- **זיכרון RAM:** 2GB מומלץ
- **אחסון:** 100MB פנויים
- **חיבור אינטרנט:** אופציונלי (לחיפוש ברשת)

## 🚀 התקנה

### הורדה מ-GitHub Releases

1. עבור ל[עמוד ה-Releases](https://github.com/username/manus-free-android/releases)
2. הורד את הקובץ `manus-free-release.apk`
3. הפעל "התקנה ממקורות לא ידועים" בהגדרות Android
4. התקן את ה-APK
5. פתח את האפליקציה ותתחיל לשוחח!

### בנייה מקוד המקור

```bash
# שכפול הפרויקט
git clone https://github.com/username/manus-free-android.git
cd manus-free-android

# בנייה
./gradlew assembleDebug

# ה-APK יימצא ב:
# app/build/outputs/apk/debug/app-debug.apk
```

## 🛠️ פיתוח

### מבנה הפרויקט

```
app/src/main/
├── java/com/manusfree/ai/
│   ├── MainActivity.java      # Activity ראשי
│   ├── ManusAI.java          # מנוע AI
│   ├── ChatAdapter.java      # מתאם צ'אט
│   ├── ChatMessage.java      # מודל הודעה
│   ├── WebSearchTool.java    # כלי חיפוש
│   └── FileManager.java      # ניהול קבצים
├── res/
│   ├── layout/              # קבצי Layout
│   ├── values/              # צבעים, מחרוזות, סגנונות
│   └── drawable/            # אייקונים ורקעים
└── AndroidManifest.xml      # הגדרות האפליקציה
```

### טכנולוגיות

- **Java 17** - שפת הפיתוח
- **Android Gradle Plugin 8.1.0** - כלי בנייה
- **AndroidX** - ספריות תמיכה מודרניות
- **Material Design Components** - עיצוב UI
- **OkHttp** - תקשורת רשת
- **Gson** - עיבוד JSON

## 🎯 שימוש

### פקודות בסיסיות

האפליקציה תומכת בפקודות מיוחדות:

```
tool:search_web [שאילתה]     - חיפוש מידע ברשת
tool:write_file [שם] [תוכן]  - כתיבת קובץ
tool:read_file [שם קובץ]     - קריאת קובץ
```

### דוגמאות שימוש

**חיפוש ברשת:**
```
tool:search_web מזג האוויר בתל אביב
```

**כתיבת קובץ:**
```
tool:write_file רשימת_קניות.txt חלב, לחם, ביצים
```

**קריאת קובץ:**
```
tool:read_file רשימת_קניות.txt
```

## 🔧 GitHub Actions

הפרויקט כולל workflow אוטומטי שבונה APK עבור כל push:

- ✅ בנייה אוטומטית של Debug ו-Release APK
- 📦 העלאה כ-Artifacts
- 🏷️ יצירת Release אוטומטית
- 🧪 בדיקות איכות קוד

## 📋 רשימת תיקונים

### גרסה 1.0.0
- ✅ מבנה פרויקט Android מלא
- ✅ מנוע AI עם תשובות חכמות
- ✅ ממשק צ'אט בעברית
- ✅ כלי חיפוש ברשת
- ✅ מערכת ניהול קבצים
- ✅ עיצוב Material Design
- ✅ GitHub Actions לבנייה אוטומטית

## 🤝 תרומה

נשמח לקבל תרומות! אנא:

1. Fork את הפרויקט
2. צור branch חדש (`git checkout -b feature/amazing-feature`)
3. Commit את השינויים (`git commit -m 'Add amazing feature'`)
4. Push ל-branch (`git push origin feature/amazing-feature`)
5. פתח Pull Request

## 📄 רישיון

פרויקט זה מופץ תחת רישיון MIT. ראה `LICENSE` לפרטים נוספים.

## 📞 תמיכה

- 🐛 **באגים:** פתח [Issue](https://github.com/username/manus-free-android/issues)
- 💡 **רעיונות:** פתח [Discussion](https://github.com/username/manus-free-android/discussions)
- 📧 **יצירת קשר:** support@manus-free.com

## 🙏 תודות

- [DuckDuckGo](https://duckduckgo.com) - API חיפוש
- [Material Design](https://material.io) - עיצוב UI
- [OkHttp](https://square.github.io/okhttp/) - ספריית רשת
- [Android Jetpack](https://developer.android.com/jetpack) - רכיבי Android

---

**Made with ❤️ by the Manus Team**

