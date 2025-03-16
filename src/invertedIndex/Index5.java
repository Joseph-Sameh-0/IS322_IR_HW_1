/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package invertedIndex;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.Writer;
import java.io.IOException;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 * @author ehab
 */
public class Index5 {

    int N = 0;              //total number of documents.
    public Map<Integer, SourceRecord> sources;  // store the doc_id and the file name.

    public HashMap<String, PostingDict> index; // THe inverted index
    //--------------------------------------------

    public Index5() {
        sources = new HashMap<Integer, SourceRecord>();
        index = new HashMap<String, PostingDict>();
    }

    public void setN(int n) {
        N = n;
    }


    //-----------------------------------------------------------------
    // Prints the posting list in a formatted way
    public void printPostingList(Posting p) {
        System.out.print("[");
        while (p != null) {
            if (p.next != null) {
                System.out.print(p.docId + ",");
            } else {
                System.out.print(p.docId);    // Last element (no trailing comma)
            }
            p = p.next;                       // Move to the next posting
        }
        System.out.println("]");
    }

    //-------------------------------------------------------------------
    // Prints the dictionary of the inverted index.
    public void printDictionary() {
        Iterator<Map.Entry<String, PostingDict>> idxIt = index.entrySet().iterator();  // Iterator to traverse through the entries of the 'index' map.
        while (idxIt.hasNext()) {
            Map.Entry<String, PostingDict> IdxPair = idxIt.next();                     // Get the next key-value pair from the map
            PostingDict postingDict = IdxPair.getValue();                              // Extract the PostingDict object (value) from the key-value pair.
            System.out.print("** [" + IdxPair.getKey() + "," + postingDict.doc_freq + "]       =--> ");   // Print the term (key) and its document frequency in a formatted way.
            System.out.printf("** %-15s %-5d =--> ", IdxPair.getKey(), postingDict.doc_freq);
            printPostingList(postingDict.pList);                                       // Call a function to print the posting list of the current term.
        }
        System.out.println("------------------------------------------------------");
        System.out.println("*** Number of terms = " + index.size());                    // Print the total number of terms in the dictionary.
    }

    //------------------------------------------------------------------------------------
    //Reads a set of files, processes the content line by line,calls indexOneLine function to process the line
    public void buildIndex(String[] files) {  // from disk not from the internet
        int fileId = 0;                     //  Unique ID for each file (starting from 0)
        // Loop through each file name in the list
        for (String fileName : files) {
            try (BufferedReader file = new BufferedReader(new FileReader(fileName))) {
                if (!sources.containsKey(fileName)) {  // add the file to the source if it does not exist
                    sources.put(fileId, new SourceRecord(fileId, fileName, fileName, "notext")); // EX: id  url  title  txt
                }
                String line;
                int processedWords = 0;  // Counter for processed words in the file
               
                while ((line = file.readLine()) != null) {
                    processedWords += indexOneLine(line, fileId);  // call the function on each line to process it
                }
                 // Store the number of words processed for this file
                sources.get(fileId).length = processedWords;
            } catch (IOException e) {
                System.out.println("File " + fileName + " not found. Skip it");
            }
            fileId++;    // move to the next file ID
        }
        //   printDictionary();
    }

