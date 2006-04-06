/*******************************************************************************
 * Copyright (c) 2006 Intel Corporation and others.
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
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.eclipse.cdt.core.CommandLauncher;
import org.eclipse.cdt.managedbuilder.buildmodel.IBuildCommand;
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
	private IBuildCommand fCmd;
	private Process fProcess;
	private String fErrMsg;

	protected class OutputStreamWrapper extends OutputStream {
		private OutputStream fOut;
		
		public OutputStreamWrapper(OutputStream out){
			fOut = out;
		}
		
	    public void write(int b) throws IOException {
	    	fOut.write(b);
	    }

	    public void write(byte b[]) throws IOException {
	    	fOut.write(b);
	    }

	    public void write(byte b[], int off, int len) throws IOException {
	    	fOut.write(b, off, len);
	    }

	    public void flush() throws IOException {
	    	fOut.flush();
	    }

	    public void close() throws IOException {
	    }
	    
	}

	public CommandBuilder(IBuildCommand cmd){
		fCmd = cmd;
	}
	
	protected OutputStream wrap(OutputStream out){
		return new OutputStreamWrapper(out);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.managedbuilder.internal.builddescription.IBuildDescriptionBuilder#build(java.io.OutputStream, java.io.OutputStream, org.eclipse.core.runtime.IProgressMonitor)
	 */
	public int build(OutputStream out, OutputStream err,
			IProgressMonitor monitor){
		
		CommandLauncher launcher = new CommandLauncher();
		int status = STATUS_OK;

		launcher.showCommand(true);

		fProcess = launcher.execute(fCmd.getCommand(), fCmd.getArgs(), mapToStringArray(fCmd.getEnvironment()), fCmd.getCWD());
		
		if (fProcess != null) {
			try {
				// Close the input of the process since we will never write to it
				fProcess.getOutputStream().close();
			} catch (IOException e) {
			}
			//wrapping out and err streams to avoid their closure
			int st = launcher.waitAndRead(wrap(out), wrap(err),
					new SubProgressMonitor(monitor,
							IProgressMonitor.UNKNOWN));
			switch(st){
			case CommandLauncher.OK:
				if(fProcess.exitValue() != 0)
					status = STATUS_ERROR_BUILD;
				break;
			case CommandLauncher.COMMAND_CANCELED:
				status = STATUS_CANCELLED;
				break;
			default:
				status = STATUS_ERROR_LAUNCH;
				fErrMsg = launcher.getErrorMessage(); 
				break;
			}
		} else {
			fErrMsg = launcher.getErrorMessage(); 
			if(DbgUtil.DEBUG)
				DbgUtil.traceln("error launching the command: " + fErrMsg);	//$NON-NLS-1$

			status = STATUS_ERROR_LAUNCH;
		}
		
		return status;
	}
	
	public String getErrMsg(){
		return fErrMsg;
	}
	
	private String[] mapToStringArray(Map map){
		if(map == null)
			return null;
		
		List list = new ArrayList();
		
		for(Iterator iter = map.entrySet().iterator(); iter.hasNext();){
			Map.Entry entry = (Map.Entry)iter.next();
			list.add((String)entry.getKey() + "=" + (String)entry.getValue());	//$NON-NLS-1$
		}
		
		return (String[])list.toArray(new String[list.size()]);
	}
}
