# g. Análisis de complejidad de `split` y `reconstruct`

---

## Notación

- `t` = umbral (threshold)
- `n` = número total de custodios
- `k` = tamaño en bits de `p` (y del secreto, del mismo orden de magnitud)

Las operaciones aritméticas sobre `BigInt` de `k` bits tienen costo O(k²) con aritmética clásica. El análisis se expresa en términos del número de operaciones modulares (multiplicaciones y reducciones mod p) para mayor claridad.

---

## Análisis de `split`

La función realiza dos pasos:

**1. `buildSecretPolynomial`:** genera `t - 1` coeficientes aleatorios en Z_p. Costo: **O(t)** operaciones.

**2. Evaluación en `n` puntos:** llama a `evalPoly` para cada custodio `i ∈ {1, ..., n}`.

`evalPoly` implementa el esquema de Horner con `@tailrec`, recorriendo la lista de `t` coeficientes en una sola pasada: una multiplicación y una suma por coeficiente. Costo por evaluación: **O(t)** operaciones.

**Costo total de `split`:**

```
T(split) = O(t) + n · O(t) = O(n · t)
```

En bits: **O(n · t · k²)**.

---

## Análisis de `reconstruct`

La función recibe `t` sombras y acumula la suma de Lagrange mediante `foldLeft`. Para cada sombra `(xⱼ, yⱼ)`:

1. **`basisNum`:** producto de `t - 1` términos `(-xₖ)` → **O(t)** multiplicaciones.
2. **`basisDen`:** producto de `t - 1` términos `(xⱼ - xₖ)` → **O(t)** multiplicaciones.
3. **`modInverse`:** llama a `extGcd`, que ejecuta O(log p) = **O(k)** iteraciones con operaciones de O(k²) bits cada una → **O(k³)** en bits, o **O(k)** operaciones de dominio.
4. Multiplicaciones adicionales del término: **O(1)** extra.

El `foldLeft` externo itera `t` veces, y en cada iteración los dos `foldLeft` internos son O(t) cada uno.

**Costo total de `reconstruct`:**

```
T(reconstruct) = t · (O(t) + O(t) + O(k)) = O(t² + t·k)
```

Para los parámetros del enunciado (`t` pequeño, `k` grande), el término dominante es **O(t · k)** por el costo de `modInverse`. En bits: **O(t² · k² + t · k³)**.

---

## Resumen

| Operación      | Operaciones de dominio | En bits               |
|----------------|------------------------|-----------------------|
| `split`        | O(n · t)               | O(n · t · k²)         |
| `reconstruct`  | O(t² + t · k)          | O(t² · k² + t · k³)  |

---

## Resultados experimentales — Categoría grande

Parámetros utilizados: secreto de 2000 bits, primo de 2048 bits, `t = 3`, `n = 5`.

| Operación      | Tiempo medido |
|----------------|---------------|
| `split`        | 1 ms          |
| `reconstruct`  | 2 ms          |

Tiempos obtenidos con Java 17.0.16 en la suite de pruebas (`sbt test`).

---

## Discusión

Los tiempos de la categoría grande son notablemente bajos (1–2 ms) por varias razones:

- **`t` y `n` son pequeños** (`t = 3`, `n = 5`): aunque `k = 2048` bits, el número de operaciones modulares es constante. El factor determinante no es `k` sino el número de iteraciones, que es mínimo.
- **JVM y BigInt de Java:** la implementación de `BigInt` en la JVM aprovecha aritmética nativa optimizada para enteros grandes, reduciendo la constante oculta de O(k²).
- **Escalabilidad real:** el costo crecería de forma observable si `t` o `n` aumentaran significativamente (por ejemplo, `t = 50`, `n = 100`), ya que `reconstruct` es O(t²) en operaciones y `split` es O(n·t). Para los parámetros típicos del esquema de Shamir, donde `t` y `n` son pequeños por diseño de seguridad, el algoritmo es eficiente incluso con secretos de miles de bits.
