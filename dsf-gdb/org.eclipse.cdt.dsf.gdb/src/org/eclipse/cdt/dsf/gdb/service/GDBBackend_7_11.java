/*******************************************************************************
 * Copyright (c) 2006, 2015 Wind River Systems, Nokia and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Nokia              - initial API and implementation with some code moved from GDBControl.
 *     Wind River System
 *     Ericsson
 *     Marc Khouzam (Ericsson) - Use the new IMIBackend2 interface (Bug 350837)
 *     Mark Bozeman (Mentor Graphics) - Report GDB start failures (Bug 376203)
 *     Iulia Vasii (Freescale Semiconductor) - Separate GDB command from its arguments (Bug 445360)
 *******************************************************************************/
package org.eclipse.cdt.dsf.gdb.service;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PipedOutputStream;

import org.eclipse.cdt.dsf.concurrent.RequestMonitor;
import org.eclipse.cdt.dsf.concurrent.Sequence;
import org.eclipse.cdt.dsf.concurrent.Sequence.Step;
import org.eclipse.cdt.dsf.gdb.internal.GdbPlugin;
import org.eclipse.cdt.dsf.gdb.service.command.GDBControl.InitializationShutdownStep;
import org.eclipse.cdt.dsf.mi.service.command.LargePipedInputStream;
import org.eclipse.cdt.dsf.service.DsfSession;
import org.eclipse.cdt.utils.CommandLineUtil;
import org.eclipse.cdt.utils.pty.PTY;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.debug.core.ILaunchConfiguration;

/**
 * Implementation of {@link IGDBBackend} using GDB 7.11. This version provides
 * full GDB console support.  It achieves this by launching GDB in CLI mode
 * in a special console widget and then connecting to it by telling GDB to
 * open a new MI console.  The rest of the DSF-GDB support then stays the same.
 * 
 * If we are unable to create a PTY, then we revert to the previous behavior of
 * the base class.
 * 
 * @since 5.0
 */
public class GDBBackend_7_11 extends GDBBackend {

	private PTY fPty;
	private InputStream fErrorStream;

	public GDBBackend_7_11(DsfSession session, ILaunchConfiguration lc) {
		super(session, lc);
	}

	@Override
	public void initialize(final RequestMonitor requestMonitor) {
		super.initialize(requestMonitor);
	}

	@Override
	protected void doInitialize(final RequestMonitor requestMonitor) {

		final Sequence.Step[] initializeSteps = new Sequence.Step[] {
				new CreatePty(InitializationShutdownStep.Direction.INITIALIZING),
				new GDBProcessStep(InitializationShutdownStep.Direction.INITIALIZING),
				new MonitorJobStep(InitializationShutdownStep.Direction.INITIALIZING),
				new SetupNewConsole(InitializationShutdownStep.Direction.INITIALIZING),
				new RegisterStep(InitializationShutdownStep.Direction.INITIALIZING),
		};

		Sequence startupSequence = new Sequence(getExecutor(), requestMonitor) {
			@Override public Step[] getSteps() { return initializeSteps; }
		};
		getExecutor().execute(startupSequence);
	}

	@Override
    protected Step[] getShutdownSteps() {
        return new Sequence.Step[] {
                new RegisterStep(InitializationShutdownStep.Direction.SHUTTING_DOWN),
				new SetupNewConsole(InitializationShutdownStep.Direction.SHUTTING_DOWN),
                new MonitorJobStep(InitializationShutdownStep.Direction.SHUTTING_DOWN),
                new GDBProcessStep(InitializationShutdownStep.Direction.SHUTTING_DOWN),
				new CreatePty(InitializationShutdownStep.Direction.SHUTTING_DOWN),
            };
    }        

