import scala.annotation.tailrec
import scala.util.Random
import java.nio.charset.StandardCharsets

object Polynomial {

  type Polynomial = List[BigInt]

  def evalPoly(coefs: Polynomial, x: BigInt, p: BigInt): BigInt = {

    @tailrec
    def loop(remaining: List[BigInt], acc: BigInt): BigInt =
      remaining match {
        case Nil => acc
        case h :: t =>
          val newAcc = ((acc * x + h) % p + p) % p
          loop(t, newAcc)
      }

    loop(coefs.reverse, BigInt(0))
  }

  // Preconditions (threshold >= 2, p > secret) are documented and
  // validated upstream by Shamir.split via Either. We do NOT use
  // `require` here because it throws IllegalArgumentException, which
  // is forbidden by section 4 of the assignment.
  def buildSecretPolynomial(
                             secret: BigInt,
                             threshold: Int,
                             p: BigInt,
                             seed: Long
                           ): Polynomial = {

    val rng = new Random(seed)

    val randomCoefs =
      List.fill(threshold - 1) {
        BigInt(p.bitLength, rng) % p
      }

    secret :: randomCoefs
  }
}

object CryptoMath {

  def extGcd(a: BigInt, b: BigInt): (BigInt, BigInt, BigInt) = {

    @tailrec
    def loop(
              oldR: BigInt,
              r: BigInt,
              oldS: BigInt,
              s: BigInt,
              oldT: BigInt,
              t: BigInt
            ): (BigInt, BigInt, BigInt) =

      if (r == 0) (oldR, oldS, oldT)
      else {
        val q = oldR / r

        loop(
          r,
          oldR - q * r,
          s,
          oldS - q * s,
          t,
          oldT - q * t
        )
      }

    loop(a, b, BigInt(1), BigInt(0), BigInt(0), BigInt(1))
  }

  def modInverse(a: BigInt, p: BigInt): Option[BigInt] = {

    val aReduced = ((a % p) + p) % p

    val (g, x, _) = extGcd(aReduced, p)

    if (g == 1)
      Some(((x % p) + p) % p)
    else
      None
  }

  def modPow(base: BigInt, exp: BigInt, mod: BigInt): BigInt = {

    @tailrec
    def loop(b: BigInt, e: BigInt, acc: BigInt): BigInt =
      if (e == 0) acc
      else if ((e % 2) == 1)
        loop((b * b) % mod, e / 2, (acc * b) % mod)
      else
        loop((b * b) % mod, e / 2, acc)

    if (mod == 1) BigInt(0)
    else loop(((base % mod) + mod) % mod, exp, BigInt(1))
  }
}

object Shamir {

  type Share = (BigInt, BigInt)

  def split(
             secret: BigInt,
             threshold: Int,
             n: Int,
             p: BigInt,
             seed: Long
           ): Either[String, List[Share]] = {

    validateSplitInputs(secret, threshold, n, p) match {

      case Left(err) => Left(err)

      case Right(_) =>

        val poly =
          Polynomial.buildSecretPolynomial(secret, threshold, p, seed)

        Right(
          (1 to n).toList.map { i =>
            val x = BigInt(i)
            (x, Polynomial.evalPoly(poly, x, p))
          }
        )
    }
  }

  private def validateSplitInputs(
                                   secret: BigInt,
                                   threshold: Int,
                                   n: Int,
                                   p: BigInt
                                 ): Either[String, Unit] =

    if (secret < 0)
      Left("secret debe ser >= 0")
    else if (threshold < 2)
      Left("threshold debe ser >= 2")
    else if (n < threshold)
      Left("n debe ser >= threshold")
    else if (p <= secret)
      Left("p debe ser > secret")
    else if (p <= BigInt(n))
      Left("p debe ser > n")
    else
      Right(())

