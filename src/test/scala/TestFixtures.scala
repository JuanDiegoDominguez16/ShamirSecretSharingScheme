import scala.util.Random

object TestFixtures {

  /** Assignment examples (arithmetic and canonical polynomial). */
  val P1613: BigInt = BigInt(1613)

  val CanonicalSecret: BigInt = BigInt(1234)

  val CanonicalCoefs: Polynomial.Polynomial =
    List(
      CanonicalSecret,
      BigInt(166),
      BigInt(94)
    )

  val CanonicalShares: List[Shamir.Share] =
    List(
      (BigInt(1), BigInt(1494)),
      (BigInt(2), BigInt(329)),
      (BigInt(3), BigInt(965)),
      (BigInt(4), BigInt(176)),
      (BigInt(5), BigInt(1188))
    )

  /** Toy category: 20-bit secret, 24-bit prime. */
  object Juguete {

    val secretBits: Int = 20
    val primeBits: Int  = 24

    val p: BigInt =
      BigInt.probablePrime(primeBits, new Random(42L))

    val secret: BigInt =
      secretOfBits(secretBits, p, seed = 43L)

    val operandA: BigInt = secret

    val operandB: BigInt = {
      val b = BigInt(secretBits, new Random(44L)).abs % p
      if (b == 0) BigInt(1) else b
    }

    val coprimeA: BigInt =
      Iterator
        .from(2)
        .map(BigInt(_))
        .find(a => CryptoMath.extGcd(a % p, p)._1 == 1)
        .get % p
  }

  /** Small category: 200-bit secret, 208-bit prime. */
  object Pequena {

    val secretBits: Int = 200
    val primeBits: Int  = 208

    val p: BigInt =
      BigInt.probablePrime(primeBits, new Random(100L))

    val secret: BigInt =
      secretOfBits(secretBits, p, seed = 101L)
  }

  /** Medium category: 500-bit secret, 512-bit prime. */
  object Mediana {

    val secretBits: Int = 500
    val primeBits: Int  = 512

    val p: BigInt =
      BigInt.probablePrime(primeBits, new Random(200L))

    val secret: BigInt =
      secretOfBits(secretBits, p, seed = 201L)
  }

  /** Large category: 2000+-bit secret, 2048-bit prime. */
  object Grande {

    val secretBits: Int = 2000
    val primeBits: Int  = 2048

    val p: BigInt =
      BigInt.probablePrime(primeBits, new Random(300L))

    val secret: BigInt =
      secretOfBits(secretBits, p, seed = 301L)
  }

  // ---------------------------------------------------------------------------
  // Helpers
  // ---------------------------------------------------------------------------

  def bezoutHolds(
      a: BigInt,
      b: BigInt,
      g: BigInt,
      x: BigInt,
      y: BigInt
  ): Boolean =
    a * x + b * y == g

  def modMul(a: BigInt, b: BigInt, p: BigInt): BigInt =
    ((a * b) % p + p) % p

  def secretOfBits(secretBits: Int, p: BigInt, seed: Long): BigInt = {
    val raw     = BigInt(secretBits, new Random(seed)).abs
    val bounded = if (raw == 0) BigInt(1) else raw
    bounded % p
  }
}