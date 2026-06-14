# Matriz de trazabilidad TAD–operaciones

| Operación               | TAD Principal  | Operaciones Auxiliares                  |
| ----------------------- | -------------- | --------------------------------------- |
| `extGcd`                | CryptoMath     | Ninguna                                 |
| `modInverse`            | CryptoMath     | `extGcd`                                |
| `modPow`                | CryptoMath     | Ninguna                                 |
| `evalPoly`              | Polynomial     | Esquema de Horner                       |
| `buildSecretPolynomial` | Polynomial     | Generación pseudoaleatoria              |
| `split`                 | Share / Shamir | `buildSecretPolynomial`, `evalPoly`     |
| `reconstruct`           | Share / Shamir | `modInverse`, interpolación de Lagrange |
| `verifyThreshold`       | Share / Shamir | `reconstruct`, combinaciones            |
| `encode`                | MessageCodec   | Conversión UTF-8                        |
| `decode`                | MessageCodec   | Reconstrucción UTF-8                    |

## Relación entre módulos

| Módulo       | Depende de                                   |
| ------------ | -------------------------------------------- |
| CryptoMath   | Ninguno                                      |
| Polynomial   | Ninguno                                      |
| Shamir       | CryptoMath, Polynomial                       |
| MessageCodec | Ninguno                                      |
| Runner       | CryptoMath, Polynomial, Shamir, MessageCodec |

La arquitectura mantiene bajo acoplamiento entre módulos y alta cohesión interna. Las operaciones criptográficas se concentran en `CryptoMath`, el manejo algebraico de polinomios en `Polynomial`, la lógica del esquema de Shamir en `Shamir` y la transformación de mensajes en `MessageCodec`.
