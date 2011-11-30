/*******************************************************************************
 * Copyright (c) 2006, 2011 Wind River Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Wind River Systems - initial API and implementation
 *******************************************************************************/
//#ifdef exercises
package org.eclipse.cdt.examples.dsf.dataviewer;
//#else
//#package org.eclipse.cdt.examples.dsf.dataviewer.answers;
//#endif

import java.util.Set;

import org.eclipse.cdt.dsf.concurrent.DataRequestMonitor;
import org.eclipse.cdt.dsf.concurrent.RequestMonitor;
//#ifdef answers
//#import org.eclipse.cdt.dsf.concurrent.ThreadSafe;
//#endif

/**
 * Data generator is simple source of data used to populate the example table 
 * view.  It contains two asynchronous methods for retrieving the data 
 * parameters: the count and the value for a given index.  It also allows the 
 * view to receive events indicating when the data supplied by the generator 
 * is changed. 
 */
//#ifdef exercises
// TODO Exercise 4 - Add an annotation (ThreadSafe/ConfinedToDsfExecutor) 
// indicating allowed thread access to this class/method/member
//#else
//#@ThreadSafe
//#endif
public interface IDataGenerator {

    // Constants which control the data generator behavior.
    // Changing the count range can stress the scalability of the system, while
    // changing of the process delay and random change interval can stress 
    // its performance.
    final static int MIN_COUNT = 50;
    final static int MAX_COUNT = 100;
    final static int PROCESSING_DELAY = 500;
    final static int RANDOM_CHANGE_INTERVAL = 4000;
    final static int RANDOM_COUNT_CHANGE_INTERVALS = 5;
    final static int RANDOM_CHANGE_SET_PERCENTAGE = 10;

    
    // Listener interface that the view needs to implement to react
    // to the changes in data.
    public interface Listener {
        void countChanged();
        void valuesChanged(Set<Integer> indexes);
    }

    // Data access methods.
    void getCount(DataRequestMonitor<Integer> rm);
    void getValue(int index, DataRequestMonitor<Integer> rm); 
    
    // Method used to shutdown the data generator including any threads that 
    // it may use.  
    void shutdown(RequestMonitor rm);

    // Methods for registering change listeners.
    void addListener(Listener listener);
    void removeListener(Listener listener);
}
