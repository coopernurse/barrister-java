# Barrister Java Bindings

## Installation

To use this in your project, add this dependency to your `pom.xml`

```xml
    <dependency>
        <groupId>com.bitmechanic</groupId>
        <artifactId>barrister</artifactId>
        <version>0.1.0-SNAPSHOT</version>
    </dependency>
```

This project depends on Jackson 1.9 for JSON serialization support.

## Usage

To use Barrister on your Java project, follow these steps:

* Write a [Barrister IDL](http://barrister.bitmechanic.com/docs.html) file
* Run the `barrister` tool to convert the IDL file to `.json` and (optionally) `.html` representations
  * See the [download page](http://barrister.bitmechanic.com/download.html) for details on installing the barrister tool.  It is separate from the Java bindings
* Run the `idl2java` tool bundled with the above Maven dependency to generate Java classes from the IDL `json` file
  * See the tutorial below for details on running `idl2java`
* Write server and/or client implementations based on the generated classes

## Documentation

* Read the [Contact Service Tutorial](https://github.com/coopernurse/barrister-demo-contact/tree/master/java/barrister-demo-contact)
* Read the [IDL Docs](http://barrister.bitmechanic.com/docs.html) for more info on writing 
  Barrister IDL files
* View the [Javadoc API Reference](http://barrister.bitmechanic.com/api/java/latest/) based on the 
  latest commit to master
* [Barrister Google Group](https://groups.google.com/forum/#!forum/barrister-rpc) - Post questions and ideas here

## License

Distributed under the MIT license.  See LICENSE file for details.
