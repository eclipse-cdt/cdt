/********************************************************************************
 * Copyright (c) 2006 IBM Corporation. All rights reserved.
 * This program and the accompanying materials are made available under the terms
 * of the Eclipse Public License v1.0 which accompanies this distribution, and is 
 * available at http://www.eclipse.org/legal/epl-v10.html
 * 
 * Initial Contributors:
 * The following IBM employees contributed to the Remote System Explorer
 * component that contains this file: David McKnight, Kushal Munir, 
 * Michael Berger, David Dykstal, Phil Coulthard, Don Yantzi, Eric Simpson, 
 * Emily Bruner, Mazen Faraj, Adrian Storisteanu, Li Ding, and Kent Hawley.
 * 
 * Contributors:
 * {Name} (company) - description of contribution.
 ********************************************************************************/

package org.eclipse.rse.dstore.universal.miners.command;




import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;

import org.eclipse.dstore.core.model.DE;
import org.eclipse.dstore.core.model.DataElement;
import org.eclipse.dstore.core.model.DataStore;
import org.eclipse.rse.dstore.universal.miners.environment.EnvironmentMiner;

/**
 * QueryPathThread is used to determine available command completions
 */
public class QueryPathThread extends Thread
{

	private DataStore _dataStore;
	private DataElement _status;

	public QueryPathThread(DataElement status)
	{
		super();
		_status = status;
		_dataStore = status.getDataStore();
	}

	public void run()
	{
		getPossibleCommands(_status);

	}

	public List getPathEnvironment()
	{
		DataElement envMinerData = _dataStore.findMinerInformation(EnvironmentMiner.MINER_ID);
		if (envMinerData != null)
		{
			DataElement systemEnvironment = _dataStore.find(envMinerData, DE.A_NAME, "System Environment", 1);
			if (systemEnvironment != null)
			{
				// d54675
				// for Windows, ignore the case sensitiveness of PATH variable
				boolean isIgnoreCase = System.getProperty("os.name").toLowerCase().startsWith("win");
				ArrayList vars = _dataStore.searchForPattern(systemEnvironment, DE.A_NAME, "PATH=*", isIgnoreCase);
				
				if (vars == null || vars.size() == 0) {
					return new ArrayList();
				}
				
				DataElement pathVariable = (DataElement) vars.get(0);
				if (pathVariable != null)
				{
					String varStr = pathVariable.getValue();
					int separatorIndex = varStr.indexOf("=");
					if (separatorIndex > 0)
					{
						varStr = varStr.substring(separatorIndex + 1, varStr.length());
					}
					
					return parsePathEnvironmentVariable(varStr);
				}
			}
		}
		return null;
	}

	protected List parsePathEnvironmentVariable(String path)
	{
		ArrayList addedPaths = new ArrayList();
		ArrayList addedFolders = new ArrayList();

		boolean isWindows = System.getProperty("os.name").toLowerCase().startsWith("win");
		char separator = isWindows ? ';' : ':';
		StringTokenizer tokenizer = new StringTokenizer(path, separator + "");
		while (tokenizer.hasMoreTokens())
		{
			String token = tokenizer.nextToken();
			if (!addedPaths.contains(token))
			{
				addedPaths.add(token);

				File folder = new File(token);
				if (folder.exists() && folder.isDirectory())
				{
					addedFolders.add(folder);
				}
			}
		}
		return addedFolders;
	}

	public void getPossibleCommands(DataElement status)
	{
		List resolvedPaths = new ArrayList();
		List paths = getPathEnvironment();
		for (int i = 0; i < paths.size(); i++)
		{
			File folder = (File) paths.get(i);
			String abspath = folder.getAbsolutePath().toLowerCase();
			if (!resolvedPaths.contains(abspath))
			{
				resolveCommandsInPath(folder, status);
				resolvedPaths.add(abspath);
			}
		}
		status.setAttribute(DE.A_NAME, "done");
		_dataStore.refresh(status);
	}

	private void resolveCommandsInPath(File file, DataElement status)
	{
		if (file.isDirectory())
		{
			File[] files = file.listFiles();
			for (int i = 0; i < files.length; i++)
			{
				File afile = files[i];
				if (afile.isFile() && !afile.isHidden())
				{
					String name = afile.getName();
					DataElement fileObj = _dataStore.createObject(status, "file", name);
					fileObj.setAttribute(DE.A_SOURCE, afile.getAbsolutePath());
				}
			}
		}
	}
}