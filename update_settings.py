import os

filepath = 'app/src/main/java/com/infinityempire/manusai/SettingsActivity.java' # נתיב משוער לפי המבנה
if not os.path.exists(filepath):
    # חיפוש דינמי אם הנתיב שונה
    for root, dirs, files in os.walk('.'):
        if 'SettingsActivity' in "".join(files):
            filepath = os.path.join(root, [f for f in files if 'SettingsActivity' in f][0])
            break

if os.path.exists(filepath):
    with open(filepath, 'r') as f:
        content = f.read()
    
    # ביטול הבדיקה הקשיחה ל-sk- ואישור מפתחות ארוכים (גוגל)
    content = content.replace('if (key.startsWith("sk-"))', 'if (key.length() > 10)')
    content = content.replace('saveButton.setEnabled(false)', 'saveButton.setEnabled(true)')
    
    with open(filepath, 'w') as f:
        f.write(content)
    print(f"Updated {filepath} successfully.")
else:
    print("Settings file not found. Please check the path.")

