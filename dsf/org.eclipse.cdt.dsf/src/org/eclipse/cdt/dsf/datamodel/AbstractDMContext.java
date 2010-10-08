/*******************************************************************************
 * Copyright (c) 2006, 2010 Wind River Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Wind River Systems - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.dsf.datamodel;

import org.eclipse.cdt.dsf.concurrent.Immutable;
import org.eclipse.cdt.dsf.service.DsfSession;
import org.eclipse.cdt.dsf.service.IDsfService;
import org.eclipse.core.runtime.PlatformObject;

/**
 * Base implementation of the IDMContext interface.  There are two pieces of 
 * functionality here: <br>
 * 1) The {@link #getAdapter(Class)} implementation which retrieves model
 * adapters registered with the session. <br>
 * 2) Methods to help compare DM Contexts.  <br>
 * <p>
 * Note: The {@link #equals(Object)} and {@link #hashCode()} methods are 
 * made abstract to force the deriving classes to provide a proper 
 * implementation.  Data Model Context objects are meant to be used as handles, 
 * therefore a proper equals implementation is critical. 
 * </p>
 * @param <V> Data Model data type that this context is for.
 * 
 * @since 1.0
 */
@Immutable
abstract public class AbstractDMContext extends PlatformObject 
    implements IDMContext     
{
    private final DsfSession fSession;
    private final IDMContext[] fParents;

    /** 
     * Main constructor provides all data needed to implement the <code>IDMContext</code>
     * interface.
     * @since 2.2
     */
    public AbstractDMContext(DsfSession session, IDMContext[] parents) {
        fSession = session;
        fParents = parents;
        for (IDMContext parent : parents) {
        	assert(parent != null);
        }
    }

    /** Convenience constructor */
    public AbstractDMContext(IDsfService service, IDMContext[] parents) {
        this(service.getSession(), parents);
    }
    
    /** Backward compatiblity constructor */
    public AbstractDMContext(String fSessionId, IDMContext[] parents) {
    	this(DsfSession.getSession(fSessionId), parents);
    }

    /** 
     * Should be used by the deriving class to compare the basic context object
     * information.
     * @param other the other service to compare to
     * @return true if contexts are equal
     */
    protected boolean baseEquals(Object other) {
        if (other == null) return false;
        if ( !(other.getClass().equals(getClass()))) return false;
        IDMContext otherCtx = (IDMContext)other;
        return getSessionId().equals(otherCtx.getSessionId()) &&
               areParentsEqual(otherCtx.getParents());
    }

    private boolean areParentsEqual(IDMContext[] otherParents) {
        IDMContext[] parents = getParents();
        if ( !(parents.length == otherParents.length) ) return false;
        for (int i = 0; i < parents.length; i++) {
            if (!parents[i].equals(otherParents[i])) {
                return false;
            }
        }
        return true;
    }
    
    protected int baseHashCode() {
        int parentsHash = 0;
        for (Object parent : getParents()) {
            parentsHash += parent.hashCode();
        }
        return getSessionId().hashCode() + parentsHash;
    }

	/**
	 * @return a stringified representation of our parent context. If we have
	 *         more than one parent, the parents are separated by commas, and
	 *         the entire collection is wrapped with parenthesis:
	 *         "(p1,p2,...,pn)"
	 */
    protected String baseToString() {
        StringBuilder retVal = new StringBuilder(); 
        for (IDMContext parent : getParents()) {
            retVal.append(parent);
            retVal.append(',');
        }
        if (retVal.length() > 0) {
        	retVal.deleteCharAt(retVal.length() - 1);	// remove trailing comma
        }
        if (getParents().length > 1) {
        	retVal.insert(0, '(');
        	retVal.insert(retVal.length(), ')');
        }
        return retVal.toString(); 
    }
    
    public String getSessionId() { return fSession.getId(); }
    public IDMContext[] getParents() { return fParents; }
        
    /**
     * Overrides the standard platform getAdapter to provide session-specific 
     * adapters.
     * <p>
     * ModelContext is intended to be used in views, which call the 
     * contexts.getAdapter() method to retrieve model-specific content and 
     * label providers.  But since many different sessions could be active
     * at the same time, each requiring different content providers, the 
     * standard platform <code>IAdapterManager</code> is not sufficient in 
     * handling adapters for the model context object.  This is because 
     * <code>IAdapterManager</code> uses only the class of the adaptable to 
     * select the correct adapter factoru, while for model context, the 
     * session is equally important. 
     * @see org.eclipse.runtime.IAdapterManager 
     */
    @Override
    @SuppressWarnings("rawtypes")
    public Object getAdapter(Class adapterType) {
        Object retVal = fSession.getModelAdapter(adapterType);
        if (retVal == null) {
            retVal = super.getAdapter(adapterType);
        }
        return retVal;
    }

    /**
     * Deriving classes must implement proper equals and hashCode operations 
     * based on context data.
     */
    @Override
    abstract public boolean equals(Object obj);
    
    /**
     * Deriving classes must implement proper equals and hashCode operations 
     * based on context data.
     */
    @Override
    abstract public int hashCode();
}
