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

    //--------------------------------------------
    int N = 0;
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


    //---------------------------------------------
    public void printPostingList(Posting p) {
        System.out.print("[");
        while (p != null) {
            if (p.next != null) {
                System.out.print(p.docId + ",");
            } else {
                System.out.print(p.docId);
            }
            p = p.next;
        }
        System.out.println("]");
    }

    //---------------------------------------------
    public void printDictionary() {
        Iterator<Map.Entry<String, PostingDict>> idxIt = index.entrySet().iterator();
        while (idxIt.hasNext()) {
            Map.Entry<String, PostingDict> IdxPair = idxIt.next();
            PostingDict postingDict = IdxPair.getValue();
//            System.out.print("** [" + IdxPair.getKey() + "," + postingDict.doc_freq + "]       =--> ");
            System.out.printf("** %-15s %-5d =--> ", IdxPair.getKey(), postingDict.doc_freq);
            printPostingList(postingDict.pList);
        }
        System.out.println("------------------------------------------------------");
        System.out.println("*** Number of terms = " + index.size());
    }

    //-----------------------------------------------
    //Reads a set of files, processes the content line by line,calls indexOneLine fun to process the line
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

    //----------------------------------------------------------------------------  
    public int indexOneLine(String line, int fileId) {
        int processedWordsInLine = 0;

        String[] words = line.split("\\W+");
        //   String[] words = line.replaceAll("(?:[^a-zA-Z0-9 -]|(?<=\\w)-(?!\\S))", " ").toLowerCase().split("\\s+");
        processedWordsInLine += words.length;
        for (String word : words) {
            word = word.toLowerCase();
            if (stopWord(word)) {
                continue;
            }
            word = stemWord(word);
            // check to see if the word is not in the dictionary
            // if not add it
            if (!index.containsKey(word)) {
                index.put(word, new PostingDict());
            }
            // add document id to the posting list
            PostingDict wordPostingDict = index.get(word);
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
                wordPostingDict.last.dtf += 1;
            }
            //set the term_fteq in the collection
            wordPostingDict.term_freq += 1;
            if (word.equalsIgnoreCase("lattice")) {

                System.out.println("  <<" + wordPostingDict.getPosting(1) + ">> " + line);
            }

        }
        return processedWordsInLine;
    }

    //----------------------------------------------------------------------------
    boolean stopWord(String word) {
        if (word.equals("the") || word.equals("to") || word.equals("be") || word.equals("for") || word.equals("from") || word.equals("in")
                || word.equals("a") || word.equals("into") || word.equals("by") || word.equals("or") || word.equals("and") || word.equals("that")) {
            return true;
        }
        return word.length() < 2;
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


    //---------------------------------
    String[] sort(String[] words) {  //bubble sort
        boolean sorted = false;
        String sTmp;
        //-------------------------------------------------------
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

    //---------------------------------

    public void store(String storageName) {
        try {
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
                wr.write(pair.getKey().toString() + "," + dd.doc_freq + "," + dd.term_freq + ";"); //term, doc freq, term freq
                Posting p = dd.pList;
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
            e.printStackTrace();
        }
    }

    //=========================================
    public boolean storageFileExists(String storageName) {
        java.io.File f = new java.io.File("/home/ehab/tmp11/rl/" + storageName);
        if (f.exists() && !f.isDirectory())
            return true;
        return false;

    }

    //----------------------------------------------------
    public void createStore(String storageName) {
        try {
            String pathToStorage = "/home/ehab/tmp11/" + storageName;
            Writer wr = new FileWriter(pathToStorage);
            wr.write("end" + "\n");
            wr.close();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    //----------------------------------------------------
    //load index from hard disk into memory
    public HashMap<String, PostingDict> load(String storageName) {
        try {
            String pathToStorage = "/home/ehab/tmp11/rl/" + storageName;
            sources = new HashMap<Integer, SourceRecord>();
            index = new HashMap<String, PostingDict>();
            BufferedReader file = new BufferedReader(new FileReader(pathToStorage));
            String ln = "";
            int flen = 0;
            while ((ln = file.readLine()) != null) {
                if (ln.equalsIgnoreCase("section2")) {
                    break;
                }
                String[] ss = ln.split(",");
                int fid = Integer.parseInt(ss[0]);
                try {
                    System.out.println("**>>" + fid + " " + ss[1] + " " + ss[2].replace('~', ',') + " " + ss[3] + " [" + ss[4] + "]   " + ss[5].replace('~', ','));

                    SourceRecord sr = new SourceRecord(fid, ss[1], ss[2].replace('~', ','), Integer.parseInt(ss[3]), Double.parseDouble(ss[4]), ss[5].replace('~', ','));
                    //   System.out.println("**>>"+fid+" "+ ss[1]+" "+ ss[2]+" "+ ss[3]+" ["+ Double.parseDouble(ss[4])+ "]  \n"+ ss[5]);
                    sources.put(fid, sr);
                } catch (Exception e) {

                    System.out.println(fid + "  ERROR  " + e.getMessage());
                    e.printStackTrace();
                }
            }
            while ((ln = file.readLine()) != null) {
                //     System.out.println(ln);
                if (ln.equalsIgnoreCase("end")) {
                    break;
                }
                String[] ss1 = ln.split(";");
                String[] ss1a = ss1[0].split(",");
                String[] ss1b = ss1[1].split(":");
                index.put(ss1a[0], new PostingDict(Integer.parseInt(ss1a[1]), Integer.parseInt(ss1a[2])));
                String[] ss1bx;   //posting
                for (int i = 0; i < ss1b.length; i++) {
                    ss1bx = ss1b[i].split(",");
                    if (index.get(ss1a[0]).pList == null) {
                        index.get(ss1a[0]).pList = new Posting(Integer.parseInt(ss1bx[0]), Integer.parseInt(ss1bx[1]));
                        index.get(ss1a[0]).last = index.get(ss1a[0]).pList;
                    } else {
                        index.get(ss1a[0]).last.next = new Posting(Integer.parseInt(ss1bx[0]), Integer.parseInt(ss1bx[1]));
                        index.get(ss1a[0]).last = index.get(ss1a[0]).last.next;
                    }
                }
            }
            System.out.println("============= END LOAD =============");
            //    printDictionary();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return index;
    }
}

//=====================================================================
