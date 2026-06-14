# c. Caracterización de entradas y salidas de cada operación

---

## 3.1 Núcleo aritmético modular

### `extGcd(a: BigInt, b: BigInt): (BigInt, BigInt, BigInt)`

**Entradas:**
- `a`: entero grande, primer operando.
- `b`: entero grande, segundo operando.

**Salida:** tupla `(g, x, y)` tal que `g = gcd(a, b)` y `a·x + b·y = g` (identidad de Bézout).

**Restricciones:** ninguna explícita. Si `b = 0`, retorna `(a, 1, 0)`.

**Ejemplos:**
```
extGcd(240, 46)   → (2, -9, 47)    // 240·(-9) + 46·47 = 2
extGcd(17, 1613)  → (1, -759, 8)   // 17·(-759) + 1613·8 = 1
extGcd(8, 12)     → (4, -1, 1)     // gcd(8,12) = 4
```

---

### `modInverse(a: BigInt, p: BigInt): Option[BigInt]`

**Entradas:**
- `a`: entero cuyo inverso se busca en Z_p.
- `p`: módulo (primo en el contexto del esquema).

**Salida:**
- `Some(inv)` donde `a · inv ≡ 1 (mod p)`, si `gcd(a, p) = 1`.
- `None` si `gcd(a, p) ≠ 1` (el inverso no existe).

**Restricciones:** `p > 0`. Internamente reduce `a` al rango `[0, p)` antes de operar.

**Ejemplos:**
```
modInverse(17, 1613)  → Some(854)   // 17·854 mod 1613 = 1
modInverse(2, 1613)   → Some(807)   // 2·807 mod 1613 = 1
modInverse(6, 12)     → None        // gcd(6,12) = 6 ≠ 1
```

---

### `modPow(base: BigInt, exp: BigInt, mod: BigInt): BigInt`

**Entradas:**
- `base`: base de la exponenciación.
- `exp`: exponente, debe ser ≥ 0.
- `mod`: módulo, debe ser > 0.

**Salida:** `base^exp mod mod`.

**Restricciones:** si `mod = 1`, retorna `0` (cualquier entero mod 1 es 0).

**Ejemplos:**
```
modPow(7, 128, 1613)  → 1184
modPow(2, 10, 1000)   → 24      // 2^10 = 1024, 1024 mod 1000 = 24
modPow(5, 0, 13)      → 1       // base^0 = 1
```

---

## 3.2 TAD Polinomio

### `evalPoly(coefs: Polynomial, x: BigInt, p: BigInt): BigInt`

**Entradas:**
- `coefs`: lista de coeficientes `[a₀, a₁, ..., aₐ]` donde `head = a₀` es el término constante (el secreto).
- `x`: punto de evaluación en Z_p.
- `p`: primo que define el campo finito.

**Salida:** `f(x) mod p` donde `f(x) = a₀ + a₁·x + ... + aₐ·xᵈ`.

**Restricciones:** `coefs` no vacía; `p > 0`. Implementada con esquema de Horner y `@tailrec`.

**Ejemplos** (con `coefs = [1234, 166, 94]`, es decir `f(x) = 1234 + 166x + 94x²`):
```
evalPoly([1234, 166, 94], 0, 1613)  → 1234
evalPoly([1234, 166, 94], 1, 1613)  → 1494
evalPoly([1234, 166, 94], 2, 1613)  → 329
evalPoly([1234, 166, 94], 3, 1613)  → 965
```

---

### `buildSecretPolynomial(secret: BigInt, threshold: Int, p: BigInt, seed: Long): Polynomial`

**Entradas:**
- `secret`: el secreto S, que será el coeficiente `a₀`.
- `threshold`: umbral `t`; el polinomio resultante tiene grado `t - 1`.
- `p`: primo del campo finito.
- `seed`: semilla para reproducibilidad del generador pseudoaleatorio.

**Salida:** lista `[secret, a₁, ..., a_{t-1}]` donde `a₁, ..., a_{t-1}` son uniformes en `[0, p)`.

**Restricciones:** `threshold ≥ 2`, `p > secret` (validados por `split`). La función es pura respecto al `seed`: la misma semilla siempre produce el mismo polinomio.

