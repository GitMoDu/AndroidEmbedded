# AndroidEmbedded

Collection of utilities for Android apps to interact with embedded devices.

This library acts primarily as an Android-side integration layer for embedded-oriented libraries and related app components, with supporting utilities for transport, storage, and device interaction.

# Embedded integrations

## Uart Interface

Embedded Library: https://github.com/GitMoDu/UartInterface

Android integration utilities for serial communication with embedded targets.

- Interface ViewModel with configurable messenger parameters such as baud rate and keys
- Hardware serial (UART) connection management
- Realtime-compatible asynchronous receive handling
- Async request/reply helpers with lambda-based callbacks

## VirtualPad

Embedded Library: https://github.com/GitMoDu/VirtualPad

Android-side components for virtual control mapping.

- Android native controller to virtual map mapper
- Scheduled task updater utilities

## Inertia

Embedded Library: https://github.com/GitMoDu/Inertia

Android-side support for integrating Inertia-based embedded systems.

- Inertia component models and payload surface abstractions
- Structured log tags and formatters for embedded events
- Android helpers for parsing, presenting, and working with Inertia component data
- Support code for storage, task profiling, AHRS, power train, and related embedded domains

# Android-side support components

These components support the embedded integrations above, but are not all standalone embedded libraries themselves.

## BLE serial support

Additional Android BLE transport support for serial-style device communication.

- BLE scanning and device discovery
- BLE connection dialog and ViewModel support
- BLE serial manager integrations
- Nordic BLE-based transport helpers

## Cloud storage support

Android cloud integrations for moving device data, logs, or exported files.

- Google Drive support
- OneDrive support
- Shared cloud storage provider abstraction for file operations

## Secure local storage

Utilities for storing app/device-related data securely on Android.

- Tink-backed SharedPreferences implementation
- Android secure storage helper components

## Haptics and Android utilities

General Android helper components used by device-facing applications.

- Haptic engine utilities
- View and UI extension helpers
- Shared Android support code

# Module

This repository currently exposes a single Android library module:

- `:library`

# Platform

- `minSdk 28`
- `compileSdk 36`
- Java 11 / Kotlin JVM 11

# Notes

Most embedded-specific functionality in this repository is designed to complement or integrate with external embedded libraries rather than replace them directly. The Android module provides the application-side transport, UI, storage, and integration glue needed to use those systems in Android apps.
