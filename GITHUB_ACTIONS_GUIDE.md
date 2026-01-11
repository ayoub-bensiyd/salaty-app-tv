# 🤖 دليل GitHub Actions - البناء التلقائي

## 📋 نظرة عامة

تم إعداد **GitHub Actions** لبناء تطبيق Android تلقائياً. يتضمن المشروع 3 workflows:

### 1️⃣ Android CI Build (`android-build.yml`)
- **المشغل**: عند كل push أو pull request
- **الوظيفة**: بناء APK (Debug & Release)
- **المخرجات**: ملفات APK كـ artifacts

### 2️⃣ Release (`release.yml`)
- **المشغل**: عند إنشاء tag جديد (مثل `v1.0.0`)
- **الوظيفة**: إنشاء release مع APK
- **المخرجات**: Release على GitHub مع APK مرفق

### 3️⃣ PR Check (`pr-check.yml`)
- **المشغل**: عند فتح Pull Request
- **الوظيفة**: فحص الكود وبناء APK
- **المخرجات**: تعليق تلقائي على PR

## 🚀 كيفية الاستخدام

### البناء التلقائي

عند كل push إلى `main` أو `develop`:

```bash
git add .
git commit -m "تحديث التطبيق"
git push
```

سيتم تلقائياً:
1. ✅ بناء APK
2. ✅ رفع APK كـ artifact
3. ✅ إنشاء ملخص البناء

### تحميل APK من GitHub Actions

1. اذهب إلى: https://github.com/ayoub-bensiyd/salaty-app-tv/actions
2. اختر آخر workflow run ناجح
3. انزل لأسفل إلى قسم **Artifacts**
4. حمّل `app-debug` أو `app-release-unsigned`

### إنشاء Release

#### الطريقة 1: باستخدام Git Tags

```bash
# إنشاء tag
git tag -a v1.0.0 -m "النسخة 1.0.0 - الإصدار الأول"

# رفع tag إلى GitHub
git push origin v1.0.0
```

سيتم تلقائياً:
1. ✅ بناء Release APK
2. ✅ إنشاء GitHub Release
3. ✅ رفع APK إلى Release

#### الطريقة 2: يدوياً من GitHub

1. اذهب إلى: https://github.com/ayoub-bensiyd/salaty-app-tv/actions
2. اختر **Create Release** workflow
3. اضغط **Run workflow**
4. أدخل رقم النسخة (مثل `v1.0.0`)
5. اضغط **Run workflow**

### عرض Releases

https://github.com/ayoub-bensiyd/salaty-app-tv/releases

## 📊 حالة البناء

يمكنك إضافة شارة حالة البناء في `README.md`:

```markdown
![Build Status](https://github.com/ayoub-bensiyd/salaty-app-tv/workflows/Android%20CI%20-%20Build%20APK/badge.svg)
```

## ⚙️ تكوين Workflows

### تعديل متى يتم البناء

في `.github/workflows/android-build.yml`:

```yaml
on:
  push:
    branches: [ main, develop ]  # أضف أو احذف فروع
  pull_request:
    branches: [ main ]
```

### تغيير إصدار JDK

```yaml
- name: Set up JDK 17
  uses: actions/setup-java@v4
  with:
    java-version: '17'  # غيّر إلى 11 أو 21 إذا أردت
```

### إضافة خطوات إضافية

يمكنك إضافة خطوات مثل:

```yaml
- name: Run Tests
  run: ./gradlew test

- name: Generate Coverage Report
  run: ./gradlew jacocoTestReport
```

## 🔐 Secrets (للتوقيع)

لتوقيع APK في GitHub Actions:

### 1. إنشاء Keystore

```bash
keytool -genkey -v -keystore prayer-tv.keystore -alias prayertv -keyalg RSA -keysize 2048 -validity 10000
```

### 2. تحويل Keystore إلى Base64

```bash
# على Linux/Mac
base64 prayer-tv.keystore > keystore.txt

# على Windows (PowerShell)
[Convert]::ToBase64String([IO.File]::ReadAllBytes("prayer-tv.keystore")) > keystore.txt
```

### 3. إضافة Secrets في GitHub

1. اذهب إلى: Settings → Secrets and variables → Actions
2. أضف Secrets التالية:
   - `KEYSTORE_BASE64`: محتوى ملف `keystore.txt`
   - `KEYSTORE_PASSWORD`: كلمة مرور الـ keystore
   - `KEY_ALIAS`: الاسم المستعار (مثل `prayertv`)
   - `KEY_PASSWORD`: كلمة مرور المفتاح

### 4. تحديث Workflow

أضف في `.github/workflows/release.yml`:

