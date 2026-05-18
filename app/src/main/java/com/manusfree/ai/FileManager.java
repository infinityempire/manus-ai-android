package com.manusfree.ai;

import android.content.Context;
import android.util.Log;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;

public class FileManager {
    
    private static final String TAG = "FileManager";
    private Context context;
    private File documentsDir;
    
    public FileManager(Context context) {
        this.context = context;
        this.documentsDir = new File(context.getFilesDir(), "documents");
        if (!documentsDir.exists()) {
            documentsDir.mkdirs();
        }
    }
    
    public String writeFile(String params) {
        try {
            // פרסור הפרמטרים: filename content
            String[] parts = params.split(" ", 2);
            if (parts.length < 2) {
                return "שימוש: tool:write_file [שם קובץ] [תוכן]";
            }
            
            String filename = parts[0];
            String content = parts[1];
            
            // וידוא שם קובץ בטוח
            if (!isValidFilename(filename)) {
                return "שם קובץ לא חוקי. השתמש באותיות, מספרים, נקודות ומקפים בלבד.";
            }
            
            File file = new File(documentsDir, filename);
            FileOutputStream fos = new FileOutputStream(file);
            OutputStreamWriter writer = new OutputStreamWriter(fos, "UTF-8");
            
            writer.write(content);
            writer.close();
            fos.close();
            
            Log.d(TAG, "File written: " + filename);
            return "✅ הקובץ '" + filename + "' נשמר בהצלחה!\nנתיב: " + file.getAbsolutePath();
            
        } catch (Exception e) {
            Log.e(TAG, "Error writing file", e);
            return "❌ שגיאה בכתיבת הקובץ: " + e.getMessage();
        }
    }
    
    public String readFile(String filename) {
        try {
            // וידוא שם קובץ בטוח
            if (!isValidFilename(filename)) {
                return "שם קובץ לא חוקי.";
            }
            
            File file = new File(documentsDir, filename);
            if (!file.exists()) {
                return "❌ הקובץ '" + filename + "' לא נמצא.\n\nקבצים זמינים:\n" + listFiles();
            }
            
            FileInputStream fis = new FileInputStream(file);
            BufferedReader reader = new BufferedReader(new InputStreamReader(fis, "UTF-8"));
            StringBuilder content = new StringBuilder();
            String line;
            
            while ((line = reader.readLine()) != null) {
                content.append(line).append("\n");
            }
            
            reader.close();
            fis.close();
            
            Log.d(TAG, "File read: " + filename);
            return "📄 תוכן הקובץ '" + filename + "':\n\n" + content.toString();
            
        } catch (Exception e) {
            Log.e(TAG, "Error reading file", e);
            return "❌ שגיאה בקריאת הקובץ: " + e.getMessage();
        }
    }
    
    public String listFiles() {
        try {
            File[] files = documentsDir.listFiles();
            if (files == null || files.length == 0) {
                return "אין קבצים שמורים.";
            }
            
            StringBuilder list = new StringBuilder("📁 קבצים שמורים:\n");
            for (File file : files) {
                if (file.isFile()) {
                    long size = file.length();
                    String sizeStr = size < 1024 ? size + " bytes" : (size / 1024) + " KB";
                    list.append("• ").append(file.getName()).append(" (").append(sizeStr).append(")\n");
                }
            }
            
            return list.toString();
            
        } catch (Exception e) {
            Log.e(TAG, "Error listing files", e);
            return "❌ שגיאה ברישום הקבצים: " + e.getMessage();
        }
    }
    
    private boolean isValidFilename(String filename) {
        if (filename == null || filename.trim().isEmpty()) {
            return false;
        }
        
        // בדיקה לתווים מסוכנים
        return filename.matches("[a-zA-Z0-9._-]+") && !filename.startsWith(".") && filename.length() <= 50;
    }
}

