package edu.coursera.parallel;

import java.util.ArrayList;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.ForkJoinTask;
import java.util.concurrent.RecursiveAction;
//import java.lang.*;

/**
 * Class wrapping methods for implementing reciprocal array sum in parallel.
 */
public final class ReciprocalArraySum {

    /**
     * Default constructor.
     */
    private ReciprocalArraySum() {
    }

    // System.setProperty("java.util.concurrent.ForkJoinPool.common.parallelism", "4");

    /**
     * Sequentially compute the sum of the reciprocal values for a given array.
     *
     * @param input Input array
     * @return The sum of the reciprocals of the array input
     */
    protected static double seqArraySum(final double[] input) {
        double sum = 0;

        // Compute sum of reciprocals of array elements
        for (int i = 0; i < input.length; i++) {
            sum += 1 / input[i];
        }

        return sum;
    }

    /**
     * Computes the size of each chunk, given the number of chunks to create
     * across a given number of elements.
     *
     * @param nChunks The number of chunks to create
     * @param nElements The number of elements to chunk across
     * @return The default chunk size
     */
    private static int getChunkSize(final int nChunks, final int nElements) {
        // Integer ceil
        return (nElements + nChunks - 1) / nChunks;
    }

    /**
     * Computes the inclusive element index that the provided chunk starts at,
     * given there are a certain number of chunks.
     *
     * @param chunk The chunk to compute the start of
     * @param nChunks The number of chunks created
     * @param nElements The number of elements to chunk across
     * @return The inclusive index that this chunk starts at in the set of
     *         nElements
     */
    private static int getChunkStartInclusive(final int chunk,
            final int nChunks, final int nElements) {
        final int chunkSize = getChunkSize(nChunks, nElements);
        return chunk * chunkSize;
    }

    /**
     * Computes the exclusive element index that the provided chunk ends at,
     * given there are a certain number of chunks.
     *
     * @param chunk The chunk to compute the end of
     * @param nChunks The number of chunks created
     * @param nElements The number of elements to chunk across
     * @return The exclusive end index for this chunk
     */
    private static int getChunkEndExclusive(final int chunk, final int nChunks,
            final int nElements) {
        final int chunkSize = getChunkSize(nChunks, nElements);
        final int end = (chunk + 1) * chunkSize;
        if (end > nElements) {
            return nElements;
        } else {
            return end;
        }
    }

    /**
     * This class stub can be filled in to implement the body of each task
     * created to perform reciprocal array sum in parallel.
     */
    private static class ReciprocalArraySumTask extends RecursiveAction {
        /**
         * Starting index for traversal done by this task.
         */
        private final int startIndexInclusive;
        /**
         * Ending index for traversal done by this task.
         */
        private final int endIndexExclusive;
        /**
         * Input array to reciprocal sum.
         */
        private final double[] input;
        /**
         * Intermediate value produced by this task.
         */

        private double value;

        private int numTasks;

        private int SEQUENTIAL_THRESHOLD;

        private ForkJoinPool pool;

        /**
         * Constructor.
         * @param setStartIndexInclusive Set the starting index to begin
         *        parallel traversal at.
         * @param setEndIndexExclusive Set ending index for parallel traversal.
         * @param setInput Input values
         */
        ReciprocalArraySumTask(final int setStartIndexInclusive,
                final int setEndIndexExclusive, final double[] setInput, final int numTasks, final int SEQUENTIAL_THRESHOLD) {
            this.startIndexInclusive = setStartIndexInclusive;
            this.endIndexExclusive = setEndIndexExclusive;
            this.input = setInput;
            this.value = 0.0;
            this.numTasks = numTasks;
            this.SEQUENTIAL_THRESHOLD = SEQUENTIAL_THRESHOLD;
        }

        /**
         * Getter for the value produced by this task.
         * @return Value produced by this task
         */
        public double getValue() {
            return value;
        }

        private ArrayList<ReciprocalArraySumTask> createSubTasks(){
            ArrayList<ReciprocalArraySumTask> subTasks = new ArrayList<>();
            for( int chunk = 0; chunk < numTasks; chunk++){
                int chunkStartIndex = getChunkStartInclusive(chunk, numTasks, endIndexExclusive - startIndexInclusive) + startIndexInclusive;
                int chunkEndIndex = getChunkEndExclusive(chunk, numTasks, endIndexExclusive - startIndexInclusive) + startIndexInclusive;
                subTasks.add(new ReciprocalArraySumTask(chunkStartIndex, chunkEndIndex, input, numTasks, SEQUENTIAL_THRESHOLD));
                // System.out.println("-----" + chunkStartIndex + chunkEndIndex);
            }

            return subTasks;
        }

        @Override
        protected void compute() {
            // TODO
            if( endIndexExclusive - startIndexInclusive <= SEQUENTIAL_THRESHOLD){
                for (int i = startIndexInclusive; i < endIndexExclusive; i++) {
                    value += 1 / input[i];
                }
            } else {
                ArrayList<ReciprocalArraySumTask> subTasks = createSubTasks();
                invokeAll(subTasks);
                for(ReciprocalArraySumTask task : subTasks){
                    value += task.getValue();
                }
            }

        }
    }

    /**
     * TODO: Modify this method to compute the same reciprocal sum as
     * seqArraySum, but use two tasks running in parallel under the Java Fork
     * Join framework. You may assume that the length of the input array is
     * evenly divisible by 2.
     *
     * @param input Input array
     * @return The sum of the reciprocals of the array input
     */
    protected static double parArraySum(final double[] input) {
        assert input.length % 2 == 0;
        //long startTime = System.nanoTime();
        ForkJoinPool pool = new ForkJoinPool(2);
        //System.out.println("length: " + input.length);
        ReciprocalArraySumTask task = new ReciprocalArraySumTask(0, input.length, input, 2, input.length/8);
        pool.invoke(task);
//        long timeInNanos = System.nanoTime() - startTime;
//        System.out.println("parArraySum " + timeInNanos/1000000 + " "+ task.getValue() + " expected 104672.524998 | 10459940.214862");
        return task.getValue();
    }

    /**
     * TODO: Extend the work you did to implement parArraySum to use a set
     * number of tasks to compute the reciprocal array sum. You may find the
     * above utilities getChunkStartInclusive and getChunkEndExclusive helpful
     * in computing the range of element indices that belong to each chunk.
     *
     * @param input Input array
     * @param numTasks The number of tasks to create
     * @return The sum of the reciprocals of the array input
     */
    protected static double parManyTaskArraySum(final double[] input,
            final int numTasks) {
        ForkJoinPool pool = new ForkJoinPool(numTasks);
        assert input.length % 2 == 0;
        // long startTime = System.nanoTime();
        // System.out.println("length: " + input.length);
        ReciprocalArraySumTask task = new ReciprocalArraySumTask(0, input.length, input, numTasks, 5_000_000);
        pool.invoke(task);
        // long timeInNanos = System.nanoTime() - startTime;
        // System.out.println("parMayTaskArraySum " + timeInNanos + " "+ task.getValue());

        return task.getValue();
    }

}
