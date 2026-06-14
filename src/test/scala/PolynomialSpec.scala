import org.scalatest.funsuite.AnyFunSuite

class PolynomialSpec extends AnyFunSuite {

  private val p = TestFixtures.P1613

  private val coefs =
    TestFixtures.CanonicalCoefs

  // ---------------------------------------------------------------------------
  // evalPoly
  // ---------------------------------------------------------------------------

  test("evalPoly(coefs,0,p) == 1234") {
    assert(
      Polynomial.evalPoly(
        coefs,
        BigInt(0),
        p
      ) == BigInt(1234)
    )
  }

  test("evalPoly(coefs,1,p) == 1494") {
    assert(
      Polynomial.evalPoly(
        coefs,
        BigInt(1),
        p
      ) == BigInt(1494)
    )
  }

  test("evalPoly(coefs,2,p) == 329") {
    assert(
      Polynomial.evalPoly(
        coefs,
        BigInt(2),
        p
      ) == BigInt(329)
    )
  }

  test("evalPoly(coefs,3,p) == 965") {
    assert(
      Polynomial.evalPoly(
        coefs,
        BigInt(3),
        p
      ) == BigInt(965)
    )
  }

  test("constant polynomial returns a0") {
    assert(
      Polynomial.evalPoly(
        List(BigInt(42)),
        BigInt(99),
        p
      ) == BigInt(42)
    )
  }

  // ---------------------------------------------------------------------------
  // buildSecretPolynomial
  // ---------------------------------------------------------------------------

  test("head is secret and degree is threshold-1") {

    val poly =
      Polynomial.buildSecretPolynomial(
        TestFixtures.CanonicalSecret,
        3,
        p,
        42L
      )

    assert(poly.head == TestFixtures.CanonicalSecret)
    assert(poly.length == 3)
  }

  test("same seed produces same polynomial") {

    val seed = 99L

    val once =
      Polynomial.buildSecretPolynomial(
        TestFixtures.CanonicalSecret,
        3,
        p,
        seed
      )

    val twice =
      Polynomial.buildSecretPolynomial(
        TestFixtures.CanonicalSecret,
        3,
        p,
        seed
      )

    assert(once == twice)
  }

  test("random coefficients belong to Z_p") {

    val poly =
      Polynomial.buildSecretPolynomial(
        TestFixtures.CanonicalSecret,
        4,
        p,
        7L
      )

    poly.tail.foreach { c =>
      assert(c >= 0)
      assert(c < p)
    }
  }

  // ---------------------------------------------------------------------------
  // Toy category
  // ---------------------------------------------------------------------------

  test("toy evalPoly is deterministic") {

    val pJ =
      TestFixtures.Juguete.p

    val secret =
      TestFixtures.Juguete.secret

    val poly =
      Polynomial.buildSecretPolynomial(
        secret,
        3,
        pJ,
        77L
      )

    val x = BigInt(5)

    val y1 =
      Polynomial.evalPoly(poly, x, pJ)

    val y2 =
      Polynomial.evalPoly(poly, x, pJ)

    assert(y1 == y2)
  }

  test("toy polynomial evaluates to secret at x=0") {

    val pJ =
      TestFixtures.Juguete.p

    val secret =
      TestFixtures.Juguete.secret

    val poly =
      Polynomial.buildSecretPolynomial(
        secret,
        3,
        pJ,
        88L
      )

    assert(poly.head == secret)

    assert(
      Polynomial.evalPoly(
        poly,
        BigInt(0),
        pJ
      ) == secret
    )
  }
}