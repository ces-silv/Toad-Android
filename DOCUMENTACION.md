# Documentación Técnica — Toad App
## Guía completa de todos los archivos creados y modificados

> Este documento explica línea por línea cada archivo que fue agregado o modificado al proyecto base de Android. Está pensado como material de clase para entender el **patrón MVVM**, la **comunicación con backend**, y el **diseño de UI** en Jetpack Compose.

---

## Estructura general del proyecto

```
app/src/main/
├── java/org/ckdk/toad_app/
│   ├── MainActivity.kt                        ← MODIFICADO
│   ├── data/
│   │   ├── model/
│   │   │   ├── User.kt                        ← NUEVO
│   │   │   └── LoginResult.kt                 ← NUEVO
│   │   └── network/
│   │       ├── model/
│   │       │   ├── LoginRequest.kt            ← NUEVO
│   │       │   └── LoginResponse.kt           ← NUEVO
│   │       ├── AuthService.kt                 ← NUEVO
│   │       └── BackendOrchestrator.kt         ← NUEVO
│   └── ui/
│       ├── login/
│       │   ├── LoginViewModel.kt              ← NUEVO
│       │   └── LoginScreen.kt                 ← NUEVO
│       ├── main/
│       │   └── MainScreen.kt                  ← NUEVO
│       └── theme/
│           ├── Color.kt                       ← MODIFICADO
│           ├── Theme.kt                       ← MODIFICADO
│           └── Type.kt                        ← MODIFICADO
└── res/
    └── font/                                  ← MODIFICADO (fuentes locales)
        ├── poppins_regular.ttf
        ├── poppins_medium.ttf
        ├── poppins_semibold.ttf
        ├── poppins_bold.ttf
        ├── inter_variablefont_opsz_wght.ttf
        └── inter_italic_variablefont_opsz_wght.ttf
```

---

## ¿Qué es el patrón MVVM?

MVVM significa **Model — View — ViewModel**. Es una forma de organizar el código para separar responsabilidades:

| Capa | Responsabilidad | Archivos en este proyecto |
|------|----------------|--------------------------|
| **Model** | Datos y lógica de negocio | `User`, `LoginResult`, `LoginRequest`, `LoginResponse`, `BackendOrchestrator` |
| **ViewModel** | Estado de la UI, valida datos, llama al Model | `LoginViewModel` |
| **View** | Dibuja la pantalla y reacciona al estado | `LoginScreen`, `MainScreen`, `MainActivity` |

La UI **nunca** habla directamente con el backend. Siempre pasa por el ViewModel, que a su vez usa el Orquestador.

```
LoginScreen  →  LoginViewModel  →  BackendOrchestrator  →  Backend HTTP
    ↑                  |
    └──── uiState ─────┘
```

---

## 1. Configuración de Gradle

### `gradle/libs.versions.toml` — Catálogo de versiones

Este archivo es un **catálogo centralizado** donde se declaran todas las librerías del proyecto. En lugar de escribir la versión en cada módulo, se define una sola vez aquí.

```toml
[versions]
retrofit = "2.11.0"   # versión de Retrofit (cliente HTTP)
okhttp = "4.12.0"     # versión de OkHttp (motor de red bajo Retrofit)
```

Las versiones se referencian por nombre en la sección `[libraries]`:

```toml
[libraries]
# Retrofit — librería para hacer peticiones HTTP de forma declarativa
retrofit = { group = "com.squareup.retrofit2", name = "retrofit", version.ref = "retrofit" }

# Converter Gson — convierte automáticamente JSON ↔ objetos Kotlin
retrofit-converter-gson = { group = "com.squareup.retrofit2", name = "converter-gson", version.ref = "retrofit" }

# Logging Interceptor — imprime en Logcat el cuerpo de cada petición HTTP (útil para depuración)
okhttp-logging-interceptor = { group = "com.squareup.okhttp3", name = "logging-interceptor", version.ref = "okhttp" }

# ViewModel para Compose — permite crear ViewModels directamente desde funciones @Composable
androidx-lifecycle-viewmodel-compose = { group = "androidx.lifecycle", name = "lifecycle-viewmodel-compose", version.ref = "lifecycleRuntimeKtx" }

# Íconos extendidos de Material — incluye íconos como Person, Lock, Visibility, etc.
androidx-compose-material-icons-extended = { group = "androidx.compose.material", name = "material-icons-extended" }
```

### `app/build.gradle.kts` — Dependencias del módulo

Aquí se le dice a Gradle qué librerías del catálogo debe descargar e incluir en la app:

