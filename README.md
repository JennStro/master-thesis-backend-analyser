### The backend for master thesis.

This analyser uses Java Parser to analyse java code. Give the code as a string. 

### Installation
Maven: Add in your `pom.xml` file: 

```
<repositories>
    <repository>
      <id>repsy</id>
      <name>master-thesis-analyser</name>
      <url>https://repo.repsy.io/mvn/jennstro/master-thesis-analyser</url>
    </repository>
</repositories>
```
  
  
```
<dependency>
  <groupId>master.thesis.backend.analyser</groupId>
  <artifactId>master-thesis-backend-analyser</artifactId>
  <version>LATEST_VERSION</version>
</dependency>
```

## Architecture 

![arcitecture](https://user-images.githubusercontent.com/48728008/151001283-69cd144b-766d-4972-97be-93d5a03f28a8.png)

## Settings 

The errors can be ignored by adding annotations to the file that is being analysed. 

Equals operator on objects: ```@EqualsOperatorOnObjectAllowed```   
Bitwise operator on boolean: ``@BitwiseOperationAllowed``   
If without brackets: ``@IfWithoutBracketsAllowed``   
Integer division: ``@IntegerDivisionAllowed``   
Missing equals method: ```@NoEqualsMethod```   
Semi colon after if: ```@IfStatementWithSemicolonAllowed```

