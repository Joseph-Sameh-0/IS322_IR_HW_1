# Inverted Index Project


## **Overview**
This project implements an **inverted index** in Java, which is a data structure used for full-text search. An inverted index maps words to the set of document IDs where they appear, enabling fast search and retrieval.


------

## **Features**

  - Builds an inverted index from a set of text files.
  
  - Stores the index in a serialized format for quick loading.
  
  - Allows querying the index to find documents containing a specific word.

-----

## **Technologies Used**
- **Programming Language:** Java.   
- **Development Tools:** IntelliJ IDEA, or any Java IDE.  
- **Version Control System:** Git and GitHub.
-----


## **How It Works**

1. **Building the Index:**
   - The program reads multiple text files from a directory.
   - It tokenizes the words and maps each unique word to the document(s) where it appears.
   - The index is then serialized and stored for future use.

2. **Querying the Index:**
   - Users can enter a word to search for.
   - The program retrieves and displays all document IDs containing the word.