```kotlin
dependencies {
    // Retrofit y red
    implementation(libs.retrofit)                    // motor de peticiones HTTP
    implementation(libs.retrofit.converter.gson)     // serialización JSON automática
    implementation(libs.okhttp.logging.interceptor)  // logs de red en Logcat

    // Compose ViewModel e íconos
    implementation(libs.androidx.lifecycle.viewmodel.compose)       // viewModel() en Composables
    implementation(libs.androidx.compose.material.icons.extended)   // íconos Material
}
```

> **¿Por qué `implementation` y no `api`?**
> `implementation` significa que la librería es privada al módulo; otros módulos que dependan de este no la verán. Es la opción recomendada para la mayoría de dependencias.

### `AndroidManifest.xml` — Permisos y configuración de la app

```xml
<!-- Declara que la app necesita acceso a internet.
     Sin esta línea, el sistema bloquea todas las peticiones HTTP. -->
<uses-permission android:name="android.permission.INTERNET" />

<application
    ...
    <!-- Permite conexiones HTTP sin cifrar (http://).
         Necesario para conectar a un backend local durante desarrollo.
         En producción se debe usar HTTPS y eliminar este atributo. -->
    android:usesCleartextTraffic="true">
```

---

## 2. Capa de Tema (ui/theme/)

### `Color.kt` — Paleta de colores del proyecto

```kotlin
import androidx.compose.ui.graphics.Color

// ── Colores principales (definidos por el cliente) ──────────────────────────

// Verde Hoja: color primario, usado en botones, íconos y acentos principales
val LeafGreen = Color(0xFF2E7D32)

// Naranja Alerta: color de acción/error, usado en banners de error y validaciones
val AlertOrange = Color(0xFFF57C00)

// Blanco Roto / Eco: color de fondo de toda la app
val EcoWhite = Color(0xFFF9FBE7)

// Gris Pizarra: color principal de texto
val SlateGray = Color(0xFF263238)

// ── Colores de soporte (derivados de la paleta principal) ────────────────────

val LightGreen  = Color(0xFFE8F5E9)  // verde muy suave, para fondos de secciones
val DarkGreen   = Color(0xFF1B5E20)  // verde oscuro, para texto sobre fondo verde
val SoftOrange  = Color(0xFFFFF3E0)  // naranja muy suave, fondo del banner de error
val MediumGray  = Color(0xFFECEFF1)  // gris medio, para superficies secundarias
val LightGray   = Color(0xFFF5F7F8)  // gris muy claro
val TextGray    = Color(0xFF455A64)  // gris azulado, para texto secundario
```

> **¿Qué significa `0xFF2E7D32`?**
> Es un número hexadecimal de 32 bits: `FF` = alpha (opacidad 100%), `2E7D32` = el color RGB. En CSS equivaldría a `#2E7D32`.

---

### `Theme.kt` — Configuración del tema Material 3

```kotlin
// lightColorScheme() crea un esquema de colores para modo claro de Material 3.
// Cada parámetro le dice al sistema qué color usar en cada rol semántico.
private val ToadLightColorScheme = lightColorScheme(
    primary          = LeafGreen,   // color de botones, checkboxes, sliders
    onPrimary        = EcoWhite,    // texto/íconos SOBRE el color primary
    primaryContainer = LightGreen,  // fondo de chips o FAB secundarios
    onPrimaryContainer = DarkGreen, // texto sobre primaryContainer

    secondary          = AlertOrange, // color de accentos secundarios
    onSecondary        = EcoWhite,
    secondaryContainer = SoftOrange,
    onSecondaryContainer = SlateGray,

    background   = EcoWhite,   // fondo de toda la pantalla
    onBackground = SlateGray,  // texto sobre el fondo

    surface    = EcoWhite,    // fondo de Cards, Dialogs, BottomSheets
    onSurface  = SlateGray,   // texto sobre superficies

    surfaceVariant   = MediumGray, // variante de superficie (campos de texto)
    onSurfaceVariant = TextGray,   // texto sobre surfaceVariant

    error   = AlertOrange,  // color de errores (borde rojo en campos)
    onError = EcoWhite,
    outline = TextGray,     // borde de campos de texto no enfocados
)

@Composable
fun Toad_AppTheme(
    // No recibe parámetro darkTheme: la app solo tiene modo claro por ahora
    content: @Composable () -> Unit  // el contenido que se pintará con este tema
) {
    MaterialTheme(
        colorScheme = ToadLightColorScheme, // aplica la paleta definida arriba
        typography  = Typography,           // aplica las fuentes definidas en Type.kt
        content     = content               // renderiza el contenido
    )
}
```

