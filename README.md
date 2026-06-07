# 📚 Book Reader App

A mobile-friendly Android book reader that extracts text from PDFs and reflows it for comfortable reading. Supports Light, Dark, and Sepia themes with adjustable font size.

---

## Features
- 📖 PDF text extraction & reflow (no more zooming!)
- 🌙 Light / Dark / Sepia reading themes
- 🔡 Adjustable font size (12–28sp)
- 📏 Adjustable line spacing
- 📊 Reading progress bar per book
- 📚 Book library with last-read tracking
- 👆 Swipe left/right to turn pages
- 💾 Preferences saved automatically

---

## How to Build (Codespaces only)

> Project SDK settings: `compileSdk 34`, `targetSdk 34`, `minSdk 26`, Java 17 / Kotlin JVM target `1.8`.

### Step 1: Open this project in Codespaces
1. Push this folder to a GitHub repository
2. Click **Code → Codespaces → Create codespace on main**
3. Wait for it to load (takes ~2 minutes)

### Android Studio option
If you prefer Android Studio, open the project directly from the repository folder and let Gradle sync the project. Use the SDK manager to install Android SDK Platform 34 and Build Tools 34.0.0 if needed.

### Step 2: Install Java & Android SDK in Codespaces
Paste this into the Codespaces terminal:

```bash
# Install Java 17
sudo apt-get update
sudo apt-get install -y openjdk-17-jdk wget unzip

# Set JAVA_HOME
echo 'export JAVA_HOME=/usr/lib/jvm/java-17-openjdk-amd64' >> ~/.bashrc
echo 'export PATH=$PATH:$JAVA_HOME/bin' >> ~/.bashrc
source ~/.bashrc

# Download Android command-line tools
mkdir -p ~/android-sdk/cmdline-tools
cd ~/android-sdk/cmdline-tools
wget https://dl.google.com/android/repository/commandlinetools-linux-11076708_latest.zip
unzip commandlinetools-linux-11076708_latest.zip
mv cmdline-tools latest

# Set Android SDK environment variables
echo 'export ANDROID_HOME=$HOME/android-sdk' >> ~/.bashrc
echo 'export PATH=$PATH:$ANDROID_HOME/cmdline-tools/latest/bin:$ANDROID_HOME/platform-tools' >> ~/.bashrc
source ~/.bashrc

# Accept licenses and install required SDK components
yes | sdkmanager --licenses
sdkmanager "platform-tools" "platforms;android-34" "build-tools;34.0.0"
```

### Step 3: Build the APK
Navigate to the project folder and build:

```bash
cd /workspaces/YOUR_REPO_NAME

# Make gradlew executable
chmod +x gradlew

# Build debug APK
./gradlew assembleDebug
```

This takes 3–5 minutes the first time. When done, your APK is at:
```
app/build/outputs/apk/debug/app-debug.apk
```

### Step 4: Download the APK
1. In Codespaces, find the file in the Explorer panel:
   `app → build → outputs → apk → debug → app-debug.apk`
2. Right-click it → **Download**

### Step 5: Install on your Android phone
1. Transfer the APK to your phone (via USB, WhatsApp, Google Drive, etc.)
2. On your phone: **Settings → Security → Install unknown apps** → Allow your file manager
3. Open the APK file → Install
4. Done! Open "Book Reader" from your home screen

---

## Using the App

1. **Add a book**: Tap the **+** button → pick a PDF from your phone
2. **Read**: Tap the book card to open it
3. **Navigate**: Swipe left/right, or use the arrow buttons at the bottom
4. **Settings**: Tap the ⚙️ icon → adjust font size, theme, line spacing
5. **Delete**: Long-press a book card in the library

---

## Notes
- Works best with text-based PDFs (ebooks, reports, articles)
- Scanned/image PDFs will show empty pages (they need OCR, not included)
- Your reading progress is saved automatically when you turn pages
