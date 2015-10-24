Change Log
==========

NEXT Version 0.6.0 *(FUTURE-Oct-2015)*
----------------------------------
* Fix: fix logs
* New: add authenticator for each `Request` not global for the `Network`
* New: add `Authenticator` factory for `RequestQueue`
* Change: Update `Wire converter` to v2.0.0.


Version 0.6.0beta *(22-Oct-2015)*
----------------------------------
* New: add wire request
* New: add formencoding support
* New: add multipart support


Version 0.6.0a *(20-Oct-2015)*
----------------------------------
* New: Major refactoring and new modules
    * Split android dependencies and make compatible for java only
    * New: Simplify `Request` class and allow more customization withut need of extending it
    * New: Introduce `Converters`
    * New: add Android Json support (json<->json element)
    * New: add gson support (json<->pojo)
    * New: add jackson support (josn<->pojo)
    * New: add jjson support (json<->json element)
    * New: add moshi support (josn<->pojo)
    * New: add protobuf support
    * New: add simplexml support
    * New: add wire support
    * New: add marker caalback
    * Change: Improved Rx support for requests and queue
    * New: add RetroProxy support for easy Api client creation *(thanks to `RetroFit`)*

Version 0.5.2 *(18-Sep-2015)*
----------------------------------
* Change: update libs & gradle
* Fix:    304 cache bug

Version 0.5.1 *(16-Sep-2015)*
----------------------------------
* New: recognize server timeout exceptions in response
* Fix: thrown network errors

Version 0.5.0 *(03-Sep-2015)*
----------------------------------
* Change: remove depracted apache http client dependencies
* New: RxJava support