  // Returns Right(secret) on success, Left("...") on invalid input
  // (empty list, repeated x coordinates, or non-invertible denominator).
  // No exceptions are thrown, complying with section 4.
  def reconstruct(shares: List[Share], p: BigInt): Either[String, BigInt] = {

    def mod(x: BigInt): BigInt =
      ((x % p) + p) % p

    if (shares.isEmpty)
      Left("se requiere al menos una sombra")
    else if (shares.map(_._1).distinct.size != shares.size)
      Left("las coordenadas x de las sombras deben ser distintas")
    else
      shares.foldLeft[Either[String, BigInt]](Right(BigInt(0))) {

        case (acc, (xj, yj)) =>

          acc.flatMap { sumSoFar =>

            val basisNum =
              shares.foldLeft(BigInt(1)) {

                case (a, (xm, _)) =>
                  if (xm == xj) a
                  else mod(a * (-xm))
              }

            val basisDen =
              shares.foldLeft(BigInt(1)) {

                case (a, (xm, _)) =>
                  if (xm == xj) a
                  else mod(a * (xj - xm))
              }

            CryptoMath.modInverse(basisDen, p) match {

              case None =>
                Left(s"denominador no invertible mod p para x = $xj")

              case Some(inv) =>
                val term = mod(yj * mod(basisNum * inv))
                Right(mod(sumSoFar + term))
            }
          }
      }
  }

  // 3.5 -- Experimental verification of the (t, n) threshold property.
  // Returns true iff every t-subset reconstructs the secret AND some
  // (t - 1)-subset does NOT reconstruct it.
  def verifyThreshold(
                       secret: BigInt,
                       shares: List[Share],
                       threshold: Int,
                       p: BigInt
                     ): Boolean =

    allThresholdSubsetsRecover(secret, shares, threshold, p) &&
      someSubThresholdSubsetFails(secret, shares, threshold, p)

  def allThresholdSubsetsRecover(
                                  secret: BigInt,
                                  shares: List[Share],
                                  threshold: Int,
                                  p: BigInt
                                ): Boolean =

    shares.combinations(threshold).forall { sub =>
      reconstruct(sub, p).fold(_ => false, _ == secret)
    }

  def someSubThresholdSubsetFails(
                                   secret: BigInt,
                                   shares: List[Share],
                                   threshold: Int,
                                   p: BigInt
                                 ): Boolean =

    if (threshold <= 1) true
    else
      shares.combinations(threshold - 1).exists { sub =>
        reconstruct(sub, p).fold(_ => true, _ != secret)
      }
}

object MessageCodec {

  def encode(message: String): BigInt = {

    val bytes: List[Int] =
      message
        .getBytes(StandardCharsets.UTF_8)
        .toList
        .map(b => b & 0xff)

    bytes.foldLeft(BigInt(0)) { (acc, b) =>
      acc * 256 + BigInt(b)
    }
  }

  def decode(value: BigInt): String = {

    @tailrec
    def loop(n: BigInt, acc: List[Byte]): List[Byte] =
      if (n == 0) acc
      else loop(n / 256, (n % 256).toByte :: acc)

    val bytes =
      if (value == 0) List.empty[Byte]
      else loop(value, List.empty[Byte])

    new String(bytes.toArray, StandardCharsets.UTF_8)
  }
}

object Runner {

  def main(args: Array[String]): Unit = {

    val message = "Computacion y estructuras discretas 2"

    val encoded = MessageCodec.encode(message)

    println("Mensaje original: " + message)
    println("Codificado: " + encoded)

    val decoded = MessageCodec.decode(encoded)

    println("Decodificado: " + decoded)

    // Pick a real prime above the secret, with the 8-bit margin
    // recommended by the assignment.
    val p =
      BigInt.probablePrime(encoded.bitLength + 8, new Random(42L))

    println("Primo p: " + p)

    val result =
      Shamir.split(
        secret = encoded,
        threshold = 3,
        n = 5,
        p = p,
        seed = 42L
      )

    println(result)

    result match {

      case Right(shares) =>

        Shamir.reconstruct(shares.take(3), p) match {

          case Right(recovered) =>
            println("Secreto recuperado: " + recovered)
            println("Mensaje recuperado: " + MessageCodec.decode(recovered))

          case Left(err) =>
            println("Error al reconstruir: " + err)
        }

        val ok =
          Shamir.verifyThreshold(
            secret = encoded,
            shares = shares,
            threshold = 3,
            p = p
          )

        println("Propiedad de umbral verificada: " + ok)

      case Left(err) =>
        println("Error en split: " + err)
    }
  }
}