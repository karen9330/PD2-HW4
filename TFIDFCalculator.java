import java.io.BufferedReader;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.Iterator;
import java.io.File;


public class TFIDFCalculator{

    public static double[] tfIdfCalculate(ArrayList<String> docs, LinkedList<String> targetWords , LinkedList<Integer> docNumbers) {
    //System.out.printf("%f * %f\n", idf,tf);
    double[] result = new double[targetWords.size()];
    double numberTargetWordInDoc = 0;
    double numberOfDocContainTargetWord = 0;
    double wordNumInDoc = 0;   //指定文檔的總字數

    Trie node = new Trie();

    for(int i=0;i<docs.size();i++){
        for(String word : docs.get(i).split("\\s+")){
            node.insert(word,i);
        }
    }

    for(int i=0;i<docNumbers.size();i++){
        int[] TFIDFResult = node.searchCount(targetWords.get(i),docNumbers.get(i));
        numberTargetWordInDoc = TFIDFResult[1];
        numberOfDocContainTargetWord = TFIDFResult[0];
        wordNumInDoc = docs.get(docNumbers.get(i)).split("\\s+").length;
        result[i] = (Math.log(docs.size() / numberOfDocContainTargetWord)) * (numberTargetWordInDoc / wordNumInDoc);
    }
    return result;
}
    
    public static void main(String[] args) {
        //long startTime = System.nanoTime();
        CorpusHandling corpus = new CorpusHandling();
        OutputIssues output = new OutputIssues();
        ArrayList<String> documents =  corpus.readTxt(args[0]);

        LinkedList<InputArguments>  inputIssues = InputArguments.getInputIssues(args[1]);

        LinkedList<String> allTargetWords = new LinkedList<>();
        LinkedList<Integer> allDocNumbers = new LinkedList<>();

        for (InputArguments arg : inputIssues) {
            allTargetWords.addAll(arg.targetWords);  
            allDocNumbers.addAll(arg.docNumbers);    
        }

        output.getResult(allTargetWords, allDocNumbers,documents);
        //long endTime = System.nanoTime();
        //double duration = (endTime - startTime)/1000000000.0;
        //System.out.println("time: " +duration + " s");
    }
}

class TrieNode {
    TrieNode[] children = new TrieNode[26];
    boolean isEndOfWord = false;
    SimpleHashMap<Integer, Integer> docMap = new SimpleHashMap<>();
    int docsCnt = 0;   //出現targetWord的文檔數量

    public void addOrUpdateDoc(int docIndex) {
        Integer currentCount = docMap.get(docIndex);
        if (currentCount == null) {
            docMap.put(docIndex, 1);
            docsCnt++;  // 新文档，增加文档计数
        } else {
            docMap.put(docIndex, currentCount + 1);
        }
    }
}

class Trie {
    TrieNode root = new TrieNode();
    
    // 插入一個單詞到 Trie
    public void insert(String word , int indexOfDoc) {
        TrieNode node = root;
        for (char c : word.toCharArray()) {
            if (node.children[c - 'a'] == null) {
                node.children[c - 'a'] = new TrieNode();
            }
            node = node.children[c - 'a'];
        }

        node.isEndOfWord = true;
        node.addOrUpdateDoc(indexOfDoc);
    }

    public int[] searchCount(String word, int indexOfDoc) {
        int[] returnTFIDF = {0,0};
        TrieNode node = root;
        for (char c : word.toCharArray()) {
            node = node.children[c - 'a'];
            if (node == null) {
                return returnTFIDF;
            }
        }
        if (node.isEndOfWord) {
            Integer count = node.docMap.get(indexOfDoc);
            returnTFIDF[1] = count == null ? 0 : count; 
            returnTFIDF[0] = node.docsCnt; 
        }
        return returnTFIDF;
    }
}


class  CorpusHandling {
    
    //把corpus中的句子5個一組放到ArrayList中
    public  ArrayList<String> readTxt(String path){
        ArrayList<String> documents = new ArrayList<>();
        try(BufferedReader bufferedReader = new BufferedReader(new FileReader(path))){
            String line;
            StringBuilder combinedLine = new StringBuilder();
            int cnt = 0;
            while (((line = bufferedReader.readLine()) != null)) {
                combinedLine.append(segmentation(line)).append(" ");
                cnt++;
                if(cnt == 5){
                    documents.add(combinedLine.toString().trim());
                    combinedLine.setLength(0);
                    cnt=0;
                }
            }
        }catch (IOException e) {
            e.printStackTrace();
        }

        return documents;
    }
    //把文本的內容進行處理
    public String segmentation(String line){
        line = line.replaceAll("[^a-zA-Z]", " ");
        line = line.replaceAll("\\s+", " ");
        line = line.trim();
        line = line.toLowerCase();
        return line;
    }
}

class InputArguments{

    public LinkedList<String> targetWords;
    public LinkedList<Integer> docNumbers;

    public InputArguments(LinkedList<String> text, LinkedList<Integer> numbers) {
        this.targetWords = text ;
        this.docNumbers = numbers;
    }

    public static LinkedList<InputArguments> getInputIssues(String fileName){
        LinkedList<InputArguments> inputList = new LinkedList<>();
    
        try(BufferedReader bufferedReader = Files.newBufferedReader(Paths.get(fileName))){
            String lineTargetWord;
            while (((lineTargetWord = bufferedReader.readLine()) != null)) {
                String lineNumbers = bufferedReader.readLine();  

                LinkedList<String> words = new LinkedList<>();
                LinkedList<Integer> numbers = new LinkedList<>();

                for (String word : lineTargetWord.split(" ")) {
                    words.add(word);
                }

                for (String number : lineNumbers.split(" ")) {
                    numbers.add(Integer.parseInt(number));
                }

                inputList.add(new InputArguments(words, numbers));
            }
        }catch (IOException e) {
            e.printStackTrace();
        }

        return inputList;
    }
}

class OutputIssues{

    public void getResult(LinkedList<String> targetWords, LinkedList<Integer> docNumbers,ArrayList<String> documents){
        double[] result = TFIDFCalculator.tfIdfCalculate(documents,targetWords,docNumbers);

        String fileName = "output.txt";
        File file = new File(fileName);
        try (FileWriter fw = new FileWriter(fileName)){
            for(double writedResult : result){
                fw.write(String.format("%.5f", writedResult)+" ");
            }
        }
        catch (IOException e) {
                System.out.println("An error occurred while writing to CSV file.");
                e.printStackTrace();
        }
    }
}