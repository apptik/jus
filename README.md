# jus

[![Build Status](https://travis-ci.org/apptik/jus.svg?branch=master)](https://travis-ci.org/apptik/jus)
[![Join the chat at https://gitter.im/apptik/jus](https://badges.gitter.im/apptik/jus.svg)](https://gitter.im/apptik/jus?utm_source=badge&utm_medium=badge&utm_campaign=pr-badge&utm_content=badge)
[![StackExchange](https://img.shields.io/stackexchange/stackoverflow/t/jus.svg)](http://stackoverflow.com/questions/tagged/jus)

Flexible and Easy HTTP/REST Communication library for Java and Android
Based on Volley, inspired by many.

* it is like Volley but much easier
* it is like Retrofit but infinitely more flexible


## Download

Find [the latest JARs][mvn] or grab via Maven:
```xml
<dependency>
  <groupId>io.apptik.comm</groupId>
  <artifactId>jus-XXX</artifactId>
  <version>0.6.5</version>
</dependency>
```
or Gradle:
```groovy
compile 'io.apptik.comm:jus-XXX:0.6.5'
```

Downloads of the released versions are available in [Sonatype's `releases` repository][release].

Snapshots of the development versions are available in [Sonatype's `snapshots` repository][snap].

Jus requires at minimum Java 7 or Android SDK 15.

## Examples



## Questions

[StackOverflow with tag 'jus' or 'apptik'](http://stackoverflow.com/questions/ask)

## Modules
* [jus for Java][jus-java] - main jus library for java
[![Maven Central](https://img.shields.io/maven-central/v/io.apptik.comm/jus-java.svg?style=flat)](https://maven-badges.herokuapp.com/maven-central/io.apptik.comm/jus-java)
[![VersionEye](https://www.versioneye.com/java/io.apptik.comm:jus-java/0.6.5/badge.svg)](https://www.versioneye.com/java/io.apptik.comm:jus-java/0.6.5)
* [Reactive jus][rx-jus] - RxJava support for jus
[![Maven Central](https://img.shields.io/maven-central/v/io.apptik.comm/rx-jus.svg?style=flat)](https://maven-badges.herokuapp.com/maven-central/io.apptik.comm/rx-jus)
[![VersionEye](https://www.versioneye.com/java/io.apptik.comm:rx-jus/0.6.5/badge.svg)](https://www.versioneye.com/java/io.apptik.comm:rx-jus/0.6.5)
* HTTP Stacks
    * [OkHttp][jus-okhttp] = OkHttp Client Stack for jus
    [![Maven Central](https://img.shields.io/maven-central/v/io.apptik.comm/jus-okhttp.svg?style=flat)](https://maven-badges.herokuapp.com/maven-central/io.apptik.comm/jus-okhttp)
    [![VersionEye](https://www.versioneye.com/java/io.apptik.comm:jus-okhttp/0.6.5/badge.svg)](https://www.versioneye.com/java/io.apptik.comm:jus-okhttp/0.6.5)
    * [OkHttp3][jus-okhttp3] = OkHttp3 Client Stack for jus
    [![Maven Central](https://img.shields.io/maven-central/v/io.apptik.comm/jus-okhttp3.svg?style=flat)](https://maven-badges.herokuapp.com/maven-central/io.apptik.comm/jus-okhttp3)
    [![VersionEye](https://www.versioneye.com/java/io.apptik.comm:jus-okhttp3/0.6.5/badge.svg)](https://www.versioneye.com/java/io.apptik.comm:jus-okhttp3/0.6.5)
    * [ApacheHttp][jus-apachehttp] = ApacheHttp Client Stack for jus
    [![Maven Central](https://img.shields.io/maven-central/v/io.apptik.comm/jus-apachehttp.svg?style=flat)](https://maven-badges.herokuapp.com/maven-central/io.apptik.comm/jus-apachehttp)
    [![VersionEye](https://www.versioneye.com/java/io.apptik.comm:jus-apachehttp/0.6.5/badge.svg)](https://www.versioneye.com/java/io.apptik.comm:jus-apachehttp/0.6.5)
    * [Netty][jus-netty] = Netty Client Stack for jus
    [![Maven Central](https://img.shields.io/maven-central/v/io.apptik.comm/jus-netty.svg?style=flat)](https://maven-badges.herokuapp.com/maven-central/io.apptik.comm/jus-netty)
    [![VersionEye](https://www.versioneye.com/java/io.apptik.comm:jus-netty/0.6.5/badge.svg)](https://www.versioneye.com/java/io.apptik.comm:jus-netty/0.6.5)

* Jus for Android
    * [Android jus][jus-android] - jus optimized for Android
    [![Maven Central](https://img.shields.io/maven-central/v/io.apptik.comm/jus-android.svg?style=flat)](https://maven-badges.herokuapp.com/maven-central/io.apptik.comm/jus-android)
    [![VersionEye](https://www.versioneye.com/java/io.apptik.comm:jus-android/0.6.5/badge.svg)](https://www.versioneye.com/java/io.apptik.comm:jus-android/0.6.5)
* Data Serializers Support for jus
    * [Gson][jus-gson] - support for Gson
    [![Maven Central](https://img.shields.io/maven-central/v/io.apptik.comm/jus-gson.svg?style=flat)](https://maven-badges.herokuapp.com/maven-central/io.apptik.comm/jus-gson)
    [![VersionEye](https://www.versioneye.com/java/io.apptik.comm:jus-gson/0.6.5/badge.svg)](https://www.versioneye.com/java/io.apptik.comm:jus-gson/0.6.5)
    * [Jackson][jus-jackson] - support for Jackson
    [![Maven Central](https://img.shields.io/maven-central/v/io.apptik.comm/jus-jackson.svg?style=flat)](https://maven-badges.herokuapp.com/maven-central/io.apptik.comm/jus-jackson)
    [![VersionEye](https://www.versioneye.com/java/io.apptik.comm:jus-jackson/0.6.5/badge.svg)](https://www.versioneye.com/java/io.apptik.comm:jus-jackson/0.6.5)
    * [JJson][jus-jjson] - support for JustJson
    [![Maven Central](https://img.shields.io/maven-central/v/io.apptik.comm/jus-jjson.svg?style=flat)](https://maven-badges.herokuapp.com/maven-central/io.apptik.comm/jus-jjson)
    [![VersionEye](https://www.versioneye.com/java/io.apptik.comm:jus-jjson/0.6.5/badge.svg)](https://www.versioneye.com/java/io.apptik.comm:jus-jjson/0.6.5)       
    * [Moshi][jus-moshi] - support for Moshi
    [![Maven Central](https://img.shields.io/maven-central/v/io.apptik.comm/jus-moshi.svg?style=flat)](https://maven-badges.herokuapp.com/maven-central/io.apptik.comm/jus-moshi)
    [![VersionEye](https://www.versioneye.com/java/io.apptik.comm:jus-moshi/0.6.5/badge.svg)](https://www.versioneye.com/java/io.apptik.comm:jus-moshi/0.6.5)
    * [Protobuf][jus-protobuf] - support for Google Protobuf
    [![Maven Central](https://img.shields.io/maven-central/v/io.apptik.comm/jus-protobuf.svg?style=flat)](https://maven-badges.herokuapp.com/maven-central/io.apptik.comm/jus-protobuf)
    [![VersionEye](https://www.versioneye.com/java/io.apptik.comm:jus-protobuf/0.6.5/badge.svg)](https://www.versioneye.com/java/io.apptik.comm:jus-protobuf/0.6.5)
    * [SimpleXML][jus-simplexml] - support for Simple
    [![Maven Central](https://img.shields.io/maven-central/v/io.apptik.comm/jus-simplexml.svg?style=flat)](https://maven-badges.herokuapp.com/maven-central/io.apptik.comm/jus-simplexml)
    [![VersionEye](https://www.versioneye.com/java/io.apptik.comm:jus-simplexml/0.6.5/badge.svg)](https://www.versioneye.com/java/io.apptik.comm:jus-simplexml/0.6.5)
    * [Wire][jus-wire] - support for wire
    [![Maven Central](https://img.shields.io/maven-central/v/io.apptik.comm/jus-wire.svg?style=flat)](https://maven-badges.herokuapp.com/maven-central/io.apptik.comm/jus-wire)
	[![VersionEye](https://www.versioneye.com/java/io.apptik.comm:jus-wire/0.6.5/badge.svg)](https://www.versioneye.com/java/io.apptik.comm:jus-wire/0.6.5)
    
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
