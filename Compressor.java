import java.io.*;
import java.util.ArrayList;

import java.util.List;
import java.util.Objects;

public class Compressor {
    public static void compress(String inputFile, String outputFile) throws IOException{
        // Read the input file
        String input = FileHandler.readFile(inputFile);

        List<Integer> compressedContent = encode(input);
        // Write the compressed data to the output file
        FileHandler.writeFile(outputFile, compressedContent);
    }

    public static void decompress(String compressedFile, String decompressedOutputFile) throws IOException {
        List<Integer> input = FileHandler.readCompressedData(compressedFile);
        String decompressedData = decode(input);
        // Write the decompressed data to the output file
        FileHandler.writeDecompressedData(decompressedOutputFile, decompressedData);
    }

/*
Encoding algorithm using Lempel Ziv Welch algorithm, where we establish a dictionary to compress a String
 */
    public static List<Integer> encode(String fileContent) {
        int dictionarySize = 256;
        Dictionary dictionary = new Dictionary();

        //Initialize initial dictionary
        for (int i = 0; i < dictionarySize; i++) {
            dictionary.addEntry(String.valueOf((char) i));
        }

        List<Integer> compressedContent = new ArrayList<>();

        String current = "";

        for (char character : fileContent.toCharArray()) {
            String combined = current + character;

            if (dictionary.contains(combined)) {
                current = combined;
            }
            else {
                compressedContent.add(dictionary.translateSeqToValue(current));
                // Add the new entry to the dictionary
                dictionary.addEntry(combined);
                current = String.valueOf(character);
            }
        }

        // Handle the last character
        if (!current.isEmpty()) {
            compressedContent.add(dictionary.translateSeqToValue(current));
        }

        return compressedContent;
    }

    /*
    Decoding algorithm that decompresses a file compressed using Lempel Ziv Welch algorithm.
     */
    public static String decode(List<Integer> compressedContent) {
        int dictionarySize = 256;
        Dictionary dictionary = new Dictionary();

        for (int i = 0; i < dictionarySize; i++) {
            dictionary.addEntry(String.valueOf((char) i));
        }

        String characters = String.valueOf((char) compressedContent.remove(0).intValue());
        StringBuilder uncompressedContent = new StringBuilder(characters);
        for (int value : compressedContent) {
            String entry;
            if (dictionary.getLength() > value) {
                entry = dictionary.translateValueToSeq(value);
            }
            else {
                entry = characters + characters.charAt(0);
            }
            uncompressedContent.append(entry);
            dictionary.addEntry(characters + entry.charAt(0));
            characters = entry;
        }
        return uncompressedContent.toString();
    }
}

class FileHandler {
    public static void writeFile(String fileName, List<Integer> data) {
        try (DataOutputStream outputStream = new DataOutputStream(new FileOutputStream(fileName))) {
            for (Integer number : data) {
                outputStream.writeInt(number);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static List<Integer> readCompressedData(String fileName) {
        List<Integer> integerList = new ArrayList<>();

        try (DataInputStream inputStream = new DataInputStream(new FileInputStream(fileName))) {
            while (inputStream.available() > 0) {
                integerList.add(inputStream.readInt());
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        return integerList;
    }

    public static void writeDecompressedData(String filePath, String decompressedData) throws IOException {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(filePath))) {
            writer.write(decompressedData);
        }
    }

    public static String readFile(String filePath) throws IOException {
        StringBuilder content = new StringBuilder();
        try (BufferedReader br = new BufferedReader(new FileReader(filePath))) {
            String line;
            while ((line = br.readLine()) != null) {
                content.append(line).append("\n");
            }
        }
        return content.toString();
    }


    }



class Entry {
    String sequence;
    int compressedValue;

    public Entry(String sequence, int compressedValue) {
        this.sequence = sequence;
        this.compressedValue = compressedValue;
    }

    public Entry(String sequence) {
        this.sequence = sequence;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Entry entry = (Entry) o;
        return Objects.equals(sequence, entry.sequence);
    }

    @Override
    public int hashCode() {
        return Objects.hash(sequence);
    }
}


class Dictionary {
    ArrayList<Entry> dictionary = new ArrayList<>();

    public void addEntry(String sequence) {
        dictionary.add(new Entry(sequence, dictionary.size()));
    }

    public boolean contains(String sequence) {
        return dictionary.contains(new Entry(sequence));
    }

    public int translateSeqToValue(String sequence) {
        Entry entry = dictionary.stream().filter(e -> e.equals(new Entry(sequence))).findFirst().orElse(null);
        if (entry == null) return 0;
        return entry.compressedValue;
    }

    public String translateValueToSeq(int value) {
        if (dictionary.size() >= value) {
            return dictionary.get(value).sequence;
        }
        return null;
    }

    public int getLength() {
        return dictionary.size();
    }

}

class Test {
    public static void main(String[] args) throws IOException {
        Compressor.compress("diverse.lyx", "compressed.txt");
        Compressor.decompress("compressed.txt", "decompressed.txt");
    }
}