> **¿Qué es `@Composable`?** Es una anotación que marca funciones que pueden dibujar UI en Jetpack Compose. Solo pueden llamarse desde otras funciones `@Composable`.

---

### `Type.kt` — Fuentes y escala tipográfica

#### Definición de familias de fuentes locales

```kotlin
// FontFamily agrupa varios archivos .ttf en una sola familia tipográfica.
// Cuando el sistema necesita texto en Bold, busca automáticamente
// el archivo registrado para FontWeight.Bold.

val PoppinsFontFamily = FontFamily(
    // Font(resId = ...) lee el archivo desde res/font/
    // weight = ... le dice al sistema para qué grosor sirve este archivo
    Font(resId = R.font.poppins_regular,  weight = FontWeight.Normal),   // 400
    Font(resId = R.font.poppins_medium,   weight = FontWeight.Medium),   // 500
    Font(resId = R.font.poppins_semibold, weight = FontWeight.SemiBold), // 600
    Font(resId = R.font.poppins_bold,     weight = FontWeight.Bold),     // 700
)

// Inter usa archivos de fuente VARIABLE — un solo .ttf contiene todos los pesos.
// Se referencia el mismo archivo varias veces, indicando distintos pesos.
// El parámetro style = FontStyle.Italic distingue entre recto e itálica.
val InterFontFamily = FontFamily(
    Font(resId = R.font.inter_variablefont_opsz_wght,        weight = FontWeight.Normal,   style = FontStyle.Normal),
    Font(resId = R.font.inter_variablefont_opsz_wght,        weight = FontWeight.Medium,   style = FontStyle.Normal),
    Font(resId = R.font.inter_variablefont_opsz_wght,        weight = FontWeight.SemiBold, style = FontStyle.Normal),
    Font(resId = R.font.inter_italic_variablefont_opsz_wght, weight = FontWeight.Normal,   style = FontStyle.Italic),
)
```

#### Escala tipográfica Material 3

```kotlin
// Typography define los estilos de texto de toda la app.
// Jetpack Compose tiene 15 estilos predefinidos (displayLarge, headlineLarge, etc.)
val Typography = Typography(

    // displayLarge: texto más grande, usado para emojis/logos grandes
    displayLarge = TextStyle(
        fontFamily   = PoppinsFontFamily,
        fontWeight   = FontWeight.Bold,
        fontSize     = 32.sp,       // sp = scale-independent pixels (respeta ajuste de accesibilidad)
        lineHeight   = 40.sp,       // espacio entre líneas
        letterSpacing = (-0.5).sp   // kerning negativo = letras más juntas (elegante en títulos grandes)
    ),

    // headlineLarge: título principal de pantalla ("Toad App")
    headlineLarge = TextStyle(
        fontFamily = PoppinsFontFamily,
        fontWeight = FontWeight.SemiBold,
        fontSize   = 28.sp,
        lineHeight = 36.sp,
        letterSpacing = 0.sp
    ),

    // titleMedium: subtítulos dentro de Cards
    titleMedium = TextStyle(
        fontFamily    = PoppinsFontFamily,
        fontWeight    = FontWeight.Medium,
        fontSize      = 16.sp,
        lineHeight    = 24.sp,
        letterSpacing = 0.15.sp
    ),

    // bodyMedium: texto de cuerpo normal (párrafos, subtítulos de formulario)
    bodyMedium = TextStyle(
        fontFamily    = InterFontFamily,   // Inter para texto corrido — más legible
        fontWeight    = FontWeight.Normal,
        fontSize      = 14.sp,
        lineHeight    = 20.sp,
        letterSpacing = 0.25.sp
    ),

    // bodySmall: texto de apoyo, mensajes de error debajo de campos
    bodySmall = TextStyle(
        fontFamily    = InterFontFamily,
        fontWeight    = FontWeight.Normal,
        fontSize      = 12.sp,
        lineHeight    = 16.sp,
        letterSpacing = 0.4.sp
    ),

    // labelLarge: texto dentro de botones ("Iniciar Sesión", "Cerrar Sesión")
    labelLarge = TextStyle(
        fontFamily    = PoppinsFontFamily,  // Poppins en botones: más impactante
        fontWeight    = FontWeight.SemiBold,
        fontSize      = 14.sp,
        lineHeight    = 20.sp,
        letterSpacing = 0.1.sp
    ),
)
```

---

## 3. Capa de Datos — Modelos (data/model/)