    //---------------------------------------------------------------------------------- 
    // processes a single line from a file, extracts words, and updates the index
    public int indexOneLine(String line, int fileId) {
        int processedWordsInLine = 0;    // Counter for words in this line

        // Split the line into words
        String[] words = line.split("\\W+");
        //   String[] words = line.replaceAll("(?:[^a-zA-Z0-9 -]|(?<=\\w)-(?!\\S))", " ").toLowerCase().split("\\s+");
        processedWordsInLine += words.length;  // Count words in this line
        for (String word : words) {
            word = word.toLowerCase();     // Convert word to lowercase
            if (stopWord(word)) {          // Skip stop words
                continue;
            }
            word = stemWord(word);
            // check to see if the word is not in the dictionary
            // if not add it
            if (!index.containsKey(word)) {
                index.put(word, new PostingDict());
            }
             // Retrieve the posting dictionary for the word
            PostingDict wordPostingDict = index.get(word);
            // if this word hasn't appeared in this file before, add it to the posting list.
            if (!wordPostingDict.postingListContains(fileId)) {
                wordPostingDict.doc_freq += 1; //set doc freq to the number of doc that contain the term
                if (wordPostingDict.pList == null) {
                    wordPostingDict.pList = new Posting(fileId);
                    wordPostingDict.last = wordPostingDict.pList;
                } else {
                    wordPostingDict.last.next = new Posting(fileId);
                    wordPostingDict.last = wordPostingDict.last.next;
                }
            } else {
                wordPostingDict.last.dtf += 1;     // increase term frequency in this document
            }
            //set the term_fteq in the collection
            wordPostingDict.term_freq += 1;
            
            if (word.equalsIgnoreCase("lattice")) {
                System.out.println("  <<" + wordPostingDict.getPosting(1) + ">> " + line);
            }

        }
        return processedWordsInLine;  // return the number of words processed in this line
    }

    //----------------------------------------------------------------------------
    //stopWord(String word) is a Boolean function that checks whether a given word is a stop word or not
    boolean stopWord(String word) {
    // Check if the word is in the list of common stop words (the,to,be,for,from,in,a,into,by,or,and,that)
        if (word.equals("the") || word.equals("to") || word.equals("be") || word.equals("for") || word.equals("from") || word.equals("in")
                || word.equals("a") || word.equals("into") || word.equals("by") || word.equals("or") || word.equals("and") || word.equals("that")) {
            return true; // Return true if it is a stop word
        }
         // If the word has less than 2 characters, consider it a stop word
        return word.length() < 2; // returns true for very short words
    }
//----------------------------------------------------------------------------  

    String stemWord(String word) {
        //skip for now
        return word;
//        Stemmer s = new Stemmer();
//        s.addString(word);
//        s.stem();
//        return s.toString();
    }

    //----------------------------------------------------------------------------
    Posting intersect(Posting pL1, Posting pL2) {
        Posting answer = null; // the final answer
        Posting last = null; // the last node in the answer list
        while (pL1 != null && pL2 != null) {
            if (pL1.docId == pL2.docId) {
                // if the two posting lists have the same doc id,
                // add the doc id to the answer list
                Posting newNode = new Posting(pL1.docId);
                if (answer == null) {
                    // if this is the first node in the answer list
                    answer = newNode;
                    last = answer;
                } else {
                    // add the new node to the end of the answer list
                    last.next = newNode;
                    last = newNode;
                }
                pL1 = pL1.next; // move to the next doc in the first list
                pL2 = pL2.next; // move to the next doc in the second list
            } else if (pL1.docId < pL2.docId) {
                // if the doc id in the first list is smaller, move to the next doc in the first list
                pL1 = pL1.next;
            } else {
                // if the doc id in the second list is smaller, move to the next doc in the second list
                pL2 = pL2.next;
            }
        }
        return answer;
    }

    public String find_24_01(String phrase) { // any mumber of terms non-optimized search
        String result = "";
        String[] words = phrase.split("\\W+");
        int len = words.length;

        // Check if all words exist in the index before proceeding
        for (int i = 0; i < len; i++) {
            String word = words[i].toLowerCase();
            if (!index.containsKey(word)) {
                return "No results found for '" + phrase + "'";
            }
        }

        // Start with the posting list of the first word
        Posting posting = index.get(words[0].toLowerCase()).pList;

        // Intersect the posting lists of the remaining words
        int i = 1;
        while (i < len) {
            // Intersect the current posting list with the posting list of the next word
            posting = intersect(posting, index.get(words[i].toLowerCase()).pList);
            i++;
        }

        // Print out the results
        while (posting != null) {
            //System.out.println("\t" + sources.get(num));
            // Print out the document ID, title and length of the document
            result += "\t" + posting.docId + " - " + sources.get(posting.docId).title + " - " + sources.get(posting.docId).length + "\n";
            posting = posting.next;
        }
        return result;
    }


