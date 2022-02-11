### :star: 3.5.0
New feature: Add annotations for ignoring errors
Bug: Remove duplicate if-error

### :star: 3.4.0
New feature: Make better suggestions

### :star: 3.3.0
New feature: Make better suggestions

### :star: 3.2.0
New feature: Add more links for explanation

### :star: 3.1.0
New feature: Add better explanations

### :fire: 3.0.0   
Breaking change: int division not allowed even though it evaluates to int 
New feature: Allow print for bitwise

### :bug: 2.2.1   
Bug: Allow if-statement with one statement

### :star: 2.2.0  
New feature: Add bitwise op, and enhance explanations

### :bug: 2.1.2  
Bug: Add error when two statements on same line if

### :bug: 2.1.1  
Bug: Int division resulting in int not error

### :star: 2.1.0   
New feature: Allow to print error in console

### :fire: 2.0.0   
Breaking change: Remove `Ignoring return` error, `Bitwise operator` error.

### :bug: 1.4.3
Bug: Attach the exception to the report

### :bug: 1.4.2
Bug: Also add exception when symbol is unresolved in methodInvocation

### :bug: 1.4.1   
Bug: Also add exception when symbol is unresolved

### :star: 1.4.0   
New feature: Add integer division error

### :bug: 1.3.1
Bug: Propagate the parse error message.

### :star: 1.3.0
New feature: Add tip and make separate text from suggestion code.

### :star: 1.2.0
New feature: Add links for more info.

### :bug: 1.1.2
Bug fix: Fixed error when equal op is used in equals method and no equals method in interface. 

### :star: 1.1.0   
New feature: Save linenumber of error.

### :bug: 1.0.1   
Bug fix: Fix `UnresolvedSymbolException`. Types that are not in the scope of the file being analyzed will be unresolved. 

### 🔥 1.0.0 
New feature: Add suggestions for errors.  
Breaking change: Now use `Optional` instead of null-check to get suggestion from an error. 

Old code:   
`if (error.hasSuggestion()) {  
  // Do something with error.getSuggestion()...
}`

New code:   
`if (error.getSuggestion().isPresent()) {
  // Do something with error.getSuggestion()...
}`

### :star: 0.1.0
Features: Add all errors

### :star: 0.0.0
Baseproject from Java Parser library