### `User.kt` — Modelo de dominio del usuario

```kotlin
// data class en Kotlin genera automáticamente equals(), hashCode(), toString() y copy().
// Representa al usuario DESPUÉS de una autenticación exitosa.
data class User(
    val username: String,  // nombre del usuario (viene del backend)
    val token: String      // JWT token para autenticar futuras peticiones
)
```

> Un **modelo de dominio** es la representación interna del dato en la app. Es diferente del `LoginResponse` (que es la representación de la red). Separar ambos permite cambiar el backend sin tocar la lógica de la app.

---

### `LoginResult.kt` — Resultado posible de un intento de login

```kotlin
// sealed class = clase sellada. Solo puede tener subtipos definidos aquí mismo.
// Es la forma idiomática en Kotlin de representar "uno de varios estados posibles".
sealed class LoginResult {

    // Caso exitoso: contiene el usuario autenticado
    data class Success(val user: User) : LoginResult()

    // Caso de credenciales incorrectas: no contiene datos extra
    // (object = singleton, solo existe una instancia)
    object InvalidCredentials : LoginResult()

    // Caso de error de red: contiene el mensaje de error para mostrarlo
    data class NetworkError(val message: String) : LoginResult()
}
```

**¿Por qué una sealed class y no un booleano?**

```kotlin
// ❌ Malo — no se sabe qué falló ni qué datos retornó
fun login(): Boolean

// ✅ Bueno — el compilador obliga a manejar TODOS los casos posibles
fun login(): LoginResult
```

Con `sealed class`, si se agrega un nuevo caso en el futuro (ej. `AccountLocked`), el compilador marcará error en todos los `when` que no lo manejen.

---

## 4. Capa de Red (data/network/)

### `LoginRequest.kt` — Cuerpo de la petición HTTP

```kotlin
import com.google.gson.annotations.SerializedName

// Esta clase representa el JSON que se enviará al backend:
// { "username": "admin", "password": "1234" }
data class LoginRequest(
    // @SerializedName indica el nombre exacto del campo en el JSON.
    // Aunque la propiedad Kotlin se llame "username", en el JSON irá como "username".
    // Útil cuando el backend usa snake_case: @SerializedName("user_name")
    @SerializedName("username") val username: String,
    @SerializedName("password") val password: String
)
```

### `LoginResponse.kt` — Respuesta del servidor

```kotlin
// Refleja exactamente el DTO del backend Spring Boot:
// { "token": "eyJhbGci...", "username": "admin" }
data class LoginResponse(
    @SerializedName("token")    val token: String,    // JWT generado por el servidor
    @SerializedName("username") val username: String  // nombre confirmado por el servidor
)
```

> **¿Por qué hay `LoginRequest`/`LoginResponse` Y `User`?**
> `LoginRequest`/`LoginResponse` son objetos de **transporte** (lo que viaja por la red). `User` es un objeto de **dominio** (lo que usa la app internamente). Si mañana el backend cambia `"username"` por `"user_name"`, solo se modifica `LoginResponse`, no toda la app.

---

### `AuthService.kt` — Interfaz Retrofit

```kotlin
// Una "interface" en Kotlin es un contrato — declara métodos sin implementarlos.
// Retrofit leerá esta interfaz y generará automáticamente el código HTTP.
interface AuthService {

    // @POST indica el método HTTP y la ruta relativa al BASE_URL
    // La ruta completa será: BASE_URL + "api/auth/login"
    @POST("api/auth/login")

    // suspend = función de corrutina. No bloquea el hilo principal mientras espera respuesta.
    // @Body = el objeto LoginRequest se serializa a JSON y se pone en el cuerpo de la petición.
    // Response<LoginResponse> = wrapper que da acceso al código HTTP (200, 401, etc.)
    suspend fun login(@Body request: LoginRequest): Response<LoginResponse>
}
```

> **¿Qué es una corrutina (`suspend`)?**
> Es una forma de ejecutar código asíncrono (como esperar una respuesta HTTP) sin bloquear la pantalla. Sin `suspend`, la app se congelaría mientras espera al servidor.

---

### `BackendOrchestrator.kt` — Orquestador de red

Este es el archivo más importante de la capa de datos. Centraliza **toda** la comunicación con el backend.

