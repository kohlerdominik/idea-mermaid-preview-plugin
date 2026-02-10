# GitHub Copilot Instructions: Mermaid Previewer for JetBrains IDEs

## 1. Role & Context
You are an expert JetBrains Plugin Developer specialized in Kotlin and the IntelliJ Platform SDK. You are building a high-performance, real-time Mermaid diagram previewer for IntelliJ-based IDEs (PHPStorm, WebStorm, etc.). This plugin provides a split-view editor: a text editor on the left and a live-rendering preview on the right.

## 2. Architectural Guidelines
- **Base Template:** Strictly follow the `intellij-platform-plugin-template` project structure.
- **Gradle Versioning:** Use the `intellij-platform-gradle-plugin` (version 2.x) with Kotlin DSL (`build.gradle.kts`).
- **Core Components:**
    - Use `TextEditorWithPreview` for the primary UI layout.
    - Use `JBCefBrowser` (Chromium Embedded Framework) for the preview pane. Do not use the native IDEA SVG viewer; JCEF is required for full Mermaid.js feature support (CSS-in-JS, text wrapping, interactivity).
    - Implement `FileEditorProvider` to handle `.mmd` and `.mermaid` files.

## 3. Implementation Details: JCEF Bridge
- **Communication:** Use `JBCefJSQuery` to pass text from the IDE's `Document` to the WebView.
- **Throttling:** Implement a `Throttler` or `Alarm` with a 300ms delay to prevent re-rendering on every single keystroke.
- **Frontend Logic:** - The browser loads a local `index.html` from resources.
    - The HTML must use `mermaid.render()` to generate SVGs dynamically.
    - Implement zoom and pan inside the WebView using CSS transforms or a JS library (e.g., d3-zoom).

## 4. Automation & Maintenance
- **Mermaid Updates:** There is a GitHub Action named `update-mermaid.yml` that runs weekly. It fetches the latest `mermaid.min.js` from `unpkg.com` and creates a PR if the version has changed.
- **CI/CD:** Ensure that every Pull Request and Release triggers the `build.yml` workflow to generate signed artifacts (`.zip`).
- **Dependency Management:** Use `gradle.properties` for versioning the IntelliJ Platform and other dependencies.

## 5. Coding Standards
- **UI Safety:** Always execute rendering or browser updates outside the main UI thread using `AppExecutorUtil`.
- **Resource Management:** Ensure `JBCefBrowser` and `JBCefJSQuery` are properly disposed of in the `FileEditor.dispose()` method to avoid memory leaks.
- **Theming:** Detect the current IDE theme (Darcula vs. Light) using `JBColor.isBright()` or `EditorColorsManager` and pass the corresponding theme value ('dark', 'default', 'neutral') to the `mermaid.initialize` call.
- **Language:** Default to Kotlin 1.9+. Use modern IntelliJ APIs; avoid deprecated methods from versions prior to 2023.x.

## 6. Goal Features
- **Real-Time Preview:** Synchronous scrolling or immediate updates.
- **Syntax Highlighting:** Provide basic lexer/parser for `.mmd` files for better editor visuals.
- **Export:** Implementation of an action button to "Save as SVG" by pulling the innerHTML from the JCEF browser.
- **Error Feedback:** Catch Mermaid syntax errors in the browser and display an error toast or overlay in the preview pane.

## 7. Interaction Rules
- When asked to generate code, provide full Kotlin classes including imports.
- Always include the relevant `plugin.xml` snippets for any new `extensionPoints` or `actions`.
- Prioritize scannable, well-commented code blocks.
