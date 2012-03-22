/*******************************************************************************
 * Copyright (c) 2009 Ericsson and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Ericsson - Initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.debug.internal.ui.actions.breakpoints;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.cdt.debug.core.CDIDebugModel;
import org.eclipse.cdt.debug.core.model.ICBreakpointType;
import org.eclipse.cdt.debug.core.model.ICFunctionBreakpoint;
import org.eclipse.cdt.debug.core.model.ICLineBreakpoint;
import org.eclipse.cdt.debug.core.model.ICWatchpoint;
import org.eclipse.cdt.debug.ui.breakpoints.AbstractToggleBreakpointAdapter;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.ui.IWorkbenchPart;

/**
 * Toggles a tracepoint in a C/C++ editor.
 */
public class ToggleTracepointAdapter extends AbstractToggleBreakpointAdapter {

	@Override
	protected ICLineBreakpoint findLineBreakpoint( String sourceHandle, IResource resource, int lineNumber ) throws CoreException {
		return CDIDebugModel.lineBreakpointExists( sourceHandle, resource, lineNumber );
	}

    @Override
    protected void createLineBreakpoint(boolean interactive, IWorkbenchPart part, String sourceHandle, 
        IResource resource, int lineNumber) throws CoreException 
    {
        if (interactive) {
            ICLineBreakpoint lineBp = CDIDebugModel.createBlankLineTracepoint();
            Map<String, Object> attributes = new HashMap<String, Object>();
            CDIDebugModel.setLineBreakpointAttributes(
                attributes, sourceHandle, getBreakpointType(), lineNumber, true, 0, "" ); //$NON-NLS-1$
            openBreakpointPropertiesDialog(lineBp, part, resource, attributes);
        } else {
            CDIDebugModel.createLineTracepoint( sourceHandle, resource, getBreakpointType(), lineNumber, true, 0, "", true );//$NON-NLS-1$
        }
    }

	@Override
	protected ICFunctionBreakpoint findFunctionBreakpoint( String sourceHandle, IResource resource, String functionName ) throws CoreException {
		return CDIDebugModel.functionBreakpointExists( sourceHandle, resource, functionName );
	}

    @Override
    protected void createFunctionBreakpoint(boolean interactive, IWorkbenchPart part, String sourceHandle, 
        IResource resource, String functionName, int charStart, int charEnd, int lineNumber ) throws CoreException 
    {
        if (interactive) {
            ICFunctionBreakpoint bp = CDIDebugModel.createBlankFunctionTracepoint();
            Map<String, Object> attributes = new HashMap<String, Object>();
            CDIDebugModel.setFunctionBreakpointAttributes( attributes, sourceHandle, getBreakpointType(), functionName, 
                charStart, charEnd, lineNumber, true, 0, "" ); //$NON-NLS-1$
            openBreakpointPropertiesDialog(bp, part, resource, attributes);
        } else {        
            CDIDebugModel.createFunctionTracepoint(sourceHandle, resource, getBreakpointType(), functionName, charStart,
                charEnd, lineNumber, true, 0, "", true); //$NON-NLS-1$
        }            
    }

	@Override
	protected ICWatchpoint findWatchpoint( String sourceHandle, IResource resource, String expression ) throws CoreException {
		return null;
	}

	@Override
	public boolean canToggleWatchpoints(IWorkbenchPart part, ISelection selection) {
	    return false;
	}
	
    protected void createWatchpoint( boolean interactive, IWorkbenchPart part, String sourceHandle, IResource resource, 
        int charStart, int charEnd, int lineNumber, String expression) throws CoreException 
    {
    }
    
	protected int getBreakpointType() {
		return ICBreakpointType.REGULAR;
	}
}
