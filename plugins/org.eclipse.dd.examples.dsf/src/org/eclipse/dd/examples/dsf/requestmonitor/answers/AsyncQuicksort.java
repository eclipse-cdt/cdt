/*******************************************************************************
 * Copyright (c) 2008 Wind River Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Wind River Systems - initial API and implementation
 *******************************************************************************/
package org.eclipse.dd.examples.dsf.requestmonitor.answers;

import java.util.Arrays;
import java.util.concurrent.Executor;

import org.eclipse.dd.dsf.concurrent.CountingRequestMonitor;
import org.eclipse.dd.dsf.concurrent.DataRequestMonitor;
import org.eclipse.dd.dsf.concurrent.ImmediateExecutor;
import org.eclipse.dd.dsf.concurrent.RequestMonitor;

/**
 * Example of using a CountingRequestMonitor to wait for multiple 
 * asynchronous calls to complete.  
 */
public class AsyncQuicksort {

    static Executor fgExecutor = ImmediateExecutor.getInstance();

    public static void main(String[] args) {
        final int[] array = {5, 7, 8, 3, 2, 1, 9, 5, 4};

        System.out.println("To sort: " + Arrays.toString(array));
        asyncQuicksort(
            array, 0, array.length - 1, 
            new RequestMonitor(fgExecutor, null) {
                @Override
                protected void handleCompleted() {
                    System.out.println("Sorted: " + Arrays.toString(array));
                }
            });
    }

    static void asyncQuicksort(final int[] array, final int left, 
        final int right, final RequestMonitor rm) 
    {
        if (right > left) {
            int pivot = left;
            asyncPartition(
                array, left, right, pivot, 
                new DataRequestMonitor<Integer>(fgExecutor, rm) {
                    @Override
                    protected void handleCompleted() {
                        int newPivot = getData();
                        printArray(array, left, right, newPivot);
                       
                        CountingRequestMonitor countingRm = new CountingRequestMonitor(fgExecutor, rm);
                        asyncQuicksort(array, left, newPivot - 1, countingRm);
                        asyncQuicksort(array, newPivot + 1, right, countingRm);
                        countingRm.setDoneCount(2);
                    }
            });
        } else {
            rm.done();
        }
    }

    static void asyncPartition(int[] array, int left, int right, int pivot, DataRequestMonitor<Integer> rm)
    {
        int pivotValue = array[pivot];
        array[pivot] = array[right]; 
        array[right] = pivotValue; 
        int store = left;
        for (int i  = left; i < right; i++) {
            if (array[i] <= pivotValue) {
                int tmp = array[store];
                array[store] = array[i];
                array[i] = tmp;
                store++;
            }
        }
        array[right] = array[store];
        array[store] = pivotValue;

        // Java 5 automatically converts the int type of the store variable
        // to an Integer object.
        rm.setData(store);
        rm.done();
    }

    static void printArray(int[] array, int left, int right, int pivot) {
        StringBuffer buffer = new StringBuffer();
        for (int i = 0; i < array.length; i++ ) {
            if (i == left) {
                buffer.append('>');
            } else if (i == pivot) {
                buffer.append('-');
            } else {
                buffer.append(' ');
            }
            buffer.append(array[i]);

            if (i == right) {
                buffer.append('<');
            } else if (i == pivot) {
                buffer.append('-');
            } else {
                buffer.append(' ');
            }
        }

        System.out.println(buffer);
    }
}
