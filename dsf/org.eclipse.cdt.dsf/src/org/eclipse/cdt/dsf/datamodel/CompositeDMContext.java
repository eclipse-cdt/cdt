/*******************************************************************************
 * Copyright (c) 2007, 2009 Wind River Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Wind River Systems - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.dsf.datamodel;

import java.util.Arrays;

/**
 * Generic DM context used to combine several DM Contexts.  This object allows
 * clients and other services to combine several contexts into one in order to
 * pass them as an argument to a method which takes a generic context as an 
 * argument. 
 * 
 * @since 1.0
 */
public class CompositeDMContext implements IDMContext {
    
    public static String INVALID_SESSION_ID = ""; //$NON-NLS-1$
    
    /**
     * The list of parent contexts that this composite context is made up of.
     */
    private final IDMContext[] fParents;
    
    /** 
     * Main constructor provides all data needed to implement the <code>IDMContext</code>
     * interface.
     * @param parents Array of parent contexts that this composite context is
     * made up of.  It can be an empty array, but it cannot be null.
     */
    public CompositeDMContext(IDMContext[] parents) {
        fParents = parents;
    }

    /**
     * Returns the session ID of the first element in the array of parents of this 
     * context.  May return an empty string if the parents array has no elements.
     * <p>
     * Note: The session ID is primarily used by UI components to get access to the 
     * correct session and executor for the given context.  The composite context is
     * intended to be created by clients which already know the session ID so 
     * the fact that this method may not return a reliable result is acceptable.
     * </p>
     */
    @Override
    public String getSessionId() {
        IDMContext[] parents = getParents(); 
        if (parents.length > 0) {
            return parents[0].getSessionId();
        } else {
            return INVALID_SESSION_ID;
        }
    }
    
    /**
     * Returns the list of parents that this composite context is based on.  Subclasses
     * may override this method to calculate their own set of parents.
     */
    @Override
    public IDMContext[] getParents() {
        return fParents;
    }
        
    /**
     * Returns the given adapter of the last DMVMContext element found in the tree 
     * path of this composite context.  Will return null if no DMVMContext is found
     * in path.
     * @see #getSessionId()
     */
    @SuppressWarnings("unchecked")
    @Override
    public Object getAdapter(Class adapterType) {
        IDMContext[] parents = getParents(); 
        if (parents.length > 0) {
            return parents[0].getAdapter(adapterType);
        } else {
            return null;
        }
    }

    @Override
    public boolean equals(Object obj) {
        return obj instanceof CompositeDMContext && Arrays.equals(((CompositeDMContext)obj).getParents(), getParents());
    }

    @Override
    public int hashCode() {
        return Arrays.hashCode(getParents());
    }
}
