# Namespaced-Modal Term-Rewriting Language

A powerful term-rewriting language with namespace support, designed for symbolic computation and rule-based transformations.

## Overview

Namespaced-Modal is a term-rewriting system that allows you to define and apply transformation rules to symbolic expressions. The system supports namespaces to organize rules, making it easier to manage complex rule sets.

Key features:
- Namespace-based rule organization
- Interactive REPL (Read-Eval-Print Loop)
- Support for arithmetic, list operations, boolean logic, and conditionals
- Multiple debugging modes for tracing rule applications
- Extensible rule system

## Installation

### Prerequisites
- Java 21 or higher
- Gradle 7.0 or higher

### Building from Source
1. Clone the repository:
   ```
   git clone https://github.com/marcel951/namespaced-modal.git
   cd namespaced-modal
   ```

2. Build the project using Gradle:
   ```
   ./gradlew build
   ```

3. Run the application:
   ```
   ./gradlew run
   ```

## Usage

### Starting the REPL

The application provides an interactive REPL where you can evaluate expressions:

```
$ ./gradlew run

Loading rules...
Rules loaded successfully!
Namespaced-Modal Term-Rewriting Language
Loaded 35 rules
Type :help for commands or :exit to quit

>
```

### Basic Commands

- `:help` - Show available commands
- `:mode [debug|trace|quiet|step-by-step]` - Set or show evaluation mode
- `:rules [namespace]` - Show rules (all or for specific namespace)
- `:namespaces` - Show all available namespaces
- `:exit` - Exit the REPL

### Evaluating Expressions

You can evaluate expressions directly in the REPL:

```
> (+ 5 3)
8

> (length (1 2 3 4))
4

> (reverse (1 2 3))
(3 2 1)
```

### Rule Syntax

Rules are defined in the following format:

```
<namespace.name> pattern replacement
```

For example:
```
<list.length.empty> (length ()) 0
<list.length.cons> (length (?head . ?tail)) (+ 1 (length ?tail))
```

Variables in patterns are prefixed with `?` and can match any term.

## Available Rule Sets

The standard rule set includes:

1. **Arithmetic Operations**
   - Addition, subtraction, multiplication, division, modulo

2. **List Operations**
   - length, first, rest, cons, append, reverse

3. **Boolean Operations**
   - and, or, not

4. **Conditional Operations**
   - if-then-else

## Extending the System

You can create your own rule files and load them into the system. Rule files should follow the syntax shown in the `src/main/resources/rules/standard.modal` file.

## Debugging

The system supports multiple debugging modes:

- `quiet` - Only shows the final result (default)
- `debug` - Shows detailed information about term parsing and evaluation
- `trace` - Shows each step of the rewriting process
- `step-by-step` - Pauses after each rewriting step for interactive debugging

## License

This project is licensed under the MIT License - see the LICENSE file for details.