    //-----------------------------------------------------------------------------------------------
    String[] sort(String[] words) {  //bubble sort
        boolean sorted = false;
        String sTmp;
    //------------------------------------------------------------------------------------------------
        while (!sorted) { // while the array is not sorted
            sorted = true; // assume the array is sorted
            for (int i = 0; i < words.length - 1; i++) { // loop through the array
                int compare = words[i].compareTo(words[i + 1]);
                if (compare > 0) { // if the current element is larger than the next element
                    sTmp = words[i]; // swap the two elements
                    words[i] = words[i + 1];
                    words[i + 1] = sTmp;
                    sorted = false; // the array is not sorted
                }
            }
        }
        return words;
    }

    //--------------------------------------------------------------------------------------------------------------------------
//This method saves (stores) an index into a file for later retrieval.
// It writes both source records and index records to a file in a structured format.
    public void store(String storageName) {
        try {
            // Define the file path where the index will be stored
            String pathToStorage = "index/" + storageName;
            Writer wr = new FileWriter(pathToStorage);

            // write the source records
            for (Map.Entry<Integer, SourceRecord> entry : sources.entrySet()) {
                System.out.println("Key = " + entry.getKey() + ", Value = " + entry.getValue().URL + ", Value = " + entry.getValue().title + ", Value = " + entry.getValue().text);
                wr.write(entry.getKey().toString() + ","); //doc id
                wr.write(entry.getValue().URL.toString() + ","); //url
                wr.write(entry.getValue().title.replace(',', '~') + ","); //title
                wr.write(entry.getValue().length + ","); //length of the text
                wr.write(String.format("%4.4f", entry.getValue().norm) + ","); //norm of the text
                wr.write(entry.getValue().text.toString().replace(',', '~') + "\n"); //text
            }
            wr.write("section2" + "\n");

            // write the index records
            Iterator<Map.Entry<String, PostingDict>> it = index.entrySet().iterator();
            while (it.hasNext()) {
                Map.Entry<String, PostingDict> pair = it.next();
                PostingDict dd = pair.getValue();
                //  System.out.print("** [" + pair.getKey() + "," + dd.doc_freq + "] <" + dd.term_freq + "> =--> ");
                wr.write(pair.getKey().toString() + "," + dd.doc_freq + "," + dd.term_freq + ";");// Write term, document frequency, and term frequency
                Posting p = dd.pList;// Write posting list (linked list of documents containing the term)
                while (p != null) {
                    //    System.out.print( p.docId + "," + p.dtf + ":");
                    wr.write(p.docId + "," + p.dtf + ":"); //doc id, term freq in the doc
                    p = p.next;
                }
                wr.write("\n");
            }
            wr.write("end" + "\n");
            wr.close();
            System.out.println("=============EBD STORE=============");

        } catch (Exception e) {
            e.printStackTrace();// Print any error that occurs during file writing
        }
    }

    //---------------------------------------------------------------------------------------------
    
    // checks if a file with the given storageName exists in the specified directory
    public boolean storageFileExists(String storageName) {
         // Create a file object representing the file in the specified directory
        java.io.File f = new java.io.File("/home/ehab/tmp11/rl/" + storageName);
        // Check if the file exists and is not a directory
        if (f.exists() && !f.isDirectory())
            return true;
        return false;

    }

    //-----------------------------------------------------------------------------------------------

    // creates a new file with the given storageName inside the directory "/home/ehab/tmp11/"
    public void createStore(String storageName) {
        try {
            
            // construct the full path to the storage file
            String pathToStorage = "/home/ehab/tmp11/" + storageName;
            
            // create a FileWriter to write to the specified file
            Writer wr = new FileWriter(pathToStorage);
            
            wr.write("end" + "\n");
            wr.close();

        } catch (Exception e) {
            // print stack trace if an error occur
            e.printStackTrace();
        }
    }

