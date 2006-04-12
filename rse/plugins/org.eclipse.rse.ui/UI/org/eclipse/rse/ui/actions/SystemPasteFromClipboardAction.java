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

package org.eclipse.rse.ui.actions;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.jobs.ISchedulingRule;
import org.eclipse.core.runtime.jobs.MultiRule;
import org.eclipse.jface.dialogs.ProgressMonitorDialog;
import org.eclipse.jface.operation.IRunnableContext;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.rse.core.subsystems.ISubSystem;
import org.eclipse.rse.model.IHost;
import org.eclipse.rse.services.clientserver.messages.SystemMessage;
import org.eclipse.rse.ui.ISystemContextMenuConstants;
import org.eclipse.rse.ui.ISystemMessages;
import org.eclipse.rse.ui.RSEUIPlugin;
import org.eclipse.rse.ui.SystemResources;
import org.eclipse.rse.ui.validators.IValidatorRemoteSelection;
import org.eclipse.rse.ui.view.ISystemDragDropAdapter;
import org.eclipse.rse.ui.view.ISystemRemoteElementAdapter;
import org.eclipse.rse.ui.view.SystemDNDTransferRunnable;
import org.eclipse.swt.dnd.Clipboard;
import org.eclipse.swt.dnd.FileTransfer;
import org.eclipse.swt.dnd.TextTransfer;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.ISharedImages;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.part.PluginTransfer;
import org.eclipse.ui.part.PluginTransferData;
import org.eclipse.ui.part.ResourceTransfer;


/**
 * Paste resources in system clipboard to the selected resource action.
 */
public class SystemPasteFromClipboardAction extends SystemBaseAction implements  ISystemMessages, IValidatorRemoteSelection
{


	private int _srcType;
	private Object _selection;
	private Clipboard _clipboard;
	/**
	 * Constructor
	 */
	public SystemPasteFromClipboardAction(Shell shell, Clipboard clipboard)
	{
		super(SystemResources.ACTION_PASTE_LABEL, 
			  PlatformUI.getWorkbench().getSharedImages().getImageDescriptor(ISharedImages.IMG_TOOL_PASTE),
			  //RSEUIPlugin.getDefault().getImageDescriptor(ISystemConstants.ICON_SYSTEM_PASTE_ID), 
			  shell);
		_clipboard = clipboard;
		_srcType = SystemDNDTransferRunnable.SRC_TYPE_RSE_RESOURCE;
		setEnabled(false);

		allowOnMultipleSelection(false);
		setContextMenuGroup(ISystemContextMenuConstants.GROUP_REORGANIZE);
		setHelp(RSEUIPlugin.HELPPREFIX + "actn0117");
	}

	public void run()
	{
		if (_selection != null)
		{
			pasteClipboardToSelection(_selection);
		}
	}
	
	
	private void pasteClipboardToSelection(Object target)
	{
		List srcObjects = RSEUIPlugin.getTheSystemRegistry().getSystemClipboardObjects(_srcType);
		if (srcObjects.size() > 0)
		{
			// do the transfer
			SystemDNDTransferRunnable runnable = new SystemDNDTransferRunnable(target, (ArrayList)srcObjects, getViewer(), _srcType);
			if (target instanceof IAdaptable)
			{
				ISystemDragDropAdapter targetAdapter = (ISystemDragDropAdapter) ((IAdaptable) target).getAdapter(ISystemDragDropAdapter.class);

				if (targetAdapter != null)
				{
					ISubSystem targetSubSystem = targetAdapter.getSubSystem(target);
					List rulesList = new ArrayList();
					int j = 0;
					for (int i = 0; i < srcObjects.size(); i++)
					{
						if (srcObjects.get(i) instanceof ISchedulingRule)
						{
							rulesList.add(srcObjects.get(i));
							j++;
						}
						/** FIXME - IREmoteFile is systems.core independent now
						else if (srcObjects.get(i) instanceof IRemoteFile)
						{
							rulesList.add(new RemoteFileSchedulingRule((IRemoteFile)srcObjects.get(i)));
							j++;
						}
						**/
					}
					if (target instanceof ISchedulingRule)
					{
						rulesList.add(target);
					}
					/** FIXME - IREmoteFile is systems.core independent now
					else if (target instanceof IRemoteFile)
					{
						rulesList.add(new RemoteFileSchedulingRule((IRemoteFile)target));
					}
					*/
					else
					{
						rulesList.add(targetSubSystem);
					}
					
					ISchedulingRule[] rules = (ISchedulingRule[])rulesList.toArray(new ISchedulingRule[rulesList.size()]);
					MultiRule rule = new MultiRule(rules);
					//runnable.setRule(rule);
				}
			}
			runnable.schedule();
			RSEUIPlugin.getTheSystemRegistry().clearRunnableContext();
		}
		// clear clipboard
		// _clipboard.setContents(new Object[] { null }, new Transfer[] { PluginTransfer.getInstance()});
		// setEnabled(false);
	}

	
	/**
	 * The user has selected a remote object. Return null if OK is to be enabled, or a SystemMessage
	 *  if it is not to be enabled. The message will be displayed on the message line.
	 * <p>
	 * This is overridden in SystemMoveRemoteFileAction
	 */
	public SystemMessage isValid(IHost selectedConnection, Object[] selectedObjects, ISystemRemoteElementAdapter[] remoteAdaptersForSelectedObjects)
	{
		return null;
	}

