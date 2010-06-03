/*******************************************************************************
 * Copyright (c) 2004, 2008 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * QNX Software Systems - Initial API and implementation
 * Wind River Systems - Refactored from ICBreakpoint
 *******************************************************************************/
package org.eclipse.cdt.debug.core.model;

import org.eclipse.core.runtime.CoreException;

/**
 * Breakpoint extension to allow filtering based on CDTs extended standard debug 
 * model elements.
 */
public interface ICBreakpointFilterExtension extends ICBreakpointExtension {
    
    /**
     * Add the given target to the list of this breakpoint's targets.    
     * Target filters are not persisted across workbench invocations.
     * 
     * @param target the target to add to the list of this breakpoint's targets.
     * @throws CoreException if unable to set the target filter
     */
    public void setTargetFilter( ICDebugTarget target ) throws CoreException;

    /**
     * Removes the given target from the breakpoint's target list.
     * The breakpoint has no effect in the given target. 
     * 
     * @param target the target filter to be removed
     * @exception CoreException if unable to remove the target filter
     */
    public void removeTargetFilter( ICDebugTarget target ) throws CoreException;

    /**
     * Restricts this breakpoint to suspend only in the given threads 
     * when encounterd in the given threads' target. 
     * All threads must be from the same target.
     * Thread filters are not persisted across workbench invocations.
     * 
     * @param threads the thread filters to be set
     * @exception CoreException if unable to set the thread filters
     */
    public void setThreadFilters( ICThread[] threads ) throws CoreException;

    /**
     * Returns all target filters set on this breakpoint.
     * 
     * @return the targets that this breakpoint is resticted to
     * @exception CoreException if unable to determine this breakpoint's
     *  target filters
     */
    public ICDebugTarget[] getTargetFilters() throws CoreException; 

    /**
     * Removes this breakpoint's thread filters in the given target, if any. 
     * Has no effect if this breakpoint does not have filters in the given target.
     * All threads must be from the same target.
     * 
     * @param threads the thread filters to be removed
     * @exception CoreException if unable to remove the thread filter
     */
    public void removeThreadFilters( ICThread[] threads ) throws CoreException;

    /**
     * Returns the threads in the given target in which this breakpoint
     * is enabled or <code>null</code> if this breakpoint is enabled in
     * all threads in the given target.
     * 
     * @return the threads in the given target that this breakpoint is enabled for
     * @exception CoreException if unable to determine this breakpoint's thread
     *  filters
     */
    public ICThread[] getThreadFilters( ICDebugTarget target ) throws CoreException;

}