```kotlin
class BackendOrchestrator {

    companion object {
        // companion object = bloque para constantes/métodos estáticos en Kotlin
        // BASE_URL es lo único que se necesita cambiar para apuntar a otro servidor.
        // "http://10.0.2.2:8080/" → IP especial del emulador Android que apunta al localhost del PC
        private const val BASE_URL = "http://10.0.2.2:8080/"
    }

    // HttpLoggingInterceptor imprime en el Logcat de Android Studio
    // el cuerpo completo de cada petición y respuesta HTTP.
    // Level.BODY muestra headers + body (útil para desarrollo).
    private val loggingInterceptor = HttpLoggingInterceptor().apply {
        level = HttpLoggingInterceptor.Level.BODY
    }

    // OkHttpClient es el motor de red real.
    // Builder() usa el patrón Builder para configurarlo.
    private val okHttpClient = OkHttpClient.Builder()
        .addInterceptor(loggingInterceptor)      // agrega el logger
        .connectTimeout(30, TimeUnit.SECONDS)    // tiempo máximo para establecer conexión
        .readTimeout(30, TimeUnit.SECONDS)       // tiempo máximo para leer la respuesta
        .writeTimeout(30, TimeUnit.SECONDS)      // tiempo máximo para enviar el cuerpo
        .build()

    // Retrofit es la capa que convierte las llamadas de AuthService
    // en peticiones HTTP reales usando OkHttpClient.
    private val retrofit = Retrofit.Builder()
        .baseUrl(BASE_URL)                           // URL base del servidor
        .client(okHttpClient)                        // usa nuestro cliente configurado
        .addConverterFactory(GsonConverterFactory.create()) // convierte JSON ↔ Kotlin
        .build()

    // Retrofit genera una implementación concreta de la interfaz AuthService.
    // A partir de aquí, llamar a authService.login() ejecuta el HTTP POST real.
    private val authService: AuthService = retrofit.create(AuthService::class.java)

    // Función pública que expone el login al ViewModel.
    // Es suspend porque hace una petición de red (operación asíncrona).
    suspend fun login(username: String, password: String): LoginResult {
        return try {
            // Ejecuta el POST HTTP y espera la respuesta
            val response = authService.login(LoginRequest(username, password))

            when {
                // isSuccessful = true cuando el código HTTP está entre 200-299
                response.isSuccessful -> {
                    val body = response.body()  // deserializa el JSON a LoginResponse
                    if (body != null) {
                        // Convierte LoginResponse (red) → User (dominio)
                        LoginResult.Success(User(username = body.username, token = body.token))
                    } else {
                        LoginResult.NetworkError("Empty response from server")
                    }
                }
                // 401 Unauthorized o 403 Forbidden = credenciales incorrectas
                response.code() == 401 || response.code() == 403 -> {
                    LoginResult.InvalidCredentials
                }
                // Cualquier otro código (500, 404, etc.) = error del servidor
                else -> {
                    LoginResult.NetworkError("Server error: ${response.code()}")
                }
            }
        } catch (e: IOException) {
            // IOException = sin internet, timeout, servidor no alcanzable
            LoginResult.NetworkError("No connection: ${e.message}")
        } catch (e: Exception) {
            // Cualquier otro error inesperado
            LoginResult.NetworkError("Unexpected error: ${e.message}")
        }
    }
}
```

---

## 5. ViewModel — `LoginViewModel.kt`

### Estado de la UI: `LoginUiState`

```kotlin
// data class que representa TODO el estado visual de la pantalla de login.
// La UI solo lee este objeto — nunca modifica el estado directamente.
data class LoginUiState(
    val username: String = "",         // texto actual del campo usuario
    val password: String = "",         // texto actual del campo contraseña
    val isPasswordVisible: Boolean = false, // controla el ojo de mostrar/ocultar contraseña
    val usernameError: String? = null, // null = sin error; String = mensaje de error a mostrar
    val passwordError: String? = null,
    val apiError: String? = null,      // error proveniente del servidor
    val isLoading: Boolean = false,    // true = muestra spinner, deshabilita botón
    val loggedInUser: User? = null     // null = no autenticado; User = autenticado → ir a Main
)
```

### La clase ViewModel

