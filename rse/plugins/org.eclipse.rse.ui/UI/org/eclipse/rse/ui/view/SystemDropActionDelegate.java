/********************************************************************************
 * Copyright (c) 2002, 2006 IBM Corporation. All rights reserved.
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

package org.eclipse.rse.ui.view;

import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.jface.dialogs.ProgressMonitorDialog;
import org.eclipse.jface.operation.IRunnableContext;
import org.eclipse.rse.core.SystemPlugin;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.part.IDropActionDelegate;



/**
 * This class is used for dropping RSE src objects on known non-RSE targets
 * 
 */
public class SystemDropActionDelegate implements IDropActionDelegate
{

	public static final String ID = "org.eclipse.rse.ui.view.DropActions"; //ID fixed, by Phil
	

	/** (non-Javadoc)
	 * Method declared on IDropActionDelegate
	 */
	public boolean run(Object data, Object target)
	{
		String localPath = null;
		IResource resource = null;
		
		
		

		if (target instanceof IResource)
		{		
			resource = (IResource) target;
			localPath = resource.getLocation().toOSString();
		}
		else if (target instanceof String)
		{
			localPath = (String)target;	
		}
		else if (target instanceof IAdaptable)
		{
		    Object resourceObj = ((IAdaptable)target).getAdapter(IResource.class);
		    if (resourceObj != null && resourceObj instanceof IResource)
		    {
		        resource = (IResource)resourceObj;
		        localPath = resource.getLocation().toOSString();
		    }
		}

		
		if (localPath != null)
		{
			/** FIXME - IREmoteFile is systems.core independent now
			IRemoteFileSubSystem localFS = getLocalFileSubSystem();
			try
			{
				IRemoteFile rsfTarget = localFS.getRemoteFileObject(localPath);

				if (data instanceof byte[])
				{
					byte[] result = (byte[]) data;

					// get the sources	
					//StringTokenizer tokenizer = new StringTokenizer(new String(result), SystemViewDataDropAdapter.RESOURCE_SEPARATOR);
					String[] tokens = (new String(result)).split("\\"+SystemViewDataDropAdapter.RESOURCE_SEPARATOR);
					ArrayList srcObjects = new ArrayList();
					ArrayList rulesList = new ArrayList();
					int j = 0;
					//while (tokenizer.hasMoreTokens())
					for (int i = 0; i <tokens.length; i++)  
					{
						String srcStr = tokens[i];

						Object srcObject = getObjectFor(srcStr);
						srcObjects.add(srcObject);
						if (srcObject instanceof ISchedulingRule)
						{
							rulesList.add(srcObject);
							j++;
						}
						else if (srcObject instanceof IRemoteFile)
						{
							rulesList.add(new RemoteFileSchedulingRule((IRemoteFile)srcObject));
							j++;
						}
					}
					if (resource != null)
					{
						rulesList.add(resource);
						j++;
					}
					
					ISchedulingRule[] rules = (ISchedulingRule[])rulesList.toArray(new ISchedulingRule[rulesList.size()]);
					MultiRule rule = null;
					if (j > 0)
					{
						rule = new MultiRule(rules);
					}
					
					Viewer currentViewer = null; // todo: figure out how to determine the current viewer! Phil
					SystemDNDTransferRunnable runnable = new SystemDNDTransferRunnable(rsfTarget, srcObjects, currentViewer, SystemDNDTransferRunnable.SRC_TYPE_RSE_RESOURCE);
										
					runnable.setRule(rule);
					
					runnable.schedule();
					
					if (resource != null)
					{
						try
						{
							resource.refreshLocal(IResource.DEPTH_INFINITE, null);
						}
						catch (CoreException e)
						{
						}
					}
					SystemPlugin.getTheSystemRegistry().clearRunnableContext();
				}
			}
			catch (SystemMessageException e)
			{
			}
			**/
			return true;
		}
		
		return false;
	}

	protected IRunnableContext getRunnableContext(Shell shell)
	{
		IRunnableContext irc = SystemPlugin.getTheSystemRegistry().getRunnableContext();
		if (irc != null)
		{
			return irc;
		}

		irc = new ProgressMonitorDialog(shell);
		SystemPlugin.getTheSystemRegistry().setRunnableContext(shell, irc);
		return irc;
	}
}