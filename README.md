#jus

Flexible and Easy HTTP/REST(not 100% yet) Communication library for Java and Android
Based on Volley, inspired by many.

* it is like Volley but much easier
* it is like Retrofit but infinitely more flexible


##Download

Find [the latest JARs][mvn] or grab via Maven:
```xml
<dependency>
  <groupId>io.apptik.comm</groupId>
  <artifactId>jus-XXX</artifactId>
  <version>0.6.2</version>
</dependency>
```
or Gradle:
```groovy
compile 'io.apptik.comm:jus-XXX:0.6.2'
```

Snapshots of the development version are available in [Sonatype's `snapshots` repository][snap].

Jus requires at minimum Java 7 or Android SDK 15.


## Modules
* [jus for Java][jus-java] - main jus library for java
* [Reactive jus][rx-jus] - RxJava support for jus
* HTTP Stacks
    * [OkHttp][jus-okhttp] = OkHttp Client Stack for jus
* Jus for Android
    * [Android jus][jus-android] - jus optimized for Android
    * [Reactive Android jus][jus-android-rx] - RxJava support for jus for Android
* Data Serializers Support for jus
    * [Gson][jus-gson] - support for Gson
    * [Jackson][jus-jackson] - support for Jackson
    * [JJson][jus-jjson] - support for JJson
    * [Moshi][jus-moshi] - support for Moshi
    * [Protobuf][jus-protobuf] - support for Google Protobuf
    * [SimpleXML][jus-simplexml] - support for Simple
    * [Wire][jus-wire] - support for wire

## Licence

    Copyright (C) 2015 AppTik Project

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
 [snap]: https://oss.sonatype.org/content/repositories/releases/io/apptik/comm/
 [jus-android]: https://github.com/apptik/jus/tree/master/jus-android
 [jus-android-rx]: https://github.com/apptik/jus/tree/master/jus-android-rx
 [jus-gson]: https://github.com/apptik/jus/tree/master/jus-gson
 [jus-jackson]: https://github.com/apptik/jus/tree/master/jus-jackson
 [jus-java]: https://github.com/apptik/jus/tree/master/jus-java
 [jus-jjson]: https://github.com/apptik/jus/tree/master/jus-jjson
 [jus-moshi]: https://github.com/apptik/jus/tree/master/jus-moshi
 [jus-protobuf]: https://github.com/apptik/jus/tree/master/jus-protobuf
 [jus-simplexml]: https://github.com/apptik/jus/tree/master/jus-simplexml
 [jus-wire]: https://github.com/apptik/jus/tree/master/jus-wire
 [retro-jus]: https://github.com/apptik/jus/tree/master/retro-jus
 [rx-jus]: https://github.com/apptik/jus/tree/master/rx-jus
 [jus-okhttp]: https://github.com/apptik/jus/tree/master/jus-okhttp
