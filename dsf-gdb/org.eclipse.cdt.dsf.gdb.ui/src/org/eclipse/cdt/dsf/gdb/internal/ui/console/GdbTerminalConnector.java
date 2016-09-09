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

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.PlatformObject;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.tm.internal.terminal.provisional.api.ISettingsStore;
import org.eclipse.tm.internal.terminal.provisional.api.ITerminalConnector;
import org.eclipse.tm.internal.terminal.provisional.api.ITerminalControl;
import org.eclipse.tm.internal.terminal.provisional.api.TerminalState;

/**
 * Class that connects the GDB process I/O with the terminal.
 */
public class GdbTerminalConnector extends PlatformObject implements ITerminalConnector {

    private ITerminalControl fControl;
    private final Process fProcess;

    public GdbTerminalConnector(Process process) {
    	if (process == null) {
    		throw new IllegalArgumentException("Invalid Process"); //$NON-NLS-1$
    	}

    	fProcess = process;
	}
	
    @Override
	public void disconnect() {
		// Set the terminal control state to CLOSED.
    	if (fControl != null) {
    		fControl.setState(TerminalState.CLOSED);
    	}
    }

	@Override
	public OutputStream getTerminalToRemoteStream() {
		// When the user writes to the terminal, it should be send
		// directly to GDB
		return fProcess.getOutputStream();
	}

	@Override
	public void connect(ITerminalControl control) {
    	if (control == null) {
    		throw new IllegalArgumentException("Invalid ITerminalControl"); //$NON-NLS-1$
    	}

		fControl = control;

		// connect the streams
		new OutputReadJob(fProcess.getInputStream()).schedule();
		new OutputReadJob(fProcess.getErrorStream()).schedule();

		// Set the terminal control state to CONNECTED
		fControl.setState(TerminalState.CONNECTED);
	}


    @Override
    public void setTerminalSize(int newWidth, int newHeight) {
    }

    @Override
    public String getId() {
    	// No need for an id, as we're are just used locally
    	return null;
    }

    @Override
    public String getName() {
    	// No name
    	return null;
    }

    @Override
    public boolean isHidden() {
    	// in case we do leak into the TM world, we shouldn't be visible
    	return true;
    }

    @Override
    public boolean isInitialized() {
    	return true;
    }

    @Override
    public String getInitializationErrorMessage() {
    	return null;
    }

    @Override
    public boolean isLocalEcho() {
    	return false;
    }

    @Override
    public void setDefaultSettings() {
    	// we don't do settings
    }

    @Override
    public String getSettingsSummary() {
    	// we don't do settings
    	return null;
    }

    @Override
    public void load(ISettingsStore arg0) {
    	// we don't do settings
    }

    @Override
    public void save(ISettingsStore arg0) {
    	// we don't do settings
    }

    private class OutputReadJob extends Job {
    	{
    		setSystem(true); 
    	}
    	
    	private InputStream fInputStream;
    	
    	OutputReadJob(InputStream inputStream) {
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
                		fControl.getRemoteToTerminalOutputStream().write(b, 0, read);
                	}
                } while (read >= 0);
            } catch (IOException e) {
            }
            return Status.OK_STATUS;
        }
    }
}
