/*******************************************************************************
 * Copyright (c) 2006 Wind River Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Wind River Systems - initial API and implementation
 *******************************************************************************/
package org.eclipse.dd.examples.dsf.concurrent;

import java.util.Set;

import org.eclipse.dd.dsf.concurrent.DsfExecutor;
import org.eclipse.dd.dsf.concurrent.DataRequestMonitor;

public interface DataProvider {
    
    /**
     * Interface for listeners for changes in Provider's data.
     */
    public interface Listener {
        /**
         * Indicates that the count of data items has changed.
         */
        void countChanged();
        
        /**
         * Indicates that some of the data values have changed.
         * @param indexes Indexes of the changed items.
         */
        void dataChanged(Set<Integer> indexes);
    }
    
    /**
     * Returns the DSF executor that has to be used to call this data 
     * provider.
     */
    DsfExecutor getDsfExecutor();
    
    /**
     * Retrieves the current item count.
     * @param rm Request monitor, to be filled in with the Integer value.
     */
    void getItemCount(DataRequestMonitor<Integer> rm);

    /** 
     * Retrieves data value for given index.
     * @param index Index of the item to retrieve
     * @param rm Return data token, to be filled in with a String value
     */
    void getItem(int index, DataRequestMonitor<String> rm); 
    
    /**
     * Registers given listener with data provider.
     */
    void addListener(Listener listener);
    
    /**
     * Removes given listener from data provider.
     */
    void removeListener(Listener listener);
}
