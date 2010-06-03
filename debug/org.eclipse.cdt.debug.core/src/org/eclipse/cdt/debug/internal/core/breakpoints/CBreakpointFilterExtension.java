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
package org.eclipse.cdt.debug.internal.core.breakpoints;

import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.eclipse.cdt.debug.core.model.ICBreakpoint;
import org.eclipse.cdt.debug.core.model.ICBreakpointFilterExtension;
import org.eclipse.cdt.debug.core.model.ICDebugTarget;
import org.eclipse.cdt.debug.core.model.ICThread;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.debug.core.model.IDebugTarget;

/**
 * 
 */
public class CBreakpointFilterExtension implements ICBreakpointFilterExtension {

    public void initialize(ICBreakpoint breakpoint) {
    }
    
    private Map fFilteredThreadsByTarget = new HashMap( 10 );

    
    /* (non-Javadoc)
     * @see org.eclipse.cdt.debug.core.model.ICBreakpoint#getTargetFilters()
     */
    public ICDebugTarget[] getTargetFilters() throws CoreException {
        Set set = fFilteredThreadsByTarget.keySet();
        return (ICDebugTarget[])set.toArray( new ICDebugTarget[set.size()] );
    }

    /* (non-Javadoc)
     * @see org.eclipse.cdt.debug.core.model.ICBreakpoint#getThreadFilters(org.eclipse.cdt.debug.core.model.ICDebugTarget)
     */
    public ICThread[] getThreadFilters( ICDebugTarget target ) throws CoreException {
        Set set = (Set)fFilteredThreadsByTarget.get( target );
        return ( set != null ) ? (ICThread[])set.toArray( new ICThread[set.size()] ) : null;
    }

    /* (non-Javadoc)
     * @see org.eclipse.cdt.debug.core.model.ICBreakpoint#removeTargetFilter(org.eclipse.cdt.debug.core.model.ICDebugTarget)
     */
    public void removeTargetFilter( ICDebugTarget target ) throws CoreException {
        if ( fFilteredThreadsByTarget.containsKey( target ) ) {
            fFilteredThreadsByTarget.remove( target );
        }
    }

    /* (non-Javadoc)
     * @see org.eclipse.cdt.debug.core.model.ICBreakpoint#removeThreadFilters(org.eclipse.cdt.debug.core.model.ICThread[])
     */
    public void removeThreadFilters( ICThread[] threads ) throws CoreException {
        if ( threads != null && threads.length > 0 ) {
            IDebugTarget target = threads[0].getDebugTarget();
            if ( fFilteredThreadsByTarget.containsKey( target ) ) {
                Set set = (Set)fFilteredThreadsByTarget.get( target );
                if ( set != null ) {
                    set.removeAll( Arrays.asList( threads ) );
                    if ( set.isEmpty() ) {
                        fFilteredThreadsByTarget.remove( target );
                    }
                }
            }
        }
    }

    /* (non-Javadoc)
     * @see org.eclipse.cdt.debug.core.model.ICBreakpoint#setTargetFilter(org.eclipse.cdt.debug.core.model.ICDebugTarget)
     */
    public void setTargetFilter( ICDebugTarget target ) throws CoreException {
        fFilteredThreadsByTarget.put( target, null );
    }

    /* (non-Javadoc)
     * @see org.eclipse.cdt.debug.core.model.ICBreakpoint#setThreadFilters(org.eclipse.cdt.debug.core.model.ICThread[])
     */
    public void setThreadFilters( ICThread[] threads ) throws CoreException {
        if ( threads != null && threads.length > 0 ) {
            fFilteredThreadsByTarget.put( threads[0].getDebugTarget(), new HashSet( Arrays.asList( threads ) ) );
        }
    }

}
