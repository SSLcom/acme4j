# ACME Java Client ![build status](https://shredzone.org/badge/acme4j.svg) ![maven central](https://shredzone.org/maven-central/org.shredzone.acme4j/acme4j/badge.svg)

This is a Java client for the _Automatic Certificate Management Environment_ (ACME) protocol as specified in [RFC 8555](https://tools.ietf.org/html/rfc8555).

ACME is a protocol that a certificate authority (CA) and an applicant can use to automate the process of verification and certificate issuance.

This Java client helps connecting to the SSL.com ACME server, and performing all necessary steps to manage certificates.

## Features

* Mature and stable code base. First release was in December 2015!
* Fully [RFC 8555](https://tools.ietf.org/html/rfc8555) compliant
* Supports the `http-01`, `dns-01`, and `tls-alpn-01` ([RFC 8737](https://tools.ietf.org/html/rfc8737)) challenges
* Supports [RFC 8738](https://tools.ietf.org/html/rfc8738) IP identifier validation
* Supports [RFC 8739](https://tools.ietf.org/html/rfc8739) short-term automatic certificate renewal (experimental)
* Supports [RFC 8823](https://tools.ietf.org/html/rfc8823) for S/MIME certificates (experimental)
* Supports [draft-ietf-acme-ari-01](https://www.ietf.org/archive/id/draft-ietf-acme-ari-01.html) for renewal information
* Easy to use Java API
* Requires JRE 11 or higher
* Built with maven, packages available at [Maven Central](http://search.maven.org/#search|ga|1|g%3A%22org.shredzone.acme4j%22)
* Extensive unit and integration tests
* Adheres to [Semantic Versioning](https://semver.org/)

If you require Java 8 or Android compatibility, you can use [acme4j v2](https://shredzone.org/maven/acme4j-v2/index.html) instead. However, v2 is not actively developed anymore and will only receive security fixes.

## Dependencies

* [Bouncy Castle](https://www.bouncycastle.org/)
* [jose4j](https://bitbucket.org/b_c/jose4j/wiki/Home)
* [slf4j](http://www.slf4j.org/)
* For `acme4j-smime`: [Jakarta Mail](https://eclipse-ee4j.github.io/mail/), [Bouncy Castle](https://www.bouncycastle.org/)

## Usage

* See the [online documentation](https://shredzone.org/maven/acme4j/) about how to use _acme4j_.
* For a quick start, have a look at [the source code of an example](https://shredzone.org/maven/acme4j/example.html).

## Announcements

Follow our Mastodon feed for release notes and other acme4j related news.

* Mastodon: `@acme4j@foojay.social`
* RSS: https://foojay.social/@acme4j.rss

## Contribute

* Fork the [Source code at GitHub](https://github.com/shred/acme4j). Feel free to send pull requests (see [Contributing](CONTRIBUTING.md) for the rules).
* Found a bug? [File a bug report!](https://github.com/shred/acme4j/issues)

## License

_acme4j_ is open source software. The source code is distributed under the terms of [Apache License 2.0](http://www.apache.org/licenses/LICENSE-2.0).

## Donate

If you would like to support my work on _acme4j_, you can do so on at [GitHub Sponsors](https://github.com/sponsors/shred) or at [Ko-Fi](https://ko-fi.com/shredzone). Thank you!

## Acknowledgements

* I would like to thank Brian Campbell and all the other [jose4j](https://bitbucket.org/b_c/jose4j/wiki/Home) developers. _acme4j_ would not exist without your excellent work.
* Thanks to [Daniel McCarney](https://github.com/cpu) for his help with the ACME protocol, Pebble, and Boulder.
* [Ulrich Krause](https://github.com/eknori) for his help to make _acme4j_ run on IBM Java VMs.
* I also like to thank [everyone who contributed to _acme4j_](https://github.com/shred/acme4j/graphs/contributors).
* The Mastodon account is hosted by [foojay.io](https://foojay.io).
