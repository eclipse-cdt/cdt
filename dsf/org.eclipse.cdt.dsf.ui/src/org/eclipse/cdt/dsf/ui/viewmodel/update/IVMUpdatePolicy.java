/*******************************************************************************
 * Copyright (c) 2007, 2010 Wind River Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Wind River Systems - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.dsf.ui.viewmodel.update;

import java.util.Map;



/**
 * Interface for an update policy.  The main function of an update policy is 
 * to create an element tester for each given event.  The element tester
 * is then used to update the viewer cache. 
 * 
 * @since 1.0
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
     * Flag indicating that the cache should flush only selected properties of 
     * an element.  The list of properties to clear can be accessed using
     * {@link IElementUpdateTesterExtension#getPropertiesToFlush(Object, org.eclipse.jface.viewers.TreePath, boolean)}.
     * 
     * @since 2.1
     */
    public static int FLUSH_PARTIAL_PROPERTIES = 0x8;

    /**
     * Flag indicating that the cache should flush all properties of 
     * an element.
     *  
     * @since 2.2
     */
    public static int FLUSH_ALL_PROPERTIES = 0x10;

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
    
    /**
     * Returns the array of elements that should be used to initially populate 
     * the cache, or <code>null</code> if this update policy does not need to
     * pre-populate the cache.  These elements will be shown as children 
     * of the root element in the view. 
     * <p/>
     * This method allows an update policy to prevent the UI from reading the
     * model when the UI first appears and the cache has not been populated yet.
     * 
     * @param rootElement The rootElement for which the cache is being
     * pre-populated.
     * 
     * @since 2.0
     */
    public Object[] getInitialRootElementChildren(Object rootElement);

    /**
     * Returns the properties that should be used to initially populate 
     * the cache, or <code>null</code> if the cache should not be pre-populated
     * for this update policy.  These properties may be used by the 
     * view model to generate the label for the root element.
     * <p/>
     * This method allows an update policy to prevent the UI from reading the
     * model when the UI first appears and the cache has not been populated 
     * yet.  Note however that if the root element is the view input it is 
     * not shown in the view.
     * 
     * @param rootElement The rootElement for which the cache is being
     * pre-populated.
     * 
     * @since 2.0
     */
    public Map<String, Object> getInitialRootElementProperties(Object rootElement);
}
