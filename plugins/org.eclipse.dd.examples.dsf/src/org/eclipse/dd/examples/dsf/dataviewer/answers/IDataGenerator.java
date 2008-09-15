/*******************************************************************************
 * Copyright (c) 2006, 2008 Wind River Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Wind River Systems - initial API and implementation
 *******************************************************************************/
package org.eclipse.dd.examples.dsf.dataviewer.answers;

import java.util.Set;

import org.eclipse.dd.dsf.concurrent.DataRequestMonitor;
import org.eclipse.dd.dsf.concurrent.RequestMonitor;
import org.eclipse.dd.dsf.concurrent.ThreadSafe;

/**
 * Data generator is simple source of data used to populate the example table 
 * view.  It contains two asynchronous methods for retrieving the data 
 * parameters: the count and the value for a given index.  It also allows the 
 * view to receive events indicating when the data supplied by the generator 
 * is changed. 
 */
@ThreadSafe
public interface IDataGenerator {

    // Constants which control the data generator behavior.
    // Changing the count range can stress the scalability of the system, while
    // changing of the process delay and random change interval can stress 
    // its performance.
    final static int MIN_COUNT = 100;
    final static int MAX_COUNT = 200;
    final static int PROCESSING_DELAY = 10;
    final static int RANDOM_CHANGE_INTERVAL = 10000;
    final static int RANDOM_COUNT_CHANGE_INTERVALS = 3;
    final static int RANDOM_CHANGE_SET_PERCENTAGE = 10;

    
    // Listener interface that the view needs to implement to react
    // to the changes in data.
    public interface Listener {
        void countChanged();
        void valuesChanged(Set<Integer> indexes);
    }

    // Data access methods.
    void getCount(DataRequestMonitor<Integer> rm);
    void getValue(int index, DataRequestMonitor<String> rm); 
    
    // Method used to shutdown the data generator including any threads that 
    // it may use.  
    void shutdown(RequestMonitor rm);

    // Methods for registering change listeners.
    void addListener(Listener listener);
    void removeListener(Listener listener);
}
