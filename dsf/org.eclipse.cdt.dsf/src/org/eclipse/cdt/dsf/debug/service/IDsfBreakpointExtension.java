/*******************************************************************************
 * Copyright (c) 2007, 2011 Wind River Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Wind River Systems - initial API and implementation
 *     Marc Khouzam (Ericsson) - Adding javadoc (Bug 355833)
 *******************************************************************************/
package org.eclipse.cdt.dsf.debug.service;

import org.eclipse.cdt.debug.core.model.ICBreakpoint;
import org.eclipse.cdt.debug.core.model.ICBreakpointExtension;
import org.eclipse.cdt.dsf.debug.service.IRunControl.IContainerDMContext;
import org.eclipse.cdt.dsf.debug.service.IRunControl.IExecutionDMContext;
import org.eclipse.core.runtime.CoreException;

/**
 * An extension to {@link ICBreakpoint} with model-specific breakpoint 
 * attributes. Different debug models can use the standard C breakpoints that 
 * extend the basic <code>ICBreakpoint</code>.  The can use this extension 
 * mechanism to edit and store model-specific data in the original breakpoint 
 * object.
 *   
 * A breakpoint extension is defined by an extension of kind 
 * <code>"org.eclipse.cdt.debug.core.BreakpointExtension"</code></li>.
 * The <code>ICBreakpoint</code> implementation instantiates breakpoint 
 * extensions registered for its specific marker type when a client requests
 * extensions for a given debug model type.  Thus the extension classes and 
 * plugins that declare them are not loaded unless requested by a client.   
 * 
 * @see ICBreakpoint#getExtension(String, Class)
 * @since 1.0
 */
public interface IDsfBreakpointExtension extends ICBreakpointExtension {
   
    /**
     * Add the given target to the list of this breakpoint's targets.    
     * Target filters are not persisted across workbench invocations.
     * 
     * @param target the container to add to the list of this breakpoint's targets.
     * @throws CoreException if unable to set the target filter
     */
	public void setTargetFilter(IContainerDMContext target) throws CoreException;
	
    /**
     * Removes the given target from the breakpoint's target list.
     * The breakpoint has no effect in the given target. 
     * 
     * @param target the container filter to be removed
     * @exception CoreException if unable to remove the target filter
     */
    public void removeTargetFilter(IContainerDMContext target) throws CoreException;
    
    /**
     * Returns all target filters set on this breakpoint.
     * 
     * @return the targets that this breakpoint is restricted to
     * @exception CoreException if unable to determine this breakpoint's target filters
     */
    public IContainerDMContext[] getTargetFilters() throws CoreException;

    /**
     * Restricts this breakpoint to suspend only in the given threads 
     * when encountered in the given threads' target. 
     * All threads must be from the same target.
     * Thread filters are not persisted across workbench invocations.
     * 
     * @param threads the thread filters to be set
     * @exception CoreException if unable to set the thread filters
     */
    public void setThreadFilters(IExecutionDMContext[] threads) throws CoreException;

    /**
     * Removes this breakpoint's thread filters in the given target, if any. 
     * Has no effect if this breakpoint does not have filters in the given target.
     * All threads must be from the same target.
     * 
     * @param threads the thread filters to be removed
     * @exception CoreException if unable to remove the thread filter
     */
    public void removeThreadFilters(IExecutionDMContext[] threads) throws CoreException;
    
    /**
     * Returns the threads in the given target in which this breakpoint
     * is enabled or <code>null</code> if this breakpoint is enabled in
     * all threads in the given target.
     * 
     * @return the threads in the given target that this breakpoint is enabled for
     * @exception CoreException if unable to determine this breakpoint's thread filters
     */
    public IExecutionDMContext[] getThreadFilters(IContainerDMContext target) throws CoreException;
}
