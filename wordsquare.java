import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

//INPUT FORMAT - Write the command as: java wordsquare 4 aaccdeeeemmnnnoo
public class Wordsquare {
    static int n;
    public static void main(String[] args) {
        n = Integer.parseInt(args[0]);
        char[] chars = args[1].toCharArray();
      
        //To see how frequently each letter appears in the input
        int[] charFreq = new int[26];
        for (char c : chars) {
            charFreq[c - 'a']++; 
        }
        
        //Making a tree structure to find which words can be formed
        Node wordsTrie = new Node();
       
        try {
          //Accessing the dictionary
          URL dict = new URL("http://norvig.com/ngrams/enable1.txt");
          HttpURLConnection dictCon = (HttpURLConnection)dict.openConnection();
          if(dictCon.getResponseCode()==200){
              InputStream dictStream = dictCon.getInputStream();
              BufferedReader dictReader = new BufferedReader(new InputStreamReader(dictStream));     
              String dictWord;
          while ((dictWord = dictReader.readLine()) != null) {
          //Creates new nodes for new letter combinations by adding children to each node
            if (dictWord.length() == n && allowedWords(dictWord, charFreq)) {
                Node currentNode = wordsTrie;
                for (int i = 0; i < dictWord.length(); i++) {
                    int c = dictWord.charAt(i) - 'a';
                    if (currentNode.children[c] == null) {
                        currentNode.children[c] = new Node(c);
                    }
                    currentNode = currentNode.children[c];
                }
             }
           }
          }
         }
         catch (Exception e) {
            System.out.println(e);
         }

         //Generates output wordsquare, if it exists!
         char[][] output = getWordsquare(wordsTrie, charFreq);
         if (output != null) {
            for (int i = 0; i < output.length; i++) {
                System.out.println(new String(output[i]));
         
            }}  else {
            System.out.println("Cannot produce a valid wordsquare for the given sequence.");
            }
       
    }
    
    //These two functions take the allowed word list (only words with length n and containing letters provided) and iterate through each position of the grid
    static char[][] getWordsquare(Node trieRoot, int[] charFreq) {
        Node[][] grid = new Node[n][n+1];
        for (int i = 0; i < grid.length; i++) {
            grid[i][0] = trieRoot;
        }
            
        int[] letterBank = charFreq.clone();
        if (constructGrid(0, 1, letterBank, grid)) {
            char[][] result = new char[n][n];
            for (int r = 0; r < n; r++) {
                for (int c = 0; c < n; c++)
                    result[r][c] = (char) (grid[r][c + 1].key + 'a');
            }
            return result;
        } else {
            return null;
        }
    }
    
    //The int r refers to the row and c to the column of the grid
    static boolean constructGrid(int r, int c, int[] letterBank, Node[][] grid)  {
        //Position divides the grid into the diagonal elements (1) and the other elements (2), 2 because all the others are mirrored
        int position = r==c-1 ? 1 : 2; 

        for (int l = 0; l < 26; l++) {
            Node normalNode = grid[r][c-1].children[l];
            Node mirroredNode = grid[c-1][r].children[l];
            if (normalNode != null && mirroredNode != null && letterBank[l] >= position) {

                grid[r][c] = normalNode;
                grid[c - 1][r + 1] = mirroredNode;
                letterBank[l] -= position; 
               
                if (c == n) { 
                    if (r == n - 1 
                            || constructGrid(r + 1, r + 2, letterBank, grid)) {
                        return true;
                    }
                } else if (constructGrid(r, c + 1, letterBank, grid)) { 
                    return true;
                }

                letterBank[l] += position; 
            }
        }
        return false; 
    }

       static class Node {
        Node[] children;
        int key;
        Node(){
            children = new Node[26];
        }
        Node(int key){
            this();
            this.key = key;
        }
    }
    
    //Iterates through all letters of a word and checks to see whether the letters provided can produce that word
    static boolean allowedWords(String word, int[] charFreq) {
        int[] charsInWord = new int[26];
        boolean diagonal = false;
        for (int i = 0; i < word.length(); i++) {
            int c = word.charAt(i) - 'a';
            int positionsLeft = charFreq[c] - charsInWord[c];
            if (positionsLeft > 1) { 
                charsInWord[c] += 2;
            } else if (positionsLeft == 1 && !diagonal) { 
                charsInWord[c] += 1;
                diagonal = true;
            } else { 
                return false;
            }
        }
        return true;
    }

}
