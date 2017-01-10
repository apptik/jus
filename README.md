# jus

[JavaDocs](http://apptik.github.io/jus/)

[![Build Status](https://travis-ci.org/apptik/jus.svg?branch=master)](https://travis-ci.org/apptik/jus)
[![Join the chat at https://gitter.im/apptik/jus](https://badges.gitter.im/apptik/jus.svg)](https://gitter.im/apptik/jus?utm_source=badge&utm_medium=badge&utm_campaign=pr-badge&utm_content=badge)
[![StackExchange](https://img.shields.io/stackexchange/stackoverflow/t/jus.svg)](http://stackoverflow.com/questions/tagged/jus)
[![Stories in Ready](https://badge.waffle.io/apptik/jus.png?label=ready&title=Ready)](https://waffle.io/apptik/jus)

Jus is a flexible and easy HTTP/REST client library for Java and Android.

* it is like Volley but much easier
* it is like Retrofit but infinitely more flexible

Jus is inspired by the flexibility, modularity and transparency of Google's [Volley Library][1] 
and the extreme simplicity of declarative API mapping of [Retrofit][2].

Like Volley the main thing where Requests are executed is the RequestQueue.

    RequestQueue queue = Jus.newRequestQueue();

and then Requests can be added to the Queue:

    queue.add(new Request<String>(
                            Request.Method.GET,
                            HttpUrl.parse(BeerService.fullUrl),
                            new Converters.StringResponseConverter())
                            .addResponseListener((r) -> out.println("RESPONSE: " + r))
                            .addErrorListener((e) -> out.println("ERROR: " + e))
            );


Anther option similarly to Retrofit is to map Java Interface to an API.

    public interface BeerService {
        @GET("locquery/{user}/{q}")
        Request<String> getBeer(
                @Path("user") String user,
                @Path("q") String q);
    }

Then create the Service Instance:

    RetroProxy retroJus = new RetroProxy.Builder()
                .baseUrl(BeerService.baseUrl)
                .requestQueue(queue)
                .addConverterFactory(new BasicConverterFactory())
                .build();

        BeerService beerService = retroJus.create(BeerService.class);

And execute a request:

    beerService.getBeer(BeerService.userString, "777")
                    .addResponseListener((r) -> out.println("RESPONSE: " + r))
                    .addErrorListener((e) -> out.println("ERROR: " + e.networkResponse));


## Download

Find [the latest JARs][mvn] or grab via Maven:
```xml
<dependency>
  <groupId>io.apptik.comm</groupId>
  <artifactId>jus-XXX</artifactId>
  <version>0.6.9</version>
</dependency>
```
or Gradle:
```groovy
compile 'io.apptik.comm:jus-XXX:0.6.9'
```

Downloads of the released versions are available in [Sonatype's `releases` repository][release].

Snapshots of the development versions are available in [Sonatype's `snapshots` repository][snap].

Jus requires at minimum Java 7 or Android SDK 15.

## Examples

* [java examples]
* [NEW android demo app]
* [android examples (old)]
* [volley to jus migration examples]


## Questions

[StackOverflow with tag 'jus' or 'apptik'](http://stackoverflow.com/questions/ask)

## Modules
* [jus for Java][jus-java] - main jus library for java
[![Maven Central](https://img.shields.io/maven-central/v/io.apptik.comm/jus-java.svg?style=flat)](https://maven-badges.herokuapp.com/maven-central/io.apptik.comm/jus-java)
[![VersionEye](https://www.versioneye.com/java/io.apptik.comm:jus-java/0.6.9/badge.svg)](https://www.versioneye.com/java/io.apptik.comm:jus-java/0.6.9)
* [Reactive jus][rx-jus] - RxJava support for jus
[![Maven Central](https://img.shields.io/maven-central/v/io.apptik.comm/rx-jus.svg?style=flat)](https://maven-badges.herokuapp.com/maven-central/io.apptik.comm/rx-jus)
[![VersionEye](https://www.versioneye.com/java/io.apptik.comm:rx-jus/0.6.9/badge.svg)](https://www.versioneye.com/java/io.apptik.comm:rx-jus/0.6.9)
* HTTP Stacks
    * [OkHttp][jus-okhttp] = OkHttp Client Stack for jus
    [![Maven Central](https://img.shields.io/maven-central/v/io.apptik.comm/jus-okhttp.svg?style=flat)](https://maven-badges.herokuapp.com/maven-central/io.apptik.comm/jus-okhttp)
    [![VersionEye](https://www.versioneye.com/java/io.apptik.comm:jus-okhttp/0.6.9/badge.svg)](https://www.versioneye.com/java/io.apptik.comm:jus-okhttp/0.6.9)
    * [OkHttp3][jus-okhttp3] = OkHttp3 Client Stack for jus
    [![Maven Central](https://img.shields.io/maven-central/v/io.apptik.comm/jus-okhttp3.svg?style=flat)](https://maven-badges.herokuapp.com/maven-central/io.apptik.comm/jus-okhttp3)
    [![VersionEye](https://www.versioneye.com/java/io.apptik.comm:jus-okhttp3/0.6.9/badge.svg)](https://www.versioneye.com/java/io.apptik.comm:jus-okhttp3/0.6.9)
    * [ApacheHttp][jus-apachehttp] = ApacheHttp Client Stack for jus
    [![Maven Central](https://img.shields.io/maven-central/v/io.apptik.comm/jus-apachehttp.svg?style=flat)](https://maven-badges.herokuapp.com/maven-central/io.apptik.comm/jus-apachehttp)
    [![VersionEye](https://www.versioneye.com/java/io.apptik.comm:jus-apachehttp/0.6.9/badge.svg)](https://www.versioneye.com/java/io.apptik.comm:jus-apachehttp/0.6.9)
    * [Netty][jus-netty] = Netty Client Stack for jus
    [![Maven Central](https://img.shields.io/maven-central/v/io.apptik.comm/jus-netty.svg?style=flat)](https://maven-badges.herokuapp.com/maven-central/io.apptik.comm/jus-netty)
    [![VersionEye](https://www.versioneye.com/java/io.apptik.comm:jus-netty/0.6.9/badge.svg)](https://www.versioneye.com/java/io.apptik.comm:jus-netty/0.6.9)

* Jus for Android
    * [Android jus][jus-android] - jus optimized for Android
    [![Maven Central](https://img.shields.io/maven-central/v/io.apptik.comm/jus-android.svg?style=flat)](https://maven-badges.herokuapp.com/maven-central/io.apptik.comm/jus-android)
    [![VersionEye](https://www.versioneye.com/java/io.apptik.comm:jus-android/0.6.9/badge.svg)](https://www.versioneye.com/java/io.apptik.comm:jus-android/0.6.9)
* Data serializers support and custom requests for jus
    * [Gson][jus-gson] - support for Gson
    [![Maven Central](https://img.shields.io/maven-central/v/io.apptik.comm/jus-gson.svg?style=flat)](https://maven-badges.herokuapp.com/maven-central/io.apptik.comm/jus-gson)
    [![VersionEye](https://www.versioneye.com/java/io.apptik.comm:jus-gson/0.6.9/badge.svg)](https://www.versioneye.com/java/io.apptik.comm:jus-gson/0.6.9)
    * [Jackson][jus-jackson] - support for Jackson
    [![Maven Central](https://img.shields.io/maven-central/v/io.apptik.comm/jus-jackson.svg?style=flat)](https://maven-badges.herokuapp.com/maven-central/io.apptik.comm/jus-jackson)
    [![VersionEye](https://www.versioneye.com/java/io.apptik.comm:jus-jackson/0.6.9/badge.svg)](https://www.versioneye.com/java/io.apptik.comm:jus-jackson/0.6.9)
    * [JJson][jus-jjson] - support for JustJson
    [![Maven Central](https://img.shields.io/maven-central/v/io.apptik.comm/jus-jjson.svg?style=flat)](https://maven-badges.herokuapp.com/maven-central/io.apptik.comm/jus-jjson)
    [![VersionEye](https://www.versioneye.com/java/io.apptik.comm:jus-jjson/0.6.9/badge.svg)](https://www.versioneye.com/java/io.apptik.comm:jus-jjson/0.6.9)       
    * [Moshi][jus-moshi] - support for Moshi
    [![Maven Central](https://img.shields.io/maven-central/v/io.apptik.comm/jus-moshi.svg?style=flat)](https://maven-badges.herokuapp.com/maven-central/io.apptik.comm/jus-moshi)
    [![VersionEye](https://www.versioneye.com/java/io.apptik.comm:jus-moshi/0.6.9/badge.svg)](https://www.versioneye.com/java/io.apptik.comm:jus-moshi/0.6.9)
    * [Protobuf][jus-protobuf] - support for Google Protobuf
    [![Maven Central](https://img.shields.io/maven-central/v/io.apptik.comm/jus-protobuf.svg?style=flat)](https://maven-badges.herokuapp.com/maven-central/io.apptik.comm/jus-protobuf)
    [![VersionEye](https://www.versioneye.com/java/io.apptik.comm:jus-protobuf/0.6.9/badge.svg)](https://www.versioneye.com/java/io.apptik.comm:jus-protobuf/0.6.9)
    * [SimpleXML][jus-simplexml] - support for Simple
    [![Maven Central](https://img.shields.io/maven-central/v/io.apptik.comm/jus-simplexml.svg?style=flat)](https://maven-badges.herokuapp.com/maven-central/io.apptik.comm/jus-simplexml)
    [![VersionEye](https://www.versioneye.com/java/io.apptik.comm:jus-simplexml/0.6.9/badge.svg)](https://www.versioneye.com/java/io.apptik.comm:jus-simplexml/0.6.9)
    * [Wire][jus-wire] - support for wire
    [![Maven Central](https://img.shields.io/maven-central/v/io.apptik.comm/jus-wire.svg?style=flat)](https://maven-badges.herokuapp.com/maven-central/io.apptik.comm/jus-wire)
	[![VersionEye](https://www.versioneye.com/java/io.apptik.comm:jus-wire/0.6.9/badge.svg)](https://www.versioneye.com/java/io.apptik.comm:jus-wire/0.6.9)
    
## Licence

    Copyright (C) 2016 AppTik Project

    Licensed under the Apache License, Version 2.0 (the "License");
    you may not use this file except in compliance with the License.
    You may obtain a copy of the License at

         http://www.apache.org/licenses/LICENSE-2.0

    Unless required by applicable law or agreed to in writing, software
    distributed under the License is distributed on an "AS IS" BASIS,
    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
    See the License for the specific language governing permissions and
    limitations under the License.

  [1]: https://developer.android.com/training/volley/index.html
  [2]: http://square.github.io/retrofit/

 [mvn]: http://search.maven.org/#search|ga|1|io.apptik.comm.jus
 [release]: https://oss.sonatype.org/content/repositories/releases/io/apptik/comm/
 [snap]: https://oss.sonatype.org/content/repositories/snapshots/io/apptik/comm/
 [jus-android]: https://github.com/apptik/jus/tree/master/android/jus-android
 [jus-android-rx]: https://github.com/apptik/jus/tree/master/android/jus-android-rx
 [jus-gson]: https://github.com/apptik/jus/tree/master/converter/jus-gson
 [jus-jackson]: https://github.com/apptik/jus/tree/master/converter/jus-jackson
 [jus-java]: https://github.com/apptik/jus/tree/master/jus-java
 [jus-jjson]: https://github.com/apptik/jus/tree/master/converter/jus-jjson
 [jus-moshi]: https://github.com/apptik/jus/tree/master/converter/jus-moshi
 [jus-protobuf]: https://github.com/apptik/jus/tree/master/converter/jus-protobuf
 [jus-simplexml]: https://github.com/apptik/jus/tree/master/converter/jus-simplexml
 [jus-wire]: https://github.com/apptik/jus/tree/master/converter/jus-wire
 [retro-jus]: https://github.com/apptik/jus/tree/master/retro-jus
 [rx-jus]: https://github.com/apptik/jus/tree/master/rx-jus
 [jus-okhttp]: https://github.com/apptik/jus/tree/master/stack/jus-okhttp
 [jus-okhttp3]: https://github.com/apptik/jus/tree/master/stack/jus-okhttp3
 [jus-apachehttp]: https://github.com/apptik/jus/tree/master/stack/jus-apachehttp
 [jus-netty]: https://github.com/apptik/jus/tree/master/stack/jus-netty

[java examples]: https://github.com/apptik/jus/tree/master/examples-java
[android examples (old)]: https://github.com/apptik/jus/tree/master/examples-android
[NEW android demo app]: https://github.com/apptik/jus/tree/master/demo-android
[volley to jus migration examples]: https://github.com/apptik/jus/tree/master/examples-android-volley-migration