**Ejemplos:**
```
buildSecretPolynomial(1234, 3, 1613, 42L)
  → [1234, a₁, a₂]   // a₁, a₂ dependen del seed

// Para las trazas del enunciado (coeficientes fijos):
// [1234, 166, 94]  → f(x) = 1234 + 166x + 94x² (mod 1613)
```

---

## 3.3 Reparto

### `split(secret: BigInt, threshold: Int, n: Int, p: BigInt, seed: Long): Either[String, List[Share]]`

**Entradas:**
- `secret`: el secreto S a repartir (≥ 0).
- `threshold`: número mínimo de custodios para reconstruir (`t ≥ 2`).
- `n`: número total de custodios (`n ≥ t`).
- `p`: primo tal que `p > secret` y `p > n`.
- `seed`: semilla para reproducibilidad.

**Salida:**
- `Right([(1, f(1)), (2, f(2)), ..., (n, f(n))])` si todas las validaciones pasan.
- `Left("mensaje de error")` en cualquiera de estos casos:
  - `secret < 0`
  - `threshold < 2`
  - `n < threshold`
  - `p ≤ secret`
  - `p ≤ n`

**Ejemplos:**
```
split(1234, 3, 5, 1613, 42L)
  → Right([(1,1494),(2,329),(3,965),(4,176),(5,1188)])

split(1234, 5, 3, 1613, 42L)
  → Left("n debe ser >= threshold")

split(2000, 3, 5, 1613, 42L)
  → Left("p debe ser > secret")
```

---

## 3.4 Reconstrucción

### `reconstruct(shares: List[Share], p: BigInt): Either[String, BigInt]`

**Entradas:**
- `shares`: lista de sombras `(xⱼ, yⱼ)` con todas las coordenadas `xⱼ` distintas.
- `p`: primo del campo finito.

**Salida:**
- `Right(S)` con el secreto reconstruido `f(0) mod p`.
- `Left("mensaje")` si:
  - la lista está vacía.
  - hay coordenadas `x` repetidas.
  - algún denominador de Lagrange no es invertible mod p.

**Ejemplos:**
```
reconstruct([(1,1494),(2,329),(3,965)], 1613)          → Right(1234)
reconstruct([(2,329),(4,176),(5,1188)], 1613)           → Right(1234)
reconstruct([(1,1494),(2,329),(3,965),(4,176),(5,1188)], 1613) → Right(1234)
reconstruct([], 1613)                                   → Left("se requiere al menos una sombra")
reconstruct([(1,1494),(1,329)], 1613)                   → Left("las coordenadas x deben ser distintas")
```

---

## 3.5 Verificación del umbral

### `verifyThreshold(secret: BigInt, shares: List[Share], threshold: Int, p: BigInt): Boolean`

**Entradas:**
- `secret`: el secreto original S.
- `shares`: lista completa de `n` sombras.
- `threshold`: umbral `t`.
- `p`: primo del campo finito.

**Salida:** `true` si y solo si:
1. Todo subconjunto de exactamente `t` sombras reconstruye el secreto, **y**
2. Al menos un subconjunto de `t - 1` sombras **no** lo hace.

**Ejemplo:**
```
verifyThreshold(1234, [(1,1494),(2,329),(3,965),(4,176),(5,1188)], 3, 1613)
  → true
  // reconstruct(shares.take(3), 1613) == Right(1234)  ✓
  // reconstruct(shares.take(2), 1613) == Right(1046)  ≠ 1234  ✓
```

---

## 3.6 Codificación de mensajes

### `encode(message: String): BigInt`

**Entrada:** cadena de texto no vacía codificada en UTF-8.

**Salida:** entero grande que representa los bytes de la cadena en base 256, usando la fórmula:

```
encode(s) = ∑ bᵢ · 256^(len-1-i)
```

donde `bᵢ` es el i-ésimo byte UTF-8 del mensaje.

**Ejemplos:**
```
encode("Hola")        → 1215261793
  // H=72, o=111, l=108, a=97
  // 72·256³ + 111·256² + 108·256 + 97

encode("S3CR3T_2026") → 100582925573664214709449270
```

---

### `decode(value: BigInt): String`

**Entrada:** entero grande previamente producido por `encode`.

**Salida:** la cadena original. Se garantiza `decode(encode(s)) == s` para toda cadena `s` no vacía.

**Ejemplos:**
```
decode(1215261793)                   → "Hola"
decode(100582925573664214709449270)  → "S3CR3T_2026"
```
