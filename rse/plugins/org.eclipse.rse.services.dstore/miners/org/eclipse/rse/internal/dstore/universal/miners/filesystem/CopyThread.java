/*******************************************************************************
 * Copyright (c) 2007, 2013 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Initial Contributors:
 * The following IBM employees contributed to the Remote System Explorer
 * component that contains this file: David McKnight.
 * 
 * Contributors:
 * Xuan Chen (IBM) - [209827] Update DStore command implementation to enable cancelation of archive operations
 * Noriaki Takatsu (IBM)  - [220126] [dstore][api][breaking] Single process server for multiple clients
 * David McKnight  (IBM)  - [290290] [dstore] Error message when copy a file from another user’s folder
 * David McKnight   (IBM) - [414016] [dstore] new server audit log requirements
 *******************************************************************************/
package org.eclipse.rse.internal.dstore.universal.miners.filesystem;

import java.io.File;
import java.io.InputStream;

import org.eclipse.dstore.core.model.DE;
import org.eclipse.dstore.core.model.DataElement;
import org.eclipse.dstore.core.server.SecuredThread;
import org.eclipse.rse.dstore.universal.miners.ICancellableHandler;
import org.eclipse.rse.dstore.universal.miners.IUniversalDataStoreConstants;
import org.eclipse.rse.dstore.universal.miners.UniversalFileSystemMiner;
import org.eclipse.rse.dstore.universal.miners.UniversalServerUtilities;
import org.eclipse.rse.services.clientserver.IServiceConstants;
import org.eclipse.rse.services.clientserver.PathUtility;
import org.eclipse.rse.services.clientserver.SystemOperationMonitor;

public class CopyThread extends SecuredThread implements ICancellableHandler {

	protected DataElement targetFolder;
	protected DataElement theElement;
	protected DataElement status;
	protected UniversalFileSystemMiner miner;
	protected boolean isWindows;
	
	protected boolean _isCancelled = false;
	protected boolean _isDone = false;
	protected SystemOperationMonitor systemOperationMonitor = null;
	
	public static final String CLASSNAME = "CopyThread"; //$NON-NLS-1$
	
	
	public CopyThread(DataElement targetFolder, DataElement theElement, UniversalFileSystemMiner miner, boolean isWindows, DataElement status)
	{
		super(theElement.getDataStore());
		this.targetFolder = targetFolder;
		this.theElement = theElement;
		this.miner = miner;
		this.status = status;
		this.isWindows = isWindows;
	}
	
	protected File getFileFor(DataElement element) {
		File result = null;
		String type = element.getType();
		if (type.equals(IUniversalDataStoreConstants.UNIVERSAL_FILTER_DESCRIPTOR)) {
			result = new File(element.getName());
		} else if (type.equals(IUniversalDataStoreConstants.UNIVERSAL_FILE_DESCRIPTOR)
				|| type.equals(IUniversalDataStoreConstants.UNIVERSAL_FOLDER_DESCRIPTOR)
				|| type.equals(IUniversalDataStoreConstants.UNIVERSAL_ARCHIVE_FILE_DESCRIPTOR)) {
			StringBuffer buf = new StringBuffer(element
					.getAttribute(DE.A_VALUE));
			buf.append(File.separatorChar);
			buf.append(element.getName());
			result = new File(buf.toString());
		}

		return result;
	}
	
	/**
	 * Quote a file name such that it is valid in a shell
	 * @param s file name to quote
	 * @return quoted file name
	 */
	protected String enQuote(String s)
	{
		if(isWindows) {
			return '"' + s + '"';
		} else {
			return PathUtility.enQuoteUnix(s);
		}
	}
	
