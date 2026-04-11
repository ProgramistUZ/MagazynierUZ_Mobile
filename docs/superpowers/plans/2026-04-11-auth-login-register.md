# Auth: Login & Register Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Implement login and registration screens with local Room database, TabLayout navigation, and SHA-256 password hashing.

**Architecture:** Single AuthActivity hosts TabLayout + ViewPager2 with LoginFragment and RegisterFragment. Room database stores users with hashed passwords. Successful login redirects to placeholder MainActivity.

**Tech Stack:** Java 11, Android Room, ViewPager2, TabLayout (Material), SHA-256

---

## File Map

| File | Action | Responsibility |
|------|--------|---------------|
| `gradle/libs.versions.toml` | Modify | Add room and viewpager2 versions + libraries |
| `app/build.gradle.kts` | Modify | Add room, viewpager2 dependencies |
| `app/src/main/res/values/colors.xml` | Modify | Add app-specific colors |
| `app/src/main/res/values/themes.xml` | Modify | Update theme to NoActionBar |
| `app/src/main/res/values/strings.xml` | Modify | Add UI strings |
| `app/src/main/res/drawable/bg_input_field.xml` | Create | Rounded rect drawable for EditText (#D9D9D9, 24dp radius) |
| `app/src/main/res/drawable/bg_button_primary.xml` | Create | Rounded rect drawable for Button (#FDC700, 24dp radius) |
| `app/src/main/res/layout/activity_auth.xml` | Create | TabLayout + ViewPager2 |
| `app/src/main/res/layout/fragment_login.xml` | Create | Login form layout |
| `app/src/main/res/layout/fragment_register.xml` | Create | Register form layout |
| `app/src/main/res/layout/activity_main.xml` | Create | Placeholder welcome screen |
| `app/src/main/java/.../data/User.java` | Create | Room Entity |
| `app/src/main/java/.../data/UserDao.java` | Create | Room DAO interface |
| `app/src/main/java/.../data/AppDatabase.java` | Create | Room database singleton |
| `app/src/main/java/.../ui/AuthActivity.java` | Create | Hosts TabLayout + ViewPager2 |
| `app/src/main/java/.../ui/AuthPagerAdapter.java` | Create | FragmentStateAdapter for ViewPager2 |
| `app/src/main/java/.../ui/LoginFragment.java` | Create | Login form logic |
| `app/src/main/java/.../ui/RegisterFragment.java` | Create | Register form logic |
| `app/src/main/java/.../MainActivity.java` | Create | Post-login placeholder |
| `app/src/main/AndroidManifest.xml` | Modify | Declare activities, set launcher |
| `app/src/test/java/.../data/PasswordHashTest.java` | Create | Unit test for SHA-256 hashing |

All Java paths use base: `app/src/main/java/com/example/magazynieruz_mobile/`

---

### Task 1: Add Dependencies

**Files:**
- Modify: `gradle/libs.versions.toml`
- Modify: `app/build.gradle.kts`

- [ ] **Step 1: Add Room and ViewPager2 to version catalog**

In `gradle/libs.versions.toml`, add the new versions and libraries:

```toml
[versions]
agp = "9.1.0"
junit = "4.13.2"
junitVersion = "1.1.5"
espressoCore = "3.5.1"
appcompat = "1.6.1"
material = "1.10.0"
room = "2.6.1"
viewpager2 = "1.1.0"

[libraries]
junit = { group = "junit", name = "junit", version.ref = "junit" }
ext-junit = { group = "androidx.test.ext", name = "junit", version.ref = "junitVersion" }
espresso-core = { group = "androidx.test.espresso", name = "espresso-core", version.ref = "espressoCore" }
appcompat = { group = "androidx.appcompat", name = "appcompat", version.ref = "appcompat" }
material = { group = "com.google.android.material", name = "material", version.ref = "material" }
room-runtime = { group = "androidx.room", name = "room-runtime", version.ref = "room" }
room-compiler = { group = "androidx.room", name = "room-compiler", version.ref = "room" }
viewpager2 = { group = "androidx.viewpager2", name = "viewpager2", version.ref = "viewpager2" }

[plugins]
android-application = { id = "com.android.application", version.ref = "agp" }
```

- [ ] **Step 2: Add dependencies to app build.gradle.kts**

In `app/build.gradle.kts`, add to the `dependencies` block:

```kotlin
dependencies {
    implementation(libs.appcompat)
    implementation(libs.material)
    implementation(libs.room.runtime)
    annotationProcessor(libs.room.compiler)
    implementation(libs.viewpager2)
    testImplementation(libs.junit)
    androidTestImplementation(libs.ext.junit)
    androidTestImplementation(libs.espresso.core)
}
```

- [ ] **Step 3: Sync and verify**

Run: `./gradlew app:dependencies --configuration releaseRuntimeClasspath | grep -E "room|viewpager2"`

Expected: lines showing `androidx.room:room-runtime:2.6.1` and `androidx.viewpager2:viewpager2:1.1.0`

- [ ] **Step 4: Commit**

```bash
git add gradle/libs.versions.toml app/build.gradle.kts
git commit -m "Add Room and ViewPager2 dependencies"
```

---

### Task 2: Room Database Layer

**Files:**
- Create: `app/src/main/java/com/example/magazynieruz_mobile/data/User.java`
- Create: `app/src/main/java/com/example/magazynieruz_mobile/data/UserDao.java`
- Create: `app/src/main/java/com/example/magazynieruz_mobile/data/AppDatabase.java`
- Create: `app/src/test/java/com/example/magazynieruz_mobile/data/PasswordHashTest.java`

- [ ] **Step 1: Write password hashing unit test**

Create `app/src/test/java/com/example/magazynieruz_mobile/data/PasswordHashTest.java`:

```java
package com.example.magazynieruz_mobile.data;

import org.junit.Test;
import static org.junit.Assert.*;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;

public class PasswordHashTest {

    private String hashPassword(String password) throws Exception {
        MessageDigest digest = MessageDigest.getInstance("SHA-256");
        byte[] hash = digest.digest(password.getBytes(StandardCharsets.UTF_8));
        StringBuilder hexString = new StringBuilder();
        for (byte b : hash) {
            String hex = Integer.toHexString(0xff & b);
            if (hex.length() == 1) hexString.append('0');
            hexString.append(hex);
        }
        return hexString.toString();
    }

    @Test
    public void hashPassword_sameInput_sameOutput() throws Exception {
        String hash1 = hashPassword("testPassword123");
        String hash2 = hashPassword("testPassword123");
        assertEquals(hash1, hash2);
    }

    @Test
    public void hashPassword_differentInput_differentOutput() throws Exception {
        String hash1 = hashPassword("password1");
        String hash2 = hashPassword("password2");
        assertNotEquals(hash1, hash2);
    }

    @Test
    public void hashPassword_returns64CharHex() throws Exception {
        String hash = hashPassword("anything");
        assertEquals(64, hash.length());
        assertTrue(hash.matches("[0-9a-f]+"));
    }
}
```

- [ ] **Step 2: Run test to verify it passes**

Run: `./gradlew test --tests "com.example.magazynieruz_mobile.data.PasswordHashTest"`

Expected: 3 tests PASS

- [ ] **Step 3: Create User entity**

Create `app/src/main/java/com/example/magazynieruz_mobile/data/User.java`:

```java
package com.example.magazynieruz_mobile.data;

import androidx.room.Entity;
import androidx.room.Index;
import androidx.room.PrimaryKey;

@Entity(tableName = "users", indices = {@Index(value = "username", unique = true)})
public class User {

    @PrimaryKey(autoGenerate = true)
    public int id;

    public String username;

    public String passwordHash;
}
```

- [ ] **Step 4: Create UserDao**

Create `app/src/main/java/com/example/magazynieruz_mobile/data/UserDao.java`:

```java
package com.example.magazynieruz_mobile.data;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;

@Dao
public interface UserDao {

    @Insert
    void insertUser(User user);

    @Query("SELECT * FROM users WHERE username = :username LIMIT 1")
    User findByUsername(String username);
}
```

- [ ] **Step 5: Create AppDatabase**

Create `app/src/main/java/com/example/magazynieruz_mobile/data/AppDatabase.java`:

```java
package com.example.magazynieruz_mobile.data;

import android.content.Context;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;

@Database(entities = {User.class}, version = 1)
public abstract class AppDatabase extends RoomDatabase {

    private static volatile AppDatabase INSTANCE;

    public abstract UserDao userDao();

    public static AppDatabase getInstance(Context context) {
        if (INSTANCE == null) {
            synchronized (AppDatabase.class) {
                if (INSTANCE == null) {
                    INSTANCE = Room.databaseBuilder(
                            context.getApplicationContext(),
                            AppDatabase.class,
                            "magazynier_db"
                    ).allowMainThreadQueries().build();
                }
            }
        }
        return INSTANCE;
    }
}
```

Note: `allowMainThreadQueries()` is used for simplicity in this prototype. For production, use background threads.

- [ ] **Step 6: Verify compilation**

Run: `./gradlew assembleDebug`

Expected: BUILD SUCCESSFUL

- [ ] **Step 7: Commit**

```bash
git add app/src/main/java/com/example/magazynieruz_mobile/data/ app/src/test/java/com/example/magazynieruz_mobile/data/
git commit -m "Add Room database layer with User entity and DAO"
```

---

### Task 3: Resources — Colors, Strings, Drawables, Theme

**Files:**
- Modify: `app/src/main/res/values/colors.xml`
- Modify: `app/src/main/res/values/strings.xml`
- Modify: `app/src/main/res/values/themes.xml`
- Create: `app/src/main/res/drawable/bg_input_field.xml`
- Create: `app/src/main/res/drawable/bg_button_primary.xml`

- [ ] **Step 1: Add app colors**

In `app/src/main/res/values/colors.xml`, add inside `<resources>`:

```xml
<color name="input_background">#FFD9D9D9</color>
<color name="button_primary">#FFFDC700</color>
<color name="text_primary">#FF000000</color>
<color name="background_white">#FFFFFFFF</color>
<color name="tab_indicator">#FFFDC700</color>
```

- [ ] **Step 2: Add strings**

Read current `strings.xml` first. Then add these strings:

```xml
<string name="tab_login">LOGIN</string>
<string name="tab_register">REGISTER</string>
<string name="hint_login">LOGIN</string>
<string name="hint_password">PASSWORD</string>
<string name="hint_confirm_password">CONFIRM PASSWORD</string>
<string name="btn_login">LOGIN</string>
<string name="btn_register">REGISTER</string>
<string name="welcome_message">Witaj, %1$s</string>
<string name="error_empty_fields">Wypełnij wszystkie pola</string>
<string name="error_passwords_mismatch">Hasła się nie zgadzają</string>
<string name="error_username_taken">Nazwa użytkownika jest zajęta</string>
<string name="error_user_not_found">Nie znaleziono użytkownika</string>
<string name="error_wrong_password">Nieprawidłowe hasło</string>
<string name="success_registration">Rejestracja udana</string>
```

- [ ] **Step 3: Update theme to NoActionBar**

Replace the theme parent in `app/src/main/res/values/themes.xml`:

```xml
<resources xmlns:tools="http://schemas.android.com/tools">
    <style name="Theme.MagazynierUZ_Mobile" parent="Theme.MaterialComponents.DayNight.NoActionBar">
        <item name="colorPrimary">@color/button_primary</item>
        <item name="colorPrimaryVariant">@color/button_primary</item>
        <item name="colorOnPrimary">@color/black</item>
        <item name="colorSecondary">@color/teal_200</item>
        <item name="colorSecondaryVariant">@color/teal_700</item>
        <item name="colorOnSecondary">@color/black</item>
        <item name="android:statusBarColor">@color/button_primary</item>
    </style>
</resources>
```

- [ ] **Step 4: Create input field drawable**

Create `app/src/main/res/drawable/bg_input_field.xml`:

```xml
<?xml version="1.0" encoding="utf-8"?>
<shape xmlns:android="http://schemas.android.com/apk/res/android"
    android:shape="rectangle">
    <solid android:color="@color/input_background" />
    <corners android:radius="24dp" />
</shape>
```

- [ ] **Step 5: Create button drawable**

Create `app/src/main/res/drawable/bg_button_primary.xml`:

```xml
<?xml version="1.0" encoding="utf-8"?>
<shape xmlns:android="http://schemas.android.com/apk/res/android"
    android:shape="rectangle">
    <solid android:color="@color/button_primary" />
    <corners android:radius="24dp" />
</shape>
```

- [ ] **Step 6: Commit**

```bash
git add app/src/main/res/
git commit -m "Add auth UI resources: colors, strings, drawables, theme"
```

---

### Task 4: Layouts

**Files:**
- Create: `app/src/main/res/layout/activity_auth.xml`
- Create: `app/src/main/res/layout/fragment_login.xml`
- Create: `app/src/main/res/layout/fragment_register.xml`
- Create: `app/src/main/res/layout/activity_main.xml`

- [ ] **Step 1: Create activity_auth.xml**

Create `app/src/main/res/layout/activity_auth.xml`:

```xml
<?xml version="1.0" encoding="utf-8"?>
<LinearLayout xmlns:android="http://schemas.android.com/apk/res/android"
    xmlns:app="http://schemas.android.com/apk/res-auto"
    android:layout_width="match_parent"
    android:layout_height="match_parent"
    android:orientation="vertical"
    android:background="@color/background_white">

    <com.google.android.material.tabs.TabLayout
        android:id="@+id/tabLayout"
        android:layout_width="match_parent"
        android:layout_height="wrap_content"
        android:background="@color/background_white"
        app:tabIndicatorColor="@color/tab_indicator"
        app:tabIndicatorHeight="3dp"
        app:tabSelectedTextColor="@color/text_primary"
        app:tabTextColor="@color/text_primary"
        app:tabTextAppearance="@style/TabTextStyle" />

    <androidx.viewpager2.widget.ViewPager2
        android:id="@+id/viewPager"
        android:layout_width="match_parent"
        android:layout_height="0dp"
        android:layout_weight="1" />

</LinearLayout>
```

Add tab text style to `themes.xml` inside `<resources>` (after the theme):

```xml
<style name="TabTextStyle" parent="TextAppearance.MaterialComponents.Tab">
    <item name="android:textSize">16sp</item>
    <item name="android:textAllCaps">true</item>
    <item name="fontFamily">sans-serif</item>
</style>
```

- [ ] **Step 2: Create fragment_login.xml**

Create `app/src/main/res/layout/fragment_login.xml`:

```xml
<?xml version="1.0" encoding="utf-8"?>
<RelativeLayout xmlns:android="http://schemas.android.com/apk/res/android"
    android:layout_width="match_parent"
    android:layout_height="match_parent"
    android:background="@color/background_white"
    android:padding="34dp">

    <EditText
        android:id="@+id/editLogin"
        android:layout_width="match_parent"
        android:layout_height="61dp"
        android:layout_marginTop="60dp"
        android:background="@drawable/bg_input_field"
        android:hint="@string/hint_login"
        android:inputType="text"
        android:paddingStart="20dp"
        android:paddingEnd="20dp"
        android:textColor="@color/text_primary"
        android:textColorHint="@color/text_primary"
        android:textSize="20sp" />

    <EditText
        android:id="@+id/editPassword"
        android:layout_width="match_parent"
        android:layout_height="61dp"
        android:layout_below="@id/editLogin"
        android:layout_marginTop="44dp"
        android:background="@drawable/bg_input_field"
        android:hint="@string/hint_password"
        android:inputType="textPassword"
        android:paddingStart="20dp"
        android:paddingEnd="20dp"
        android:textColor="@color/text_primary"
        android:textColorHint="@color/text_primary"
        android:textSize="20sp" />

    <Button
        android:id="@+id/btnLogin"
        android:layout_width="match_parent"
        android:layout_height="61dp"
        android:layout_alignParentBottom="true"
        android:layout_marginBottom="40dp"
        android:background="@drawable/bg_button_primary"
        android:text="@string/btn_login"
        android:textColor="@color/text_primary"
        android:textSize="20sp"
        android:textAllCaps="true" />

</RelativeLayout>
```

- [ ] **Step 3: Create fragment_register.xml**

Create `app/src/main/res/layout/fragment_register.xml`:

```xml
<?xml version="1.0" encoding="utf-8"?>
<RelativeLayout xmlns:android="http://schemas.android.com/apk/res/android"
    android:layout_width="match_parent"
    android:layout_height="match_parent"
    android:background="@color/background_white"
    android:padding="34dp">

    <EditText
        android:id="@+id/editLogin"
        android:layout_width="match_parent"
        android:layout_height="61dp"
        android:layout_marginTop="60dp"
        android:background="@drawable/bg_input_field"
        android:hint="@string/hint_login"
        android:inputType="text"
        android:paddingStart="20dp"
        android:paddingEnd="20dp"
        android:textColor="@color/text_primary"
        android:textColorHint="@color/text_primary"
        android:textSize="20sp" />

    <EditText
        android:id="@+id/editPassword"
        android:layout_width="match_parent"
        android:layout_height="61dp"
        android:layout_below="@id/editLogin"
        android:layout_marginTop="44dp"
        android:background="@drawable/bg_input_field"
        android:hint="@string/hint_password"
        android:inputType="textPassword"
        android:paddingStart="20dp"
        android:paddingEnd="20dp"
        android:textColor="@color/text_primary"
        android:textColorHint="@color/text_primary"
        android:textSize="20sp" />

    <EditText
        android:id="@+id/editConfirmPassword"
        android:layout_width="match_parent"
        android:layout_height="61dp"
        android:layout_below="@id/editPassword"
        android:layout_marginTop="44dp"
        android:background="@drawable/bg_input_field"
        android:hint="@string/hint_confirm_password"
        android:inputType="textPassword"
        android:paddingStart="20dp"
        android:paddingEnd="20dp"
        android:textColor="@color/text_primary"
        android:textColorHint="@color/text_primary"
        android:textSize="20sp" />

    <Button
        android:id="@+id/btnRegister"
        android:layout_width="match_parent"
        android:layout_height="61dp"
        android:layout_alignParentBottom="true"
        android:layout_marginBottom="40dp"
        android:background="@drawable/bg_button_primary"
        android:text="@string/btn_register"
        android:textColor="@color/text_primary"
        android:textSize="20sp"
        android:textAllCaps="true" />

</RelativeLayout>
```

- [ ] **Step 4: Create activity_main.xml**

Create `app/src/main/res/layout/activity_main.xml`:

```xml
<?xml version="1.0" encoding="utf-8"?>
<RelativeLayout xmlns:android="http://schemas.android.com/apk/res/android"
    android:layout_width="match_parent"
    android:layout_height="match_parent"
    android:background="@color/background_white">

    <TextView
        android:id="@+id/textWelcome"
        android:layout_width="wrap_content"
        android:layout_height="wrap_content"
        android:layout_centerInParent="true"
        android:textColor="@color/text_primary"
        android:textSize="24sp" />

</RelativeLayout>
```

- [ ] **Step 5: Commit**

```bash
git add app/src/main/res/layout/ app/src/main/res/values/themes.xml
git commit -m "Add auth and main activity layouts"
```

---

### Task 5: AuthActivity + Pager Adapter

**Files:**
- Create: `app/src/main/java/com/example/magazynieruz_mobile/ui/AuthPagerAdapter.java`
- Create: `app/src/main/java/com/example/magazynieruz_mobile/ui/AuthActivity.java`

- [ ] **Step 1: Create AuthPagerAdapter**

Create `app/src/main/java/com/example/magazynieruz_mobile/ui/AuthPagerAdapter.java`:

```java
package com.example.magazynieruz_mobile.ui;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager2.adapter.FragmentStateAdapter;

public class AuthPagerAdapter extends FragmentStateAdapter {

    public AuthPagerAdapter(@NonNull FragmentActivity activity) {
        super(activity);
    }

    @NonNull
    @Override
    public Fragment createFragment(int position) {
        if (position == 1) {
            return new RegisterFragment();
        }
        return new LoginFragment();
    }

    @Override
    public int getItemCount() {
        return 2;
    }
}
```

- [ ] **Step 2: Create AuthActivity**

Create `app/src/main/java/com/example/magazynieruz_mobile/ui/AuthActivity.java`:

```java
package com.example.magazynieruz_mobile.ui;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.viewpager2.widget.ViewPager2;

import com.example.magazynieruz_mobile.R;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;

public class AuthActivity extends AppCompatActivity {

    private final String[] tabTitles = new String[]{"LOGIN", "REGISTER"};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_auth);

        ViewPager2 viewPager = findViewById(R.id.viewPager);
        TabLayout tabLayout = findViewById(R.id.tabLayout);

        viewPager.setAdapter(new AuthPagerAdapter(this));

        new TabLayoutMediator(tabLayout, viewPager,
                (tab, position) -> tab.setText(tabTitles[position])
        ).attach();
    }
}
```

- [ ] **Step 3: Commit**

```bash
git add app/src/main/java/com/example/magazynieruz_mobile/ui/AuthPagerAdapter.java app/src/main/java/com/example/magazynieruz_mobile/ui/AuthActivity.java
git commit -m "Add AuthActivity with TabLayout and ViewPager2"
```

---

### Task 6: LoginFragment

**Files:**
- Create: `app/src/main/java/com/example/magazynieruz_mobile/ui/LoginFragment.java`

- [ ] **Step 1: Create LoginFragment**

Create `app/src/main/java/com/example/magazynieruz_mobile/ui/LoginFragment.java`:

```java
package com.example.magazynieruz_mobile.ui;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.magazynieruz_mobile.MainActivity;
import com.example.magazynieruz_mobile.R;
import com.example.magazynieruz_mobile.data.AppDatabase;
import com.example.magazynieruz_mobile.data.User;
import com.example.magazynieruz_mobile.data.UserDao;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class LoginFragment extends Fragment {

    private EditText editLogin;
    private EditText editPassword;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_login, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        editLogin = view.findViewById(R.id.editLogin);
        editPassword = view.findViewById(R.id.editPassword);
        Button btnLogin = view.findViewById(R.id.btnLogin);

        btnLogin.setOnClickListener(v -> attemptLogin());
    }

    private void attemptLogin() {
        String username = editLogin.getText().toString().trim();
        String password = editPassword.getText().toString().trim();

        if (username.isEmpty() || password.isEmpty()) {
            Toast.makeText(getContext(), R.string.error_empty_fields, Toast.LENGTH_SHORT).show();
            return;
        }

        UserDao userDao = AppDatabase.getInstance(requireContext()).userDao();
        User user = userDao.findByUsername(username);

        if (user == null) {
            Toast.makeText(getContext(), R.string.error_user_not_found, Toast.LENGTH_SHORT).show();
            return;
        }

        String hashedPassword = hashPassword(password);
        if (!user.passwordHash.equals(hashedPassword)) {
            Toast.makeText(getContext(), R.string.error_wrong_password, Toast.LENGTH_SHORT).show();
            return;
        }

        Intent intent = new Intent(getContext(), MainActivity.class);
        intent.putExtra("username", username);
        startActivity(intent);
        requireActivity().finish();
    }

    private String hashPassword(String password) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(password.getBytes(StandardCharsets.UTF_8));
            StringBuilder hexString = new StringBuilder();
            for (byte b : hash) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) hexString.append('0');
                hexString.append(hex);
            }
            return hexString.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
    }
}
```

- [ ] **Step 2: Commit**

```bash
git add app/src/main/java/com/example/magazynieruz_mobile/ui/LoginFragment.java
git commit -m "Add LoginFragment with authentication logic"
```

---

### Task 7: RegisterFragment

**Files:**
- Create: `app/src/main/java/com/example/magazynieruz_mobile/ui/RegisterFragment.java`

- [ ] **Step 1: Create RegisterFragment**

Create `app/src/main/java/com/example/magazynieruz_mobile/ui/RegisterFragment.java`:

```java
package com.example.magazynieruz_mobile.ui;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.viewpager2.widget.ViewPager2;

import com.example.magazynieruz_mobile.R;
import com.example.magazynieruz_mobile.data.AppDatabase;
import com.example.magazynieruz_mobile.data.User;
import com.example.magazynieruz_mobile.data.UserDao;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class RegisterFragment extends Fragment {

    private EditText editLogin;
    private EditText editPassword;
    private EditText editConfirmPassword;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_register, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        editLogin = view.findViewById(R.id.editLogin);
        editPassword = view.findViewById(R.id.editPassword);
        editConfirmPassword = view.findViewById(R.id.editConfirmPassword);
        Button btnRegister = view.findViewById(R.id.btnRegister);

        btnRegister.setOnClickListener(v -> attemptRegister());
    }

    private void attemptRegister() {
        String username = editLogin.getText().toString().trim();
        String password = editPassword.getText().toString().trim();
        String confirmPassword = editConfirmPassword.getText().toString().trim();

        if (username.isEmpty() || password.isEmpty() || confirmPassword.isEmpty()) {
            Toast.makeText(getContext(), R.string.error_empty_fields, Toast.LENGTH_SHORT).show();
            return;
        }

        if (!password.equals(confirmPassword)) {
            Toast.makeText(getContext(), R.string.error_passwords_mismatch, Toast.LENGTH_SHORT).show();
            return;
        }

        UserDao userDao = AppDatabase.getInstance(requireContext()).userDao();

        if (userDao.findByUsername(username) != null) {
            Toast.makeText(getContext(), R.string.error_username_taken, Toast.LENGTH_SHORT).show();
            return;
        }

        User user = new User();
        user.username = username;
        user.passwordHash = hashPassword(password);
        userDao.insertUser(user);

        Toast.makeText(getContext(), R.string.success_registration, Toast.LENGTH_SHORT).show();

        ViewPager2 viewPager = requireActivity().findViewById(R.id.viewPager);
        viewPager.setCurrentItem(0);
    }

    private String hashPassword(String password) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(password.getBytes(StandardCharsets.UTF_8));
            StringBuilder hexString = new StringBuilder();
            for (byte b : hash) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) hexString.append('0');
                hexString.append(hex);
            }
            return hexString.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
    }
}
```

- [ ] **Step 2: Commit**

```bash
git add app/src/main/java/com/example/magazynieruz_mobile/ui/RegisterFragment.java
git commit -m "Add RegisterFragment with registration logic"
```

---

### Task 8: MainActivity + AndroidManifest

**Files:**
- Create: `app/src/main/java/com/example/magazynieruz_mobile/MainActivity.java`
- Modify: `app/src/main/AndroidManifest.xml`

- [ ] **Step 1: Create MainActivity**

Create `app/src/main/java/com/example/magazynieruz_mobile/MainActivity.java`:

```java
package com.example.magazynieruz_mobile;

import android.os.Bundle;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        TextView textWelcome = findViewById(R.id.textWelcome);
        String username = getIntent().getStringExtra("username");
        textWelcome.setText(getString(R.string.welcome_message, username));
    }
}
```

- [ ] **Step 2: Update AndroidManifest.xml**

Replace the full content of `app/src/main/AndroidManifest.xml`:

```xml
<?xml version="1.0" encoding="utf-8"?>
<manifest xmlns:android="http://schemas.android.com/apk/res/android"
    xmlns:tools="http://schemas.android.com/tools">

    <application
        android:allowBackup="true"
        android:dataExtractionRules="@xml/data_extraction_rules"
        android:fullBackupContent="@xml/backup_rules"
        android:icon="@mipmap/ic_launcher"
        android:label="@string/app_name"
        android:roundIcon="@mipmap/ic_launcher_round"
        android:supportsRtl="true"
        android:theme="@style/Theme.MagazynierUZ_Mobile">

        <activity
            android:name=".ui.AuthActivity"
            android:exported="true">
            <intent-filter>
                <action android:name="android.intent.action.MAIN" />
                <category android:name="android.intent.category.LAUNCHER" />
            </intent-filter>
        </activity>

        <activity android:name=".MainActivity" />

    </application>

</manifest>
```

- [ ] **Step 3: Build and verify**

Run: `./gradlew assembleDebug`

Expected: BUILD SUCCESSFUL

- [ ] **Step 4: Commit**

```bash
git add app/src/main/java/com/example/magazynieruz_mobile/MainActivity.java app/src/main/AndroidManifest.xml
git commit -m "Add MainActivity placeholder and wire up AndroidManifest"
```

---

### Task 9: Final Verification

- [ ] **Step 1: Run all unit tests**

Run: `./gradlew test`

Expected: All tests PASS

- [ ] **Step 2: Build full APK**

Run: `./gradlew assembleDebug`

Expected: BUILD SUCCESSFUL, APK at `app/build/outputs/apk/debug/app-debug.apk`

- [ ] **Step 3: Manual smoke test on device/emulator**

1. Install and launch — AuthActivity opens with LOGIN / REGISTER tabs
2. Switch between tabs via tap and swipe
3. Register a new user — toast "Rejestracja udana", switches to Login tab
4. Login with that user — redirects to MainActivity showing "Witaj, [username]"
5. Try wrong password — toast "Nieprawidłowe hasło"
6. Try non-existent user — toast "Nie znaleziono użytkownika"
7. Try empty fields — toast "Wypełnij wszystkie pola"
8. Try mismatched passwords in register — toast "Hasła się nie zgadzają"
