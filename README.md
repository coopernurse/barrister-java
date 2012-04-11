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

## Exposing service classes

Barrister doesn't tell you how to instantiate your classes at runtime.  Consequently you are free
to expose your Barrister service classes via Spring, Guice, or as plain Servlets.  The example
services on the Barrister web site use the provided `BarristerServlet`, which allows you to expose
a single IDL and its related interfaces with no additional code provided that the interface
implementation classes contain no-arg constructors.  See the Javadoc for a `web.xml` example of 
how to use this servlet.  The `BarristerServlet.java` source is also a good example of how to
integrate with the Service class. You could adapt this code to wire up your services using the 
framework of your choice.

## Documentation

* Read the [Contact Service Tutorial](https://github.com/coopernurse/barrister-demo-contact/tree/master/java/barrister-demo-contact)
* Read the [IDL Docs](http://barrister.bitmechanic.com/docs.html) for more info on writing 
  Barrister IDL files
* View the [Javadoc API Reference](http://barrister.bitmechanic.com/api/java/latest/) based on the 
  latest commit to master
* [Barrister Google Group](https://groups.google.com/forum/#!forum/barrister-rpc) - Post questions and ideas here

## License

Distributed under the MIT license.  See LICENSE file for details.