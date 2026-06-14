# Diseño de los TADs y diagrama de dependencias funcionales

## TAD Polynomial

Representa un polinomio sobre el cuerpo finito.

### Representación

```scala
type Polynomial = List[BigInt]
```

La lista almacena los coeficientes en orden ascendente de grado:

```text
List(a0, a1, a2, ..., an)
```

### Operaciones

* `evalPoly(coefs, x, p)`: evalúa el polinomio en un punto utilizando el esquema de Horner.
* `buildSecretPolynomial(secret, threshold, p, seed)`: construye el polinomio secreto utilizado por el esquema de Shamir.

---

## TAD Share

Representa una sombra (share) del esquema de Shamir.

### Representación

```scala
type Share = (BigInt, BigInt)
```

donde:

* Primer componente: coordenada (x)
* Segundo componente: coordenada (y=f(x))

Cada sombra corresponde a un punto del polinomio secreto.

### Operaciones

* Generación mediante `split`.
* Reconstrucción mediante `reconstruct`.

---

## Dependencias funcionales

```text
                 +----------------+
                 |  CryptoMath    |
                 |----------------|
                 | extGcd         |
                 | modInverse     |
                 | modPow         |
                 +-------+--------+
                         |
                         |
                         v
                 +----------------+
                 |    Shamir      |
                 |----------------|
                 | split          |
                 | reconstruct    |
                 | verifyThreshold|
                 +-------+--------+
                         ^
                         |
                         |
                 +-------+--------+
                 |   Polynomial   |
                 |----------------|
                 | evalPoly       |
                 | buildSecret... |
                 +----------------+

                 +----------------+
                 | MessageCodec   |
                 |----------------|
                 | encode         |
                 | decode         |
                 +-------+--------+
                         |
                         v
                 +----------------+
                 |    Runner      |
                 +----------------+
```

El módulo `CryptoMath` proporciona las operaciones algebraicas básicas. `Polynomial` utiliza dichas operaciones para modelar polinomios. El módulo `Shamir` depende de ambos para generar y reconstruir secretos. Finalmente, `MessageCodec` transforma mensajes en enteros y viceversa, mientras que `Runner` integra todos los componentes para la ejecución completa del sistema.
