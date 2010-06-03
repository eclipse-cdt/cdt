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
   
    public void setTargetFilter( IContainerDMContext target ) throws CoreException;
    public void removeTargetFilter( IContainerDMContext target ) throws CoreException;
    public IContainerDMContext[] getTargetFilters() throws CoreException;

    public void setThreadFilters( IExecutionDMContext[] threads ) throws CoreException;
    public void removeThreadFilters( IExecutionDMContext[] threads ) throws CoreException;
    public IExecutionDMContext[] getThreadFilters( IContainerDMContext target ) throws CoreException;

}
