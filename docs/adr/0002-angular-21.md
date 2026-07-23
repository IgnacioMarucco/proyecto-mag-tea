# ADR-0002: Angular 21 como framework frontend

## Contexto

Se necesita un framework frontend moderno. La elección de versión impacta directamente en qué features de Signals, standalone components y change detection están disponibles.

## Decisión

Se usa **Angular 21** con standalone components por defecto, Signals estables y change detection zoneless.

## Alternativas consideradas

**Angular 19** — descartado por ser versión anterior con Signals menos maduros y sin `input()`/`output()` como funciones estables.

## Consecuencias

**Positivas:**
- Standalone components por defecto: sin NgModule, arquitectura más simple
- Signals estables: patrón de estado recomendado y maduro
- `input()` / `output()` como funciones: alineados con el modelo reactivo
- Control flow moderno (`@if`, `@for`): mejor rendimiento que directivas estructurales
- Vitest como test runner: más rápido que Karma
- Zoneless change detection: menor overhead en detección de cambios

**Negativas / trade-offs:**
- Versión muy reciente: menos respuestas en Stack Overflow, algunos recursos de la web aún en Angular 17-19
- `signal()` como paradigma requiere aprender un modelo mental diferente a RxJS puro
