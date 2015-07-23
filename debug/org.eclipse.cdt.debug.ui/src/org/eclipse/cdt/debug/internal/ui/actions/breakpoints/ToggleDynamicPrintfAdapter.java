/*******************************************************************************
 * Copyright (c) 2014, 2015 Ericsson and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Marc Khouzam (Ericsson) - Initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.debug.internal.ui.actions.breakpoints;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;

import org.eclipse.cdt.debug.core.CDIDebugModel;
import org.eclipse.cdt.debug.core.model.ICBreakpointType;
import org.eclipse.cdt.debug.core.model.ICDynamicPrintf;
import org.eclipse.cdt.debug.core.model.ICFunctionBreakpoint;
import org.eclipse.cdt.debug.core.model.ICLineBreakpoint;
import org.eclipse.cdt.debug.core.model.ICWatchpoint;
import org.eclipse.cdt.debug.ui.breakpoints.AbstractToggleBreakpointAdapter;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.osgi.util.NLS;
import org.eclipse.ui.IWorkbenchPart;

/**
 * Toggles a dynamic printf in a C/C++ editor.
 * 
 * @since 7.5
 */
public class ToggleDynamicPrintfAdapter extends AbstractToggleBreakpointAdapter {

	@Override
	protected ICLineBreakpoint findLineBreakpoint( String sourceHandle, IResource resource, int lineNumber ) throws CoreException {
		return CDIDebugModel.lineBreakpointExists( sourceHandle, resource, lineNumber );
	}

    @Override
    protected void createLineBreakpoint(boolean interactive, IWorkbenchPart part, String sourceHandle, 
        IResource resource, int lineNumber) throws CoreException 
    {
        if (interactive) {
        	ICDynamicPrintf dprintf = (ICDynamicPrintf)CDIDebugModel.createBlankLineDynamicPrintf();
            Map<String, Object> attributes = new HashMap<String, Object>();
            CDIDebugModel.setLineBreakpointAttributes(
                attributes, sourceHandle, getBreakpointType(), lineNumber, true, 0, "" ); //$NON-NLS-1$

            // Although the user will be given the opportunity to provide the printf string 
            // in the properties dialog, we pre-fill it with the default string to be nice
            attributes.put(ICDynamicPrintf.PRINTF_STRING, 
            		       NLS.bind(Messages.Default_LineDynamicPrintf_String, escapeBackslashes(sourceHandle), lineNumber));

            openBreakpointPropertiesDialog(dprintf, part, resource, attributes);
        } else {
        	// We provide a default printf string to make the dynamic printf useful automatically
        	String printfStr = NLS.bind(Messages.Default_LineDynamicPrintf_String, escapeBackslashes(sourceHandle), lineNumber);
        	
        	CDIDebugModel.createLineDynamicPrintf( 
        			sourceHandle, resource, getBreakpointType(), lineNumber, true, 0, "", printfStr, true );//$NON-NLS-1$
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
        	ICDynamicPrintf dprintf = (ICDynamicPrintf)CDIDebugModel.createBlankFunctionDynamicPrintf();
            Map<String, Object> attributes = new HashMap<String, Object>();
            CDIDebugModel.setFunctionBreakpointAttributes( attributes, sourceHandle, getBreakpointType(), functionName, 
                charStart, charEnd, lineNumber, true, 0, "" ); //$NON-NLS-1$

            // Although the user will be given the opportunity to provide the printf string 
            // in the properties dialog, we pre-fill it with the default string to be nice
        	dprintf.setPrintfString(NLS.bind(Messages.Default_FunctionDynamicPrintf_String, escapeBackslashes(sourceHandle), functionName));

        	openBreakpointPropertiesDialog(dprintf, part, resource, attributes);
        } else {
        	// We provide a default printf string to make the dynamic printf useful automatically
        	String printfStr = NLS.bind(Messages.Default_FunctionDynamicPrintf_String, escapeBackslashes(sourceHandle), functionName);
        	
        	CDIDebugModel.createFunctionDynamicPrintf(sourceHandle, resource, getBreakpointType(), functionName, charStart,
                charEnd, lineNumber, true, 0, "", printfStr, true); //$NON-NLS-1$
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
	
    @Override
	protected void createWatchpoint( boolean interactive, IWorkbenchPart part, String sourceHandle, IResource resource, 
        int charStart, int charEnd, int lineNumber, String expression, String memorySpace, String range) throws CoreException 
    {
    }

    @Override
    public boolean canCreateEventBreakpointsInteractive(IWorkbenchPart part, ISelection selection) {
        return false;
    }
    
    @Override
    protected void createEventBreakpoint(boolean interactive, IWorkbenchPart part, IResource resource, String type,
        String arg) throws CoreException {
        
    }
 
	protected int getBreakpointType() {
		return ICBreakpointType.REGULAR;
	}

	/**
	 * Escape embedded backslashes for inclusion in C string.
	 */
	private static String escapeBackslashes(String str) {
		return str.replaceAll(Pattern.quote("\\"), "\\\\\\\\");  //$NON-NLS-1$//$NON-NLS-2$
	}

}
