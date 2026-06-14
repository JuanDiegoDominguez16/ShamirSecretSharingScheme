import org.scalatest.funsuite.AnyFunSuite

/** Unit tests for split (§3.3), reconstruct (§3.4),
  * verifyThreshold (§3.5), and encode/decode (§3.6).
  */
class ShamirSpec extends AnyFunSuite {

  // ---------------------------------------------------------------------------
  // 3.3 split — assignment examples
  // ---------------------------------------------------------------------------

  test("3.3 split canonical example produces 5 shares") {
    val result =
      Shamir.split(
        secret    = BigInt(1234),
        threshold = 3,
        n         = 5,
        p         = TestFixtures.P1613,
        seed      = 42L
      )
    assert(result.isRight)
    assert(result.toOption.get.length == 5)
  }

  test("3.3 split canonical example matches expected shares") {
    val result =
      Shamir.split(
        secret    = BigInt(1234),
        threshold = 3,
        n         = 5,
        p         = TestFixtures.P1613,
        seed      = 42L
      )
    // x-coordinates must be 1..5
    result.foreach { shares =>
      assert(shares.map(_._1) == List(1, 2, 3, 4, 5).map(BigInt(_)))
    }
  }

  test("3.3 split returns Left when n < threshold") {
    val result =
      Shamir.split(
        secret    = BigInt(1234),
        threshold = 5,
        n         = 3,
        p         = TestFixtures.P1613,
        seed      = 42L
      )
    assert(result.isLeft)
  }

  test("3.3 split returns Left when p <= secret") {
    val result =
      Shamir.split(
        secret    = BigInt(2000),
        threshold = 3,
        n         = 5,
        p         = TestFixtures.P1613,
        seed      = 42L
      )
    assert(result.isLeft)
  }

  test("3.3 split returns Left when threshold < 2") {
    val result =
      Shamir.split(
        secret    = BigInt(100),
        threshold = 1,
        n         = 5,
        p         = TestFixtures.P1613,
        seed      = 42L
      )
    assert(result.isLeft)
  }

  test("3.3 split is pure: same seed produces same shares") {
    val r1 = Shamir.split(BigInt(1234), 3, 5, TestFixtures.P1613, 42L)
    val r2 = Shamir.split(BigInt(1234), 3, 5, TestFixtures.P1613, 42L)
    assert(r1 == r2)
  }

  // ---------------------------------------------------------------------------
  // 3.4 reconstruct — assignment examples
  // ---------------------------------------------------------------------------

  test("3.4 reconstruct from first 3 canonical shares") {
    val shares = TestFixtures.CanonicalShares.take(3)
    val result = Shamir.reconstruct(shares, TestFixtures.P1613)
    assert(result == Right(BigInt(1234)))
  }

  test("3.4 reconstruct from shares {2,4,5}") {
    val shares = List(
      TestFixtures.CanonicalShares(1),
      TestFixtures.CanonicalShares(3),
      TestFixtures.CanonicalShares(4)
    )
    val result = Shamir.reconstruct(shares, TestFixtures.P1613)
    assert(result == Right(BigInt(1234)))
  }

  test("3.4 reconstruct from shares {1,3,5}") {
    val shares = List(
      TestFixtures.CanonicalShares(0),
      TestFixtures.CanonicalShares(2),
      TestFixtures.CanonicalShares(4)
    )
    val result = Shamir.reconstruct(shares, TestFixtures.P1613)
    assert(result == Right(BigInt(1234)))
  }

  test("3.4 reconstruct from all 5 shares") {
    val result =
      Shamir.reconstruct(TestFixtures.CanonicalShares, TestFixtures.P1613)
    assert(result == Right(BigInt(1234)))
  }

  test("3.4 reconstruct returns Left for empty list") {
    assert(Shamir.reconstruct(Nil, TestFixtures.P1613).isLeft)
  }

  test("3.4 reconstruct returns Left for repeated x-coordinates") {
    val repeated = List(
      (BigInt(1), BigInt(1494)),
      (BigInt(1), BigInt(329))
    )
    assert(Shamir.reconstruct(repeated, TestFixtures.P1613).isLeft)
  }

  test("3.4 reconstruct order does not matter") {
    val forward  = TestFixtures.CanonicalShares.take(3)
    val reversed = forward.reverse
    assert(
      Shamir.reconstruct(forward, TestFixtures.P1613) ==
        Shamir.reconstruct(reversed, TestFixtures.P1613)
    )
  }

  // ---------------------------------------------------------------------------
  // 3.5 verifyThreshold — assignment example
  // ---------------------------------------------------------------------------

  test("3.5 verifyThreshold returns true for canonical example") {
    assert(
      Shamir.verifyThreshold(
        secret    = BigInt(1234),
        shares    = TestFixtures.CanonicalShares,
        threshold = 3,
        p         = TestFixtures.P1613
      )
    )
  }

