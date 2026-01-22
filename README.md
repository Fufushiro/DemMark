# DemMark v1.0

**Aplicación Android nativa en Kotlin para proteger documentos con marcas de agua personalizadas.**

Una solución elegante y poderosa para agregar marcas de agua a imágenes y PDFs directamente desde tu dispositivo Android. Personaliza completamente la marca de agua con diferentes estilos, colores, tamaños y posiciones.

## 🎯 ¿Qué hace DemMark?

DemMark es una aplicación Android que te permite:

- **Proteger tus imágenes**: Agrega marcas de agua personalizadas a fotos en formato JPG y PNG
- **Asegurar tus PDFs**: Marca documentos PDF con textos de protección o branding
- **Personalizar completamente**: Controla el texto, tamaño, color, opacidad y posición de la marca
- **Ver en tiempo real**: Obtén una vista previa instantánea de cómo se verá tu marca antes de guardar
- **Guardar localmente**: Almacena tus archivos protegidos de forma segura en tu dispositivo

## ✨ Características principales

- ✅ **Soporte multi-formato**: Imágenes (JPG/PNG) y documentos PDF
- ✅ **Editor interactivo**: Editor de marca de agua con vista previa en tiempo real
- ✅ **Personalización avanzada**:
  - Texto personalizable ilimitado
  - Tamaño de fuente ajustable (12-200px)
  - Opacidad configurable (0-100%)
  - Selección de colores (Negro, Blanco, Azul)
  - Múltiples posiciones: Centro, Diagonal, Esquina superior izquierda, Esquina inferior derecha
- ✅ **Interfaz moderna**: Diseño limpio basado en Material Design 3
- ✅ **Arquitectura profesional**: Implementación MVVM con separación de capas
- ✅ **Rendimiento optimizado**: Procesamiento asincrónico con Coroutines
- ✅ **Sin permisos intrusivos**: Usa Storage Access Framework para máxima privacidad

## 📋 Requisitos

| Requisito | Valor |
|-----------|-------|
| **Android Studio** | Hedgehog 2023.1.1+ |
| **Android SDK mínimo** | API 26 (Android 8.0) |
| **Android SDK objetivo** | API 36 (Android 15) |
| **Kotlin** | 2.0.21 |
| **Java** | 11 |

## 🚀 Instalación

1. **Clona el proyecto**
   ```bash
   git clone <repository-url>
   cd DemMark
   ```

2. **Abre en Android Studio**
   - Abre Android Studio
   - Selecciona "File → Open" y elige la carpeta del proyecto

3. **Sincroniza Gradle**
   - Android Studio sincronizará automáticamente las dependencias

4. **Ejecuta la aplicación**
   - Conecta un dispositivo Android o inicia un emulador
   - Presiona Play o ejecuta: `./gradlew installDebug`

## 🔨 Comandos de compilación

```bash
# Compilar el proyecto completo
./gradlew build

# Compilar y instalar en dispositivo de prueba
./gradlew installDebug

# Ejecutar pruebas unitarias
./gradlew test

# Limpiar el proyecto
./gradlew clean
```

## 📖 Cómo usar

### Paso 1: Seleccionar archivo
1. Inicia DemMark
2. Toca el botón **"Seleccionar archivo"**
3. Elige una imagen (JPG/PNG) o un PDF de tu dispositivo

### Paso 2: Configurar marca de agua
En el editor interactivo, personaliza:
- **Texto**: Escribe el texto que deseas como marca
- **Tamaño**: Usa el deslizador para ajustar de 12 a 200px
- **Opacidad**: Controla la transparencia del 0% al 100%
- **Color**: Selecciona entre Negro, Blanco o Azul
- **Posición**: Elige cómo deseas distribuir la marca

### Paso 3: Vista previa
- Observa los cambios en **tiempo real** en la vista previa
- Ajusta hasta que esté exactamente como deseas

### Paso 4: Guardar
- Toca el botón flotante de **guardar** (esquina inferior derecha)
- El archivo se guardará con la marca de agua aplicada

## 📁 Estructura del Proyecto