	@Override
	protected String[] getGDBCommandLineArray() {
		if (fPty == null) {
			return super.getGDBCommandLineArray();
		}

		// The goal here is to keep options to an absolute minimum.
		// All configuration should be done in the final launch sequence
		// to allow for more flexibility.

		String cmd = getGDBPath().toOSString() +
				" --nx"; //$NON-NLS-1$

		// Parse to properly handle spaces and such things (bug 458499)
		return CommandLineUtil.argumentsToArray(cmd);
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

	/** @since 4.1 */
	@Override
	public InputStream getMIErrorStream() {
		if (fPty == null) {
			return super.getMIErrorStream();
		}
		return fErrorStream;
	};

	@Override
	protected String getOutputToWaitFor() {
		if (fPty == null) {
			return super.getOutputToWaitFor();
		}
		return "apropos word"; //$NON-NLS-1$
	}

	@Override
	protected Process launchGDBProcess(String[] commandLine) throws CoreException {
		if (fPty == null) {
			return super.launchGDBProcess(commandLine);
		}
		//TODO
		return null;
	}
	
	protected class CreatePty extends InitializationShutdownStep {
		CreatePty(Direction direction) { super(direction); }
		@Override
		public void initialize(final RequestMonitor requestMonitor) {
			try {
				fPty = new PTY();
				fPty.validateSlaveName();

				PipedOutputStream errorStreamPiped = new PipedOutputStream();
				try {
					// Using a LargePipedInputStream see https://bugs.eclipse.org/bugs/show_bug.cgi?id=223154
					fErrorStream = new LargePipedInputStream(errorStreamPiped);
				} catch (IOException e) {
				}
			} catch (IOException e) {
				fPty = null;
			}
			requestMonitor.done();
		}

		@Override
		protected void shutdown(RequestMonitor requestMonitor) {
			fPty = null;
			requestMonitor.done();
		}
	}
	
	protected class SetupNewConsole extends InitializationShutdownStep {
		SetupNewConsole(Direction direction) { super(direction); }
		@Override
		public void initialize(final RequestMonitor requestMonitor) {
			if (fPty == null) {
				requestMonitor.done();
				return;
			}
			
			try {
				StringBuffer buff = new StringBuffer("new-console "); //$NON-NLS-1$
				buff.append(fPty.getSlaveName());
				buff.append('\n');
				getProcess().getOutputStream().write(buff.toString().getBytes());
				getProcess().getOutputStream().flush();

				// Wait for console to be ready by reading the process output
				BufferedReader inputReader = new BufferedReader(new InputStreamReader(getProcess().getInputStream()));
				String line;
				int numLines = 0;
                boolean success = false;
				while ((line = inputReader.readLine()) != null && numLines++ < 10) {
					line = line.trim();
					if (line.indexOf("New GDB console allocated") != -1) { //$NON-NLS-1$
                    	success = true;
                    	break;
					}
				}

                // Failed to trigger new console
                if (!success) {
                	requestMonitor.done(new Status(IStatus.ERROR, GdbPlugin.PLUGIN_ID, -1, "Unable to create new console", null)); //$NON-NLS-1$
                	return;
                }
                
				// Wait for initial GDB prompt on the new MI stream
                success = false;
				inputReader = new BufferedReader(new InputStreamReader(getMIInputStream()));
				while ((line = inputReader.readLine()) != null) {
					line = line.trim();
					if (line.indexOf("(gdb)") != -1) { //$NON-NLS-1$
						success = true;
						break;
					}
				}

				// Failed to read initial prompt on MI channel
                if (!success) {
                	requestMonitor.done(new Status(IStatus.ERROR, GdbPlugin.PLUGIN_ID, -1, "Failed to read (gdb) prompt on MI channel", null)); //$NON-NLS-1$
                	return;
                }

			} catch (IOException e) {
				requestMonitor.done(new Status(IStatus.CANCEL, GdbPlugin.PLUGIN_ID, -1, "Unable to setup MI console", null)); //$NON-NLS-1$
				return;
			}

			requestMonitor.done();
		}

		@Override
		protected void shutdown(RequestMonitor requestMonitor) {
			requestMonitor.done();
		}
	}
}
