# hashids.clj

A Clojure library to generate short unique ids from integers, for brevity and obfuscation of database IDs.

This is port of the [hashids](http://hashids.org) library, and aims to be functionally identical to other implementations, while remaing pure clojure, without a dependency on any other library.

> **NOTE**   Hashids is not a [cryptographic hash](http://en.wikipedia.org/wiki/Cryptographic_hash_function), as it is reversible, and is not suitable for security purposes.  Don't try to encode secrets with hashids.

## Installation

In your Leiningen `project.clj`'s `:dependencies`:
```
[jstrutz/hashids "1.0.1"]
```

## Usage

### Setting up

You can pass a unique salt value so your hashes differ from everyone else's.
I use **this is my salt** as an example.

```clojure
(require '[hashids.core :as h])
(def hashids-opts {:salt "this is my salt"}) ;; These options are used for most examples below
```

### Encoding

```clojure
;; Encode a single number
(h/encode hashids-opts 12345) ;; => "NkK9"

;; Encode a set of numbers
(h/encode hashids-opts 683, 94108, 123, 5) ;; => "aBMswoO2UB3Sj"

;; or a collection
(h/encode hashids-opts [683, 94108, 123, 5]) ;; => "aBMswoO2UB3Sj"

;; A minimum length can be set for the encoded string
(h/encode {:salt "this is my salt" :min-length 4} 12345) ;; => "NkK9"
(h/encode {:salt "this is my salt" :min-length 5} 12345) ;; => "0NkK9"
(h/encode {:salt "this is my salt" :min-length 6} 12345) ;; => "0NkK9A"
(h/encode {:salt "this is my salt" :min-length 7} 12345) ;; => "10NkK9A"

;; Use a custom alphabet
(h/encode {:alphabet "0123456789uvwxyz"} 12345) ;; => "v95w8x"

;; Encode into hexadecimal
(h/encode-hex hashids-opts "deadbeef") ;; => "kRNrpKlJ"
```

### Decoding

Notice during decoding, same salt value is used:

```clojure
(h/decode hashids-opts "NkK9") ;; => (12345)
(h/decode hashids-opts "aBMswoO2UB3Sj") ;; => (683, 94108, 123, 5)

;; Decoding with a wrong salt will return an empty collection
(h/decode {:salt "wrong salt"} "aBMswoO2UB3Sj") ;; => ()

;; Decoding a string which is more than the given alphabet will return
;; an empty collection also
(h/decode {:alphabet "0123456789uvwxyz"} "PPPP") ;; => ()

;; Decode from hexadecimal
(h/decode-hex hashids-opts "kRNrpKlJ") ;; => ("deadbeef")
```

## Randomness

The primary purpose of hashids is to obfuscate ids. It's not meant or tested to be used for security purposes or compression.
Having said that, this algorithm does try to make these hashes unguessable and unpredictable:

### Repeating numbers

You don't see any repeating patterns for identical numbers:

```clojure
(h/encode hashids-opts 5 5 5 5) ;; => "1Wc8cwcE"
```

Same with incremented numbers:

```clojure
(h/encode hashids-opts 1 2 3 4 5 6 7 8 9 10) ;; => "kRHnurhptKcjIDTWC3sx"
```

### Incrementing numbers:

```clojure
(h/encode hashids-opts 1) ;; => "NV"
(h/encode hashids-opts 2) ;; => "6m"
(h/encode hashids-opts 3) ;; => "yD"
(h/encode hashids-opts 4) ;; => "2l"
(h/encode hashids-opts 5) ;; => "rD"
```

### Curse words

This code was written with the intent of placing created ids in visible places - like the URL. Which makes it unfortunate if generated hashes accidentally formed a bad word.

Therefore, the algorithm tries to avoid generating most common English curse words. This is done by never placing the following letters next to each other:

	c, C, s, S, f, F, h, H, u, U, i, I, t, T

## API

Hashids provides the following functions:

### `encode` - Encodes one or more numbers into a _hashid string_

`(encode opts & numbers)`

> where `opts` is a map with the following keys, each optional:
- `:salt` - a string to customize the encoding and decoding
  - Must be a string, if specified
  - Must be comprised of characters from the `:alphabet` option
  - Default: `""`
- `:min-length` - the minimum length of the returned string.
	- Must be an integer >= 0, if supplied
	- Default: `0`
- `:alphabet` - a string containing the acceptable characters to return.
  - Must be a string of at least 16 unique characters, if specified
  - Default: `"abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ1234567890"`

> `numbers` is one or more non-negative integers, or a collection (vector, list, etc.) of non-negative integers

**Returns** a string representing the encoded numbers.

### `encode-hex` - Encodes multiple hexadecimal numbers into a _hashid string_

`(encode-hex opts & strings)`

> `opts` is the same map as described in `encode`

> `strings` is one or more hexadecimal numbers represented as strings, or a collection (vector, list, etc.) of hexadecimal numbers, again represented as strings

**Returns** a string representing the encoded numbers.

### `decode` - Decodes a _hashid string_ into a collection of numbers

`(decode opts hashstr)`

> `opts` is the same map as described as for `encode`, although the `:min-length` option will have no effect when decoding

> `hashstr` is a single hashid string

**Returns** a list of one or more decoded numbers, in the order in which they were encoded.  If the given string was unable to be decoded, an empty list `()` is returned.

### `decode-hex` - Decodes a _hashid string_ into a collection of hexadecimal strings

`(decode-hex opts hashstr)`

> `opts` is the same map as described as for `encode`, although the `:min-length` option will have no effect when decoding

> `hashstr` is a single hashid string

**Returns** a list of one or more decoded numbers, in the order in which they were encoded, represented as hexadecimal strings.  If the given string was unable to be decoded, an empty list `()` is returned.

## License

Copyright © 2015 [Jason Strutz](http://jasonstrutz.com)

Distributed under the MIT License
