/*******************************************************************************
 * Copyright (c) 2006, 2016 Intel Corporation and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 * Intel Corporation - Initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.managedbuilder.internal.buildmodel;

import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.eclipse.cdt.core.CommandLauncherManager;
import org.eclipse.cdt.core.ICommandLauncher;
import org.eclipse.cdt.managedbuilder.buildmodel.IBuildCommand;
import org.eclipse.cdt.managedbuilder.core.ManagedBuilderCorePlugin;
import org.eclipse.cdt.managedbuilder.internal.core.ManagedMakeMessages;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.SubProgressMonitor;

/**
 *
 * This class implements the IBuildCommand building
 * To build the given command, create an instance of this class
 * and invoke the build method
 *
 * NOTE: This class is subject to change and discuss,
 * and is currently available in experimental mode only
 *
 */
public class CommandBuilder implements IBuildModelBuilder {
	private static final String NEWLINE = System.getProperty("line.separator", "\n"); //$NON-NLS-1$ //$NON-NLS-2$

	private IBuildCommand fCmd;
	private Process fProcess;
	private String fErrMsg;

	private IProject fProject;

	protected class OutputStreamWrapper extends OutputStream {
		private OutputStream fOut;

		public OutputStreamWrapper(OutputStream out) {
			fOut = out;
		}

		@Override
		public void write(int b) throws IOException {
			fOut.write(b);
		}

		@Override
		public void write(byte b[]) throws IOException {
			fOut.write(b);
		}

		@Override
		public void write(byte b[], int off, int len) throws IOException {
			fOut.write(b, off, len);
		}

		@Override
		public void flush() throws IOException {
			fOut.flush();
		}

		@Override
		public void close() throws IOException {
		}

	}

	public CommandBuilder(IBuildCommand cmd, IResourceRebuildStateContainer cr, IProject project) {
		fCmd = cmd;
		fProject = project;
	}

	protected OutputStream wrap(OutputStream out) {
		return new OutputStreamWrapper(out);
	}

	@Override
	public int build(OutputStream out, OutputStream err, IProgressMonitor monitor) {
		int status = STATUS_ERROR_LAUNCH;

		try {
			if (monitor == null) {
				monitor = new NullProgressMonitor();
			}
			monitor.beginTask("", getNumCommands()); //$NON-NLS-1$
			monitor.subTask(ManagedMakeMessages.getResourceString("MakeBuilder.Invoking_Command") + getCommandLine()); //$NON-NLS-1$

			ICommandLauncher launcher = createLauncher();
			launcher.showCommand(true);

			fProcess = launcher.execute(fCmd.getCommand(), fCmd.getArgs(), mapToStringArray(fCmd.getEnvironment()),
					fCmd.getCWD(), monitor);
			if (fProcess != null) {
				try {
					// Close the input of the process since we will never write to it
					fProcess.getOutputStream().close();
				} catch (IOException e) {
				}

				// Wrapping out and err streams to avoid their closure
				int st = launcher.waitAndRead(wrap(out), wrap(err), new SubProgressMonitor(monitor, getNumCommands()));
				switch (st) {
				case ICommandLauncher.OK:
					// assuming that compiler returns error code after compilation errors
					status = fProcess.exitValue() == 0 ? STATUS_OK : STATUS_ERROR_BUILD;
					break;
				case ICommandLauncher.COMMAND_CANCELED:
					status = STATUS_CANCELLED;
					break;
				case ICommandLauncher.ILLEGAL_COMMAND:
				default:
					status = STATUS_ERROR_LAUNCH;
					break;
				}
			}

			fErrMsg = launcher.getErrorMessage();
			if (fErrMsg != null && !fErrMsg.isEmpty()) {
				printMessage(fErrMsg, err);
			}
		} catch (CoreException e) {
			ManagedBuilderCorePlugin.log(new Status(IStatus.ERROR, ManagedBuilderCorePlugin.PLUGIN_ID,
					"Error launching command [" + fCmd.getCommand() + "]", e)); //$NON-NLS-1$ //$NON-NLS-2$
			status = STATUS_ERROR_LAUNCH;
		} finally {
			monitor.done();
		}

		return status;
	}

	protected ICommandLauncher createLauncher() {
		return CommandLauncherManager.getInstance().getCommandLauncher(fProject);
	}

	public String getErrMsg() {
		return fErrMsg;
	}

	private String[] mapToStringArray(Map<String, String> map) {
		if (map == null)
			return null;

		List<String> list = new ArrayList<>();

		Set<Entry<String, String>> entrySet = map.entrySet();
		for (Entry<String, String> entry : entrySet) {
			list.add(entry.getKey() + '=' + entry.getValue());
		}

		return list.toArray(new String[list.size()]);
	}

	protected void printMessage(String msg, OutputStream os) {
		if (os != null) {
			try {
				os.write((msg + NEWLINE).getBytes());
				os.flush();
			} catch (IOException e) {
				// ignore;
			}
		}

	}

	public int getNumCommands() {
		return 1;
	}

	protected String getCommandLine() {
		StringBuilder buf = new StringBuilder();
		if (fCmd != null) {
			buf.append(fCmd.getCommand().toOSString());
			String args[] = fCmd.getArgs();
			for (int i = 0; i < args.length; i++) {
				buf.append(' ');
				buf.append(args[i]);
			}
			buf.append(NEWLINE);
		}
		return buf.toString();
	}
}