    //----------------------------------------------------
    //load index from hard disk into memory
    public HashMap<String, PostingDict> load(String storageName) {
        try {
            String pathToStorage = "/home/ehab/tmp11/rl/" + storageName; // select the right path to load from
            sources = new HashMap<Integer, SourceRecord>(); // to store sourceRecord instances
            index = new HashMap<String, PostingDict>(); // to store postingDict instances
            BufferedReader file = new BufferedReader(new FileReader(pathToStorage)); // to read from the specified file path
            String ln = "";
            int flen = 0;
            while ((ln = file.readLine()) != null) { // start reading the file and as long as EOF hasn't been reached
                if (ln.equalsIgnoreCase("section2")) { // reads lines until it encounters "section2"
                    break;
                }
                String[] ss = ln.split(","); // split each line by commas
                int fid = Integer.parseInt(ss[0]); // casts the first string in the array as an int
                try {
                    // prints the fid integer along with the other strings in the array according to a specific format
                    System.out.println("**>>" + fid + " " + ss[1] + " " + ss[2].replace('~', ',') + " " + ss[3] + " [" + ss[4] + "]   " + ss[5].replace('~', ','));
                    // creates a sourceRecord object
                    SourceRecord sr = new SourceRecord(fid, ss[1], ss[2].replace('~', ','), Integer.parseInt(ss[3]), Double.parseDouble(ss[4]), ss[5].replace('~', ','));
                    //   System.out.println("**>>"+fid+" "+ ss[1]+" "+ ss[2]+" "+ ss[3]+" ["+ Double.parseDouble(ss[4])+ "]  \n"+ ss[5])
                    sources.put(fid, sr); // add the sr object to the sources map and link it with the fid
                } catch (Exception e) {
                    // in case of an error happened while casting the string as an int
                    System.out.println(fid + "  ERROR  " + e.getMessage());
                    e.printStackTrace();
                }
            }
            while ((ln = file.readLine()) != null) { // continues reading until EOF
                //     System.out.println(ln);
                if (ln.equalsIgnoreCase("end")) { // continues reading until it finds "end"
                    break;
                }
                String[] ss1 = ln.split(";"); // splits each line read by ;
                String[] ss1a = ss1[0].split(","); // splits the first element of the ss1 array by comma
                String[] ss1b = ss1[1].split(":"); // splits the second element of the ss1 array by :
                // creates a postingDict object, links it with the first string in ss1a and add them to the index map
                index.put(ss1a[0], new PostingDict(Integer.parseInt(ss1a[1]), Integer.parseInt(ss1a[2])));
                String[] ss1bx;   //posting
                for (int i = 0; i < ss1b.length; i++) { // iterate over ss1b array
                    ss1bx = ss1b[i].split(","); // split each element by commas and put the result array in ss1bx
                    if (index.get(ss1a[0]).pList == null) { // checks if the pList of the postingDict corresponding to the key ss1a[0] is null
                        // creates a new Posting object using elements in ss1bx (assumed to be integers)
                        index.get(ss1a[0]).pList = new Posting(Integer.parseInt(ss1bx[0]), Integer.parseInt(ss1bx[1]));
                        index.get(ss1a[0]).last = index.get(ss1a[0]).pList; // last points to this new posting (pList)
                    } else { // if pList in not null
                        // create a new posting and link it to the existing list
                        index.get(ss1a[0]).last.next = new Posting(Integer.parseInt(ss1bx[0]), Integer.parseInt(ss1bx[1]));
                        index.get(ss1a[0]).last = index.get(ss1a[0]).last.next; // update last pointer to the newly created posting
                    }
                }
            }
            System.out.println("============= END LOAD =============");
            //    printDictionary();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return index; // returns the index map that has mapping between string and postingDict
    }
}

