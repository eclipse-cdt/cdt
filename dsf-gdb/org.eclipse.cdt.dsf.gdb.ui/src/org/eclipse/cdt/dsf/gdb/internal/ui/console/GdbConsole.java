/*******************************************************************************
 * Copyright (c) 2015 Ericsson and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package org.eclipse.cdt.dsf.gdb.internal.ui.console;

import org.eclipse.cdt.dsf.gdb.launching.GdbLaunch;
import org.eclipse.cdt.dsf.service.DsfSession;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.debug.core.ILaunch;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.ui.DebugUITools;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.console.AbstractConsole;
import org.eclipse.ui.console.IConsoleView;
import org.eclipse.ui.part.IPageBookViewPage;

/**
 */
public class GdbConsole extends AbstractConsole {
	private ILaunch fLaunch;
	private DsfSession fSession = null;
	private String fLabel = ""; //$NON-NLS-1$

	public GdbConsole(ILaunch launch, String label) {
		super("", null); //$NON-NLS-1$
		fLaunch = launch;
        fSession = ((GdbLaunch)launch).getSession();
        fLabel = label;
        
        resetName();
	}
	
    @Override
	protected void init() {
        super.init();
    }
    
	@Override
	protected void dispose() {
		super.dispose();
	}
	
	public ILaunch getLaunch() { return fLaunch; }
    
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
        	return ConsoleMessages.ConsoleMessages_trace_console_terminated + label; 
        }
        
        return label;
    }
    
    public void resetName() {
    	final String newName = computeName();
    	String name = getName();
    	if (!name.equals(newName)) {
    		Runnable r = new Runnable() {
                @Override
    			public void run() {
    				setName(newName);
    			}
    		};
    		PlatformUI.getWorkbench().getDisplay().asyncExec(r);
    	}
    }
//	
//	public synchronized void setState(TerminalState terminalState) {
//		StringBuffer nameBuff = new StringBuffer(getConnection().getName());
//		if (index > 0) {
//			nameBuff.append(' ');
//			nameBuff.append(String.valueOf(index));
//		}
//		nameBuff.append(" ("); //$NON-NLS-1$
//		if (terminalState == TerminalState.CLOSED) {
//			nameBuff.append(ConsoleMessages.STATUS_CLOSED);
//		} else if (terminalState == TerminalState.CONNECTED) {
//			nameBuff.append(ConsoleMessages.STATUS_CONNECTED);
//		} else if (terminalState == TerminalState.CONNECTING) {
//			nameBuff.append(ConsoleMessages.STATUS_CONNECTING);
//		}
//		nameBuff.append(")"); //$NON-NLS-1$
//
//		final String name = nameBuff.toString();
//		if (!name.equals(getName())) {
//			UIJob job = new UIJob("updating name") { //$NON-NLS-1$
//				public IStatus runInUIThread(IProgressMonitor monitor) {
//					TerminalConsole.this.setName(name);
//					return Status.OK_STATUS;
//				}
//			};
//			job.setSystem(true);
//			job.schedule();
//		}
//	}
//
	public IPageBookViewPage createPage(IConsoleView view) {
		view.setFocus();
		return new GdbConsolePage(this, "UTF-8");
	}	



}
