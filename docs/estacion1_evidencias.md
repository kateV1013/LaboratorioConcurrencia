# Estación 1: Thread y Runnable — Evidencias y Análisis Técnico

**Integrante 1**  
**Rama:** `feature/integrante-1`  
**Asignatura:** Programación de Aplicaciones de Escritorio — JavaFX

---

## 1. Predicción Inicial

### ¿Se bloqueará la interfaz al invocar una tarea que duerme 500 ms cinco veces desde el controlador?
**Sí, la interfaz gráfica se congelará totalmente durante aproximadamente 2.5 segundos.**  
* **Justificación técnica:** JavaFX opera sobre un único hilo de despacho de eventos conocido como el *JavaFX Application Thread*. Si un método invocado desde un evento de la UI (como el clic de un botón) realiza `Thread.sleep(500)` cinco veces de forma secuencial, pone a dormir directamente al *JavaFX Application Thread*. Durante esos 2 500 ms, la aplicación es incapaz de procesar eventos del ratón/teclado, repintar componentes visuales o responder a las señales del sistema operativo (mostrando el estado "No responde").

### ¿Qué cambiaría al ejecutarla en un hilo independiente?
* Al instanciar un `Thread` con `Runnable` y llamar a `start()`, se crea un hilo de trabajo (*worker thread*) separado del hilo gráfico. La llamada a `start()` es no bloqueante y retorna casi instantáneamente al controlador. El hilo de fondo absorbe los bloqueos de 500 ms mientras el *JavaFX Application Thread* continúa escuchando eventos y refrescando la pantalla con total fluidez.

---

## 2. Comparativa: Invocación Directa con `run()` vs Arranque con `start()`

| Criterio | Llamada directa: `run()` | Llamada a través de: `start()` |
| :--- | :--- | :--- |
| **Hilo ejecutor** | El mismo hilo que hace la llamada (`main`) | Un nuevo hilo independiente gestionado por el SO (`Ventanilla-A`, `Ventanilla-B`) |
| **Comportamiento** | **Síncrono y bloqueante**; ejecuta una tarea tras otra | **Asíncrono y concurrente**; ambas tareas avanzan en paralelo |
| **Creación de hilo nativo** | No crea ningún hilo nuevo; es una simple llamada a un método Java | La JVM solicita al sistema operativo la creación y planificación de un hilo nativo |
| **Rendimiento percibido** | Congela el hilo invocador hasta completar los 2 500 ms por tarea | Retorna de inmediato (< 1 ms); el hilo invocador queda libre |

---

## 3. Registro de Tres Ejecuciones Reales con `start()`

En las tres ejecuciones se emplearon dos instancias: `Thread("Ventanilla-A")` y `Thread("Ventanilla-B")`, cada una procesando 5 unidades de trabajo con pausas de 500 ms.

### Tabla Comparativa de Orden Observado

| Paso | Ejecución 1 | Ejecución 2 | Ejecución 3 |
| :---: | :--- | :--- | :--- |
| **1** | `[Ventanilla-A] procesa 1` | `[Main] start() retornó` | `[Ventanilla-A] procesa 1` |
| **2** | `[Main] start() retornó` | `[Ventanilla-A] procesa 1` | `[Ventanilla-B] procesa 1` |
| **3** | `[Ventanilla-B] procesa 1` | `[Ventanilla-B] procesa 1` | `[Main] start() retornó` |
| **4** | `[Ventanilla-A] procesa 2` | `[Ventanilla-A] procesa 2` | `[Ventanilla-A] procesa 2` |
| **5** | `[Ventanilla-B] procesa 2` | `[Ventanilla-B] procesa 2` | `[Ventanilla-B] procesa 2` |
| **6** | `[Ventanilla-A] procesa 3` | `[Ventanilla-A] procesa 3` | `[Ventanilla-A] procesa 3` |
| **7** | `[Ventanilla-B] procesa 3` | `[Ventanilla-B] procesa 3` | `[Ventanilla-B] procesa 3` |
| **8** | `[Ventanilla-A] procesa 4` | `[Ventanilla-A] procesa 4` | `[Ventanilla-A] procesa 4` |
| **9** | `[Ventanilla-B] procesa 4` | `[Ventanilla-B] procesa 4` | `[Ventanilla-B] procesa 4` |
| **10** | `[Ventanilla-A] procesa 5` | `[Ventanilla-A] procesa 5` | `[Ventanilla-A] procesa 5` |
| **11** | `[Ventanilla-B] procesa 5` | `[Ventanilla-B] procesa 5` | `[Ventanilla-B] procesa 5` |

### Trazas Reales Obtenidas en Consola

```text
============================================================
=== 1. EJECUCION DIRECTA CON run() (SECUENCIAL / BLOQUEANTE) ===
============================================================
[Main: main] Invocando tareaA.run() directamente...
[main] Ventanilla-A procesa 1
[main] Ventanilla-A procesa 2
[main] Ventanilla-A procesa 3
[main] Ventanilla-A procesa 4
[main] Ventanilla-A procesa 5
[Main: main] tareaA.run() finalizo. Invocando tareaB.run()...
[main] Ventanilla-B procesa 1
[main] Ventanilla-B procesa 2
[main] Ventanilla-B procesa 3
[main] Ventanilla-B procesa 4
[main] Ventanilla-B procesa 5
[Main: main] Ambas tareas ejecutadas secuencialmente en el hilo principal.

============================================================
=== 2. TRES EJECUCIONES CON start() PARA COMPARAR ORDEN  ===
============================================================
------------------------------------------------------------
>>> EJECUCION N° 1 CON start() (HILOS CONCURRENTES)
------------------------------------------------------------
[Main: main] Invocando start() para Ventanilla-A y Ventanilla-B...
[Ventanilla-A] Ventanilla-A procesa 1
[Main: main] start() retorno de inmediato. Hilos en ejecucion.
[Ventanilla-B] Ventanilla-B procesa 1
[Ventanilla-A] Ventanilla-A procesa 2
...
```

---

## 4. Preguntas de Análisis y Justificaciones Técnicas

### 1. ¿Por qué el orden de mensajes cambia entre ejecuciones?
El orden cambia porque la concurrencia es **no determinista**. Cuando se invoca `start()`, la decisión de qué hilo obtiene tiempo de CPU y en qué milisegundo exacto depende del **planificador de hilos (*Thread Scheduler*) del sistema operativo**.  
Factores que influyen:
* Asignación de cuotas de tiempo (*time slicing*) en el procesador.
* El tiempo exacto de despertar tras `Thread.sleep(500)` (que no es matemáticamente exacto, sino que garantiza al menos 500 ms).
* El cambio de contexto (*context switching*) y la contención en el flujo de salida estándar (`System.out.println`).

### 2. ¿Es correcto asumir que `start()` termina el trabajo antes de devolver el control?
**No, es un error conceptual grave.**  
* `start()` tiene una semántica **asíncrona**: su única función es registrar el hilo ante el sistema operativo, cambiar su estado a `RUNNABLE` e indicar que es elegible para ejecución, devolviendo el control al hilo invocador de inmediato.
* El hilo invocador (`main` o el hilo de JavaFX) continúa ejecutando sus siguientes líneas de código mientras el nuevo hilo recién empieza a correr.
* Si se requiere garantizar que el hilo ha completado su trabajo antes de proseguir, se debe utilizar explícitamente el método bloqueante `hilo.join()`.
