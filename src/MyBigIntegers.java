import javax.swing.*;
import java.lang.management.ManagementFactory;
import java.lang.management.ThreadMXBean;
import java.io.*;
import java.util.Arrays;

public class MyBigIntegers {

    static ThreadMXBean bean = ManagementFactory.getThreadMXBean( );

    /* define constants */
    static long MAXVALUE =  2000000000;
    static long MINVALUE = -2000000000;
    static int numberOfTrials = 100;
    static int MAXINPUTSIZE  = (int) Math.pow(2,10);
    static int MININPUTSIZE  =  1;
    // static int SIZEINCREMENT =  10000000; // not using this since we are doubling the size each time

    static String ResultsFolderPath = "/home/karson/Results/"; // pathname to results folder
    static FileWriter resultsFile;
    static PrintWriter resultsWriter;


    public static void main(String[] args) {

        // run the whole experiment at least twice, and expect to throw away the data from the earlier runs, before java has fully optimized
        runFullExperiment("BigIntMat-Exp1-ThrowAway.txt");
        runFullExperiment("BigIntMat-Exp2.txt");
        runFullExperiment("BigIntMat-Exp3.txt");
    }

    static void runFullExperiment(String resultsFileName){

        try {
            resultsFile = new FileWriter(ResultsFolderPath + resultsFileName);
            resultsWriter = new PrintWriter(resultsFile);
        } catch(Exception e) {
            System.out.println("*****!!!!!  Had a problem opening the results file "+ResultsFolderPath+resultsFileName);
            return; // not very foolproof... but we do expect to be able to create/open the file...
        }

        ThreadCpuStopWatch BatchStopwatch = new ThreadCpuStopWatch(); // for timing an entire set of trials
        ThreadCpuStopWatch TrialStopwatch = new ThreadCpuStopWatch(); // for timing an individual trial



        resultsWriter.println("#InputSize    AverageTime"); // # marks a comment in gnuplot data
        resultsWriter.flush();
        /* for each size of input we want to test: in this case starting small and doubling the size each time */
        for(int inputSize=MININPUTSIZE;inputSize<=MAXINPUTSIZE; inputSize*=2) {
            // progress message...
            System.out.println("Running test for input size "+inputSize+" ... ");

            /* repeat for desired number of trials (for a specific size of input)... */
            long batchElapsedTime = 0;
            // generate a list of randomly spaced integers in ascending sorted order to use as test input
            // In this case we're generating one list to use for the entire set of trials (of a given input size)
            // but we will randomly generate the search key for each trial



            System.out.print("    Generating test data...");
            long[] aValue = createSortedntegerList(inputSize);
            long[] bValue = createSortedntegerList(inputSize);
            System.out.println("...done.");
            System.out.print("    Running trial batch...");

            /* force garbage collection before each batch of trials run so it is not included in the time */
            System.gc();


            // instead of timing each individual trial, we will time the entire set of trials (for a given input size)
            // and divide by the number of trials -- this reduces the impact of the amount of time it takes to call the
            // stopwatch methods themselves
            BatchStopwatch.start(); // comment this line if timing trials individually

            // run the tirals
            for (long trial = 0; trial < numberOfTrials; trial++) {
                // generate a random key to search in the range of a the min/max numbers in the list
                //long testSearchKey = (long) (0 + Math.random() * (testList[testList.length-1]));
                /* force garbage collection before each trial run so it is not included in the time */
                // System.gc();

                //TrialStopwatch.start(); // *** uncomment this line if timing trials individually
                /* run the function we're testing on the trial input */

                //long[] cValue = Plus(aValue,bValue);
                //long[] dValue = Times(aValue, bValue);
                //long[] eValue = FibLoopBig(inputSize);
                long[][] fValue = FibMatrixBig(inputSize);


                // batchElapsedTime = batchElapsedTime + TrialStopwatch.elapsedTime(); // *** uncomment this line if timing trials individually
            }
            batchElapsedTime = BatchStopwatch.elapsedTime(); // *** comment this line if timing trials individually
            double averageTimePerTrialInBatch = (double) batchElapsedTime / (double)numberOfTrials; // calculate the average time per trial in this batch

            /* print data for this size of input */
            resultsWriter.printf("%12d  %15.2f \n",inputSize, averageTimePerTrialInBatch); // might as well make the columns look nice
            resultsWriter.flush();
            System.out.println(" ....done.");
        }
    }

    /* return index of the searched number if found, or -1 if not found */
    public static long[] createSortedntegerList(int size) {
        long[] newList = new long[size];
        newList[0] = (long) (10 * Math.random());
        for (int j = 1; j < size; j++) {
            newList[j] = newList[j - 1] + (long) (10 * Math.random());
            //if(j%100000 == 0) {resultsWriter.printf("%d  %d <<<\n",j,newList[j]);  resultsWriter.flush();}
        }
        return newList;
    }

    public static long[] Plus(long[] a, long[] b){
        int h = a.length, k = b.length;
        int max = Math.max(h,k), min = Math.min(h,k), base = 2^max;
        int carry = 0;
        long[] x = new long[max];

        for(int i = 0; i < max ; i++){
            if( i < min )
                x[i] = a[i] + b[i] + carry;
            else if( i < h )
                x[i] = a[i] + carry;
            else
                x[i] = b[i] + carry;

            if(x[i] >= base){
                carry = 1;
                x[i] = x[i] - base;
            }
            else
                carry = 0;
        }
        if(carry == 1)
            x[max-1] = 1;
        return x;
    }

    public static long[] Times(long[] a, long[] b){
        int h = a.length, k = b.length;
        int max = Math.max(h,k), min = Math.min(h,k), base = 2^max;
        long F, G, H, K;
        long[] x = new long[max];

        for( int i = 0 ; i < max-1 ; i++){
            if( i < min ) {
                F = a[i] * b[i];
                G = a[i + 1] * b[i + 1];
                H = (a[i] + a[i + 1]) * (b[i] + b[i + 1]);
                K = H - F - G;
                x[i] = (F * (base ^ 2)) + (K * base) + G;
            }
            else
                x[i] = 0;
        }

        return x;
    }

    public static long[] FibLoopBig(int size){
        long[] x = new long[size]; //fibList

        if(size == 1)
            x[0] = 0;
        if(size == 2) {
            x[0] = 0;
            x[1] = 1;
        }
        if(size > 2){
            x[0] = 0;
            x[1] = 1;
            for(int i = 2; i < size; i++){
                x[i] = x[i-1] + x[i-2];
            }
        }
        return x;
    }

    public static long[][] FibMatrixBig(int size){
        long F[][] = new long[][]{{1,1},{1,0}};
        if(size == 0) {
            F[0][0] = 0;
            return F;
        }
        power(F, size-1);
        return F;
    }

    public static void power(long F[][], long x){
        long i;
        long M[][] = new long[][]{{1,1},{1,0}};

        for(i = 2; i<=x; i++)
            multiply(F, M);
    }

    public static void multiply(long F[][], long M[][]){
        long x =  F[0][0]*M[0][0] + F[0][1]*M[1][0];
        long y =  F[0][0]*M[0][1] + F[0][1]*M[1][1];
        long z =  F[1][0]*M[0][0] + F[1][1]*M[1][0];
        long w =  F[1][0]*M[0][1] + F[1][1]*M[1][1];

        F[0][0] = x;
        F[0][1] = y;
        F[1][0] = z;
        F[1][1] = w;
    }
}