# Barrister Java Bindings

## Installation

To use this in your project, add this dependency to your `pom.xml`

```xml
    <dependency>
        <groupId>com.bitmechanic</groupId>
        <artifactId>barrister</artifactId>
        <version>1.0-SNAPSHOT</version>
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

## Tutorial

This tutorial explains how to write a new web service using Barrister.  The finished code is
available in the [barrister-demo-contact](https://github.com/coopernurse/barrister-demo-contact)
repository under the `java` directory.

In this example we will be using a Barrister IDL JSON file that has already been translated from
the source IDL file.  When you write your application you will need to 
[download Barrister](http://barrister.bitmechanic.com/download.html) so you can translate your
IDL files to JSON.

**Demo App - Contact Service**

In this tutorial we'll be writing a very simple contact management service.  For the sake of
simplicity we'll store the contacts in memory.  Our project will generate a WAR file that you 
could deploy to any standard servlet container such as Tomcat or Jetty.  We will use Maven to 
manage the build process.

The [contact.idl](https://raw.github.com/coopernurse/barrister-demo-contact/master/contact.idl)
file is available on github.  The file contains a single interface which we will implement in Java:

    interface ContactService {
        put(contact Contact) string 
        get(contactId string, userId string) Contact [optional]
        getAll(userId string) []Contact
        delete(contactId string, userId string) bool
    }

**Create a new Maven Project**

Open a shell and go to the parent directory you'd like to create your new WAR project under.  
Then type:

    mvn archetype:generate \
        -DgroupId=com.bitmechanic \
        -DartifactId=barrister-demo-contact \
        -Dversion=1.0-SNAPSHOT \
        -DarchetypeGroupId=org.apache.maven.archetypes \
        -DarchetypeArtifactId=maven-archetype-webapp \
        -DarchetypeVersion=1.0 

This will create a `barrister-demo-contact` directory that looks like this:

    barrister-demo-contact/
    ├── pom.xml
    └── src
        └── main
            ├── resources
            └── webapp
                ├── WEB-INF
                │   └── web.xml
                └── index.jsp

**Copy IDL JSON file into project**

The generated `contact.json` file is available on github.  Copy it into the `src/main/resources`
directory so that it will be bundled into your WAR file automatically.

    cd barrister-demo-contact/src/main/resources
    curl https://raw.github.com/coopernurse/barrister-demo-contact/master/contact.json > contact.json
    
**Add Barrister to pom.xml**

Go back to the `barrister-demo-contact` dir and edit the `pom.xml` file.  Add these dependencies:

    <dependency>
      <groupId>com.bitmechanic</groupId>
      <artifactId>barrister</artifactId>
      <version>1.0-SNAPSHOT</version>
    </dependency>
    <dependency>
        <groupId>javax.servlet</groupId>
        <artifactId>servlet-api</artifactId>
        <version>2.5</version>
        <scope>provided</scope>
    </dependency>    
    
**Download Barrister Dependency**

Run: `mvn compile` to force Maven to resolve the project dependencies and download them to your
local `.m2/repository` cache.

**Generate classes from IDL**

Now that you've resovled the Barrister dependency, you should have the Barrister JAR in your
local .m2 directory.  Grab the `idl2java` script.  You can place this script wherever you like.
I suggest placing it in a directory that's in your PATH so your shell can find it.

Mac/Linux:

    curl https://raw.github.com/coopernurse/barrister-java/master/idl2java.sh > /usr/local/bin/idl2java
    chmod 755 /usr/local/bin/idl2java
    export PATH=$PATH:/usr/local/bin

Now run it against the `contact.json` file we just downloaded.  From your `barrister-demo-contact` 
directory, try:

    idl2java -j src/main/resources/contact.json -p com.bitmechanic.contact.generated -o src/main/java
    
You should see this output:

    Reading IDL from: src/main/resources/contact.json
    Using package name: com.bitmechanic.contact.generated
    Creating directory: src/main/java/com/bitmechanic/contact/generated
    Writing file: src/main/java/com/bitmechanic/contact/generated/Phone.java
    Writing file: src/main/java/com/bitmechanic/contact/generated/Address.java
    Writing file: src/main/java/com/bitmechanic/contact/generated/Contact.java
    Writing file: src/main/java/com/bitmechanic/contact/generated/PhoneType.java
    Writing file: src/main/java/com/bitmechanic/contact/generated/ContactService.java
    Writing file: src/main/java/com/bitmechanic/contact/generated/ContactServiceClient.java

Now try compiling the source:

    mvn compile
    
**What Just Happened?**

`idl2java` loaded the `contact.json` file and generated a `.java` file for each enum and struct
defined in the file.  Two files were created for each interface: a Java interface that you will
implement in your server implementation, and a client class that can be used to consume this 
interface from a remote server.

Hopefully the command line flags are self-explanatory, but just in case:

* The `-j` flag is the path to the IDL JSON file to use
* The `-p` flag is the name of the Java package to use for the generated files
* The `-o` flag is the root source directory to write the files to.  As you can see from the example
  above, the package is automatically appended to this directory.

A few suggestions:

* **Never** edit the generated files
* It's up to you whether you want to check your IDL into your source control system.  If you do, folks can build the system without having Barrister installed, but you run the risk of forgetting to regenerate the classes when you change your IDL.

**Writing the Server**

Now we're ready to get down to business.  Create this file: 
`src/main/java/com/bitmechanic/contact/ContactServiceImpl.java` with these contents:

```java
package com.bitmechanic.contact;

