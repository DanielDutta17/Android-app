# MoneyMate Python version

This folder contains a Python-based version of the expense app that can be packaged for Android using Kivy and Buildozer.

## Run locally
Install dependencies:

```bash
pip install -r requirements.txt
python main.py
```

## Build APK
Install Buildozer and the Android SDK/NDK, then run:

```bash
buildozer -v android debug
```

## Note
If your machine cannot build Kivy dependencies, use a Linux/macOS environment or a GitHub Actions/Cloud Build pipeline for APK generation.
