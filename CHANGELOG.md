<!-- Keep a Changelog guide -> https://keepachangelog.com -->

# Mermaid Preview Changelog

## [Unreleased]
### Added
- Mermaid file type registration for .mmd and .mermaid extensions
- Split-view editor with text editor on left and live preview on right
- Real-time preview using JBCefBrowser (Chromium Embedded Framework)
- Throttled document updates (300ms) for smooth editing experience
- Automatic IDE theme detection (Light/Dark mode integration)
- Interactive preview with pan and zoom controls
- Syntax error feedback with detailed error messages
- Support for all Mermaid diagram types
- Fallback to text-only editor when JCEF is not supported
- Basic Mermaid language support with syntax highlighter framework

### Fixed
- Fixed critical crash when opening Mermaid files (AsyncFileEditorProvider implementation)
- Plugin name changed from "idea-mermaid-preview" to "Mermaid Preview" to comply with JetBrains guidelines

### Removed
- Template demo files (MyBundle.kt, MyBundle.properties)
- Unused resource-bundle configuration
