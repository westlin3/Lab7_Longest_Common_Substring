package csc482;

import javafx.util.Pair;

import java.io.*;
import java.lang.management.ManagementFactory;
import java.lang.management.ThreadMXBean;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

public class LongestCommonString {

    static ThreadMXBean bean = ManagementFactory.getThreadMXBean( );

    /* define constants */

    static long MAXVALUE =  200000000;

    static long MINVALUE = -200000000;

    static int numberOfTrials = 20;
    static int MAXINPUTSIZE  = (int) Math.pow(2,14);;
    static int MININPUTSIZE  =  1;

    static String ResultsFolderPath = "/home/curtis/Bean/LAB7/"; // pathname to results folder
    static FileWriter resultsFile;
    static PrintWriter resultsWriter;

    static void runFullExperiment(String resultsFileName) {

        try {
            resultsFile = new FileWriter(ResultsFolderPath + resultsFileName);
            resultsWriter = new PrintWriter(resultsFile);
        } catch (Exception e) {
            System.out.println("*****!!!!!  Had a problem opening the results file " + ResultsFolderPath + resultsFileName);
            return; // not very foolproof... but we do expect to be able to create/open the file...
        }

        ThreadCpuStopWatch BatchStopwatch = new ThreadCpuStopWatch(); // for timing an entire set of trials
        ThreadCpuStopWatch TrialStopwatch = new ThreadCpuStopWatch(); // for timing an individual trial

        resultsWriter.println("#InputSize    AverageTime"); // # marks a comment in gnuplot data
        resultsWriter.flush();

        for (int inputSize = MININPUTSIZE; inputSize <= MAXINPUTSIZE; inputSize *= 2) {
            // progress message...
            System.out.println("Running test for input size " + inputSize + " ... ");

            /* repeat for desired number of trials (for a specific size of input)... */
            long batchElapsedTime = 0;

            /* force garbage collection before each batch of trials run so it is not included in the time */
            //System.gc();

            // instead of timing each individual trial, we will time the entire set of trials (for a given input size)
            // and divide by the number of trials -- this reduces the impact of the amount of time it takes to call the
            // stopwatch methods themselves
            String book = "/home/curtis/Bean/LAB7/XmasCarol";
            String string1 = ReadBook(inputSize, book);
            String string2 = ReadBook(inputSize, book);
            BatchStopwatch.start(); // comment this line if timing trials individually

            // run the trials
            for (long trial = 0; trial < numberOfTrials; trial++) {
                GoodLCS(string1, string2);
            }

            batchElapsedTime = BatchStopwatch.elapsedTime(); // *** comment this line if timing trials individually
            double averageTimePerTrialInBatch = (double) batchElapsedTime / (double) numberOfTrials; // calculate the average time per trial in this batch

            /* print data for this size of input */
            resultsWriter.printf("%12d  %15.2f\n", inputSize, averageTimePerTrialInBatch); // might as well make the columns look nice
            resultsWriter.flush();
            System.out.println(" ....done.");

        }
    }

    public static void main(String[] args) {
        runFullExperiment("LCSDynamicBook-Exp1.txt");
        runFullExperiment("LCSDynamicBook-Exp2.txt");
        runFullExperiment("LCSDynamicBook-Exp3.txt");

    }

    // Find LCS based on given pseudo code
    public static int LCSBruteForce(String S1, String S2) {
        int Length1 = S1.length();
        int Length2 = S2.length();
        int k;
        int LCS = 0;
        // Using char arrays for ease of use
        char[] S1Array = S1.toCharArray();
        char[] S2Array = S2.toCharArray();

        for (int i = 0; i < Length1; i++)
            for (int j = 0; j < Length2; j++){
                // iterate over the strings for as long as they match
                for(k = 0; i+k <Length1 && j+k<Length2; k++) {
                    if ( S1Array[i+k] != S2Array[j+k] ) {
                        break;
                    }
                }
                // set the lcs value
                if ( k > LCS ) {
                    LCS= k;
                }
            }
        return LCS;
    }

    public static int GoodLCS(String S1, String S2) {
        int Length1 = S1.length();
        int Length2 = S2.length();
        int LCSMatrix[][] = new int[Length1][Length2];
        int LCS = 0;
        //Loop over string 1 and 2
        for (int i = 0; i < Length1; i++)
            for (int j = 0; j < Length2; j++) {
                if (S1.charAt(i) == S2.charAt(j)) {
                    if (i == 0 || j == 0) { //case where we are at start of matrix
                        LCSMatrix[i][j] = 1;
                    } else {
                        //This increments the current location of the substring by the value of the location before it
                        LCSMatrix[i][j] = LCSMatrix[i - 1][j - 1] + 1;
                    }
                    // Set LCS if the current LCS is longer than the stored value.
                    if (LCS < LCSMatrix[i][j])
                        LCS = LCSMatrix[i][j];
                }
            }
        return LCS;
    }


    //https://www.geeksforgeeks.org/generate-random-string-of-given-size-in-java/
    static String getAlphaNumericString(int n)
    {
        // chose a Character random from this String
        String AlphaNumericString = "ABCDEFGHIJKLMNOPQRSTUVWXYZ"
                + "0123456789"
                + "abcdefghijklmnopqrstuvxyz";
        // create StringBuffer size of AlphaNumericString
        StringBuilder sb = new StringBuilder(n);
        for (int i = 0; i < n; i++) {
            // generate a random number between
            // 0 to AlphaNumericString variable length
            int index
                    = (int)(AlphaNumericString.length()
                    * Math.random());
            // add Character one by one in end of sb
            sb.append(AlphaNumericString
                    .charAt(index));
        }
        return sb.toString();
    }

    //Create a string of x given a length
    static String RepeatedString(int length){
        StringBuilder sb = new StringBuilder(length);
        for (int i = 0; i < length; i++) {
            sb.append("x");
        }
        return sb.toString();
    }

    //https://howtodoinjava.com/java/io/java-read-file-to-string-examples/
    public static String ReadBook(int length, String filePath){
        StringBuilder contentBuilder = new StringBuilder();
        try (Stream<String> stream = Files.lines( Paths.get(filePath), StandardCharsets.UTF_8))
        {
            stream.forEach(s -> contentBuilder.append(s).append("\n"));
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
        String book = contentBuilder.toString();
        int l = book.length();
        // Set a random start point from the beginning of the book that won't
        // let the end put be longer than the books length
        int beginning = (int) ((l-length-1)*Math.random());
        return book.substring(beginning, beginning+length);
    }
}
