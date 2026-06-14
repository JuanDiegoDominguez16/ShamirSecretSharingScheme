# 🔐 Shamir Secret Sharing Scheme

> Scala implementation of Shamir's Secret Sharing algorithm developed as part of a university team project.

![Scala](https://img.shields.io/badge/Scala-2.13-red?logo=scala)
![License](https://img.shields.io/badge/License-Educational-blue)
![Status](https://img.shields.io/badge/Status-Completed-success)

---

## 📖 Overview

This project implements **Shamir's Secret Sharing (SSS)**, a cryptographic algorithm proposed by Adi Shamir in 1979. The scheme allows a secret to be divided into multiple shares such that:

- Any subset of at least **k shares** can reconstruct the secret.
- Any subset with fewer than **k shares** reveals no information about the original secret.

The implementation uses arithmetic over finite fields and polynomial interpolation to securely split and reconstruct secrets.

---

## 🧠 Mathematical Background

Given a secret `s`, the algorithm constructs a random polynomial of degree `k - 1`:

\[
f(x) = s + a_1x + a_2x^2 + \cdots + a_{k-1}x^{k-1} \pmod p
\]

where:

- `s` is the secret,
- `k` is the reconstruction threshold,
- `p` is a prime number larger than the secret.

The generated shares are points of the polynomial:

\[
(x_i, f(x_i))
\]

To recover the secret, **Lagrange interpolation** is used to evaluate the polynomial at `x = 0`.

---

##  Features

-  Secret splitting using Shamir's Secret Sharing scheme.
-  Secret reconstruction from valid subsets of shares.
-  Modular arithmetic over finite fields.
-  Polynomial evaluation and interpolation.
-  Efficient implementation using tail recursion where appropriate.
-  Functional testing to verify correctness.

---

## 🛠 Technologies Used

- **Scala**
- Functional Programming concepts
- Git & GitHub
- BigInt arithmetic

---

##  Project Structure

```text
src/
├── CryptoMath.scala      # Modular arithmetic utilities
├── Polynomial.scala      # Polynomial operations
├── Shamir.scala          # Secret splitting and reconstruction
├── Share.scala           # Share representation
└── ...
```

---

## 🚀 Running the Project

### Clone the repository

```bash
git clone https://github.com/JuanDiegoDominguez16/shamir-secret-sharing.git
```

### Enter the project directory

```bash
cd shamir-secret-sharing
```

### Compile and run

Using SBT:

```bash
sbt run
```

---

## 💻 Example Usage

### Splitting a secret

```scala
val secret = BigInt(12345)
val shares = Shamir.split(
  secret = secret,
  totalShares = 5,
  threshold = 3,
  prime = BigInt(...)
)
```

### Reconstructing the secret

```scala
val recovered = Shamir.reconstruct(
  shares.take(3),
  prime
)
```

Output:

```text
Recovered secret: 12345
```

---

## 🧪 Testing

The project includes tests and validation procedures to verify that:

- The secret is correctly reconstructed using at least `k` shares.
- Reconstruction fails when insufficient shares are provided.
- Polynomial operations behave as expected.

---

##  Team Project

This project was originally developed as part of a university assignment and was completed collaboratively.

### My contributions

- Implemented a substantial portion of the project's core functionality.
- Developed components related to modular arithmetic.
- Participated in the implementation of cryptographic operations.
- Conducted functional testing and validation.

### Other contributors

This project also benefited from the work of my teammates in areas such as documentation, testing, and additional implementation tasks.

---

##  Educational Purpose

This repository is intended to showcase the implementation of a real cryptographic algorithm and demonstrate concepts related to:

- Finite field arithmetic
- Polynomial interpolation
- Functional programming in Scala
- Collaborative software development

---

##  References

- Shamir, A. (1979). *How to Share a Secret*. Communications of the ACM.
- Menezes, A., van Oorschot, P., & Vanstone, S. *Handbook of Applied Cryptography*.
- Kenneth H. Rosen. Matemáticas Discretas y sus Aplicaciones, 5ta edición, McGraw-Hill. Capítulos de teoría de números (Euclides, Bezout, congruencias).
- Thomas H. Cormen, Charles E. Leiserson, Ronald L. Rivest, Clifford Stein. Introduction to Algorithms, 3rd edition, MIT Press, 2009. Capítulo 31 (Number-theoretic algorithms).
- Martin Odersky, Lex Spoon, Bill Venners. Programming in Scala, 3rd edition, Artima Press, 2016. Capítulos 8–9 (funciones de alto orden, currying).


---

##  Author

**Juan Diego Domínguez**

GitHub: https://github.com/JuanDiegoDominguez16

---

## ⚠ Disclaimer

This implementation was developed for **educational purposes** and has **not been audited for production use**.
