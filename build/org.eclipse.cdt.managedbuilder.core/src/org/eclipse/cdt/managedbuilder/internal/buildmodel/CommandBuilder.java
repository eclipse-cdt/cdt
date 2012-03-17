/*******************************************************************************
 * Copyright (c) 2006, 2012 Intel Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
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

import org.eclipse.cdt.core.CommandLauncher;
import org.eclipse.cdt.core.ICommandLauncher;
import org.eclipse.cdt.managedbuilder.buildmodel.IBuildCommand;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
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
	private static final String PATH_ENV = "PATH"; //$NON-NLS-1$
	private static final String NEWLINE = System.getProperty("line.separator", "\n"); //$NON-NLS-1$ //$NON-NLS-2$

	private IBuildCommand fCmd;
	private Process fProcess;
	private String fErrMsg;

	protected class OutputStreamWrapper extends OutputStream {
		private OutputStream fOut;

		public OutputStreamWrapper(OutputStream out){
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

	public CommandBuilder(IBuildCommand cmd, IResourceRebuildStateContainer cr){
		fCmd = cmd;
	}

	protected OutputStream wrap(OutputStream out){
		return new OutputStreamWrapper(out);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.managedbuilder.internal.builddescription.IBuildDescriptionBuilder#build(java.io.OutputStream, java.io.OutputStream, org.eclipse.core.runtime.IProgressMonitor)
	 */
	@Override
	public int build(OutputStream out, OutputStream err, IProgressMonitor monitor){
		//TODO: should we display the command line here?
		monitor.beginTask("", getNumCommands());	//$NON-NLS-1$
		monitor.subTask(""/*getCommandLine()*/);	//$NON-NLS-1$

		ICommandLauncher launcher = createLauncher();
		int status = STATUS_OK;

		launcher.showCommand(true);

		try {
			fProcess = launcher.execute(fCmd.getCommand(), fCmd.getArgs(), mapToStringArray(fCmd.getEnvironment()), fCmd.getCWD(), monitor);
		} catch (CoreException e1) {
			// TODO Auto-generated catch block
			if(DbgUtil.DEBUG)
				DbgUtil.trace("Error launching command: " + e1.getMessage());	//$NON-NLS-1$
			monitor.done();
			return STATUS_ERROR_LAUNCH;
		}

		int st = ICommandLauncher.ILLEGAL_COMMAND;
		if (fProcess != null) {
			try {
				// Close the input of the process since we will never write to it
				fProcess.getOutputStream().close();
			} catch (IOException e) {
			}
			//wrapping out and err streams to avoid their closure
			st = launcher.waitAndRead(wrap(out), wrap(err),
					new SubProgressMonitor(monitor,	getNumCommands()));
		}

		switch(st){
		case ICommandLauncher.OK:
			if(fProcess.exitValue() != 0)
				status = STATUS_ERROR_BUILD;
			break;
		case ICommandLauncher.COMMAND_CANCELED:
			status = STATUS_CANCELLED;
			fErrMsg = launcher.getErrorMessage();
			if(DbgUtil.DEBUG)
				DbgUtil.trace("command cancelled: " + fErrMsg);	//$NON-NLS-1$

			printMessage(fErrMsg, out);
			break;
		case ICommandLauncher.ILLEGAL_COMMAND:
		default:
			status = STATUS_ERROR_LAUNCH;
			fErrMsg = launcher.getErrorMessage();
			if(DbgUtil.DEBUG)
				DbgUtil.trace("error launching the command: " + fErrMsg);	//$NON-NLS-1$

			printMessage(fErrMsg, out);
			break;
		}

		monitor.done();
		return status;
	}

	protected ICommandLauncher createLauncher() {
		return new CommandLauncher();
	}

	public String getErrMsg() {
		return fErrMsg;
	}

	private String[] mapToStringArray(Map<String, String> map){
		if(map == null)
			return null;

		List<String> list = new ArrayList<String>();

		Set<Entry<String, String>> entrySet = map.entrySet();
		for (Entry<String, String> entry : entrySet) {
			list.add(entry.getKey() + '=' + entry.getValue());
		}

		return list.toArray(new String[list.size()]);
	}

	protected void printMessage(String msg, OutputStream os){
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
		StringBuffer buf = new StringBuffer();
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
