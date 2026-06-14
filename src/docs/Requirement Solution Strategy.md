# Estrategia funcional de solución para cada requerimiento

## 3.1 Operaciones aritméticas modulares

### 3.1.1 Extended Euclidean Algorithm

1. Inicializar los coeficientes de Bézout.
2. Aplicar iterativamente la división euclidiana.
3. Actualizar residuos y coeficientes mediante recursión de cola.
4. Finalizar cuando el residuo sea cero.
5. Retornar `(gcd, x, y)`.

---

### 3.1.2 Modular Inverse

1. Normalizar el valor de entrada.
2. Ejecutar `extGcd`.
3. Verificar si el máximo común divisor es 1.
4. Si existe inverso, retornar `Some(inverso)`.
5. En caso contrario retornar `None`.

---

### 3.1.3 Modular Exponentiation

1. Inicializar un acumulador con valor 1.
2. Aplicar el método Square-and-Multiply.
3. Si el exponente es impar, multiplicar por la base.
4. Elevar la base al cuadrado en cada iteración.
5. Reducir módulo (p) en cada paso.
6. Finalizar cuando el exponente sea cero.

---

## 3.2 Polinomios

### 3.2.1 Evaluación de polinomios

1. Invertir la lista de coeficientes.
2. Aplicar el esquema de Horner.
3. Mantener un acumulador parcial.
4. Reducir módulo (p) en cada iteración.
5. Retornar el valor final.

---

### 3.2.2 Construcción del polinomio secreto

1. Fijar el secreto como término independiente.
2. Generar coeficientes pseudoaleatorios mediante una semilla.
3. Construir una lista inmutable de coeficientes.
4. Retornar el polinomio resultante.

---

## 3.3 Generación de sombras

1. Validar parámetros de entrada.
2. Construir el polinomio secreto.
3. Evaluar el polinomio para cada valor (x=1,\dots,n).
4. Generar la lista de sombras.
5. Retornar el resultado mediante `Either`.

---

## 3.4 Reconstrucción del secreto

1. Verificar que existan sombras válidas.
2. Comprobar que las coordenadas (x) sean distintas.
3. Calcular los coeficientes de Lagrange.
4. Obtener inversos modulares de los denominadores.
5. Acumular cada término interpolado.
6. Evaluar el resultado en (x=0).
7. Retornar el secreto recuperado.

---

## 3.5 Verificación experimental

1. Generar todas las combinaciones de tamaño (t).
2. Reconstruir el secreto para cada combinación.
3. Verificar que todas recuperen el valor correcto.
4. Generar combinaciones de tamaño (t-1).
5. Confirmar que al menos una falla.
6. Concluir si se cumple la propiedad de umbral.

---

## 3.6 Codificación de mensajes

### Codificación

1. Convertir el mensaje a UTF-8.
2. Interpretar la secuencia de bytes como un entero en base 256.
3. Retornar el valor entero.

### Decodificación

1. Descomponer el entero en bytes.
2. Reconstruir la secuencia UTF-8.
3. Convertir los bytes nuevamente en texto.