  test("3.5 all t-subsets reconstruct the secret") {
    assert(
      Shamir.allThresholdSubsetsRecover(
        secret    = BigInt(1234),
        shares    = TestFixtures.CanonicalShares,
        threshold = 3,
        p         = TestFixtures.P1613
      )
    )
  }

  test("3.5 some (t-1)-subset fails to reconstruct") {
    assert(
      Shamir.someSubThresholdSubsetFails(
        secret    = BigInt(1234),
        shares    = TestFixtures.CanonicalShares,
        threshold = 3,
        p         = TestFixtures.P1613
      )
    )
  }

  // ---------------------------------------------------------------------------
  // 3.6 encode / decode
  // ---------------------------------------------------------------------------

  test("3.6 encode(\"Hola\") == 1215261793") {
    assert(MessageCodec.encode("Hola") == BigInt(1215261793))
  }

  test("3.6 decode(1215261793) == \"Hola\"") {
    assert(MessageCodec.decode(BigInt(1215261793)) == "Hola")
  }

  test("3.6 encode(\"S3CR3T_2026\") matches expected value") {
    assert(
      MessageCodec.encode("S3CR3T_2026") ==
        BigInt("100582925573664214709449270")
    )
  }

  test("3.6 decode(encode(s)) == s for \"S3CR3T_2026\"") {
    val s = "S3CR3T_2026"
    assert(MessageCodec.decode(MessageCodec.encode(s)) == s)
  }

  test("3.6 decode(encode(s)) roundtrip for arbitrary string") {
    val s = "NumeriCustodia SAS — clave maestra 2026"
    assert(MessageCodec.decode(MessageCodec.encode(s)) == s)
  }

  // ---------------------------------------------------------------------------
  // Toy category — split + reconstruct + verifyThreshold
  // ---------------------------------------------------------------------------

  test("Toy: split then reconstruct recovers secret") {
    val p      = TestFixtures.Juguete.p
    val secret = TestFixtures.Juguete.secret
    val Right(shares) =
      Shamir.split(secret, threshold = 3, n = 5, p = p, seed = 77L)
    val result = Shamir.reconstruct(shares.take(3), p)
    assert(result == Right(secret))
  }

  test("Toy: verifyThreshold") {
    val p      = TestFixtures.Juguete.p
    val secret = TestFixtures.Juguete.secret
    val Right(shares) =
      Shamir.split(secret, threshold = 3, n = 5, p = p, seed = 77L)
    assert(Shamir.verifyThreshold(secret, shares, threshold = 3, p = p))
  }
  test("Toy: split execution time (reported)") {

    val p = TestFixtures.Juguete.p
    val secret = TestFixtures.Juguete.secret

    val iterations = 10000

    val t0 = System.nanoTime()

    for (_ <- 1 to iterations) {
      Shamir.split(secret, threshold = 3, n = 5, p = p, seed = 77L)
    }

    val avgMs =
      (System.nanoTime() - t0).toDouble / iterations / 1000000.0

    println(f"[Toy] split average time: $avgMs%.6f ms")

    assert(true)
  }

  test("Toy: reconstruct execution time (reported)") {

    val p = TestFixtures.Juguete.p
    val secret = TestFixtures.Juguete.secret

    val Right(shares) =
      Shamir.split(secret, threshold = 3, n = 5, p = p, seed = 77L)

    val iterations = 10000

    val t0 = System.nanoTime()

    for (_ <- 1 to iterations) {
      Shamir.reconstruct(shares.take(3), p)
    }

    val avgMs =
      (System.nanoTime() - t0).toDouble / iterations / 1000000.0

    println(f"[Toy] reconstruct average time: $avgMs%.6f ms")

    assert(true)
  }
  // ---------------------------------------------------------------------------
  // Small category — split + reconstruct
  // ---------------------------------------------------------------------------

  test("Small: split then reconstruct recovers secret") {
    val p      = TestFixtures.Pequena.p
    val secret = TestFixtures.Pequena.secret
    val Right(shares) =
      Shamir.split(secret, threshold = 3, n = 5, p = p, seed = 100L)
    assert(Shamir.reconstruct(shares.take(3), p) == Right(secret))
  }

  test("Small: verifyThreshold") {
    val p      = TestFixtures.Pequena.p
    val secret = TestFixtures.Pequena.secret
    val Right(shares) =
      Shamir.split(secret, threshold = 3, n = 5, p = p, seed = 100L)
    assert(Shamir.verifyThreshold(secret, shares, threshold = 3, p = p))
  }

