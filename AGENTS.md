# Project Style & UI Guidelines: EasiestBudget

This document outlines the standards for development to ensure consistency across the project.

## 🎨 UI & Design (Material 3)

- **Design System:** All new UI must follow **Material Design 3 (M3)** guidelines.
- **Color Palette:** 
    - **Primary:** `#6200EE` (Vibrant Violet)
    - **Background:** Use `?attr/colorSurface` or `?attr/colorBackground`.
    - **Contrast:** Maintain a minimum contrast ratio of **4.5:1** for text (WCAG 2.1 AA).
- **Layout:**
    - **Padding/Margins:** Use a consistent **16dp** padding for screen containers.
    - **Touch Targets:** All interactive elements (buttons, chips, list items) must have a minimum size of **48x48dp**.
- **Components:**
    - **Buttons:** Use `MaterialButton` with a corner radius of **12dp**.
    - **Cards:** Use `MaterialCardView` for list items with consistent elevation and corner radius.

## 🛠 Tech Stack Standards

- **Architecture:** Follow **Unidirectional Data Flow (UDF)**. 
    - State flows **down** from ViewModels to UI.
    - Events flow **up** from UI to ViewModels.
- **Jetpack Compose (Future/Refactor):**
    - Use `derivedStateOf` to minimize recompositions for calculated states.
    - Keep Composables stateless where possible (State Hoisting).
- **Data Persistence:** 
    - Use **Room ORM** for all local storage.
    - Return **Flows** from DAOs for reactive UI updates.

## 📝 Code Documentation

- Every class must have a KDoc header explaining its responsibility.
- Public methods should document parameters and return values.
- Complex business logic (e.g., budget calculations) must be commented inline.

## 🚀 Git & Workflow

- **Branching:** Use descriptive names (e.g., `feature/add-charts`, `fix/nav-bug`).
- **Commits:** Follow conventional commits (e.g., `feat:`, `fix:`, `docs:`, `style:`).
- **Ignore:** Ensure `.gitignore` is kept up to date to exclude build artifacts and IDE-specific files.
