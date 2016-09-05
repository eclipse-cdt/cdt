/*******************************************************************************
 * Copyright (c) 2016 Ericsson and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package org.eclipse.cdt.dsf.gdb.internal.ui.console;

import java.io.IOException;

import org.eclipse.cdt.debug.ui.debuggerconsole.IDebuggerConsole;
import org.eclipse.cdt.debug.ui.debuggerconsole.IDebuggerConsoleView;
import org.eclipse.cdt.dsf.gdb.IGdbDebugPreferenceConstants;
import org.eclipse.cdt.dsf.gdb.internal.ui.GdbUIPlugin;
import org.eclipse.cdt.dsf.gdb.service.IGDBBackend;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.debug.core.ILaunch;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.ui.DebugUITools;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.console.IConsoleView;
import org.eclipse.ui.console.IOConsole;
import org.eclipse.ui.console.IOConsoleOutputStream;
import org.eclipse.ui.part.IPageBookViewPage;

/**
 * A GDB CLI console.
 * This console simply provides an IOConsole to perform CLI commands
 * towards GDB.  It is used whenever {@link IGDBBackend#isFullGdbConsoleSupported()}
 * returns false.
 */
public class GdbBasicCliConsole extends IOConsole implements IDebuggerConsole, IGdbCliConsole {

	private final ILaunch fLaunch;
	private final String fLabel;
	private final Process fProcess;
	private final IOConsoleOutputStream fOutputStream;
	private final IOConsoleOutputStream fErrorStream;
	
	public GdbBasicCliConsole(ILaunch launch, String label, Process process) {
		super("", null, null, false); //$NON-NLS-1$
		fLaunch = launch;
        fLabel = label;
        fProcess = process;
        fOutputStream = newOutputStream();
        fErrorStream = newOutputStream();
        
        assert(process != null);

        // Create a lifecycle listener to call init() and dispose()
        new GdbConsoleLifecycleListener(this);
        
        resetName();
        setColors();
        
		new InputReadJob().schedule();
		new OutputReadJob().schedule();
		new ErrorReadJob().schedule();
	}

	@Override
	protected void dispose() {
        try {
        	fOutputStream.close();
		} catch (IOException e) {
		}
        try {
        	fErrorStream.close();
		} catch (IOException e) {
		}

		super.dispose();
	}
	
	private void setColors() {
		// Set the inverted colors option based on the stored preference
		IPreferenceStore store = GdbUIPlugin.getDefault().getPreferenceStore();
		boolean enabled = store.getBoolean(IGdbDebugPreferenceConstants.PREF_CONSOLE_INVERTED_COLORS);

		Display.getDefault().asyncExec(() -> {
			getInputStream().setColor(Display.getDefault().getSystemColor(SWT.COLOR_GREEN));
        	fErrorStream.setColor(Display.getDefault().getSystemColor(SWT.COLOR_RED));

    		setInvertedColors(enabled);
        });
	}

	@Override
	public ILaunch getLaunch() {
		return fLaunch;
	}
	
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
        return new GdbBasicCliConsolePage(this, view);
    }

	@Override
	public IPageBookViewPage createDebuggerPage(IDebuggerConsoleView view) {
		if (view instanceof IConsoleView) {
			return createPage((IConsoleView)view);
		}
		return null;
	}
	
	@Override
	public void setInvertedColors(boolean enable) {
		if (enable) {
			setBackground(Display.getDefault().getSystemColor(SWT.COLOR_BLACK));
			fOutputStream.setColor(Display.getDefault().getSystemColor(SWT.COLOR_WHITE));
		} else {
			setBackground(Display.getDefault().getSystemColor(SWT.COLOR_WHITE));			
			fOutputStream.setColor(Display.getDefault().getSystemColor(SWT.COLOR_BLACK));
		}
	}

    private class InputReadJob extends Job {
    	{
    		setSystem(true); 
    	}
    	
        InputReadJob() {
            super("GDB CLI Input Job"); //$NON-NLS-1$
        }

        @Override
		protected IStatus run(IProgressMonitor monitor) {
            try {
                byte[] b = new byte[1024];
                int read = 0;
                do {
                	read = getInputStream().read(b);
                	if (read > 0) {
                		fProcess.getOutputStream().write(b, 0, read);
                	}
                } while (read >= 0);
            } catch (IOException e) {
            }
            return Status.OK_STATUS;
        }
    }

    private class OutputReadJob extends Job {
    	{
    		setSystem(true); 
    	}
    	
    	OutputReadJob() {
            super("GDB CLI output Job"); //$NON-NLS-1$
        }

        @Override
		protected IStatus run(IProgressMonitor monitor) {
            try {
                byte[] b = new byte[1024];
                int read = 0;
                do {
                	read = fProcess.getInputStream().read(b);
                	if (read > 0) {
                		fOutputStream.write(b, 0, read);
                	}
                } while (read >= 0);
            } catch (IOException e) {
            }
            return Status.OK_STATUS;
        }
    }

    private class ErrorReadJob extends Job {
    	{
    		setSystem(true); 
    	}
    	
    	ErrorReadJob() {
            super("GDB CLI error output Job"); //$NON-NLS-1$
        }

        @Override
		protected IStatus run(IProgressMonitor monitor) {
            try {
                byte[] b = new byte[1024];
                int read = 0;
                do {
                	read = fProcess.getErrorStream().read(b);
                	if (read > 0) {
                		fErrorStream.write(b, 0, read);
                	}
                } while (read >= 0);
            } catch (IOException e) {
            }
            return Status.OK_STATUS;
        }
    }
}