import com.bitmechanic.barrister.RpcException;
import com.bitmechanic.contact.generated.*;
import java.util.HashMap;
import java.util.ArrayList;
import java.util.UUID;

public class ContactServiceImpl implements ContactService {

    enum CustomErr {
        INVALID(100, "Invalid %s"),
        DENIED(101, "Permission denied: %s"),
        LIMIT(102, "Limit exceeded: %s");

        private int code;
        private String msgFormat;

        CustomErr(int code, String msgFormat) {
            this.code = code;
            this.msgFormat = msgFormat;
        }

        RpcException toException(String... msgArgs) {
            return new RpcException(code, String.format(msgFormat, msgArgs));
        }
    }

    private HashMap<String,Contact> byId = new HashMap<String,Contact>();

    public String put(Contact contact) throws RpcException {
        String contactId = getOrCreateId(contact);
        if (!byId.containsKey(contactId)) {
            String userId = contact.getUserId();
            if (getAll(userId).length >= 10) {
                throw CustomErr.LIMIT.toException("User " + userId + " has 10 or more contacts");
            }
        }
        byId.put(contactId, contact);
        return contactId;
    }

    public Contact get(String contactId, String userId) throws RpcException {
        Contact c = byId.get(contactId);
        if (c == null || c.getUserId().equals(userId)) {
            return c;
        }
        else {
            throw CustomErr.DENIED.toException("User " + userId + " doesn't own contact: " + contactId);
        }
    }

    public Contact[] getAll(String userId) throws RpcException {
        ArrayList<Contact> list = new ArrayList<Contact>();
        for (Contact c : byId.values()) {
            if (c.getUserId().equals(userId)) {
                list.add(c);
            }
        }
        return list.toArray(new Contact[0]);
    }

    public Boolean delete(String contactId, String userId) throws RpcException {
        Contact c = get(contactId, userId);
        if (c == null) {
            return false;
        }
        else {
            byId.remove(contactId);
            return true;
        }
    }

    private String getOrCreateId(Contact contact) {
        if (contact.getContactId() == null) {
            contact.setContactId(UUID.randomUUID().toString());
        }
        return contact.getContactId();
    }

}
```

Compile with: `mvn compile`

**Exposing the Service in the WAR**

Now that we have our service we need to expose it in our WAR.  In this example we'll expose it 
using a simple Java Servlet class.  You could use similar code to expose your service in the 
web framework of your choosing.  The API is the same.

Create a file called: `src/main/java/com/bitmechanic/contact/ContactServlet.java`

```java
package com.bitmechanic.contact;

import com.bitmechanic.barrister.Contract;
import com.bitmechanic.barrister.Server;
import com.bitmechanic.barrister.JacksonSerializer;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpServletRequest;

import java.io.InputStream;
import java.io.OutputStream;

public class ContactServlet extends HttpServlet {

    private Contract contract;
    private Server server;
    private JacksonSerializer serializer;

    public ContactServlet() {
        // Serialize requests/responses as JSON using Jackson
        serializer = new JacksonSerializer();
    }

    public void init(ServletConfig config) throws ServletException {
        try {
            // Load the contract from the IDL JSON file
            contract = Contract.load(getClass().getResourceAsStream("/contact.json"));
            server = new Server(contract);

            // Register our service implementation
            server.addHandler(ContactService.class, new ContactServiceImpl());
        }
        catch (Exception e) {
            throw new ServletException(e);
        }
    }

    public void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException {
        try {
            InputStream is = req.getInputStream();
            OutputStream os = resp.getOutputStream();
            resp.addHeader("Content-Type", "application/json");

            // This will deserialize the request and invoke
            // our ContactServiceImpl code based on the method and params
            // specified in the request. The result, including any
            // RpcException (if thrown), will be serialized to the OutputStream
            server.call(serializer, is, os);

            is.close();
            os.close();
        }
        catch (Exception e) {
            throw new ServletException(e);
        }
    }

}
```

Then modify the `src/main/webapp/WEB-INF/web.xml` to register the servlet and bind it to `/contact`

```xml
<web-app>
  <display-name>Barrister Demo Contact Service</display-name>

  <servlet>
      <servlet-name>contact</servlet-name>
      <servlet-class>com.bitmechanic.contact.ContactServlet</servlet-class>
  </servlet>

  <servlet-mapping>
      <servlet-name>contact</servlet-name>
      <url-pattern>/contact</url-pattern>
  </servlet-mapping>