```
DemMark/
├── app/
│   ├── src/main/
│   │   ├── java/ia/ankherth/demmark/
│   │   │   ├── ui/                          # Capa de presentación
│   │   │   │   ├── MainActivity.kt          # Pantalla principal
│   │   │   │   ├── WatermarkEditorActivity  # Editor interactivo
│   │   │   │   └── viewmodel/               # ViewModels
│   │   │   │       ├── MainViewModel
│   │   │   │       └── WatermarkEditorViewModel
│   │   │   ├── domain/                      # Capa de dominio
│   │   │   │   └── model/                   # Modelos de datos
│   │   │   │       ├── WatermarkConfig
│   │   │   │       ├── WatermarkPosition
│   │   │   │       └── FileType
│   │   │   ├── data/                        # Capa de datos
│   │   │   │   └── repository/
│   │   │   │       └── FileRepository
│   │   │   └── utils/                       # Utilidades
│   │   │       ├── WatermarkRenderer        # Motor de renderizado
│   │   │       ├── DocumentRenderer         # Renderizado de PDFs/imágenes
│   │   │       └── ImageSaver               # Guardado de archivos
│   │   └── res/                             # Recursos
│   │       ├── layout/                      # Layouts XML
│   │       ├── values/                      # Strings, colores, etc
│   │       └── drawable/                    # Íconos y drawables
│   └── build.gradle.kts                     # Configuración de Gradle
├── gradle/
│   └── libs.versions.toml                   # Versiones de dependencias
├── build.gradle.kts                         # Build root
└── README.md                                # Este archivo

```

## 🏗️ Arquitectura

DemMark utiliza la arquitectura **MVVM (Model-View-ViewModel)** con separación clara de responsabilidades:

```
┌─────────────────────────────────────────┐
│          User Interface (UI)            │
│  Activities, Layouts, ViewBinding       │
└──────────────────┬──────────────────────┘
                   │ Observa
                   ▼
┌─────────────────────────────────────────┐
│     ViewModel (Lógica de negocio)       │
│  MainViewModel, WatermarkEditorViewModel│
└──────────────────┬──────────────────────┘
                   │ Delega
                   ▼
┌─────────────────────────────────────────┐
│      Repository (Gestión de datos)      │
│          FileRepository                 │
└──────────────────┬──────────────────────┘
                   │ Usa
                   ▼
┌─────────────────────────────────────────┐
│    Utilities (Lógica del dominio)       │
│  WatermarkRenderer, DocumentRenderer    │
└─────────────────────────────────────────┘
```

## 🛠️ Tecnologías utilizadas

### Core Android
- **Kotlin** 2.0.21 - Lenguaje principal
- **AndroidX** - Soporte moderno
- **Material Design 3** - Componentes UI

### Jetpack Libraries
- **ViewModel** - Gestión de estado UI-safe
- **LiveData** - Observables reactivos
- **ViewBinding** - Type-safe acceso a vistas
- **Coroutines** - Programación asincrónica

### Procesamiento de media
- **PdfRenderer API** - Renderizado nativo de PDFs
- **Canvas API** - Dibujo de marcas de agua
- **Bitmap** - Procesamiento de imágenes

### Almacenamiento
- **Storage Access Framework (SAF)** - Acceso seguro a archivos
- **DocumentFile** - API de archivos modernos

## 🧪 Testing

Pruebas unitarias incluidas para validar la lógica del renderizado:

```bash
# Ejecutar todas las pruebas
./gradlew test

# Ejecutar pruebas específicas
./gradlew test -DtestFilter=*WatermarkRendererTest
```

**Cobertura de pruebas**:
- ✅ Validación de dimensiones de bitmap
- ✅ Aplicación de múltiples posiciones
- ✅ Control de opacidad
- ✅ Variación de tamaños de fuente

## ⚠️ Limitaciones conocidas

- El procesamiento de PDFs muy grandes (>50MB) puede requerir tiempo
- Solo la primera página del PDF se muestra en la vista previa (se procesan todas)
- La resolución máxima de la vista previa es 2048x2048px para optimizar memoria
- En emuladores antiguos el renderizado puede ser más lento

## 🔒 Privacidad y Permisos

- DemMark utiliza **Storage Access Framework** - No requiere permisos de almacenamiento global
- Los archivos se guardan solo en la carpeta de datos de la aplicación
- No se recopilan datos del usuario
- No hay conexión a internet

## 📝 Changelog

### v1.0 (Versión inicial)
- ✨ Soporte para imágenes y PDFs
- 🎨 Editor de marca de agua completo
- 📱 Interfaz Material Design 3
- 🏗️ Arquitectura MVVM
- 🧪 Suite de pruebas unitarias

## 📄 Licencia

Este proyecto está disponible bajo la licencia MIT.

## 👨‍💻 Desarrollador

Proyecto educativo desarrollado para demostrar:
- Arquitectura MVVM en Android
- Procesamiento de imágenes con Canvas
- Renderizado de PDFs en Android
- Programación reactiva con Coroutines
- Mejores prácticas en Android

# DemMark
