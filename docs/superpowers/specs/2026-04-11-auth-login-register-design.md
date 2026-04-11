# Auth: Login & Register Panel — Design Spec

## Overview

Login and registration screens for MagazynierUZ_Mobile. Uses TabLayout + ViewPager2 with two fragments (Login, Register). Local user storage via Android Room with SHA-256 hashed passwords. After successful login, user is redirected to MainActivity.

## Tech Stack

- Java (no Kotlin, no Compose)
- Android Room for local database
- ViewPager2 + TabLayout for tab navigation
- SHA-256 for password hashing

## Architecture

```
AuthActivity (TabLayout + ViewPager2)
├── LoginFragment
├── RegisterFragment
└── AuthPagerAdapter (FragmentStateAdapter)

MainActivity (placeholder after login)

Room Database
├── User (Entity)
├── UserDao (DAO)
└── AppDatabase (singleton)
```

## Database Schema (Room)

### Entity: User

| Field        | Type   | Constraints              |
|-------------|--------|--------------------------|
| id          | int    | PK, autoGenerate         |
| username    | String | unique, non-null         |
| passwordHash| String | non-null                 |

### DAO: UserDao

- `insertUser(User user)` — insert new user
- `findByUsername(String username)` — find user by username (for login and duplicate check)

### AppDatabase

- Singleton via `Room.databaseBuilder()`
- Version 1, single entity (User)

## UI Design (from Figma)

### Design Tokens

| Token          | Value    |
|---------------|----------|
| Input bg      | #D9D9D9  |
| Button bg     | #FDC700  |
| Text color    | #000000  |
| Background    | #FFFFFF  |
| Corner radius | 24dp     |
| Input height  | 61dp     |
| Font          | Inter Regular, 20sp |
| Side margin   | 34dp     |

### AuthActivity

- `TabLayout` at top with two tabs: "LOGIN" / "REGISTER"
- Tab indicator color: #FDC700 (yellow)
- Tab text color: black
- `ViewPager2` below, filling remaining screen space
- White background

### LoginFragment

- White background
- EditText "LOGIN" — bg #D9D9D9, cornerRadius 24dp, height 61dp, width match_parent with 34dp horizontal margin, Inter 20sp, hint text
- EditText "PASSWORD" — same style, inputType textPassword
- Button "LOGIN" — bg #FDC700, cornerRadius 24dp, black text Inter 20sp, positioned toward bottom

### RegisterFragment

- Same style as LoginFragment
- EditText "LOGIN"
- EditText "PASSWORD"
- EditText "CONFIRM PASSWORD"
- Button "REGISTER"

## Business Logic

### Registration Flow

1. Validate: all fields non-empty
2. Validate: password == confirm password
3. Check username not taken (`findByUsername`)
4. Hash password with SHA-256
5. Insert user via `insertUser()`
6. Toast "Rejestracja udana"
7. Auto-switch to Login tab

### Login Flow

1. Validate: all fields non-empty
2. `findByUsername()` — if null → Toast "Nie znaleziono użytkownika"
3. Hash entered password with SHA-256, compare with stored `passwordHash`
4. Match → Intent to MainActivity with extra "username", finish AuthActivity
5. No match → Toast "Nieprawidłowe hasło"

### MainActivity (placeholder)

- Simple layout with TextView "Witaj, [username]"
- Reads username from `getIntent().getStringExtra("username")`

## File Structure

```
com.example.magazynieruz_mobile/
├── data/
│   ├── AppDatabase.java
│   ├── User.java
│   └── UserDao.java
├── ui/
│   ├── AuthActivity.java
│   ├── AuthPagerAdapter.java
│   ├── LoginFragment.java
│   └── RegisterFragment.java
└── MainActivity.java

res/layout/
├── activity_auth.xml
├── activity_main.xml
├── fragment_login.xml
└── fragment_register.xml

res/drawable/
├── bg_input_field.xml    (rounded rect #D9D9D9)
└── bg_button_primary.xml (rounded rect #FDC700)
```

## Out of Scope

- Remote API / backend authentication
- Session management / tokens
- Password reset
- Email validation
- Remember me / auto-login