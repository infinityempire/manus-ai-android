import requests
import time

def send_whatsapp_alert(message):
    # כאן נכנס המספר שלך והטוקן להתראות
    phone_number = "972XXXXXXXXX" # תעדכן למספר שלך בפורמט בינלאומי
    api_url = f"https://api.callmebot.com/whatsapp.php?phone={phone_number}&text={message}&apikey=YOUR_API_KEY"
    try:
        requests.get(api_url)
    except:
        pass

def scan_and_operate():
    print("Delta Agent is scanning...")
    # כאן נמצא הלוגיקה של הסריקה שלך
    # ברגע שיש הצלחה:
    send_whatsapp_alert("היי טל, מצאתי ליד חדש! שלחתי לו את לינק ה-PayPal שלך 🚀")

if __name__ == "__main__":
    while True:
        scan_and_operate()
        time.sleep(3600) # סריקה כל שעה
