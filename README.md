# Shopping List App

A modern Android shopping list application built with Jetpack Compose and following clean architecture principles.

## Features

- Add, edit, and delete shopping list items
- Mark items as complete/incomplete
- Optional quantity and notes for each item
- Filter items by status (All/Active/Completed)
- Local data persistence using Room database
- Material Design 3 theming
- Accessibility support
- Dark/Light theme support

## Tech Stack

- Kotlin 1.8+
- Jetpack Compose for UI
- Room for data persistence
- Hilt for dependency injection
- Kotlin Coroutines and Flow
- MVVM architecture
- Material Design 3
- Unit tests with JUnit and Mockito

## Project Structure

The project follows clean architecture principles with the following layers:

- **Data**: Room database, DAOs, and repository implementations
- **Domain**: Repository interfaces and business logic
- **Presentation**: ViewModels and Compose UI components

## Setup

1. Clone the repository
2. Open the project in Android Studio Electric Eel or newer
3. Sync the project with Gradle files
4. Run the app on an emulator or physical device (min SDK 26)

## Architecture

The app follows the MVVM (Model-View-ViewModel) architecture pattern:

- **Model**: Room database and repository pattern for data operations
- **View**: Jetpack Compose UI components
- **ViewModel**: Manages UI state and business logic

## Testing

The project includes unit tests for:
- Repository layer (data operations)
- ViewModel layer (business logic)

Run tests using:
```bash
./gradlew test
```

## Extending the App

To add new features:

1. For new data fields:
   - Update the `Product` entity
   - Create a Room migration
   - Update the DAO and Repository interfaces

2. For new UI features:
   - Add new states/actions to ViewModel
   - Create new Composable functions
   - Update the main screen

## License

This project is open source and available under the MIT License.
