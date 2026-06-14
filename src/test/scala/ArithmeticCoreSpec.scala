import org.scalatest.funsuite.AnyFunSuite

class ArithmeticCoreSpec extends AnyFunSuite {

  // ---------------------------------------------------------------------------
  // 3.1.1 extGcd
  // ---------------------------------------------------------------------------

  test("3.1.1 extGcd(240, 46)") {
    val (g, x, y) =
      CryptoMath.extGcd(BigInt(240), BigInt(46))

    assert(g == BigInt(2))
    assert(240 * x + 46 * y == g)
  }

  test("3.1.1 extGcd(17, 1613)") {
    val (g, x, y) =
      CryptoMath.extGcd(BigInt(17), BigInt(1613))

    assert(g == BigInt(1))
    assert(17 * x + 1613 * y == g)
  }

  test("3.1.1 extGcd(8, 12)") {
    val (g, x, y) =
      CryptoMath.extGcd(BigInt(8), BigInt(12))

    assert(g == BigInt(4))
    assert(8 * x + 12 * y == g)
  }

  // ---------------------------------------------------------------------------
  // 3.1.2 modInverse
  // ---------------------------------------------------------------------------

  test("3.1.2 modInverse(17,1613)") {

    val inv =
      CryptoMath.modInverse(BigInt(17), BigInt(1613))

    assert(inv.isDefined)

    assert(
      (BigInt(17) * inv.get) % BigInt(1613) == 1
    )
  }

  test("3.1.2 modInverse(6,12)") {
    assert(
      CryptoMath.modInverse(BigInt(6), BigInt(12)).isEmpty
    )
  }

  // ---------------------------------------------------------------------------
  // 3.1.3 modPow
  // ---------------------------------------------------------------------------

  test("3.1.3 modPow(7,128,1613)") {

    val expected =
      BigInt(7).modPow(BigInt(128), BigInt(1613))

    assert(
      CryptoMath.modPow(
        BigInt(7),
        BigInt(128),
        BigInt(1613)
      ) == expected
    )
  }

  test("3.1.3 modPow(2,10,1000)") {
    assert(
      CryptoMath.modPow(
        BigInt(2),
        BigInt(10),
        BigInt(1000)
      ) == BigInt(24)
    )
  }

  test("3.1.3 modPow(5,0,13)") {
    assert(
      CryptoMath.modPow(
        BigInt(5),
        BigInt(0),
        BigInt(13)
      ) == BigInt(1)
    )
  }

}