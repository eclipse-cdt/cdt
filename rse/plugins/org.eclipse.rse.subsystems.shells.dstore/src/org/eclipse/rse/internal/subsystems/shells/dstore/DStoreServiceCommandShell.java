/*******************************************************************************
 * Copyright (c) 2006, 2010 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Initial Contributors:
 * The following IBM employees contributed to the Remote System Explorer
 * component that contains this file: David McKnight, Kushal Munir,
 * Michael Berger, David Dykstal, Phil Coulthard, Don Yantzi, Eric Simpson,
 * Emily Bruner, Mazen Faraj, Adrian Storisteanu, Li Ding, and Kent Hawley.
 *
 * Contributors:
 * David McKnight (IBM) - [202822] cleanup output datalements after use
 * Martin Oberhuber (Wind River) - [225510][api] Fix OutputRefreshJob API leakage
 * David McKnight (IBM) - [286671] Dstore shell service interprets &lt; and &gt; sequences
 *******************************************************************************/

package org.eclipse.rse.internal.subsystems.shells.dstore;

import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.dstore.core.model.DataElement;
import org.eclipse.dstore.core.model.DataStore;
import org.eclipse.dstore.core.model.DataStoreResources;
import org.eclipse.dstore.extra.DomainEvent;
import org.eclipse.dstore.extra.IDomainListener;
import org.eclipse.rse.internal.services.dstore.shells.DStoreHostOutput;
import org.eclipse.rse.internal.services.dstore.shells.DStoreHostShell;
import org.eclipse.rse.internal.services.dstore.shells.DStoreShellOutputReader;
import org.eclipse.rse.services.shells.IHostOutput;
import org.eclipse.rse.services.shells.IHostShell;
import org.eclipse.rse.services.shells.IHostShellChangeEvent;
import org.eclipse.rse.subsystems.files.core.subsystems.IRemoteFileSubSystem;
import org.eclipse.rse.subsystems.shells.core.model.RemoteError;
import org.eclipse.rse.subsystems.shells.core.model.RemoteOutput;
import org.eclipse.rse.subsystems.shells.core.subsystems.IRemoteCmdSubSystem;
import org.eclipse.rse.subsystems.shells.core.subsystems.IRemoteOutput;
import org.eclipse.rse.subsystems.shells.core.subsystems.servicesubsystem.ServiceCommandShell;
import org.eclipse.swt.widgets.Shell;

public class DStoreServiceCommandShell extends ServiceCommandShell
{
	private class CleanUpSpirited extends Thread implements IDomainListener
	{
		private DataElement _status;
		private DataStore _ds;
		private boolean _done = false;

		public CleanUpSpirited(DataElement status, String name)
		{
			_status = status;
			_ds = status.getDataStore();
			_ds.getDomainNotifier().addDomainListener(this);
		}

		public void domainChanged(DomainEvent e)
		{
			deleteElements();
		}

		public void run()
		{
			while (!_done)
			{
				try
				{
					Thread.sleep(10000);
				}
				catch (Exception e)
				{
				}
				deleteElements();
			}
		}

		private void deleteElements()
		{
			if (_status.getNestedSize() > 0)
			{
				//synchronized (_status)
				{
					int ssize = _status.getNestedSize();
				if (_status.get(ssize - 1).isSpirit())
				{
					// delete
					_ds.deleteObjects(_status);
					_ds.refresh(_status);

					_ds.getDomainNotifier().removeDomainListener(this);
					_done = true;
				}
				}
			}
		}

		public Shell getShell() {
			// TODO Auto-generated method stub
			return null;
		}

		public boolean listeningTo(DomainEvent e) {
			if (e.getParent() == _status)
				return true;
			return false;
		}

	}

	public DStoreServiceCommandShell(IRemoteCmdSubSystem cmdSS, IHostShell hostShell)
	{
		super(cmdSS, hostShell);
	}

