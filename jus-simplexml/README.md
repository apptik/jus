#Simple XML support for jus

Custom `Converters` and `Request` which supports [Simple][1] for XML serialization.

Android
-------

Simple depends on artifacts which are already provided by the Android platform. When specifying as
a Maven or Gradle dependency, exclude the following transitive dependencies: `stax:stax-api`,
`stax:stax`, and `xpp3:xpp3`.



##Download

Download [the latest JAR][mvn] or grab via Maven:
```xml
<dependency>
  <groupId>io.apptik.comm</groupId>
  <artifactId>jus-simplexml</artifactId>
  <version>0.6.1</version>
</dependency>
```
or Gradle:
```groovy
compile 'io.apptik.comm:jus-simplexml:0.6.1'
```

Snapshots of the development version are available in [Sonatype's `snapshots` repository][snap].


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

 [mvn]: https://search.maven.org/remote_content?g=io.apptik.comm&a=jus-simplexml&v=LATEST
 [snap]: https://oss.sonatype.org/content/repositories/releases/io/apptik/comm/
[1]: http://simple.sourceforge.net/