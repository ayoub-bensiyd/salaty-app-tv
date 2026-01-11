# 📤 دليل رفع المشروع إلى GitHub

## ✅ تم بالفعل

تم تهيئة Git وعمل commit أولي للمشروع بنجاح! 

```
✓ git init
✓ git add .
✓ git commit -m "Initial commit..."
```

## 🚀 خطوات رفع المشروع إلى GitHub

### الطريقة 1: إنشاء مستودع جديد على GitHub (موصى بها)

#### 1️⃣ إنشاء المستودع على GitHub

1. اذهب إلى [GitHub.com](https://github.com)
2. سجل الدخول إلى حسابك
3. اضغط على زر **"+"** في الأعلى ← **New repository**
4. املأ التفاصيل:
   - **Repository name**: `OujdaPrayerTV` أو `prayer-tv-oujda`
   - **Description**: `🕌 Android TV app for displaying prayer times in Oujda, Morocco - Works 100% offline`
   - **Visibility**: اختر **Public** أو **Private**
   - **⚠️ لا تختر** "Initialize this repository with a README" (لأن لدينا README بالفعل)
5. اضغط **Create repository**

#### 2️⃣ ربط المشروع المحلي بـ GitHub

بعد إنشاء المستودع، ستظهر لك تعليمات. استخدم الأوامر التالية:

```bash
# أضف رابط المستودع البعيد (استبدل USERNAME باسم المستخدم الخاص بك)
git remote add origin https://github.com/USERNAME/OujdaPrayerTV.git

# أو إذا كنت تستخدم SSH:
git remote add origin git@github.com:USERNAME/OujdaPrayerTV.git

# تأكد من الفرع الرئيسي
git branch -M main

# ارفع المشروع
git push -u origin main
```

#### 3️⃣ أدخل بيانات الاعتماد

عند الرفع لأول مرة، سيُطلب منك:
- **اسم المستخدم** (GitHub username)
- **كلمة المرور** (Personal Access Token - ليس كلمة مرور الحساب)

**ملاحظة**: GitHub لم يعد يقبل كلمات المرور العادية. يجب استخدام **Personal Access Token**.

### 📝 إنشاء Personal Access Token

1. اذهب إلى GitHub → **Settings**
2. في القائمة الجانبية، اختر **Developer settings**
3. اختر **Personal access tokens** → **Tokens (classic)**
4. اضغط **Generate new token** → **Generate new token (classic)**
5. املأ التفاصيل:
   - **Note**: `Prayer TV App`
   - **Expiration**: اختر المدة المناسبة
   - **Scopes**: اختر **repo** (كامل)
6. اضغط **Generate token**
7. **⚠️ انسخ التوكن فوراً** (لن تتمكن من رؤيته مرة أخرى!)

استخدم هذا التوكن بدلاً من كلمة المرور عند الرفع.

### الطريقة 2: استخدام GitHub Desktop (أسهل)

1. حمّل وثبّت [GitHub Desktop](https://desktop.github.com/)
2. افتح GitHub Desktop
3. اختر **File** → **Add Local Repository**
4. حدد مجلد المشروع: `C:\Users\amrao\tv-app`
5. اضغط **Publish repository**
6. املأ التفاصيل واضغط **Publish**

### الطريقة 3: استخدام VS Code (إذا كنت تستخدمه)

1. افتح المشروع في VS Code
2. اذهب إلى تبويب **Source Control** (Ctrl+Shift+G)
3. اضغط على أيقونة **"..."** ← **Remote** → **Add Remote**
4. أدخل رابط المستودع
5. اضغط **Publish Branch**

## 🔄 الأوامر الأساسية للعمل مع Git

### رفع تغييرات جديدة

```bash
# إضافة الملفات المعدلة
git add .

# عمل commit
git commit -m "وصف التغييرات"

# رفع إلى GitHub
git push
```

### سحب آخر التحديثات

```bash
git pull
```

### التحقق من الحالة

```bash
git status
```

### عرض السجل

```bash
git log --oneline
```

## 📋 نصائح مهمة

### ✅ افعل

- ✅ اكتب رسائل commit واضحة وذات معنى
- ✅ اعمل commit بشكل منتظم
- ✅ استخدم `.gitignore` لاستبعاد الملفات غير الضرورية
- ✅ احتفظ بنسخة احتياطية من Personal Access Token

### ❌ لا تفعل

- ❌ لا ترفع ملفات الصوت الكبيرة (أضفها في `.gitignore`)
- ❌ لا ترفع ملفات build (محمية بالفعل في `.gitignore`)
- ❌ لا تشارك Personal Access Token مع أحد
- ❌ لا ترفع معلومات حساسة (مفاتيح، كلمات مرور)

## 🎯 بعد الرفع

### إضافة شارات (Badges) للـ README

أضف في بداية `README.md`:

```markdown
![Android](https://img.shields.io/badge/Platform-Android-green.svg)
![API](https://img.shields.io/badge/API-24%2B-brightgreen.svg)
![License](https://img.shields.io/badge/License-MIT-blue.svg)
![Kotlin](https://img.shields.io/badge/Language-Kotlin-purple.svg)
```

### إنشاء Releases

1. اذهب إلى المستودع على GitHub
2. اضغط **Releases** → **Create a new release**
3. أضف:
   - **Tag**: `v1.0.0`
   - **Title**: `النسخة 1.0.0 - الإصدار الأول`
   - **Description**: وصف الميزات
   - **Assets**: ارفع ملف APK

### إضافة Topics

في صفحة المستودع:
1. اضغط على أيقونة الترس بجانب **About**
2. أضف Topics:
   - `android`
   - `android-tv`
   - `prayer-times`
   - `islamic-app`
   - `kotlin`
   - `morocco`
   - `oujda`

## 🔐 الأمان

### ملفات يجب عدم رفعها

تأكد من أن `.gitignore` يحتوي على:

```
# Build files
*.apk
*.aab
/build
.gradle

# Audio files (large)
*.mp3
*.wav

# Keystore files
*.jks
*.keystore

# Local config
local.properties
```

### حماية الفروع

في إعدادات المستودع على GitHub:
1. **Settings** → **Branches**
2. أضف قاعدة حماية للفرع `main`
3. فعّل **Require pull request reviews**

## 📞 المساعدة

إذا واجهت مشاكل:

### خطأ: "remote origin already exists"

```bash
git remote remove origin
git remote add origin https://github.com/USERNAME/REPO.git
```

### خطأ: "Authentication failed"

- تأكد من استخدام Personal Access Token وليس كلمة المرور
- تحقق من صلاحيات التوكن

### خطأ: "Updates were rejected"

```bash
git pull origin main --rebase
git push
```

## 🎉 تم!

بعد رفع المشروع بنجاح، سيكون متاحاً على:

```
https://github.com/YOUR_USERNAME/OujdaPrayerTV
```

شارك الرابط مع الآخرين! 🚀

---

**ملاحظة**: تذكر تحديث رابط المستودع في ملفات التوثيق بعد الرفع.
