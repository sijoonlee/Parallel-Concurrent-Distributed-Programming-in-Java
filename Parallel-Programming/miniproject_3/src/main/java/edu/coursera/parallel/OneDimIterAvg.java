package edu.coursera.parallel;

import static edu.rice.pcdp.PCDP.*;

public class OneDimIterAvg {
    private static int n = 8;
    private static double[] myNew = {0,0,0,0,0,0,0,0,0,1};
    private static double[] myVal = {0,0,0,0,0,0,0,0,0,1};

    public static void init(){
        myNew = new double[]{0, 0, 0, 0, 0, 0, 0, 0, 0, 1};
        myVal = new double[]{0, 0, 0, 0, 0, 0, 0, 0, 0, 1};
    }

    public static void printMyVal(){
        for(int i=0; i<myVal.length; i++)
            System.out.println(myVal[i]);
    }

    public static void runSequential(int iterations){
        init();
        for(int iter=0; iter<iterations ; iter++){
            for(int j=1; j<=n; j++){
                myNew[j] = (myVal[j-1] + myVal[j+1]) / 2.0;
            }
            double[] temp = myNew;
            myNew = myVal;
            myVal = temp;
        }
        printMyVal();
    }

    public static void runForall(int iterations){
        init();
        for(int iter=0; iter<iterations ; iter++){
            forall(1, n, (j) -> {
                myNew[j] = (myVal[j-1] + myVal[j+1]) / 2.0;
            });
            double[] temp = myNew;
            myNew = myVal;
            myVal = temp;
        }
        printMyVal();
    }

    public static void runForallGrouped(int iterations, int tasks){
        init();
        for(int iter=0; iter<iterations ; iter++){
            forall(0, tasks-1, (i) -> {
                for(int j= i*(n/tasks)+1; j <= (i+1)*(n/tasks);j++)
                    myNew[j] = (myVal[j-1] + myVal[j+1]) / 2.0;
            });
            double[] temp = myNew;
            myNew = myVal;
            myVal = temp;
        }
        printMyVal();
    }

    public static void main(String[] args) {
        runSequential(100);
        runForall(100);
        runForallGrouped(100, 2);
    }


/* Not supported method
    public void runForallGroupedBarrier(int iterations, int tasks){
        forallPhased(0, tasks-1, (i)-> {
            double[] myVal = this.myVal;
            double[] myNew = this.myNew;
            for(int iter=0; iter<iterations ; iter++){
                for(int j= i*(n/tasks)+1; j <= (i+1)*(n/tasks);j++)
                    myNew[j] = (myVal[j-1] + myVal[j+1]) / 2.0;

                next(); // barrier

                double[] temp = myNew;
                myNew = myVal;
                myVal = temp;
            }
        })
    }
*/
}
