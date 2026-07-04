# MoneyMate

MoneyMate is an Android expense-planning app built with Jetpack Compose. It helps you track salary, expenses, and savings while offering AI-generated money-saving advice through OpenAI.

## Features
- Enter your monthly salary
- Add expense categories and amounts
- See remaining balance after expenses
- Ask OpenAI for a personalized money-saving tip

## OpenAI setup
1. Create an OpenAI API key at https://platform.openai.com/api-keys
2. Open the app and go to the expense screen
3. Paste your API key into the OpenAI API key field
4. Tap Ask AI

## Important note
For production apps, avoid storing API keys directly in the client app. A safer approach is to call OpenAI through your own backend service.

## GitHub Actions APK build
1. Push this repository to GitHub.
2. Open the Actions tab.
3. Run the workflow named "Build Android APK".
4. Download the generated APK from the workflow artifact.

## Release workflow
To publish a signed APK automatically:
1. Create a keystore and base64-encode it.
2. Add these GitHub secrets:
   - KEYSTORE_BASE64
   - KEYSTORE_PASSWORD
   - KEY_ALIAS
   - KEY_PASSWORD
3. Run the workflow named "Release APK" or push a tag like v1.0.0.

## Run locally
Open the project folder in Android Studio and run it on an emulator or physical device.
