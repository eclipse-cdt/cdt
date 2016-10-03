/*******************************************************************************
 * Copyright (c) 2016 Ericsson and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package org.eclipse.cdt.dsf.gdb.internal.ui.console;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.HashSet;
import java.util.Set;

import org.eclipse.cdt.debug.ui.debuggerconsole.IDebuggerConsole;
import org.eclipse.cdt.debug.ui.debuggerconsole.IDebuggerConsoleView;
import org.eclipse.cdt.utils.pty.PTY;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.debug.core.ILaunch;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.ui.DebugUITools;
import org.eclipse.tm.internal.terminal.provisional.api.ITerminalControl;
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
	private final IGDBTerminalControlManager fTerminalConnector; 
	
	public GdbFullCliConsole(ILaunch launch, String label, Process process, PTY pty) {
		super(label, null, false);
		fLaunch = launch;
        fLabel = label;
        
        // Create a lifecycle listener to call init() and dispose()
        new GdbConsoleLifecycleListener(this);
        fTerminalConnector = new GdbTerminalConnector(process);
        resetName();
	}
    
	private final class GdbTerminalConnector implements IGDBTerminalControlManager {
		private final Set<ITerminalControl> fTerminalPageControls = new HashSet<>();
		private final Process fProcess;
		
		public GdbTerminalConnector(Process process) {
			fProcess = process;
			new OutputReadJob(process.getInputStream()).schedule();
			new OutputReadJob(process.getErrorStream()).schedule();
		}

		@Override
		public void addPageTerminalControl(ITerminalControl terminalControl) {
			fTerminalPageControls.add(terminalControl);
		}

		@Override
		public void removePageTerminalControl(ITerminalControl terminalControl) {
			if (terminalControl != null) {
				fTerminalPageControls.remove(terminalControl);
			}
		}

		@Override
		public OutputStream getTerminalToRemoteStream() {
			return fProcess.getOutputStream();
		}
		
		private class OutputReadJob extends Job {
	    	{
	    		setSystem(true); 
	    	}
	    	
	    	private InputStream fInputStream;
	    	
	    	private OutputReadJob(InputStream inputStream) {
	            super("GDB CLI output Job"); //$NON-NLS-1$
	            fInputStream = inputStream;
	        }

	        @Override
			protected IStatus run(IProgressMonitor monitor) {
	            try {
	                byte[] b = new byte[1024];
	                int read = 0;
	                do {
	                	read = fInputStream.read(b);
	                	if (read > 0) {
	                		for (ITerminalControl control : fTerminalPageControls) {
		                		control.getRemoteToTerminalOutputStream().write(b, 0, read);	                			
	                		}
	                	}
	                } while (read >= 0);
	            } catch (IOException e) {
	            }

	            return Status.OK_STATUS;
	        }
	    }
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

	public IGDBTerminalControlManager getTerminalControlManger() {
		return fTerminalConnector;
	}
}