  test("Small: split execution time (reported)") {

    val p = TestFixtures.Pequena.p
    val secret = TestFixtures.Pequena.secret

    val iterations = 5000

    val t0 = System.nanoTime()

    for (_ <- 1 to iterations) {
      Shamir.split(secret, threshold = 3, n = 5, p = p, seed = 100L)
    }

    val avgMs =
      (System.nanoTime() - t0).toDouble / iterations / 1000000.0

    println(f"[Small] split average time: $avgMs%.6f ms")

    assert(true)
  }

  test("Small: reconstruct execution time (reported)") {

    val p = TestFixtures.Pequena.p
    val secret = TestFixtures.Pequena.secret

    val Right(shares) =
      Shamir.split(secret, threshold = 3, n = 5, p = p, seed = 100L)

    val iterations = 5000

    val t0 = System.nanoTime()

    for (_ <- 1 to iterations) {
      Shamir.reconstruct(shares.take(3), p)
    }

    val avgMs =
      (System.nanoTime() - t0).toDouble / iterations / 1000000.0

    println(f"[Small] reconstruct average time: $avgMs%.6f ms")

    assert(true)
  }
  // ---------------------------------------------------------------------------
  // Medium category — split + reconstruct
  // ---------------------------------------------------------------------------

  test("Medium: split then reconstruct recovers secret") {
    val p      = TestFixtures.Mediana.p
    val secret = TestFixtures.Mediana.secret
    val Right(shares) =
      Shamir.split(secret, threshold = 3, n = 5, p = p, seed = 200L)
    assert(Shamir.reconstruct(shares.take(3), p) == Right(secret))
  }

  test("Medium: verifyThreshold") {
    val p      = TestFixtures.Mediana.p
    val secret = TestFixtures.Mediana.secret
    val Right(shares) =
      Shamir.split(secret, threshold = 3, n = 5, p = p, seed = 200L)
    assert(Shamir.verifyThreshold(secret, shares, threshold = 3, p = p))
  }

  test("Medium: split execution time (reported)") {

    val p = TestFixtures.Mediana.p
    val secret = TestFixtures.Mediana.secret

    val iterations = 1000

    val t0 = System.nanoTime()

    for (_ <- 1 to iterations) {
      Shamir.split(secret, threshold = 3, n = 5, p = p, seed = 200L)
    }

    val avgMs =
      (System.nanoTime() - t0).toDouble / iterations / 1000000.0

    println(f"[Medium] split average time: $avgMs%.6f ms")

    assert(true)
  }

  test("Medium: reconstruct execution time (reported)") {

    val p = TestFixtures.Mediana.p
    val secret = TestFixtures.Mediana.secret

    val Right(shares) =
      Shamir.split(secret, threshold = 3, n = 5, p = p, seed = 200L)

    val iterations = 1000

    val t0 = System.nanoTime()

    for (_ <- 1 to iterations) {
      Shamir.reconstruct(shares.take(3), p)
    }

    val avgMs =
      (System.nanoTime() - t0).toDouble / iterations / 1000000.0

    println(f"[Medium] reconstruct average time: $avgMs%.6f ms")

    assert(true)
  }
  // ---------------------------------------------------------------------------
  // Large category — split + reconstruct + timing
  // ---------------------------------------------------------------------------

  test("Large: split then reconstruct recovers secret") {
    val p      = TestFixtures.Grande.p
    val secret = TestFixtures.Grande.secret
    val Right(shares) =
      Shamir.split(secret, threshold = 3, n = 5, p = p, seed = 300L)
    assert(Shamir.reconstruct(shares.take(3), p) == Right(secret))
  }

  test("Large: split execution time (reported)") {
    val p      = TestFixtures.Grande.p
    val secret = TestFixtures.Grande.secret
    val t0     = System.currentTimeMillis()
    Shamir.split(secret, threshold = 3, n = 5, p = p, seed = 300L)
    val elapsed = System.currentTimeMillis() - t0
    println(s"[Large] split time: ${elapsed} ms")
    assert(true) // tiempo se reporta en consola
  }

  test("Large: reconstruct execution time (reported)") {
    val p      = TestFixtures.Grande.p
    val secret = TestFixtures.Grande.secret
    val Right(shares) =
      Shamir.split(secret, threshold = 3, n = 5, p = p, seed = 300L)
    val t0      = System.currentTimeMillis()
    Shamir.reconstruct(shares.take(3), p)
    val elapsed = System.currentTimeMillis() - t0
    println(s"[Large] reconstruct time: ${elapsed} ms")
    assert(true)
  }

  test("Large: encode then full split/reconstruct/decode roundtrip") {
    val message = "clave-maestra-2026"
    val encoded = MessageCodec.encode(message)
    val p =
      BigInt.probablePrime(encoded.bitLength + 8, new scala.util.Random(999L))
    val Right(shares) =
      Shamir.split(encoded, threshold = 3, n = 5, p = p, seed = 400L)
    val Right(recovered) = Shamir.reconstruct(shares.take(3), p)
    assert(MessageCodec.decode(recovered) == message)
  }
}