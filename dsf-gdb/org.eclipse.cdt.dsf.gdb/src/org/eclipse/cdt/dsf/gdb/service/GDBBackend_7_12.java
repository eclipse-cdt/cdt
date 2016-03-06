/*******************************************************************************
 * Copyright (c) 2016 Ericsson and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package org.eclipse.cdt.dsf.gdb.service;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PipedOutputStream;

import org.eclipse.cdt.dsf.gdb.internal.GdbPlugin;
import org.eclipse.cdt.dsf.mi.service.command.LargePipedInputStream;
import org.eclipse.cdt.dsf.service.DsfSession;
import org.eclipse.cdt.utils.pty.PTY;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.osgi.util.NLS;

/**
 * Implementation of {@link IGDBBackend} using GDB 7.12. This version provides
 * full GDB console support.  It achieves this by launching GDB in CLI mode
 * in a special console widget and then connecting to GDB via MI by telling GDB to
 * open a new MI console.  The rest of the DSF-GDB support then stays the same.
 * 
 * If we are unable to create a PTY, we then revert to the previous behavior of
 * the base class.
 * 
 * @since 5.1
 */
public class GDBBackend_7_12 extends GDBBackend {

	private PTY fPty;
	private InputStream fErrorStream;

	public GDBBackend_7_12(DsfSession session, ILaunchConfiguration lc) {
		super(session, lc);
		createPty();
	}

	/**
	 * This method can be overridden by extenders to disable the usage
	 * of the full GDB console, and instead to fallback on the lesser
	 * console as implemented by CDT.
	 */
	protected boolean isFullGdbConsoleDisabled() {
		return false;
	}
	
	protected void createPty() {
		if (isFullGdbConsoleDisabled()) {
			return;
		}
		
		try {
			fPty = new PTY();
			fPty.validateSlaveName();

			PipedOutputStream errorStreamPiped = new PipedOutputStream();
			try {
				// Using a LargePipedInputStream see http://eclip.se/223154
				fErrorStream = new LargePipedInputStream(errorStreamPiped);
			} catch (IOException e) {
			}
		} catch (IOException e) {
			fPty = null;
			GdbPlugin.log(new Status(
							IStatus.INFO, GdbPlugin.PLUGIN_ID, 
							NLS.bind(Messages.PTY_Console_not_available, e.getMessage())));
		}
	}
	
	@Override
	public OutputStream getMIOutputStream() {
		if (fPty == null) {
			return super.getMIOutputStream();
		}
		return fPty.getOutputStream();
	};

	@Override
	public InputStream getMIInputStream() {
		if (fPty == null) {
			return super.getMIInputStream();
		}
		return fPty.getInputStream();
	};

	@Override
	public InputStream getMIErrorStream() {
		if (fPty == null) {
			return super.getMIErrorStream();
		}
		return fErrorStream;
	};

	@Override
	public boolean isFullGdbConsoleSupported() {
		// If we have a PTY, use the new GDB console feature.
		// TODO make sure this works for Windows and Mac
		return !isFullGdbConsoleDisabled() && fPty != null;
	}
	
	@Override
	protected String[] getGDBCommandLine() {
		// Start from the original command line method which
		// could have been overridden, and add what we need
		// to convert it to a command that will launch in CLI mode
		// then trigger the MI console
		String[] originalCommandLine = super.getGDBCommandLineArray();
        
        if (!isFullGdbConsoleSupported()) {
            return originalCommandLine;
        }

		String[] extraArguments = new String[] {
				// Start with -q option to avoid extra output which may trigger pagination
				// This is important because if pagination is triggered on the version
				// printout, we won't be able to send the command to start the MI channel.
				"-q", //$NON-NLS-1$
				// Force a CLI console as the original command
				// probably specified "-i mi"
				"-interpreter","console", //$NON-NLS-1$ //$NON-NLS-2$
				// Now trigger the new console towards our PTY.
				"-ex","new-ui mi " + fPty.getSlaveName(), //$NON-NLS-1$ //$NON-NLS-2$
				// Now print the version so the user gets that familiar output
				"-ex","show version"  //$NON-NLS-1$ //$NON-NLS-2$
		};

		int oriLength = originalCommandLine.length;
		int extraLength = extraArguments.length;
		String[] newCommandLine = new String[oriLength+extraLength];
		System.arraycopy(originalCommandLine, 0, newCommandLine, 0, oriLength);
		System.arraycopy(extraArguments, 0, newCommandLine, oriLength, extraLength);

		return newCommandLine;
	}
}
