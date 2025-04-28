# CHECKPOINTS.md

## Phase 1) Setup and Window Creation
- [X] 1.1 Initialize Java Project
    - [X] 1.1.1 Create `Main` class extending `Application`
    - [X] 1.1.2 Set up `start(Stage primaryStage)` and launch
- [X] 1.2 Basic Window Layout
    - [X] 1.2.1 Create root pane (e.g. `BorderPane` or `AnchorPane`)
    - [X] 1.2.2 Reserve left area for “Script Viewer” and right for “Controls”
    - [X] 1.2.3 Apply minimal styling and window title

## Phase 2) Script Display & Clickable Text
- [X] 2.1 Script Loading
    - [X] 2.1.1 Load script text from resource (file/embedded string)
    - [X] 2.1.2 Define simple markup syntax for links (e.g. `[[Ivor Dotsk]]`)
- [X] 2.2 Script Viewer Implementation
    - [X] 2.2.1 Use `TextFlow` or `WebView` to render non-editable text
    - [X] 2.2.2 Apply basic CSS for readability (fonts, spacing)
- [ ] 2.3 Clickable & Hoverable Words
    - [ ] 2.3.1 Build a map of keywords → metadata (character sheet, spell info)
    - [ ] 2.3.2 Attach `Tooltip` on hover to show summary
    - [ ] 2.3.3 Attach mouse-click handler to open detailed pop-up (`Dialog`/new `Stage`)

## Phase 3) Audio System Integration
- [ ] 3.1 MediaPlayer Setup
    - [ ] 3.1.1 Instantiate a `MediaPlayer` for background **music**
    - [ ] 3.1.2 Instantiate a separate `MediaPlayer` (or `AudioClip`) for **SFX**
- [ ] 3.2 Audio Controls UI
    - [ ] 3.2.1 Add Play, Pause, Fade Out buttons for music
    - [ ] 3.2.2 Add Volume slider for music channel
    - [ ] 3.2.3 Add buttons/list for triggering SFX
- [ ] 3.3 Playback Logic
    - [ ] 3.3.1 Hook up Play/Pause/Fade Out actions
    - [ ] 3.3.2 Ensure SFX playback layers over music without stopping it
    - [ ] 3.3.3 Handle media end-of-stream (loop or stop)

## Phase 4) Section Manager & Script–Audio Mapping
- [ ] 4.1 Section Identification
    - [ ] 4.1.1 Define how script is broken into “sections” (scene headers, markers)
    - [ ] 4.1.2 Visually highlight current section in the Script Viewer
- [ ] 4.2 Assignment UI
    - [ ] 4.2.1 Provide UI controls to assign a **song** or **SFX** to each section
    - [ ] 4.2.2 Store assignments in an in-memory map (e.g. `Map<SectionID, AudioClip>`)
- [ ] 4.3 Auto-Triggering
    - [ ] 4.3.1 On section change, auto-play assigned music
    - [ ] 4.3.2 Trigger assigned SFX at section start or via explicit button

## Phase 5) Data Persistence & Future Extensions
- [ ] 5.1 Save/Load Configuration
    - [ ] 5.1.1 Export section–audio mapping to JSON or properties file
    - [ ] 5.1.2 Load saved mapping on startup
- [ ] 5.2 Character/Spell Database (Optional)
    - [ ] 5.2.1 Define lightweight SQLite schema for characters/spells
    - [ ] 5.2.2 Add CRUD UI to edit database entries
- [ ] 5.3 Nice-to-Haves
    - [ ] 5.3.1 Searchable/Foldable script viewer
    - [ ] 5.3.2 Keyboard shortcuts for audio triggers
    - [ ] 5.3.3 Export session notes or PDF reports


# Stretch Goals
- YAML support
- 