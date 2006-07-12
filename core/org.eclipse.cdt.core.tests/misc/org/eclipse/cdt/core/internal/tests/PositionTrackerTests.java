/*******************************************************************************
 * Copyright (c) 2006 Wind River Systems, Inc. and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Markus Schorn - initial API and implementation
 *******************************************************************************/ 

package org.eclipse.cdt.core.internal.tests;

import java.util.Random;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import org.eclipse.cdt.internal.core.PositionTracker;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.Region;

public class PositionTrackerTests extends TestCase {
    public static Test suite() {
        return new TestSuite(PositionTrackerTests.class, "PositionTrackerTests");
    }

    public void testInitialFailures() {
        int[][] moves = {
            {46, -18, 95, -76, 98, -89, 10, -10, 85, -80, 16, 6, 5, -3, 
                22, -8, 29, -20, 86, -62, 34, -21, 63, -41, 9, 10, 18, -7},
            {0, 2,  1,-4},
            {4,-1,  0, 2, 0,-5},
            {0, 1,  2, 1, 0,-5},
            {0, 1,  2,-3, 1,-4},
            {4, 3,  3,-2, 0,-1},
            {4,-1,  3, 1, 2,-1},
            {0, 1,  2, 8, 1,-8, 0,-10},
            {4,-1,  2, 1, 4, 1, 0,-1, 0,-5},
        };
        int[] buffer = new int[100];
        for (int i = 0; i < moves.length; i++) {
            testMove(buffer, moves[i]);
        }
    }

    public void testRotations() {
        int[][] moves = { { 0, 1, 2, 1, 4, 1, 6, 1, 8, 1, 10, 1, 12, 1, 14, 1, 16, 1, 18, 1, 20, 1, 22, 1, 24, 1 }, {
                15, 1, 14, 1, 13, 1, 12, 1, 11, 1, 10, 1, 9, 1, 8, 1, 7, 1, 6, 1, 5, 1, 4, 1, 3, 1 }, {
                0, 1, 10, 1, 2, 1, 20, 1, 4, 1, 20, 1, 6, 1, 20, 1, 8, 1, 20, 1, 10, 1, 20, 1, 12, 1 }, };
        int[] buffer = new int[30];
        for (int i = 0; i < moves.length; i++) {
            assertTrue(testMove(buffer, moves[i]).depth() <= 5);
        }
    }

    public void testDepth4() {
        fullTest(5, 4);
    }

    public void testRandomDepth5() {
        randomTest(20, 5, 5, 50000);
    }

    public void testRandomDepth10() {
        randomTest(50, 10, 10, 50000);
    }

    public void testRandomDepth15() {
        randomTest(100, 15, 15, 50000);
    }

    public void testRandomDepth20() {
        randomTest(100, 15, 20, 50000);
    }

    public void testRetireDepth2() {
        randomRetireTest(100, 10, 25, 2, 1000);
    }

    public void testRetireDepth5() {
        randomRetireTest(100, 10, 10, 5, 1000);
    }

    public void testRetireDepth10() {
        randomRetireTest(100, 10, 5, 10, 1000);
    }

    public static void fullTest(int len, int depth) {
        // init buffer
        int[] buffer = new int[len];
        int[] move = new int[2 * depth];
        for (int i = 0; i < move.length; i++) {
            move[i] = -1;
        }
        while (nextMove(move, len)) {
            testMove(buffer, move);
        }
    }

    public static void randomTest(int buflen, int changelen, int depth, int count) {
        // init buffer
        Random rand = new Random();

        int[] buffer = new int[buflen];
        int[] move = new int[2 * depth];

        for (int j = 0; j < count; j++) {
            for (int i = 0; i < move.length; i += 2) {
                move[i] = rand.nextInt(buflen);
                move[i + 1] = rand.nextInt(2 * changelen) - changelen;
            }
            testMove(buffer, move);
        }
    }

