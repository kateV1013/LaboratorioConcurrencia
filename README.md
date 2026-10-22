# Laboratorio Guiado: Concurrencia en Java y JavaFX

Proyecto base para la práctica de concurrencia y multihilo en JavaFX.

## 🛠️ Requisitos del Entorno
* **Java SDK**: Eclipse Temurin OpenJDK 21 (LTS) o superior.
* **Gestor de Dependencias y Construcción**: Apache Maven 3.9+.
* **JavaFX**: Versión 21.0.2.
* **IDE recomendado**: IntelliJ IDEA / VS Code / Eclipse.

## 📁 Estructura del Proyecto

```text
LaboratorioConcurrencia/
├── pom.xml
├── .gitignore
├── README.md
└── src/
    └── main/
        ├── java/
        │   ├── module-info.java            # Descriptor modular (JPMS)
        │   ├── app/
        │   │   └── MainApp.java            # Aplicación principal JavaFX y método main
        │   ├── tareas/
        │   │   └── ProcesoVentanilla.java  # Clase base Runnable para simulación de ventanillas
        │   └── modelo/
        │       └── ContadorCompartido.java # Clase base para el recurso compartido
        └── resources/
            └── .gitkeep                    # Directorio para recursos (FXML, CSS, imágenes)
```

### Paquetes y Módulo
* **`module-info.java`**: Declara el módulo `laboratorio.concurrencia`, requiere los módulos `javafx.controls` y `javafx.fxml`, y expone los paquetes necesarios permitiendo ejecutar directamente [`app.MainApp`](file:///home/wigar03/Documents/GitHub/LaboratorioConcurrencia/src/main/java/app/MainApp.java).
* **`app`**: Contiene la interfaz gráfica y el punto de entrada principal (`MainApp`).
* **`tareas`**: Contiene la lógica ejecutable en segundo plano (`ProcesoVentanilla`).
* **`modelo`**: Contiene el recurso compartido (`ContadorCompartido`).

## 🚀 Cómo Ejecutar el Proyecto

### Opción 1: Mediante Maven
```bash
mvn clean javafx:run
```

### Opción 2: Desde IntelliJ IDEA / IDE
1. Abrir la carpeta raíz como proyecto Maven.
2. Ejecutar directamente [`app.MainApp`](file:///home/wigar03/Documents/GitHub/LaboratorioConcurrencia/src/main/java/app/MainApp.java).

---

## 🌿 Flujo de Trabajo en Git y Ramas

De acuerdo con la guía del laboratorio, se debe mantener una estructura de **cinco ramas obligatorias**:

1. **`main`**: Versión estable y funcional. Solo el líder de equipo integra cambios mediante Pull Requests.
2. **`feature/integrante-1`**: Estación 1 — Thread y Runnable.
3. **`feature/integrante-2`**: Estación 2 — Estados del hilo y observación.
4. **`feature/integrante-3`**: Estación 3 — Sincronización y recursos compartidos.
5. **`feature/integrante-4`**: Estación 4 y 5 — Detención controlada e integración JavaFX.

### Convención de Commits
Utilizar mensajes en imperativo con prefijos estandarizados:
* `feat: ...` (nuevas funcionalidades)
* `fix: ...` (corrección de bugs o condiciones de carrera)
* `refactor: ...` (reestructuración sin cambio funcional)
* `test: ...` (pruebas de concurrencia e interrupción)
* `docs: ...` (documentación y análisis)
