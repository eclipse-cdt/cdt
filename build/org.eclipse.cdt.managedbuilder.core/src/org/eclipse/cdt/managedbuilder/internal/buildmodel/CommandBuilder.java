/*******************************************************************************
 * Copyright (c) 2006, 2009 Intel Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Intel Corporation - Initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.managedbuilder.internal.buildmodel;

import java.io.File;
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
import org.eclipse.cdt.managedbuilder.internal.core.ManagedMakeMessages;
import org.eclipse.cdt.utils.PathUtil;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.Path;
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
	private static final String PROPERTY_DELIMITER = "path.separator"; //$NON-NLS-1$
	private static final String PROPERTY_OS_NAME    = "os.name"; //$NON-NLS-1$
	private static final String PROPERTY_OS_VALUE = "windows";//$NON-NLS-1$
	static final String DELIMITER_UNIX = ":";    //$NON-NLS-1$
	static final String DELIMITER_WINDOWS = ";";    //$NON-NLS-1$

	private IBuildCommand fCmd;
	private Process fProcess;
	private String fErrMsg;

	private static final String BUILDER_MSG_HEADER = "InternalBuilder.msg.header"; //$NON-NLS-1$
	private static final String NEWLINE = System.getProperty("line.separator", "\n"); //$NON-NLS-1$ //$NON-NLS-2$

/*
 * no need in this for now, Spawner is always used
 *
	protected class SpawnerfreeLauncher extends CommandLauncher{

		public Process execute(IPath commandPath, String[] args, String[] env, IPath changeToDirectory) {
			try {
				// add platform specific arguments (shell invocation)
				fCommandArgs = constructCommandArray(commandPath.toOSString(), args);
				fProcess = Runtime.getRuntime().exec(fCommandArgs, env, changeToDirectory.toFile());
//					ProcessFactory.getFactory().exec(fCommandArgs, env, changeToDirectory.toFile());
				fErrorMessage = ""; //$NON-NLS-1$
			} catch (IOException e) {
				setErrorMessage(e.getMessage());
				fProcess = null;
			}
			return fProcess;
		}
	}
*/
	/*
	 * a temporary work-around to resolve the bug#145099
	 * (https://bugs.eclipse.org/bugs/show_bug.cgi?id=145099)
	 *
	 * this will be removed after fixing the bug#145737
	 * (https://bugs.eclipse.org/bugs/show_bug.cgi?id=145737)
	 */
	private class CommandSearchLauncher extends CommandLauncher{

		@Override
		protected String[] constructCommandArray(String command, String[] commandArgs) {
			String[] args = new String[1 + commandArgs.length];
			if(!isWindows()){
				//find a full path to an executable
				String cmd = getExecutable(command, fCmd.getEnvironment());
				if(cmd != null)
					command = cmd;
				//if not found, continue with the command passed as an argument
			}

			args[0] = command;
			System.arraycopy(commandArgs, 0, args, 1, commandArgs.length);
			return args;
		}


		@Override
		protected void printCommandLine(OutputStream os) {
			if (os != null) {
				String cmd = CommandBuilder.this.getCommandLine();
				try {
					os.write(cmd.getBytes());
					os.flush();
				} catch (IOException e) {
					// ignore;
				}
			}
		}
	}

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
	public int build(OutputStream out, OutputStream err,
			IProgressMonitor monitor){

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

		if (fProcess != null) {
			try {
				// Close the input of the process since we will never write to it
				fProcess.getOutputStream().close();
			} catch (IOException e) {
			}
		}

		//wrapping out and err streams to avoid their closure
		int st = launcher.waitAndRead(wrap(out), wrap(err),
				new SubProgressMonitor(monitor,	getNumCommands()));
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

			String program = fCmd.getCommand().toOSString();
			String envPath = fCmd.getEnvironment().get(PATH_ENV);
			if (envPath==null) {
				envPath = System.getenv(PATH_ENV);
			}
			if (PathUtil.findProgramLocation(program, envPath)==null) {
				printMessage(fErrMsg, out);
				String errMsg = ManagedMakeMessages.getFormattedString("ManagedMakeBuilder.message.program.not.in.path", program); //$NON-NLS-1$
				printErrorMessage(errMsg + NEWLINE, out);
				printMessage(null, PATH_ENV+"=["+envPath+"]", out); //$NON-NLS-1$//$NON-NLS-2$
			} else {
				printErrorMessage(fErrMsg, out);
			}
			break;
		}

		monitor.done();
		return status;
	}

	protected ICommandLauncher createLauncher() {
//		if(isWindows())
//			return new CommandLauncher();
		return new CommandSearchLauncher();
	}

	public String getErrMsg(){
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

	private void printMessage(String prefix, String msg, OutputStream os){
		if (os != null) {
			if (prefix==null) {
				prefix=""; //$NON-NLS-1$
			}
			msg = prefix + msg + NEWLINE;
			try {
				os.write(msg.getBytes());
				os.flush();
			} catch (IOException e) {
				// ignore;
			}
		}

	}

	protected void printMessage(String msg, OutputStream os){
		if (os != null) {
			msg = ManagedMakeMessages.getFormattedString(BUILDER_MSG_HEADER, msg);
			printMessage(null, msg, os);
		}

	}

	private void printErrorMessage(String msg, OutputStream os){
		if (os != null) {
			String errorPrefix = ManagedMakeMessages.getResourceString("ManagedMakeBuilder.error.prefix"); //$NON-NLS-1$
			printMessage(errorPrefix, msg, os);
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

	private String getExecutable(String command, Map<String, String> environment){
		if(new Path(command).isAbsolute())
			return command;
		return searchExecutable(command, getPaths(environment));
	}

	private String[] getPaths(Map<String, String> env){
		String pathsStr = env.get(PATH_ENV);
		if(pathsStr == null){
			Set<Entry<String, String>> entrySet = env.entrySet();
			for (Entry<String, String> entry : entrySet) {
				if(PATH_ENV.equalsIgnoreCase(entry.getKey())){
					pathsStr = entry.getValue();
					break;
				}
			}
		}
		if(pathsStr != null){
			String delimiter = getDelimiter();

			return pathsStr.split(delimiter);
		}
		return null;
	}

	private String getDelimiter(){
		String delimiter = System.getProperty(PROPERTY_DELIMITER);
		if(delimiter == null)
			delimiter = isWindows() ? DELIMITER_WINDOWS : DELIMITER_UNIX;
		return delimiter;
	}

	private String searchExecutable(String command, String paths[]){
		if(paths == null)
			return null;

		for(int i = 0; i < paths.length; i++){
			File file = new File(paths[i], command.toString());
			if(file.isFile())
				return file.toString();
		}
		return null;
	}

	private boolean isWindows() {
		String prop = System.getProperty(PROPERTY_OS_NAME);
		return prop != null ? prop.toLowerCase().startsWith(PROPERTY_OS_VALUE) : false;
	}
}