</web-app>
```

**Run the server**

First package the app into a war:

    mvn package
    
Then run the WAR.  An easy way to test the WAR locally is with the 
[Maven Jetty Plugin](http://docs.codehaus.org/display/JETTY/Maven+Jetty+Plugin).  To use this
plugin, edit the `<build>` section of the `pom.xml` file to read:

```xml
  <build>
    <finalName>barrister-demo-contact</finalName>
    <plugins>
        <plugin>
            <groupId>org.mortbay.jetty</groupId>
            <artifactId>maven-jetty-plugin</artifactId>
        </plugin>
    </plugins>
  </build>
```

Then run:

    mvn jetty:run
    
This will deploy the WAR locally in a Jetty instance bound to port 8080.  By default it will use
a context path of `/barrister-demo-contact`, so the full URL to our `ContactServlet` will be:

    http://localhost:8080/barrister-demo-contact/contact

**Make some requests**

Before we write a Java client for our service, let's take a moment to try it out manually using
`curl`.  This will give you a better understanding of how simple Barrister's request serialization
is and hopefully give you a better understanding of how data is marshaled on the wire.

With your server running locally using `mvn jetty:run` first try requesting the IDL.  This is the
first thing a real Barrister client will do when it binds to your server.

    curl -H 'Content-Type: application/json' \
         -d '{"jsonrpc":"2.0", "method":"barrister-idl"}' \
         http://localhost:8080/barrister-demo-contact/contact
         
That will return the same JSON contained in your local `contact.json` file.

Next try adding a contact:

    curl -H 'Content-Type: application/json' \
         -d '{"jsonrpc":"2.0", "method":"ContactService.put", "params":{"contactId":"abc123","userId":"user-12","firstName":"John","lastName":"Doe","email":"john@example.com", "phones":[]}}' \
         http://localhost:8080/barrister-demo-contact/contact
         
This should return:

    {"result":"abc123","jsonrpc":"2.0"}
    
What happens if you omit a required property on the Contact?

    curl -H 'Content-Type: application/json' \
         -d '{"jsonrpc":"2.0", "method":"ContactService.put", "params":{"userId":"user-12","firstName":"John","lastName":"Doe","email":"john@example.com", "phones":[]}}' \
         http://localhost:8080/barrister-demo-contact/contact
         
Barrister automatically detects this and rejects the request.  Very nice.

    {
        "jsonrpc": "2.0",
        "error": {
            "message": "field 'contactId' missing from input value: '{userId=user-12, firstName=John, lastName=Doe, email=john@example.com, phones=[]}'",
            "code": -32602
        }
    }

**Writing the Java Client**

It's good to understand how the JSON is encoded on the wire, but in practice we rarely have to
concern ourselves with that, as Barrister will automatically marshal JSON to and from the
generated Java classes.

Let's write a very simple JUnit test that uses the `ContactServiceClient` class to consume our
service. My use of JUnit is purely to simplify execution of the client code, although in theory
you could use this approach to write integration tests for your web services.

First create the directory for the test: 

    mkdir -p src/test/java/com/bitmechanic/contact

Then create a file: `src/test/java/com/bitmechanic/contact/ContactClientTest.java`

```java
package com.bitmechanic.contact;

import junit.framework.TestCase;

import com.bitmechanic.barrister.HttpTransport;
import com.bitmechanic.contact.generated.Contact;
import com.bitmechanic.contact.generated.Phone;
import com.bitmechanic.contact.generated.PhoneType;
import com.bitmechanic.contact.generated.Address;
import com.bitmechanic.contact.generated.ContactServiceClient;

public class ContactClientTest extends TestCase {

    public void testPutGetDelete() throws Exception {
        String endpoint = "http://localhost:8080/barrister-demo-contact/contact";
        HttpTransport trans = new HttpTransport(endpoint);
        ContactServiceClient client = new ContactServiceClient(trans);

        Contact c = new Contact();
        c.setFirstName("John");
        c.setLastName("Doe");
        c.setEmail("john@example.com");
        c.setContactId("john-123");
        c.setUserId("user-123");
        c.setPhones(new Phone[0]);

        String contactId = client.put(c);
        assertEquals(contactId, c.getContactId());

        Contact c2 = client.get(contactId, c.getUserId());
        assertEquals(c, c2);

        assertTrue(client.delete(contactId, c.getUserId()));
    }

}
```

Make sure you still have the `mvn jetty:run` process running in another shell.  Then try:

    mvn test

That's all there is to it.  When you construct the `HttpTransport`, the Contract for that
endpoint is loaded from the Server using the internal `barrister-idl` method.  As a result, you
do not need to distribute the IDL JSON file with your client programs.

**Conclusion**

In this tutorial we created a fully functioning web service from scratch.  We generated client and
server stub code from the service IDL, and tested the server from the command line and from a 
JUnit test class.  

I hope you try Barrister out on your next project!

## Other resources

* [Barrister Javadoc](http://barrister.bitmechanic.com/api/java/latest/) - Latest Javadoc output from our continuous integration server
* [Barrister Web Site](http://barrister.bitmechanic.com) - Includes docs and download links for other language bindings
* [Barrister Google Group](https://groups.google.com/forum/#!forum/barrister-rpc) - Post questions and ideas here

## License

Distributed under the MIT license.  See LICENSE file for details.