```kotlin
class LoginViewModel(
    // El orquestador se inyecta por constructor.
    // Valor por defecto = BackendOrchestrator() para uso normal.
    // En tests se puede pasar un mock.
    private val orchestrator: BackendOrchestrator = BackendOrchestrator()
) : ViewModel() {  // ViewModel sobrevive rotaciones de pantalla

    // MutableStateFlow = contenedor de estado que notifica cambios a los observadores
    // El guión bajo "_" es convención para indicar que es privado y mutable
    private val _uiState = MutableStateFlow(LoginUiState())

    // Se expone solo la versión inmutable (StateFlow) para que la UI no pueda modificarlo directamente
    val uiState: StateFlow<LoginUiState> = _uiState.asStateFlow()

    // Llamado cada vez que el usuario escribe en el campo Usuario.
    // update{} es una función atómica que toma el estado actual (it) y retorna uno nuevo.
    // copy() crea una nueva instancia cambiando solo los campos especificados.
    fun onUsernameChanged(value: String) {
        _uiState.update { it.copy(username = value, usernameError = null, apiError = null) }
        //                         ↑ nuevo texto   ↑ limpia error previo  ↑ limpia error de red
    }

    fun onPasswordChanged(value: String) {
        _uiState.update { it.copy(password = value, passwordError = null, apiError = null) }
    }

    // Invierte la visibilidad de la contraseña (true→false, false→true)
    fun onPasswordVisibilityToggled() {
        _uiState.update { it.copy(isPasswordVisible = !it.isPasswordVisible) }
    }

    fun onLoginClicked() {
        val state = _uiState.value
        if (!validate(state)) return  // detiene si hay errores de validación local

        // Activa el estado de carga ANTES de llamar a la red
        _uiState.update { it.copy(isLoading = true, apiError = null) }

        // viewModelScope.launch lanza una corrutina ligada al ciclo de vida del ViewModel.
        // Si el ViewModel se destruye, la corrutina se cancela automáticamente.
        viewModelScope.launch {
            val result = orchestrator.login(state.username.trim(), state.password)

            // Actualiza el estado según el resultado (when es como switch en otros lenguajes)
            _uiState.update { current ->
                when (result) {
                    is LoginResult.Success ->
                        current.copy(isLoading = false, loggedInUser = result.user)
                        // Al poner loggedInUser != null, MainActivity muestra MainScreen

                    is LoginResult.InvalidCredentials ->
                        current.copy(isLoading = false, apiError = "Usuario o contraseña incorrectos.")

                    is LoginResult.NetworkError ->
                        current.copy(isLoading = false, apiError = result.message)
                }
            }
        }
    }

    // Resetea el estado al valor inicial → vuelve a la pantalla de login
    fun onLogout() {
        _uiState.update { LoginUiState() }
    }

    // Validación LOCAL (antes de hacer la petición de red)
    private fun validate(state: LoginUiState): Boolean {
        val usernameError = when {
            state.username.isBlank() -> "El usuario no puede estar vacío."
            else -> null
        }
        val passwordError = when {
            state.password.isBlank() -> "La contraseña no puede estar vacía."
            state.password.length < 6 -> "La contraseña debe tener al menos 6 caracteres."
            else -> null
        }
        // Si hay cualquier error, actualiza el estado y retorna false
        if (usernameError != null || passwordError != null) {
            _uiState.update { it.copy(usernameError = usernameError, passwordError = passwordError) }
            return false
        }
        return true
    }
}
```

---

## 6. Capa de UI — Pantallas

### `MainActivity.kt` — Punto de entrada y navegación

```kotlin
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()  // extiende el contenido bajo la barra de estado/navegación

        setContent {
            Toad_AppTheme {  // aplica colores y fuentes del tema
                // viewModel() crea o recupera el ViewModel (sobrevive rotaciones)
                val loginViewModel: LoginViewModel = viewModel()

                // collectAsState() observa el StateFlow y redibuja la UI cuando cambia
                val uiState by loginViewModel.uiState.collectAsState()

                // Navegación por estado: si hay usuario autenticado → Main, si no → Login
                // No se usa NavController — el estado es suficiente para 2 pantallas
                if (uiState.loggedInUser != null) {
                    MainScreen(
                        user = uiState.loggedInUser!!,
                        onLogout = loginViewModel::onLogout  // referencia a la función del ViewModel
                    )
                } else {
                    LoginScreen(viewModel = loginViewModel)
                }
            }
        }
    }
}
```

---

### `LoginScreen.kt` — Pantalla de inicio de sesión

#### Estructura general

```kotlin
@Composable
fun LoginScreen(viewModel: LoginViewModel, modifier: Modifier = Modifier) {
    // collectAsState() convierte el StateFlow en un State<T> que Compose puede observar.
    // El "by" delega la propiedad para acceder a uiState.username en vez de uiState.value.username
    val uiState by viewModel.uiState.collectAsState()

    // LocalFocusManager permite mover/limpiar el foco del teclado desde el código
    val focusManager = LocalFocusManager.current

    // Box es un contenedor que apila sus hijos. Aquí se usa para el fondo degradado.
    Box(
        modifier = modifier
            .fillMaxSize()  // ocupa todo el ancho y alto disponible
            .background(
                // Brush.verticalGradient crea un degradado de arriba hacia abajo
                Brush.verticalGradient(colors = listOf(LightGreen, EcoWhite, EcoWhite))
            )
    ) { ... }
}
```

