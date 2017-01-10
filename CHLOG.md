Change Log
==========

NEXT Version 0.7.0 *(FUTURE-2017)*
----------------------------------

Version 0.6.9 *(10-01-2017)*
----------------------------------

* Fix: networkimageview do not reuse bitmap for api<=18 #71
* Fix: NegativeArraySizeException in DiskBasedCache.streamToBytes #79
* Fix: add Unsubscribe action before setting listeners #75
* Upd:  Update(downgrade) Netty to 4.1.x #76 
* New: Better Demo App
* New: Use new external RHub to implement RxHub 

Version 0.6.8 *(01-08-2016)*
----------------------------------

* Fix: redirected temp requests will not send stack markers #66 
* Upd: use okio for Base64 encoding #64
* New: add jus markers for okhttp3 stack #63
* New: add Request RxHub to rx-jus package #62 
* New: add jsonwrapper converters #61
* New: Make loader add tag parameter to imageRequest #60

Version 0.6.7 *(15-Jun-2016)*
----------------------------------

* Fix: bug where ImageRequest tries to reuse incompatible dirty bitmap from the BitmapPool

Version 0.6.6 *(06-Jun-2016)*
----------------------------------

* New: handle Redirects (300,301,302,303,307,308)
* Fix: duplicate jsonRequests #57
* Upd: add request filter to AlwaysGetCacheDispatcher
* Upd: server proxy auth errors happens before retry policy triggers #53
* Fix: bug on on 304 response #56
* Fix: use setFixedLengthStreamingMode where possible #50 


Version 0.6.5 *(21-Mar-2016)*
----------------------------------
* New: add OkHttp3 stack
* New: add ApacheHttp stack
* New: add Netty stack
* Fix: #28 requests cannot be canceled while looping in network for retry
* New: #32 add bitmap pool for NetworkImageView
* Fix: for https://code.google.com/p/android/issues/detail?id=194495
* Upd: #29 make Authenticator more generic
* New: #2 Add authenticator for proxy 407
* Upd: #35 add more customizable & manageable bitmap pool
* Upd: Improve network image view reset/refresh
* Upd: add default and error image ids on loader
* Upd: make listener factories more generic
* New: #33 add networkmanager and noconnection policy
* Upd: add error filter for dynamic retry policy

Version 0.6.4 *(23-Nov-2015)*
----------------------------------
* New: add retry policy factory in the queue
* Upd: retry policy split read and connect timeout
* New: add queue markers and callbacks
* Upd: replace RxQueue with RxQueue binder
* New: add request event callbacks to the queue
* Fix: improve & simplify logs
* Fix: fix async listeners
* New: response converter based on returned Content type
* Fix: custom requests which override parseNetworkResponse
* New: add NetworkRequest transformers

Version 0.6.3 *(11-Nov-2015)*
----------------------------------
* Fix: Do not throw when response converter is not set but parseNetworkResponse is overridden
* New: Add NetworkRequest and NetworkResponse transformers to the queue

Version 0.6.2 *(9-Nov-2015)*
----------------------------------
* Fix: providing NetworkResponse converters using converter factories added in the queue 
* New: add OkHttp Client HTTP Stack
* New: in RetroProxy add @Tag, @Priority, @ShouldCache as property annotations
* New: in RetroProxy add TRACE and OPTIONS inbuilt http methods
* Fix: race condition bug
* Fix: RequestQueue closeWhenDone()

Version 0.6.1 *(2-Nov-2015)*
----------------------------------
* Change: make all MediaType parameters accessible (not only charset)
* Change: HttpUrl Handle null fragments & encode more (curly brackets) 
* New: Add Converter.Factory list to the queue
* Fix: head response MUST not try to parse body (we still try to parse but dont throw Ex)
* Change: add implied 'Accept' header to custom requests
* New: add setPriority to the request and Annotation to RetroProxy
* New: add shouldCache annotation in RetroProxy 
* New: add tag annotation to retroproxy

Version 0.6.0 *(29-Oct-2015)*
----------------------------------
* Fix: fix logs
* New: add serverAuthenticator for each `Request` not global for the `Network`
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