    public static void randomRetireTest(int buflen, int changelen, int depth, int trackerDepth, int count) {
        // init buffer
        Random rand = new Random();

        int[] buffer = new int[buflen];
        int[] move = new int[2 * depth];

        for (int j = 0; j < count; j++) {
            for (int i = 0; i < buffer.length; i++) {
                buffer[i] = i;
            }

            PositionTracker t0 = null;
            PositionTracker previous = null;
            for (int t = 0; t < trackerDepth; t++) {
                for (int i = 0; i < move.length; i += 2) {
                    move[i] = rand.nextInt(buflen);
                    move[i + 1] = rand.nextInt(2 * changelen) - changelen;
                }
                PositionTracker tracker = new PositionTracker();
                if (previous != null) {
                    previous.retire(tracker);
                }
                doMove(buffer, move, tracker);
                if (t0 == null) {
                    t0 = tracker;
                }
                previous = tracker;
            }
            check(t0, buffer);
        }
    }

    static PositionTracker testMove(int[] buffer, int[] move) {
        try {
            return __testMove(buffer, move);
        } catch (RuntimeException e) {
            System.out.println("Error on move: ");  //$NON-NLS-1$
            for (int i = 0; i < move.length; i++) {
                System.out.print(move[i] + ", ");  //$NON-NLS-1$
            }
            System.out.println();
            throw e;
        } catch (Error e) {
            System.out.println("Error on move: ");  //$NON-NLS-1$
            for (int i = 0; i < move.length; i++) {
                System.out.print(move[i] + ", ");  //$NON-NLS-1$
            }
            System.out.println();
            throw e;
        }
    }

    static PositionTracker __testMove(int[] buffer, int[] move) {
        PositionTracker tracker = new PositionTracker();
        for (int i = 0; i < buffer.length; i++) {
            buffer[i] = i;
        }
        doMove(buffer, move, tracker);
        check(tracker, buffer);
        return tracker;
    }

    static void doMove(int[] buffer, int[] move, PositionTracker tracker) {
        for (int i = 0; i < move.length; i += 2) {
            int m1 = move[i];
            int m2 = move[i + 1];
            if (m1 == -1) {
                break;
            }
            if (m2 > 0) {
                tracker.insert(m1, m2);
                for (int j = 0; j < buffer.length; j++) {
                    if (buffer[j] >= m1) {
                        buffer[j] += m2;
                    }
                }
            } else {
                tracker.delete(m1, -m2);
                int m3 = m1 - m2;
                for (int j = 0; j < buffer.length; j++) {
                    if (buffer[j] >= m1) {
                        if (buffer[j] < m3) {
                            buffer[j] = -1;
                        } else {
                            buffer[j] += m2;
                        }
                    }
                }
            }
        }
    }

    private static void check(PositionTracker tracker, int[] buffer) {
        int lasti2 = -1;
        for (int i = 0; i < buffer.length; i++) {
            int i2 = buffer[i];
            if (i2 >= 0) {
                int i22 = tracker.currentOffset(i);
                assertEquals(i22, i2);
                assertTrue(lasti2 < i22);
                lasti2 = i22;

                assertEquals(i, tracker.historicOffset(i2));
            }
        }
    }

    private static boolean nextMove(int[] move, int bufLen) {
        for (int i = 0; i < move.length; i += 2) {
            int m1 = move[i];
            if (m1 < 0) {
                move[i] = 0;
                move[i + 1] = -bufLen;
                return true;
            }
            int m2 = ++move[i + 1];
            if (m2 <= bufLen - m1) {
                return true;
            }
            if (m1 < bufLen - 1) {
                move[i]++;
                move[i + 1] = -bufLen + m1 + 1;
                return true;
            }
            move[i] = 0;
            move[i + 1] = -bufLen;
        }
        return false;
    }
    
    public void testInsertion() {
        PositionTracker pt= new PositionTracker();
        pt.insert(1,1);
        
        checkInsert11(pt);
    }

