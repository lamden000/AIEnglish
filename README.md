# LingoBuddy - AI-Powered English Learning App 🎓

[![Android](https://img.shields.io/badge/Platform-Android-green.svg)](https://www.android.com/)
[![Kotlin](https://img.shields.io/badge/Language-Kotlin-blue.svg)](https://kotlinlang.org/)
[![Min SDK](https://img.shields.io/badge/Min%20SDK-28-orange.svg)](https://developer.android.com/about/versions/pie)
[![Target SDK](https://img.shields.io/badge/Target%20SDK-34-orange.svg)](https://developer.android.com/)

LingoBuddy is an innovative Android application that leverages AI technology to provide an interactive and personalized English learning experience. The app offers multiple learning modes including AI chat tutoring, pronunciation practice, image-based vocabulary learning, role-play conversations, and reading comprehension quizzes.

## ✨ Features

### 🤖 Chat with AI Tutor
- Real-time conversation with an AI English tutor
- Multi-language support with automatic language detection
- Text-to-Speech (TTS) with native-like pronunciation
- Speech-to-Text for voice interactions
- Context-aware responses tailored to your learning level

### 🗣️ Pronunciation Practice
- Voice recognition for pronunciation assessment
- AI-powered feedback on pronunciation accuracy
- Daily topic-based practice sessions
- Record and compare your pronunciation with native speakers

### 🖼️ Image-Based Vocabulary Learning
- Upload images or take photos to learn vocabulary
- AI-powered image recognition and description
- Learn words in context with visual associations
- Camera integration for real-world object learning

### 🎭 Role-Play with AI
- Immersive conversational scenarios
- Customizable roles and contexts
- Choose from predefined scenarios or create your own
- Real-time conversation tracking (10-minute daily goal)
- Practice real-world English communication

### 📚 Passage Quiz
- AI-generated reading comprehension passages
- Topic-based learning with daily themes
- Grammar and vocabulary exercises
- Interactive quizzes with instant feedback

### 📖 Personal Dictionary
- Save and review learned words
- Add personal notes to vocabulary items
- Text selection for quick word saving
- Auto-translate feature (English to Vietnamese)
- Create custom quizzes from saved words

### 📊 Daily Tasks & Progress Tracking
- Daily learning goals and challenges
- Task completion tracking
- Progress monitoring
- Gamified learning experience

## 🛠️ Technologies & Architecture

### Core Technologies
- **Language**: Kotlin
- **Architecture**: MVVM (Model-View-ViewModel)
- **UI Framework**: View Binding, Material Design
- **Minimum SDK**: Android 9.0 (API 28)
- **Target SDK**: Android 14 (API 34)

### Key Libraries & Services

#### AI & Machine Learning
- **OpenAI API**: GPT-powered conversational AI
- **TogetherAI API**: Alternative AI inference service
- **Google ML Kit**: Language identification and translation
- **ML Kit Translate**: On-device translation
- **ML Kit Language ID**: Automatic language detection

#### Firebase Services
- **Firebase Authentication**: User authentication with email/password and Google Sign-In
- **Firebase Firestore**: Cloud database for user data and saved words
- **Firebase Analytics**: App usage tracking

#### Networking
- **Retrofit 2.9.0**: HTTP client for API calls
- **Gson**: JSON serialization/deserialization

#### Android Components
- **Jetpack Navigation**: Fragment navigation with Safe Args
- **LiveData & ViewModel**: Reactive UI updates
- **RecyclerView**: Efficient list displays
- **TextToSpeech**: Native Android TTS engine
- **SpeechRecognizer**: Voice input recognition

## 📱 App Structure

```
com.example.lingobuddypck/
├── adapter/           # RecyclerView adapters
├── data/             # Data models and entities
├── network/          # Retrofit API interfaces
├── repository/       # Data layer with Firebase integration
├── services/         # Business logic and AI services
├── ui/               # Activities and Fragments
│   ├── home/        # Home screen with features
│   ├── RolePlay/    # Role-play feature screens
│   └── dictionary/  # Saved words management
├── utils/           # Utility classes
└── ViewModel/       # ViewModels for MVVM architecture
```

## 🚀 Getting Started

### Prerequisites
- Android Studio Arctic Fox or later
- Android SDK 28 or higher
- JDK 8 or higher
- Active internet connection

### Installation

1. **Clone the repository**
   ```bash
   git clone https://github.com/yourusername/AIEnglish.git
   cd AIEnglish/LingoBuddy
   ```

2. **Configure API Keys**
   
   Create a `local.properties` file in the root directory with your API keys:
   ```properties
   OPENAI_API_KEY=your_openai_api_key_here
   TOGETHERAI_API_KEY=your_togetherai_api_key_here
   ```

3. **Set up Firebase**
   - Create a Firebase project at [Firebase Console](https://console.firebase.google.com/)
   - Add an Android app to your Firebase project
   - Download `google-services.json` and place it in the `app/` directory
   - Enable Email/Password and Google Sign-In authentication
   - Create a Firestore database

4. **Build and Run**
   ```bash
   ./gradlew assembleDebug
   ```
   
   Or open the project in Android Studio and click Run ▶️

## 🔑 Configuration

### Required Permissions
The app requires the following permissions:
- `INTERNET`: For API calls and Firebase connectivity
- `RECORD_AUDIO`: For speech recognition and pronunciation practice
- `CAMERA` (optional): For image-based learning

### API Configuration
The app uses two AI services:
- **OpenAI**: For advanced conversational AI
- **TogetherAI**: For alternative AI inference

You can configure which service to use in [RetrofitClient.kt](LingoBuddy/app/src/main/java/com/example/lingobuddypck/network/RetrofitClient.kt)

## 📖 Usage

### First Time Setup
1. Launch the app and create an account or sign in with Google
2. Complete your profile setup
3. Explore the various learning features from the home screen

### Daily Learning Routine
1. Check your daily tasks on the home screen
2. Complete pronunciation practice with the daily topic
3. Engage in a 10-minute role-play conversation
4. Practice reading comprehension with passage quizzes
5. Review your saved vocabulary words

### Tips for Best Results
- Use headphones for better TTS quality
- Practice in a quiet environment for voice recognition
- Complete daily tasks to track your progress
- Save unfamiliar words for later review
- Try different role-play scenarios to improve conversational skills

## 🏗️ Building for Production

To create a release build:

```bash
./gradlew assembleRelease
```

Make sure to configure your signing keys in the `build.gradle.kts` file before building for production.

## 🤝 Contributing

Contributions are welcome! Please feel free to submit a Pull Request.

1. Fork the project
2. Create your feature branch (`git checkout -b feature/AmazingFeature`)
3. Commit your changes (`git commit -m 'Add some AmazingFeature'`)
4. Push to the branch (`git push origin feature/AmazingFeature`)
5. Open a Pull Request

## 📄 License

This project is licensed under the MIT License - see the LICENSE file for details.

## 🙏 Acknowledgments

- OpenAI for providing the GPT API
- Google ML Kit for language processing capabilities
- Firebase for backend infrastructure
- Material Design for UI components

## 📞 Support

For support, please open an issue in the GitHub repository or contact the development team.

## 🔮 Future Enhancements

- [ ] Offline mode with cached lessons
- [ ] Social features for learner community
- [ ] Advanced analytics and progress reports
- [ ] More language pair support
- [ ] Gamification with achievements and leaderboards
- [ ] Voice chat with AI for more natural conversations
- [ ] Writing practice and essay correction
- [ ] Integration with popular vocabulary resources

---

Made with ❤️ by the LingoBuddy Team