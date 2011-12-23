/*******************************************************************************
 * Copyright (c) 2007, 2011 Wind River Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Wind River Systems - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.dsf.gdb.breakpoints;

import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.eclipse.cdt.debug.core.model.ICBreakpoint;
import org.eclipse.cdt.dsf.datamodel.DMContexts;
import org.eclipse.cdt.dsf.debug.service.IDsfBreakpointExtension;
import org.eclipse.cdt.dsf.debug.service.IRunControl.IContainerDMContext;
import org.eclipse.cdt.dsf.debug.service.IRunControl.IExecutionDMContext;
import org.eclipse.core.runtime.CoreException;

/**
 * 
 */
public class CBreakpointGdbThreadsFilterExtension implements IDsfBreakpointExtension {

    private Map<IContainerDMContext,Set<IExecutionDMContext>> fFilteredThreadsByTarget = 
        new HashMap<IContainerDMContext,Set<IExecutionDMContext>>(1);

    /* (non-Javadoc)
     * @see org.eclipse.cdt.debug.core.model.ICBreakpointExtension#initialize(org.eclipse.cdt.debug.core.model.ICBreakpoint)
     */
	@Override
	public void initialize(ICBreakpoint breakpoint) {
    }
    
    /* (non-Javadoc)
     * @see org.eclipse.cdt.debug.core.model.ICBreakpoint#getTargetFilters()
     */
	@Override
    public IContainerDMContext[] getTargetFilters() throws CoreException {
        Set<IContainerDMContext> set = fFilteredThreadsByTarget.keySet();
        return set.toArray( new IContainerDMContext[set.size()] );
    }

    /* (non-Javadoc)
     * @see org.eclipse.cdt.debug.core.model.ICBreakpoint#getThreadFilters(org.eclipse.cdt.debug.core.model.ICDebugTarget)
     */
	@Override
    public IExecutionDMContext[] getThreadFilters( IContainerDMContext target ) throws CoreException {
        Set<IExecutionDMContext> set = fFilteredThreadsByTarget.get( target );
        return ( set != null ) ? set.toArray( new IExecutionDMContext[set.size()] ) : null;
    }

    /* (non-Javadoc)
     * @see org.eclipse.cdt.debug.core.model.ICBreakpoint#removeTargetFilter(org.eclipse.cdt.debug.core.model.ICDebugTarget)
     */
	@Override
    public void removeTargetFilter( IContainerDMContext target ) throws CoreException {
        if ( fFilteredThreadsByTarget.containsKey( target ) ) {
            fFilteredThreadsByTarget.remove( target );
        }
    }

    /* (non-Javadoc)
     * @see org.eclipse.cdt.debug.core.model.ICBreakpoint#removeThreadFilters(org.eclipse.cdt.debug.core.model.ICThread[])
     */
	@Override
    public void removeThreadFilters( IExecutionDMContext[] threads ) throws CoreException {
        if ( threads != null && threads.length > 0 ) {
            IContainerDMContext target = DMContexts.getAncestorOfType(threads[0], IContainerDMContext.class);
            if ( fFilteredThreadsByTarget.containsKey( target ) ) {
                Set<IExecutionDMContext> set = fFilteredThreadsByTarget.get( target );
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
	@Override
    public void setTargetFilter( IContainerDMContext target ) throws CoreException {
        fFilteredThreadsByTarget.put( target, null );
    }

    /* (non-Javadoc)
     * @see org.eclipse.cdt.debug.core.model.ICBreakpoint#setThreadFilters(org.eclipse.cdt.debug.core.model.ICThread[])
     */
	@Override
    public void setThreadFilters( IExecutionDMContext[] threads ) throws CoreException {
        if ( threads != null && threads.length > 0 ) {
            IContainerDMContext target = DMContexts.getAncestorOfType(threads[0], IContainerDMContext.class);
            fFilteredThreadsByTarget.put( target, new HashSet<IExecutionDMContext>( Arrays.asList( threads ) ) );
        }
    }

}