#### Logo de la app

```kotlin
// Box con fondo verde redondeado para el ícono de la app
Box(
    modifier = Modifier
        .size(88.dp)                      // cuadrado de 88dp
        .clip(RoundedCornerShape(24.dp))  // esquinas redondeadas (clip recorta la forma)
        .background(LeafGreen),           // fondo verde
    contentAlignment = Alignment.Center   // centra el contenido
) {
    Text(text = "🐸", style = MaterialTheme.typography.displayLarge)
}
```

#### Campo de texto (usuario)

```kotlin
OutlinedTextField(
    value    = uiState.username,                  // texto actual del campo
    onValueChange = viewModel::onUsernameChanged, // llamado en cada tecla presionada

    label = { Text("Usuario") },  // etiqueta flotante del campo

    leadingIcon = {
        Icon(
            imageVector = Icons.Outlined.Person,
            // El ícono cambia de color si hay error: naranja=error, verde=normal
            tint = if (uiState.usernameError != null) AlertOrange else LeafGreen
        )
    },

    isError = uiState.usernameError != null,  // activa el estilo de error del campo

    supportingText = {
        // Solo se muestra si hay error. El !! asegura que no es null (ya verificamos arriba)
        if (uiState.usernameError != null) {
            Text(text = uiState.usernameError!!, color = AlertOrange)
        }
    },

    keyboardOptions = KeyboardOptions(
        keyboardType = KeyboardType.Text,  // teclado normal
        imeAction = ImeAction.Next          // botón "Siguiente" en el teclado
    ),
    keyboardActions = KeyboardActions(
        // Al presionar "Siguiente", mueve el foco al siguiente campo
        onNext = { focusManager.moveFocus(FocusDirection.Down) }
    ),

    singleLine = true,                // el campo no permite saltos de línea
    enabled = !uiState.isLoading,    // se deshabilita mientras carga

    // Personaliza los colores del campo
    colors = OutlinedTextFieldDefaults.colors(
        focusedBorderColor = LeafGreen,   // borde cuando está activo
        errorBorderColor   = AlertOrange  // borde cuando hay error
    ),
    shape = RoundedCornerShape(12.dp)  // esquinas redondeadas del campo
)
```

#### Campo de contraseña (diferencias clave)

```kotlin
// trailingIcon = ícono al final derecho del campo
trailingIcon = {
    IconButton(onClick = viewModel::onPasswordVisibilityToggled) {
        Icon(
            // Cambia el ícono según si la contraseña es visible o no
            imageVector = if (uiState.isPasswordVisible) Icons.Filled.VisibilityOff
                          else Icons.Filled.Visibility,
        )
    }
},

// visualTransformation oculta o muestra el texto de la contraseña
visualTransformation = if (uiState.isPasswordVisible)
    VisualTransformation.None         // muestra el texto real
else
    PasswordVisualTransformation()    // reemplaza cada carácter por "•"
```

#### Banner de error animado

```kotlin
// AnimatedVisibility muestra/oculta su contenido con animación
AnimatedVisibility(
    visible = uiState.apiError != null,          // visible cuando hay error de API
    enter = fadeIn() + slideInVertically(),       // aparece: fundido + deslizamiento desde arriba
    exit  = fadeOut()                             // desaparece: solo fundido
) {
    Row(
        modifier = Modifier
            .clip(RoundedCornerShape(10.dp))
            .background(SoftOrange)               // fondo naranja suave
            .padding(horizontal = 14.dp, vertical = 10.dp)
    ) {
        Icon(imageVector = Icons.Outlined.ErrorOutline, tint = AlertOrange)
        Text(text = uiState.apiError ?: "")       // ?: "" = si es null, usa string vacío
    }
}
```

#### Botón de login con estado de carga

```kotlin
Button(
    onClick = { focusManager.clearFocus(); viewModel.onLoginClicked() },
    enabled = !uiState.isLoading,  // deshabilita el botón mientras espera respuesta
    colors = ButtonDefaults.buttonColors(
        containerColor         = LeafGreen,
        disabledContainerColor = LeafGreen.copy(alpha = 0.5f)  // alpha = transparencia (0=invisible, 1=sólido)
    )
) {
    // Muestra spinner O texto, dependiendo del estado de carga
    if (uiState.isLoading) {
        CircularProgressIndicator(modifier = Modifier.size(22.dp), color = EcoWhite, strokeWidth = 2.5.dp)
    } else {
        Text(text = "Iniciar Sesión", style = MaterialTheme.typography.labelLarge)
    }
}
```