    private void checkInsert11(PositionTracker pt) {
        // chars
        doubleCheck(pt, 0, 0);
        backwdCheck(pt, 1, 1);
        doubleCheck(pt, 1, 2);
        doubleCheck(pt, 2, 3);
        
        // ranges
        doubleRangeCheck(pt, new Region(0,2), new Region(0,3));
        backwdRangeCheck(pt, new Region(0,1), new Region(0,2));
        doubleRangeCheck(pt, new Region(0,1), new Region(0,1));
        backwdRangeCheck(pt, new Region(1,0), new Region(1,1));
        backwdRangeCheck(pt, new Region(1,0), new Region(1,0));
        doubleRangeCheck(pt, new Region(1,1), new Region(2,1));
        doubleRangeCheck(pt, new Region(1,0), new Region(2,0));
    }

    public void testDeletion() {
        PositionTracker pt= new PositionTracker();
        pt.delete(1,1);
        checkDelete11(pt);
    }

    private void checkDelete11(PositionTracker pt) {
        doubleCheck(pt, 0, 0);
        fwdCheck   (pt, 1, 1);
        doubleCheck(pt, 2, 1);
        doubleCheck(pt, 3, 2);

        // ranges
        doubleRangeCheck(pt, new Region(0,3), new Region(0,2));
        fwdRangeCheck   (pt, new Region(0,2), new Region(0,1));
        doubleRangeCheck(pt, new Region(0,1), new Region(0,1));
        fwdRangeCheck   (pt, new Region(1,1), new Region(1,0));
        fwdRangeCheck   (pt, new Region(1,0), new Region(1,0));
        doubleRangeCheck(pt, new Region(2,1), new Region(1,1));
        doubleRangeCheck(pt, new Region(2,0), new Region(1,0));
    }
    
    public void testReplace() {
        PositionTracker pt= new PositionTracker();
        pt.delete(1,1);
        pt.insert(1,1);
        doubleCheck(pt, 0, 0);
        doubleCheck(pt, 1, 1);
        doubleCheck(pt, 2, 2);
        doubleCheck(pt, 3, 3);

        pt.clear();
        pt.insert(1,1);
        pt.delete(1,1);
        doubleCheck(pt, 0, 0);
        doubleCheck(pt, 1, 1);
        doubleCheck(pt, 2, 2);
        doubleCheck(pt, 3, 3);
        
        pt.clear();
        pt.delete(0,2);
        pt.insert(0,1);
        checkDelete11(pt);

        pt.clear();
        pt.insert(1,1);
        pt.delete(1,2);
        checkDelete11(pt);

        pt.clear();
        pt.insert(1,2);
        pt.delete(1,1);
        checkInsert11(pt);

        pt.clear();
        pt.delete(1,1);
        pt.insert(1,2);
        checkInsert11(pt);
    }       

    private void doubleCheck(PositionTracker pt, int orig, int mapped) {
        fwdCheck(pt, orig, mapped);
        backwdCheck(pt, orig, mapped); 
    }

    private void fwdCheck(PositionTracker pt, int orig, int mapped) {
        assertEquals(mapped, pt.currentOffset(orig));
    }

    private void backwdCheck(PositionTracker pt, int orig, int mapped) {
        assertEquals(orig, pt.historicOffset(mapped));
    }
    
    private void doubleRangeCheck(PositionTracker pt, IRegion orig, IRegion mapped) {
        fwdRangeCheck(pt, orig, mapped);
        backwdRangeCheck(pt, orig, mapped);         
    }

    private void fwdRangeCheck(PositionTracker pt, IRegion orig, IRegion mapped) {
        assertEquals(mapped, pt.historicToActual(orig));
    }

    private void backwdRangeCheck(PositionTracker pt, IRegion orig, IRegion mapped) {
        assertEquals(orig, pt.actualToHistoric(mapped));
    }
}
