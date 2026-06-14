# f. Demostración informal de correctitud del esquema

El objetivo es argumentar por qué `reconstruct`, dado exactamente `t` sombras válidas generadas por `split`, siempre retorna el secreto original `S = a₀`.

---

## Premisa

El secreto `S` se almacena como el coeficiente constante `a₀` de un polinomio de grado `t - 1` sobre Z_p:

```
f(x) = a₀ + a₁·x + a₂·x² + ... + a_{t-1}·x^{t-1}  (mod p)
```

donde `a₀ = S` y `a₁, ..., a_{t-1}` son elegidos uniformemente al azar en Z_p. Cada custodio `i ∈ {1, ..., n}` recibe la sombra `(i, f(i) mod p)`.

---

## Por qué t sombras son suficientes

Dados `t` pares distintos `{(x₁, y₁), ..., (xₜ, yₜ)}` con `yⱼ = f(xⱼ)`, el **teorema de interpolación de Lagrange** garantiza que existe un único polinomio de grado ≤ t - 1 que pasa por todos esos puntos. Ese polinomio es precisamente `f`.

Su valor en `x = 0` se calcula como:

```
f(0) = ∑ⱼ yⱼ · Lⱼ(0)   (mod p)
```

donde el j-ésimo polinomio base de Lagrange evaluado en 0 es:

```
Lⱼ(0) = ∏_{k≠j} (0 - xₖ) · inv(xⱼ - xₖ, p)   (mod p)
```

e `inv(a, p)` es el inverso modular de `a` en Z_p, calculado mediante `extGcd`.

**¿Por qué los inversos siempre existen?** Las coordenadas `xⱼ ∈ {1, ..., n}` son distintas y `p > n` (validado por `split`), por lo tanto las diferencias `xⱼ - xₖ` nunca son múltiplos de `p`. Como `p` es primo, todo elemento no nulo de Z_p es invertible, garantizando que `modInverse` siempre retorna `Some`.

La implementación en `reconstruct` acumula esta suma mediante `foldLeft`: para cada sombra `(xⱼ, yⱼ)` calcula el numerador (`basisNum`) y el denominador (`basisDen`) del polinomio base, invierte el denominador con `modInverse`, y suma el término `yⱼ · basisNum · inv(basisDen)` al acumulador. El resultado final es `f(0) = S`.

---

## Por qué t - 1 sombras son insuficientes

Con solo `t - 1` puntos hay infinitos polinomios de grado ≤ t - 1 que los interpolan — exactamente uno por cada valor posible de `a₀` en Z_p. La interpolación con `t - 1` sombras produce un polinomio de grado ≤ t - 2 cuyo valor en `x = 0` es, desde la perspectiva de un atacante, uniforme sobre los `p` posibles secretos. Por lo tanto, tener `t - 1` sombras no aporta ninguna información sobre `S`.

Esto se verifica experimentalmente con `verifyThreshold`: la función comprueba que todo subconjunto de `t` sombras reconstruye `S`, y que algún subconjunto de `t - 1` sombras no lo hace.

---

## Verificación numérica

Usando las tres primeras sombras del ejemplo del enunciado `(1, 1494), (2, 329), (3, 965)` con `p = 1613`:

**Cálculo de L₁(0)** con `x₁ = 1, y₁ = 1494`:
```
num = (0-2)·(0-3) = 6
den = (1-2)·(1-3) = 2
inv(2, 1613) = 807   // 2·807 = 1614 ≡ 1 (mod 1613)
L₁(0) = 6·807 mod 1613 = 3
y₁·L₁(0) = 1494·3 mod 1613 = 1256
```

**Cálculo de L₂(0)** con `x₂ = 2, y₂ = 329`:
```
num = (0-1)·(0-3) = 3
den = (2-1)·(2-3) = -1 ≡ 1612 (mod 1613)
inv(1612, 1613) = 1612
L₂(0) = 3·1612 mod 1613 = 1610
y₂·L₂(0) = 329·1610 mod 1613 = 626
```

**Cálculo de L₃(0)** con `x₃ = 3, y₃ = 965`:
```
num = (0-1)·(0-2) = 2
den = (3-1)·(3-2) = 2
L₃(0) = 2·807 mod 1613 = 1
y₃·L₃(0) = 965·1 mod 1613 = 965
```

**Suma final:**
```
S = (1256 + 626 + 965) mod 1613 = 2847 mod 1613 = 1234  ✓
```

El secreto `S = 1234` se recupera correctamente. La misma operación con cualquier otro subconjunto de 3 sombras produce el mismo resultado, como lo confirman los 49 tests que pasan en la suite de pruebas.
