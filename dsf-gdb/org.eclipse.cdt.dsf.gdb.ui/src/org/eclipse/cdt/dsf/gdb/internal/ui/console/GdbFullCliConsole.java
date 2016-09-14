/*******************************************************************************
 * Copyright (c) 2016 Ericsson and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package org.eclipse.cdt.dsf.gdb.internal.ui.console;

import org.eclipse.cdt.debug.ui.debuggerconsole.IDebuggerConsole;
import org.eclipse.cdt.debug.ui.debuggerconsole.IDebuggerConsoleView;
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
public class GdbFullCliConsole extends AbstractConsole implements IDebuggerConsole, IGdbCliConsole {
	private final ILaunch fLaunch;
	private final String fLabel;
	private GdbFullCliConsolePage fConsolePage;
	
	public GdbFullCliConsole(ILaunch launch, String label) {
		super(label, null, false);
		fLaunch = launch;
        fLabel = label;
        
        // Create a lifecycle listener to call init() and dispose()
        new GdbConsoleLifecycleListener(this);
        
        resetName();
	}
    
	@Override
	public ILaunch getLaunch() { return fLaunch; }
    
    @Override
	public void resetName() {
    	String newName = computeName();
    	String name = getName();
    	if (!name.equals(newName)) {
    		PlatformUI.getWorkbench().getDisplay().asyncExec(() -> setName(newName));
    	}
    }
	
    protected String computeName() {
    	if (fLaunch == null) {
    		return ""; //$NON-NLS-1$
    	}
    	
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
    
	@Override
	public IPageBookViewPage createPage(IConsoleView view) {
		// This console is not meant for the standard console view
		return null;
	}
	
	@Override
	public IPageBookViewPage createDebuggerPage(IDebuggerConsoleView view) {
		view.setFocus();
		fConsolePage = new GdbFullCliConsolePage(this, view);
		return fConsolePage;
	}

	@Override
	public void setInvertedColors(boolean enable) {
		if (fConsolePage != null) {
			fConsolePage.setInvertedColors(enable);
		}
	}

	@Override
	public void setAutoTerminateGDB(boolean autoTerminate) {
		if (fConsolePage != null) {
			fConsolePage.setAutoTerminateGDB(autoTerminate);
		}
	}

	@Override
	public void setBufferLineLimit(int bufferLines) {
		if (fConsolePage != null) {
			fConsolePage.setBufferLineLimit(bufferLines);
		}
	}
}
