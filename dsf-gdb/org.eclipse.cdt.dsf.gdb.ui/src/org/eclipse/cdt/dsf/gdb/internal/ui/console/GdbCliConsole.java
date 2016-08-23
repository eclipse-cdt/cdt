/*******************************************************************************
 * Copyright (c) 2015 Ericsson and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package org.eclipse.cdt.dsf.gdb.internal.ui.console;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.debug.core.ILaunch;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.ui.DebugUITools;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.console.AbstractConsole;
import org.eclipse.ui.console.IConsoleView;
import org.eclipse.ui.part.IPageBookViewPage;

/**
 * A GDB CLI console.
 * This console actually runs a GDB process in CLI mode to achieve a 
 * full-featured CLI interface.  This is only supported with GDB >= 7.12
 * and if IGDBBackend.isFullGdbConsoleSupported() returns true.
 */
public class GdbCliConsole extends AbstractConsole {
	private final ILaunch fLaunch;
	private String fLabel;
	private GdbCliConsolePage fConsolePage;
	
	public GdbCliConsole(ILaunch launch, String label) {
		super(label, null);
		fLaunch = launch;
        fLabel = label;
        
        resetName();
	}
    
	public ILaunch getLaunch() { return fLaunch; }
    
    public void resetName() {
    	String newName = computeName();
    	String name = getName();
    	if (!name.equals(newName)) {
    		PlatformUI.getWorkbench().getDisplay().asyncExec(() -> setName(newName));
    	}
    }
	
    protected String computeName() {
        String label = fLabel;

        ILaunchConfiguration config = fLaunch.getLaunchConfiguration();
        if (config != null && !DebugUITools.isPrivate(config)) {
        	String type = null;
        	try {
        		type = config.getType().getName();
        	} catch (CoreException e) {
        	}
        	StringBuffer buffer = new StringBuffer();
        	buffer.append(config.getName());
        	if (type != null) {
        		buffer.append(" ["); //$NON-NLS-1$
        		buffer.append(type);
        		buffer.append("] "); //$NON-NLS-1$
        	}
        	buffer.append(label);
        	label = buffer.toString();
        }

        if (fLaunch.isTerminated()) {
        	return ConsoleMessages.ConsoleMessages_console_terminated + label; 
        }
        
        return label;
    }
    
	/**
	 * Creates and returns a new page for this console. The page is displayed
	 * for this console in the GdbConsoleView.
	 * 
	 * @param view the view in which the page is to be created
	 * @return a page book view page representation of this console
	 */
	public IPageBookViewPage createPage(GdbConsoleView view) {
		view.setFocus();
		fConsolePage = new GdbCliConsolePage(this);
		return fConsolePage;
    }
	
	@Override
	public IPageBookViewPage createPage(IConsoleView view) {
		// This console is not handled by the console view
		return null;
	}
	
	public void setReverseVideo(boolean enable) {
		if (fConsolePage != null) {
			fConsolePage.setReverseVideo(enable);
		}
	}
}
