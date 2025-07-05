# Namespaced Modal

Ein Term-Rewriting-System mit Namensräumen und einer interaktiven REPL (Read-Eval-Print-Loop).

## Beschreibung

Namespaced Modal ist ein leistungsstarkes Term-Rewriting-System, das die Definition und Anwendung von Regeln zur Transformation von Termen ermöglicht. Das System unterstützt Namensräume für Regeln, was eine bessere Organisation und Modularität ermöglicht.

Das Projekt implementiert:
- Eine Kernsprache für die Definition von Termen und Regeln
- Ein Pattern-Matching-System mit Variablenbindung
- Eine Rewriting-Engine zur Anwendung von Regeln auf Terme
- Eine interaktive REPL für die Benutzung des Systems

## Features

- **Term-Repräsentation**: Unterstützung für atomare Terme und Listen von Termen
- **Variablen**: Verwendung von Variablen in Regeln mit `?name`-Syntax
- **Namensräume**: Organisation von Regeln in hierarchischen Namensräumen
- **Regelmanagement**: Hinzufügen, Entfernen und Auflisten von Regeln
- **Interaktive REPL**: Benutzerfreundliche Schnittstelle zur Interaktion mit dem System

## Installation

### Voraussetzungen
- Java 11 oder höher
- Gradle 7.0 oder höher

### Schritte
1. Repository klonen:
   ```
   git clone https://github.com/marcel951/namespaced-modal.git
   cd namespaced-modal
   ```

2. Projekt mit Gradle bauen:
   ```
   ./gradlew build
   ```

3. REPL starten:
   ```
   ./gradlew run
   ```

## Verwendung

### REPL-Befehle

- `:rules` - Zeigt alle definierten Regeln an
- `:stats` - Zeigt Statistiken über die Regeln an
- `:exit` - Beendet die REPL

### Regeln definieren

Regeln werden mit der folgenden Syntax definiert:
```
<> (pattern) (replacement)
```

Oder mit einem Namen:
```
<name> (pattern) (replacement)
```

Oder mit einem Namensraum:
```
<namespace.name> (pattern) (replacement)
```

### Regeln entfernen

Regeln können mit der folgenden Syntax entfernt werden:
```
>< (pattern)
```

### Beispiele

```
<math.+> (?x ?y) (+ ?x ?y)  # Definiert eine benannte Regel im Namensraum "math"
<> (?x ?x) ?x               # Definiert eine anonyme Regel zur Vereinfachung von Duplikaten
(math.+ 5 3)                # Wendet die math.+-Regel an, ergibt: (+ 5 3)
(a a)                       # Wendet die Duplikat-Regel an, ergibt: a
```

## Projektstruktur

```
src/main/java/
├── REPL/
│   ├── Repl.java             # Hauptklasse mit REPL-Implementierung
│   └── ModalInterpreter.java # Interpreter für die Modalsprache
├── core/
│   ├── Term.java             # Repräsentation von Termen
│   └── Rule.java             # Repräsentation von Regeln
├── engine/
│   ├── Rewriter.java         # Term-Rewriting-Engine
│   └── RuleManager.java      # Verwaltung von Regeln
└── parser/
    └── Parser.java           # Parser für die Eingabesprache
```

## Abhängigkeiten

- JUnit 5 (nur für Tests)

## Lizenz

[MIT](https://opensource.org/licenses/MIT)
