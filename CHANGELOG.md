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