```yaml
- name: Decode Keystore
  run: |
    echo "${{ secrets.KEYSTORE_BASE64 }}" | base64 -d > app/keystore.jks

- name: Build Signed APK
  run: ./gradlew assembleRelease
  env:
    KEYSTORE_PASSWORD: ${{ secrets.KEYSTORE_PASSWORD }}
    KEY_ALIAS: ${{ secrets.KEY_ALIAS }}
    KEY_PASSWORD: ${{ secrets.KEY_PASSWORD }}
```

وفي `app/build.gradle.kts`:

```kotlin
android {
    signingConfigs {
        create("release") {
            storeFile = file("keystore.jks")
            storePassword = System.getenv("KEYSTORE_PASSWORD")
            keyAlias = System.getenv("KEY_ALIAS")
            keyPassword = System.getenv("KEY_PASSWORD")
        }
    }
    
    buildTypes {
        release {
            signingConfig = signingConfigs.getByName("release")
        }
    }
}
```

## ⚠️ ملاحظات مهمة

### ملفات الصوت

الـ workflows تقوم بإنشاء ملفات صوت فارغة (placeholders) لكي يكتمل البناء.

**⚠️ هام**: APK المبني لن يعمل بشكل صحيح بدون ملفات الصوت الحقيقية!

لإضافة ملفات الصوت الحقيقية:

#### الخيار 1: رفعها إلى Git (غير موصى به)

```bash
# إزالة *.mp3 من .gitignore
# ثم:
git add app/src/main/res/raw/*.mp3
git commit -m "Add audio files"
git push
```

**⚠️ تحذير**: سيزيد حجم المستودع بشكل كبير!

#### الخيار 2: استخدام Git LFS (موصى به)

```bash
# تثبيت Git LFS
git lfs install

# تتبع ملفات MP3
git lfs track "*.mp3"

# إضافة الملفات
git add .gitattributes
git add app/src/main/res/raw/*.mp3
git commit -m "Add audio files with LFS"
git push
```

#### الخيار 3: استخدام Secrets (للملفات الصغيرة)

1. حوّل ملفات MP3 إلى Base64
2. أضفها كـ Secrets
3. فك تشفيرها في Workflow

## 📈 مراقبة البناء

### عرض سجلات البناء

1. اذهب إلى: https://github.com/ayoub-bensiyd/salaty-app-tv/actions
2. اختر workflow run
3. اضغط على job name لعرض السجلات

### إشعارات البناء

يمكنك تفعيل إشعارات البريد الإلكتروني:

1. Settings → Notifications
2. فعّل **Actions**

## 🐛 استكشاف الأخطاء

### خطأ: "Gradle build failed"

**الحل**:
- تحقق من السجلات
- تأكد من صحة `build.gradle.kts`
- جرب البناء محلياً أولاً

### خطأ: "Permission denied: gradlew"

**الحل**: تأكد من وجود:
```yaml
- name: Grant execute permission for gradlew
  run: chmod +x gradlew
```

### خطأ: "Out of memory"

**الحل**: أضف في `gradle.properties`:
```properties
org.gradle.jvmargs=-Xmx4096m
```

## 📊 إحصائيات البناء

يمكنك إضافة شارات في `README.md`:

```markdown
![Build](https://github.com/ayoub-bensiyd/salaty-app-tv/workflows/Android%20CI%20-%20Build%20APK/badge.svg)
![Release](https://github.com/ayoub-bensiyd/salaty-app-tv/workflows/Create%20Release/badge.svg)
![License](https://img.shields.io/github/license/ayoub-bensiyd/salaty-app-tv)
![Downloads](https://img.shields.io/github/downloads/ayoub-bensiyd/salaty-app-tv/total)
```

## 🎯 أفضل الممارسات

### ✅ افعل

- ✅ اختبر البناء محلياً قبل الـ push
- ✅ استخدم semantic versioning (v1.0.0, v1.1.0, etc.)
- ✅ اكتب رسائل commit واضحة
- ✅ راجع سجلات البناء بانتظام

### ❌ لا تفعل

- ❌ لا ترفع ملفات كبيرة بدون LFS
- ❌ لا تضع معلومات حساسة في الكود
- ❌ لا تتجاهل أخطاء البناء

## 🔗 روابط مفيدة

- **Actions**: https://github.com/ayoub-bensiyd/salaty-app-tv/actions
- **Releases**: https://github.com/ayoub-bensiyd/salaty-app-tv/releases
- **Workflows**: https://github.com/ayoub-bensiyd/salaty-app-tv/tree/main/.github/workflows

---

**نصيحة**: راقب استخدام GitHub Actions Minutes في Settings → Billing!
