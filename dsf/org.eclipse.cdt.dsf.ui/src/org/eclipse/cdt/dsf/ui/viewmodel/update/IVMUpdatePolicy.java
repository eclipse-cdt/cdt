/*******************************************************************************
 * Copyright (c) 2007, 2008 Wind River Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Wind River Systems - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.dsf.ui.viewmodel.update;


/**
 * Interface for an update policy.  The main function of an update policy is 
 * to create an element tester for each given event.  The element tester
 * is then used to update the viewer cache. 
 */
public interface IVMUpdatePolicy {

    /**
     * Flag indicating that a given entry in the cache should be cleared.
     */
    public static int FLUSH = 0x1;
    
    /**
     * Flag indicating that a given entry in the cache should be cleared
     * and saved for purpose of change tracking.
     */
    public static int ARCHIVE = FLUSH | 0x2; // Flush is required when archiving.
    
    /**
     * Flag indicating that the a given cache entry should be marked as dirty.
     * A dirty cache entry is one that is known not to be consistent with 
     * target data.
     */
    public static int DIRTY = 0x4;

    /**
     * Returns unique ID of this update policy.
     */
    public String getID();
    
    /**
     * Returns the user-presentable name of this update policy.
     */
    public String getName();

    /**
     * Creates an element tester for the given event.
     */
    public IElementUpdateTester getElementUpdateTester(Object event);
}
