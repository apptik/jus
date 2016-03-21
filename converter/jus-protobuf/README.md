#Google Protocol Buffer support for jus

Custom Converters and Request with supports [Protocol Buffer][1] for binary (protocol buffer-compatible serialization) serialization.

##Download

Download [the latest JAR][mvn] or grab via Maven:
```xml
<dependency>
  <groupId>io.apptik.comm</groupId>
  <artifactId>jus-protobuf</artifactId>
  <version>0.6.5</version>
</dependency>
```
or Gradle:
```groovy
compile 'io.apptik.comm:jus-protobuf:0.6.5'
```

Downloads of the released versions are available in [Sonatype's `releases` repository][release].

Snapshots of the development versions are available in [Sonatype's `snapshots` repository][snap].


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

 [mvn]: https://search.maven.org/remote_content?g=io.apptik.comm&a=jus-protobuf&v=LATEST
 [release]: https://oss.sonatype.org/content/repositories/releases/io/apptik/comm/jus-protobuf
 [snap]: https://oss.sonatype.org/content/repositories/snapshots/io/apptik/comm/jus-protobuf
 [1]: https://developers.google.com/protocol-buffers/