	protected void doCopyCommand(String source, String tgt, boolean folderCopy, DataElement status)
	{
        String[] auditData = new String[] {"COPY", source, tgt, null}; //$NON-NLS-1$
    	UniversalServerUtilities.logAudit(auditData, _dataStore);

		String command = null;
		if (isWindows) {
			
			if (folderCopy) {
				command = "xcopy " + source //$NON-NLS-1$
					+ " " + tgt //$NON-NLS-1$
					+ " /S /E /K /Q /H /I /Y"; //$NON-NLS-1$
			}
			else {
				String unquotedTgt = tgt.substring(1, tgt.length() - 1);
				
				File targetFile = new File(unquotedTgt);
				if (!targetFile.exists())
				{
					// create file so as to avoid ambiguity
					try
					{
						targetFile.createNewFile();
					}
					catch (Exception e)
					{
						status.setAttribute(DE.A_SOURCE, IServiceConstants.FAILED);
						status.setAttribute(DE.A_VALUE, e.getMessage());		
						return;
					}
				}				
				command = "xcopy " + source + " " + tgt + " /Y /K /Q /H"; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ 
			}
		}
		else {
			if (folderCopy) {
				command = "cp  -Rp " + source + " " + tgt; //$NON-NLS-1$ //$NON-NLS-2$
			}
			else {
				command = "cp -p " + source + " " + tgt; //$NON-NLS-1$ //$NON-NLS-2$
			}
		}

		// run copy command
		try
		{	
			Runtime runtime = Runtime.getRuntime();
			Process p = null;
				
			if (isWindows)
			{
				String theShell = "cmd /C "; //$NON-NLS-1$
				p = runtime.exec(theShell + command);	
			}
			else
			{
				String theShell = "sh"; //$NON-NLS-1$
				String args[] = new String[3];
				args[0] = theShell;					
				args[1] = "-c"; //$NON-NLS-1$
				args[2] = command;
												
				p = runtime.exec(args);
			}
			
			// ensure there is a process
			if (p != null) {
			    
			    // wait for process to finish
			    p.waitFor();
			    
			    // get the exit value of the process
			    int result = p.exitValue();
			    
			    // if the exit value is not 0, then the process did not terminate normally
			    if (result != 0) {
			        
			        // get the error stream
					InputStream errStream = p.getErrorStream();
					
					// error buffer
					StringBuffer errBuf = new StringBuffer();
					
					byte[] bytes = null;
					
					int numOfBytesRead = 0;
					
					int available = errStream.available();
					
					// read error stream and store in error buffer
					while (available > 0) {
						
						bytes = new byte[available];
						
						numOfBytesRead = errStream.read(bytes);
						
						if (numOfBytesRead > -1) {
						    errBuf.append(new String(bytes, 0, numOfBytesRead));
						}
						else {
						    break;
						}
						
						available = errStream.available();
					}
					
					String err = errBuf.toString();
					
					// omit new line if there is one at the end because datastore does not
					// handle new line in the attributes
					// TODO: what to do if newline occurs in the middle of the string?
					String newLine = System.getProperty("line.separator"); //$NON-NLS-1$
					
					if (newLine != null && err.endsWith(newLine)) {
					    err = err.substring(0, err.length() - newLine.length());
					}
					
					String theOS = System.getProperty("os.name"); //$NON-NLS-1$
					boolean isZ = theOS.toLowerCase().startsWith("z"); //$NON-NLS-1$
					
					// special case for bug 290290 - which only occurs on z/OS
					if (isZ && err.startsWith("cp: FSUM8985")){ //$NON-NLS-1$
						// attempt against without the -p
						if (folderCopy) {
							command = "cp  -R " + source + " " + tgt; //$NON-NLS-1$ //$NON-NLS-2$
						}
						else {
							command = "cp " + source + " " + tgt; //$NON-NLS-1$ //$NON-NLS-2$
						}
						String theShell = "sh"; //$NON-NLS-1$
						String args[] = new String[3];
						args[0] = theShell;					
						args[1] = "-c"; //$NON-NLS-1$
						args[2] = command;
														
						p = runtime.exec(args);
						
					    // wait for process to finish
					    p.waitFor();
					    
					    // get the exit value of the process
					   result = p.exitValue();
					    
					    // if the exit value is not 0, then the process did not terminate normally
					    if (result == 0) {
					    	status.setAttribute(DE.A_SOURCE, IServiceConstants.SUCCESS);
					    	return;
					    }
					}
					// if there is something in error buffer
					// there was something in the error stream of the process
					if (err.length() > 0) {
						status.setAttribute(DE.A_SOURCE, IServiceConstants.FAILED);
						status.setAttribute(DE.A_VALUE, err);
					}
					// otherwise, nothing in the error stream
					// but we know process did not exit normally, so we indicate an unexpected error
					else {
						status.setAttribute(DE.A_SOURCE, IServiceConstants.FAILED);
						status.setAttribute(DE.A_VALUE, IServiceConstants.UNEXPECTED_ERROR);
					}
			    }
			    // otherwise if exit value is 0, process terminated normally
			    else {
					status.setAttribute(DE.A_SOURCE, IServiceConstants.SUCCESS);
			    }
			}
			// no process, so something is wrong
			else {
				status.setAttribute(DE.A_SOURCE, IServiceConstants.FAILED);
				status.setAttribute(DE.A_VALUE, IServiceConstants.UNEXPECTED_ERROR);					
			}
		}
		catch (Exception e)
		{
			UniversalServerUtilities.logError(CLASSNAME, "Exception is handleCopy", e, _dataStore); //$NON-NLS-1$
			status.setAttribute(DE.A_SOURCE, IServiceConstants.FAILED);
			status.setAttribute(DE.A_VALUE, e.getMessage());
		}	
	}
	

	public void cancel() {
		_isCancelled = true;
		if (null != systemOperationMonitor)
		{
			systemOperationMonitor.setCancelled(true);
		}
	}

	public boolean isCancelled() {
		return _isCancelled;
	}

	public boolean isDone() {
		return _isDone;
	}
	
}