	public boolean hasSource()
	{
		synchronized (_clipboard)
		{
			try
			{
				Object object = _clipboard.getContents(PluginTransfer.getInstance());
				if (object != null)
				{
					if (object instanceof PluginTransferData)
					{
						PluginTransferData data = (PluginTransferData) object;
						byte[] result = data.getData();
						if (result != null)
						{
							_srcType = SystemDNDTransferRunnable.SRC_TYPE_RSE_RESOURCE;
							return true;
						}
					}
				}
				else
				{
					// clipboard must have resources or files
					ResourceTransfer resTransfer = ResourceTransfer.getInstance();
					object = _clipboard.getContents(resTransfer);
					if (object != null)
					{
						IResource[] resourceData = (IResource[]) object;
						if (resourceData.length > 0)
						{
							_srcType = SystemDNDTransferRunnable.SRC_TYPE_ECLIPSE_RESOURCE;
							return true;
						}
					}
					else
					{
						FileTransfer fileTransfer = FileTransfer.getInstance();
						object = _clipboard.getContents(fileTransfer);

						if (object != null)
						{
							String[] fileData = (String[]) object;
							if (fileData.length > 0)
							{
								_srcType = SystemDNDTransferRunnable.SRC_TYPE_OS_RESOURCE;
								return true;
							}
						}
						else
						{
							TextTransfer textTransfer = TextTransfer.getInstance();
							object = _clipboard.getContents(textTransfer);

							if (object != null)
							{
								//String textData = (String) object;
								_srcType = SystemDNDTransferRunnable.SRC_TYPE_TEXT;
								return true;
							}
						}
					}
				}
			}
			catch (Exception e)
			{
			}
		}
		return false;
	}

	/**
	 * We override from parent to do unique checking...
	 * <p>
	 * We simply ensure every selected object is an IRemoteFile
	 * <p>
	 * @see SystemBaseAction#updateSelection(IStructuredSelection)
	 */
	public boolean updateSelection(IStructuredSelection selection)
	{
		if (hasSource())
		{
			boolean enable = true;
			Iterator e = ((IStructuredSelection) selection).iterator();
			while (enable && e.hasNext())
			{
				Object selectedObject = e.next();
				if (selectedObject instanceof IAdaptable)
				{
					IAdaptable adaptable = (IAdaptable) selectedObject;
					ISystemDragDropAdapter va = (ISystemDragDropAdapter) (adaptable.getAdapter(ISystemDragDropAdapter.class));
					if (va != null)
					{
						enable = va.canDrop(selectedObject);
						/* to allow disable of paste
						 * not sure if this is a performance hit or not
						if (enable)
						{
							SubSystem tgtSS = va.getSubSystem(selectedObject);
							List srcObjects = getClipboardObjects();						
							if (_srcType == SystemDNDTransferRunnable.SRC_TYPE_RSE_RESOURCE)
							{
								
								for (int i = 0; i < srcObjects.size() && enable; i++)
								{
									Object srcObject = srcObjects.get(i);
									ISystemDragDropAdapter srcAdapter = (ISystemDragDropAdapter)((IAdaptable)srcObject).getAdapter(ISystemDragDropAdapter.class);
									SubSystem srcSS = srcAdapter.getSubSystem(srcObject);
									boolean sameSystem = (srcSS == tgtSS);
									enable = va.validateDrop(srcObject, selectedObject, sameSystem);
								}
							}
							else if (_srcType == SystemDNDTransferRunnable.SRC_TYPE_ECLIPSE_RESOURCE)
							{
								for (int i = 0; i < srcObjects.size() && enable; i++)
								{
									Object srcObject = srcObjects.get(i);
									boolean sameSystem = false;
									enable = va.validateDrop(srcObject, selectedObject, sameSystem);
								}
							}
							else if (_srcType == SystemDNDTransferRunnable.SRC_TYPE_OS_RESOURCE)
							{
								for (int i = 0; i < srcObjects.size() && enable; i++)
								{
									Object srcObject = srcObjects.get(i);
									boolean sameSystem = false;
									enable = va.validateDrop(srcObject, selectedObject, sameSystem);
								}
							}
			
						}
						*/
					}
					else
					{
						enable = false;
					}
				}
				else
				{
					enable = false;
				}
			}
			if (enable)
			{
				_selection = selection.getFirstElement();
			}
			return enable;
		}
		else
		{
			return false;
		}

	}
	
	protected IRunnableContext getRunnableContext(Shell shell)
	{
		IRunnableContext irc = RSEUIPlugin.getTheSystemRegistry().getRunnableContext();
		if (irc != null)
		{
			return irc;
		}
		else
		{
			/*
			// for other cases, use statusbar
        	IWorkbenchWindow win = RSEUIPlugin.getActiveWorkbenchWindow();
        	if (win != null)
        	{
        		Shell winShell = RSEUIPlugin.getActiveWorkbenchShell();
               	if (winShell != null && !winShell.isDisposed() && winShell.isVisible())
        		{
        			RSEUIPlugin.logInfo("Using active workbench window as runnable context");
        			shell = winShell;
        			return win;	
        		}	
        		else
        		{
        			win = null;	
        		}
        	}	  
     */
      
			irc = new ProgressMonitorDialog(shell);
			RSEUIPlugin.getTheSystemRegistry().setRunnableContext(shell, irc);
			return irc;
		}
	}


}