---

### `MainScreen.kt` — Pantalla principal (post-login)

```kotlin
@Composable
fun MainScreen(
    user: User,          // usuario autenticado, recibido del ViewModel via MainActivity
    onLogout: () -> Unit // lambda que se ejecuta al presionar "Cerrar Sesión"
) {
    // Fondo con degradado verde → blanco
    Box(modifier = Modifier.fillMaxSize().background(Brush.verticalGradient(listOf(LightGreen, EcoWhite)))) {

        Column(horizontalAlignment = Alignment.CenterHorizontally) {

            // Círculo verde con ícono de checkmark
            Box(modifier = Modifier.size(96.dp).clip(CircleShape).background(LeafGreen)) {
                Icon(imageVector = Icons.Outlined.CheckCircle, tint = EcoWhite)
            }

            Text(text = "¡Bienvenido!", style = MaterialTheme.typography.headlineLarge, color = LeafGreen)

            // Muestra el username del usuario autenticado
            Text(text = user.username, style = MaterialTheme.typography.titleMedium)

            // Card informativa — se actualizará con futuras funcionalidades
            Card(shape = RoundedCornerShape(16.dp)) {
                Column {
                    Text(text = "Pantalla Principal")
                    HorizontalDivider()  // línea divisoria horizontal
                    Text(text = "Esta pantalla recibirá actualizaciones próximamente.")
                }
            }

            // Botón de cerrar sesión con borde naranja
            OutlinedButton(
                onClick = onLogout,
                border = BorderStroke(1.dp, AlertOrange),  // borde naranja de 1dp
                colors = ButtonDefaults.outlinedButtonColors(contentColor = AlertOrange)
            ) {
                Icon(imageVector = Icons.AutoMirrored.Outlined.Logout)
                // AutoMirrored = el ícono se voltea automáticamente en idiomas RTL (árabe, hebreo)
                Text(text = "Cerrar Sesión")
            }
        }
    }
}
```

---

## 7. Resumen de archivos en `res/font/`

Android exige que los nombres de recursos sean **solo minúsculas, números y guiones bajos**. Los archivos fueron renombrados así:

| Archivo original | Nombre en el proyecto |
|---|---|
| `Poppins-Regular.ttf` | `poppins_regular.ttf` |
| `Poppins-Medium.ttf` | `poppins_medium.ttf` |
| `Poppins-SemiBold.ttf` | `poppins_semibold.ttf` |
| `Poppins-Bold.ttf` | `poppins_bold.ttf` |
| `Inter-VariableFont_opsz,wght.ttf` | `inter_variablefont_opsz_wght.ttf` |
| `Inter-Italic-VariableFont_opsz,wght.ttf` | `inter_italic_variablefont_opsz_wght.ttf` |

Los **54 archivos estáticos** de Inter (`Inter_18pt-*.ttf`, `Inter_24pt-*.ttf`, `Inter_28pt-*.ttf`) y los **14 pesos no usados** de Poppins fueron eliminados para no aumentar el tamaño del APK innecesariamente.

---

## 8. Flujo completo de la aplicación

```
1. App abre → MainActivity → uiState.loggedInUser == null → LoginScreen

2. Usuario escribe usuario y contraseña
   → LoginScreen llama viewModel.onUsernameChanged() / onPasswordChanged()
   → ViewModel actualiza _uiState con el nuevo texto

3. Usuario presiona "Iniciar Sesión"
   → LoginScreen llama viewModel.onLoginClicked()
   → ViewModel ejecuta validate() → si falla, muestra errores en campos
   → Si pasa, pone isLoading = true → UI muestra spinner
   → ViewModel llama orchestrator.login(username, password)
   → Orchestrator hace POST HTTP a /api/auth/login
   → Servidor responde 200 con { token, username }
   → Orchestrator retorna LoginResult.Success(user)
   → ViewModel pone loggedInUser = user, isLoading = false

4. MainActivity detecta que loggedInUser != null
   → Muestra MainScreen(user = ...)

5. Usuario presiona "Cerrar Sesión"
   → MainScreen llama onLogout (que es loginViewModel::onLogout)
   → ViewModel resetea _uiState a LoginUiState() (loggedInUser = null)
   → MainActivity vuelve a mostrar LoginScreen
```
