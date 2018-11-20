/*******************************************************************************
 * Copyright (c) 2016 Ericsson and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *******************************************************************************/
package org.eclipse.cdt.dsf.gdb.internal.ui.console;

import java.io.IOException;

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
import org.eclipse.swt.SWTException;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.console.IConsoleView;
import org.eclipse.ui.console.IOConsole;
import org.eclipse.ui.console.IOConsoleInputStream;
import org.eclipse.ui.console.IOConsoleOutputStream;
import org.eclipse.ui.part.IPageBookViewPage;

/**
 * A GDB CLI console.
 * This console simply provides an IOConsole to perform CLI commands
 * towards GDB.  It is used whenever {@link IGDBBackend#isFullGdbConsoleSupported()}
 * returns false.
 */
public class GdbBasicCliConsole extends IOConsole implements IGDBDebuggerConsole {

	/**
	 * A conversion factor used to resolve number of characters from number of lines
	 */
	private final static int CHARS_PER_LINE_AVG = 80;
	private final static int HIGH_WATERMARK_OFFSET_CHARS = 8000;

	private final ILaunch fLaunch;
	private final String fLabel;
	private final Process fProcess;
	private final IOConsoleOutputStream fOutputStream;
	private final IOConsoleOutputStream fErrorStream;

	private GdbAbstractConsolePreferenceListener fPreferenceListener = new GdbAbstractConsolePreferenceListener() {

		@Override
		protected void handleAutoTerminatePref(boolean enabled) {
			// Nothing to do for this class
		}

		@Override
		protected void handleInvertColorsPref(boolean enabled) {
			setInvertedColors(enabled);
		}

		@Override
		protected void handleBufferLinesPref(int bufferLines) {
			setBufferLineLimit(bufferLines);
		}
	};

	public GdbBasicCliConsole(ILaunch launch, String label, Process process) {
		super("", "GdbBasicCliConsole", null, false); //$NON-NLS-1$ //$NON-NLS-2$
		fLaunch = launch;
		fLabel = label;
		fProcess = process;
		fOutputStream = newOutputStream();
		fErrorStream = newOutputStream();

		assert (process != null);

		// Create a lifecycle listener to call init() and dispose()
		new GdbConsoleLifecycleListener(this);

		GdbUIPlugin.getDefault().getPreferenceStore().addPropertyChangeListener(fPreferenceListener);

		resetName();
		setDefaults();

		new InputReadJob().schedule();
		new OutputReadJob().schedule();
		new ErrorReadJob().schedule();
	}

	@Override
	protected void dispose() {
		stop();
		super.dispose();
	}

	@Override
	public void stop() {
		// Closing the streams will trigger the termination of the associated reading jobs
		try {
			fOutputStream.close();
		} catch (IOException e) {
		}
		try {
			fErrorStream.close();
		} catch (IOException e) {
		}

		IOConsoleInputStream istream = getInputStream();
		if (istream != null) {
			try {
				istream.close();
			} catch (IOException e) {
			}
		}

		GdbUIPlugin.getDefault().getPreferenceStore().removePropertyChangeListener(fPreferenceListener);
	}

	private void setDefaults() {
		IPreferenceStore store = GdbUIPlugin.getDefault().getPreferenceStore();
		boolean enabled = store.getBoolean(IGdbDebugPreferenceConstants.PREF_CONSOLE_INVERTED_COLORS);
		int bufferLines = store.getInt(IGdbDebugPreferenceConstants.PREF_CONSOLE_BUFFERLINES);

		Display.getDefault().asyncExec(() -> {
			IOConsoleInputStream inputStream = getInputStream();
			if (inputStream != null) {
				inputStream.setColor(Display.getDefault().getSystemColor(SWT.COLOR_GREEN));
			}
			fErrorStream.setColor(Display.getDefault().getSystemColor(SWT.COLOR_RED));

			setInvertedColors(enabled);
			setBufferLineLimit(bufferLines);
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
			try {
				PlatformUI.getWorkbench().getDisplay().asyncExec(() -> setName(newName));
			} catch (SWTException e) {
				// display may be disposed, so ignore the exception
				if (e.code != SWT.ERROR_WIDGET_DISPOSED) {
					throw e;
				}
			}
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
			return createPage((IConsoleView) view);
		}
		return null;
	}

	private void setInvertedColors(boolean enable) {
		if (enable) {
			setBackground(Display.getDefault().getSystemColor(SWT.COLOR_BLACK));
			fOutputStream.setColor(Display.getDefault().getSystemColor(SWT.COLOR_WHITE));
		} else {
			setBackground(Display.getDefault().getSystemColor(SWT.COLOR_WHITE));
			fOutputStream.setColor(Display.getDefault().getSystemColor(SWT.COLOR_BLACK));
		}
	}

	private void setBufferLineLimit(int bufferLines) {
		int chars = bufferLines * CHARS_PER_LINE_AVG;
		// The buffer will be allowed to grow up-to the high watermark.
		// When high watermark is passed, it will be trimmed-down to the low watermark.
		// So here add an extra buffer for high watermark.
		setWaterMarks(chars, chars + HIGH_WATERMARK_OFFSET_CHARS);
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
					IOConsoleInputStream inputStream = getInputStream();
					if (inputStream == null) {
						break;
					}
					read = inputStream.read(b);
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
