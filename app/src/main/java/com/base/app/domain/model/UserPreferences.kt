package com.base.app.domain.model

data class UserPreferences(
    val theme: ThemeMode = ThemeMode.Dark,
)

enum class ThemeMode { Dark, Light }
