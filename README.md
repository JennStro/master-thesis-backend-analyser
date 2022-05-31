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
  <version>5.1.1</version>
</dependency>
```

### Usage 

```
class YourClass {
    
    public static void main(String[] args) {
        String codeToAnalyse = 
        "@NoEqualsMethod "+
        "class A {" +
            "boolean someBool = true & false;" +
        "}";
        
        Analyser analyser = new Analyser();
        
        BugReport report = analyser.analyse(codeToAnalyse);
        System.out.println(report.getBugs().get(0));
    }
}
```

## Settings 

The errors can be ignored by adding annotations from https://github.com/JennStro/master-thesis-disable-warnings-annotations.

**Equals operator on objects:**
```
@EqualsOperatorOnObjectAllowed
```   
**Bitwise operator on boolean:** 
```
@BitwiseOperationAllowed
``` 
**If without brackets:**
```
@IfWithoutBracketsAllowed
```   
**Integer division:** 
```
@IntegerDivisionAllowed
```   
**Missing equals method:** 
```
@NoEqualsMethod
```   
**Semi colon after if:** 
```
@IfStatementWithSemicolonAllowed
```

