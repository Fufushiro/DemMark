# DemMark

Aplicación Android para añadir marcas de agua a imágenes y documentos PDF.

## Características

- Selección de imágenes (JPG, PNG) desde el dispositivo
- Selección de documentos PDF
- Aplicación de marca de agua personalizada
- Procesamiento en background thread
- UI limpia y profesional
- Soporte para múltiples dispositivos

## Arquitectura

- **ViewModel**: Gestión de estado y lógica de negocio
- **LiveData**: Observables para actualizaciones de UI
- **Coroutines**: Procesamiento asincrónico
- **Repository**: Abstracción de acceso a archivos

## Estructura de Carpetas

```
app/src/main/java/ia/ankherth/demmark/
├── MainActivity.kt          # Activity principal
├── data/
│   └── WatermarkState.kt   # Data class para estado
├── util/
│   ├── Constants.kt        # Constantes de la app
│   ├── FileUtil.kt         # Operaciones con archivos
│   ├── ValidationUtil.kt   # Validaciones
│   ├── WatermarkUtil.kt    # Lógica de marca de agua
│   └── PdfWatermarkUtil.kt # Procesamiento de PDF
└── viewmodel/
    └── WatermarkViewModel.kt # ViewModel
```

## Requisitos

- Android 13+ (minSdk 33)
- Permisos de lectura/escritura de almacenamiento

## Flujo de Datos

1. Usuario selecciona archivo (imagen o PDF)
2. Se carga preview en background
3. Usuario ingresa texto de marca de agua
4. Al presionar aplicar, se procesa en thread de IO
5. Archivo watermarked se guarda en cache
6. UI actualiza con preview y mensaje de estado

## Validaciones

- URI válido y accesible
- Texto de marca de agua (1-100 caracteres)
- Bitmaps con dimensiones válidas
- Manejo de excepciones en todas las operaciones
# DemMark