	public Object getContext()
	{
		DStoreHostShell shell = (DStoreHostShell)getHostShell();
		DStoreShellOutputReader reader = (DStoreShellOutputReader)shell.getStandardOutputReader();
		String workingDir = reader.getWorkingDirectory();
		if (workingDir != null && workingDir.length() > 0)
		{
			try
			{
				IRemoteFileSubSystem ss = getFileSubSystem();
				if (ss.isConnected())
				{
					return ss.getRemoteFileObject(workingDir, new NullProgressMonitor());
				}
			}
			catch (Exception e)
			{
			}
		}
		return null;
	}

	public String getContextString()
	{
		DStoreHostShell shell = (DStoreHostShell)getHostShell();
		DStoreShellOutputReader reader = (DStoreShellOutputReader)shell.getStandardOutputReader();
		return reader.getWorkingDirectory();
	}

	private String convertSpecialCharacters(String input){
		// needed to ensure xml characters aren't converted in xml layer	
		String converted = input.replaceAll("&#38;", "&") //$NON-NLS-1$ //$NON-NLS-2$
			.replaceAll("&#59;", ";");  //$NON-NLS-1$//$NON-NLS-2$
		return converted;
	}
	
	public void shellOutputChanged(IHostShellChangeEvent event)
	{
		IHostOutput[] lines = event.getLines();
		IRemoteOutput[] outputs = new IRemoteOutput[lines.length];
		for (int i = 0; i < lines.length; i++)
		{
			RemoteOutput output = null;
			Object lineObj = lines[i];
			if (lineObj instanceof DStoreHostOutput)
			{
				DataElement line = ((DStoreHostOutput)lineObj).getElement();
				String type = line.getType();
				String src = line.getSource();
				if (event.isError())
				{
					output = new RemoteError(this, type);

				}
				else
				{
					output = new RemoteOutput(this, type);
				}

				DataStore dataStore = line.getDataStore();
				DataElement fsD= dataStore.findObjectDescriptor(DataStoreResources.model_directory);
				DataElement convDes = dataStore.localDescriptorQuery(fsD, "C_CHAR_CONVERSION", 1); //$NON-NLS-1$
				
				String text = line.getName();
				if (convDes != null){
					text = convertSpecialCharacters(text);
				}
								
				output.setText(convertSpecialCharacters(text));

				int colonSep = src.indexOf(':');
				// line numbers
				if (colonSep > 0)
				{

					String lineNo = src.substring(colonSep + 1);
					String file = src.substring(0, colonSep);
					int linen = 0;
					try
					{
						linen = Integer.parseInt(lineNo);
					}
					catch (Exception e)
					{

					}
					if (linen != 0)
					{
						output.setAbsolutePath(file);
						output.setLine(linen);
					}
					else
					{
						output.setAbsolutePath(src);
					}
				}
				else
				{
					output.setAbsolutePath(src);
				}


				addOutput(output);
				outputs[i] = output;
			}
		}
		notifyOutputChanged(outputs, false);
	}

	public boolean isActive()
	{
		boolean activeShell = _hostShell.isActive();
		if (!activeShell)
		{
			DataElement status = ((DStoreHostShell)_hostShell).getStatus();
			if (_output.size() < status.getNestedSize())
			{
				return true;
			}
		}
		return activeShell;
	}

	public void removeOutput()
	{
		DStoreHostShell shell = (DStoreHostShell)getHostShell();
		DataElement status = shell.getStatus();
		DataStore ds = status.getDataStore();

		int ssize = status.getNestedSize();
		if (ssize > 0)
		{
			if (!ds.isConnected())
			{
				status.removeNestedData();
			}
			else if (status.get(ssize - 1).isSpirit() || !ds.isDoSpirit())
			{
				// objects can be deleted directly at this point since there will be no more updates from the server
				ds.deleteObjects(status);
				ds.refresh(status);
			}
			else
			{
				// cleanup later
				// objects need to be deleted later since the server will still be sending spirited update
				// if we don't defer this, then the deleted elements would get recreated when the spirits are updated
				CleanUpSpirited cleanUp = new CleanUpSpirited(status, getId());
				cleanUp.start();
			}
		}




		synchronized(_output)
		{
			_output.clear();
		}

	}

}
