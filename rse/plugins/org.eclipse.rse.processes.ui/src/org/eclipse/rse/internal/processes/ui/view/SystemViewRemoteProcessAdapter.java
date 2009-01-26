/*******************************************************************************
 * Copyright (c) 2006, 2009 IBM Corporation and others.
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
 * Martin Oberhuber (Wind River) - [168975] Move RSE Events API to Core
 * Martin Oberhuber (Wind River) - [182454] improve getAbsoluteName() documentation
 * Martin Oberhuber (Wind River) - [186128] Move IProgressMonitor last in all API
 * Martin Oberhuber (Wind River) - [186773] split ISystemRegistryUI from ISystemRegistry
 * David McKnight   (IBM)        - [216252] [api][nls] Resource Strings specific to subsystems should be moved from rse.ui into files.ui / shells.ui / processes.ui where possible
 * David McKnight   (IBM)        - [220547] [api][breaking] SimpleSystemMessage needs to specify a message id and some messages should be shared
 * Xuan Chen        (IBM)        - [223126] [api][breaking] Remove API related to User Actions in RSE Core/UI
 *******************************************************************************/

package org.eclipse.rse.internal.processes.ui.view;


import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.rse.core.model.ISystemMessageObject;
import org.eclipse.rse.core.model.ISystemResourceSet;
import org.eclipse.rse.core.model.SystemMessageObject;
import org.eclipse.rse.core.model.SystemRemoteResourceSet;
import org.eclipse.rse.core.subsystems.ISubSystem;
import org.eclipse.rse.internal.processes.ui.ProcessesPlugin;
import org.eclipse.rse.internal.processes.ui.actions.SystemKillProcessAction;
import org.eclipse.rse.services.clientserver.messages.CommonMessages;
import org.eclipse.rse.services.clientserver.messages.ICommonMessageIds;
import org.eclipse.rse.services.clientserver.messages.SimpleSystemMessage;
import org.eclipse.rse.services.clientserver.messages.SystemMessage;
import org.eclipse.rse.services.clientserver.processes.IHostProcessFilter;
import org.eclipse.rse.services.clientserver.processes.ISystemProcessRemoteConstants;
import org.eclipse.rse.services.clientserver.processes.ISystemProcessRemoteTypes;
import org.eclipse.rse.subsystems.processes.core.subsystem.IRemoteProcess;
import org.eclipse.rse.subsystems.processes.core.subsystem.IRemoteProcessSubSystem;
import org.eclipse.rse.ui.ISystemContextMenuConstants;
import org.eclipse.rse.ui.RSEUIPlugin;
import org.eclipse.rse.ui.SystemBasePlugin;
import org.eclipse.rse.ui.SystemMenuManager;
import org.eclipse.rse.ui.actions.SystemCopyToClipboardAction;
import org.eclipse.rse.ui.view.AbstractSystemViewAdapter;
import org.eclipse.rse.ui.view.ISystemRemoteElementAdapter;
import org.eclipse.swt.dnd.Clipboard;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.views.properties.IPropertyDescriptor;
import org.eclipse.ui.views.properties.PropertyDescriptor;


public class SystemViewRemoteProcessAdapter extends AbstractSystemViewAdapter
 implements ISystemRemoteElementAdapter
{
	private SystemCopyToClipboardAction copyClipboardAction;
	public boolean canDrag(Object element)
	{
		// DKM - this is just for copy
		return true;
	}

	public boolean canDrag(SystemRemoteResourceSet elements)
	{
		// DKM - this is just for copy
		return true;
	}

	public Object doDrag(Object element, boolean sameSystemType, IProgressMonitor monitor)
	{
		return getText(element);
	}

	public ISystemResourceSet doDrag(SystemRemoteResourceSet set, IProgressMonitor monitor)
	{
		return set;
	}

	private static final Object[] EMPTY_LIST = new Object[0];
	private static PropertyDescriptor[] propertyDescriptorArray = null;
	private SystemKillProcessAction killProcessAction;

	public void addActions(SystemMenuManager menu,
			IStructuredSelection selection, Shell parent, String menuGroup)
	{
		if (killProcessAction == null)
			killProcessAction = new SystemKillProcessAction(shell);
        menu.add(ISystemContextMenuConstants.GROUP_CHANGE, killProcessAction);


        if (copyClipboardAction == null)
		{
        	Clipboard clipboard = RSEUIPlugin.getTheSystemRegistryUI().getSystemClipboard();
			copyClipboardAction = new SystemCopyToClipboardAction(shell, clipboard);
		}
        menu.add(menuGroup, copyClipboardAction);
	}

	public ISubSystem getSubSystem(Object element)
	{
		if (element instanceof IRemoteProcess)
		{
			IRemoteProcess process = (IRemoteProcess)element;
			return process.getParentRemoteProcessSubSystem();
		}
		return super.getSubSystem(element);
	}

	public ImageDescriptor getImageDescriptor(Object element)
	{
		//return RSEUIPlugin.getDefault().getImageDescriptor(ISystemIconConstants.ICON_SYSTEM_PROCESS_ID);
		return ProcessesPlugin.getDefault().getImageDescriptorFromPath("icons/full/obj16/activeprocess_obj.gif"); //$NON-NLS-1$
	}

	public String getText(Object element)
	{
		String text = ((IRemoteProcess) element).getLabel();
		return (text == null) ? "" : text; //$NON-NLS-1$
	}

	/**
	 * Used for stuff like clipboard text copy
	 */
	public String getAlternateText(Object element)
	{
		IRemoteProcess process = (IRemoteProcess)element;
		String allProperties = process.getAllProperties();
		return allProperties.replace('|', '\t');
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.rse.core.subsystems.IRemoteObjectIdentifier#getAbsoluteName(java.lang.Object)
	 */
	public String getAbsoluteName(Object object)
	{
		IRemoteProcess process = (IRemoteProcess) object;
		return "" + process.getPid(); //$NON-NLS-1$
	}

	public String getType(Object element)
	{
		IRemoteProcess process = (IRemoteProcess) element;
		if (process.isRoot())
			return SystemProcessesViewResources.RESID_PROPERTY_PROCESS_TYPE_ROOT;
		else return SystemProcessesViewResources.RESID_PROPERTY_PROCESS_TYPE;
	}

	public Object getParent(Object element)
	{
		IRemoteProcess process = (IRemoteProcess) element;
		IRemoteProcess parent = process.getParentRemoteProcess();
		if ((parent != null) && parent.getAbsolutePath().equals(process.getAbsolutePath()))
			// should never happen but sometimes it does, leading to infinite loop.
			parent = null;
		return parent;
	}

	public boolean hasChildren(IAdaptable element)
	{
		// TODO Auto-generated method stub
		return false;
	}

	public Object[] getChildren(IAdaptable element, IProgressMonitor monitor)
	{
		IRemoteProcess process = (IRemoteProcess) element;
		IRemoteProcessSubSystem ss = process.getParentRemoteProcessSubSystem();
		IHostProcessFilter orgRpfs = process.getFilterString();

		Object[] children = null;

		try
		{
			children = ss.listAllProcesses(orgRpfs, process.getContext(), null);
			if ((children == null) || (children.length == 0))
			{
				children = EMPTY_LIST;
			}
		}
		/*catch (InterruptedException exc)
		{
			children = new SystemMessageObject[1];
			children[0] = new SystemMessageObject(RSEUIPlugin.getPluginMessage(MSG_EXPAND_CANCELLED), ISystemMessageObject.MSGTYPE_CANCEL, element);
		}*/
		catch (Exception exc)
		{
			children = new SystemMessageObject[1];
			SystemMessage msg = new SimpleSystemMessage(ProcessesPlugin.PLUGIN_ID,
					ICommonMessageIds.MSG_EXPAND_FAILED,
					IStatus.ERROR,
					CommonMessages.MSG_EXPAND_FAILED);
			children[0] = new SystemMessageObject(msg, ISystemMessageObject.MSGTYPE_ERROR, element);
			SystemBasePlugin.logError("Exception resolving file filter strings", exc); //$NON-NLS-1$
		}
		return children;
	}

	protected IPropertyDescriptor[] internalGetPropertyDescriptors()
	{

		if (propertyDescriptorArray == null)
		{
			int nbrOfProperties = ISystemProcessRemoteConstants.PROCESS_ATTRIBUTES_COUNT;

			propertyDescriptorArray = new PropertyDescriptor[nbrOfProperties];

			int idx = -1;

			// pid
			propertyDescriptorArray[++idx] = createSimplePropertyDescriptor(ISystemProcessPropertyConstants.P_PROCESS_PID, SystemProcessesViewResources.RESID_PROPERTY_PROCESS_PID_LABEL, SystemProcessesViewResources.RESID_PROPERTY_PROCESS_PID_TOOLTIP);

			// name
			propertyDescriptorArray[++idx] = createSimplePropertyDescriptor(ISystemProcessPropertyConstants.P_PROCESS_NAME, SystemProcessesViewResources.RESID_PROPERTY_PROCESS_NAME_LABEL, SystemProcessesViewResources.RESID_PROPERTY_PROCESS_NAME_TOOLTIP);

			// state
			propertyDescriptorArray[++idx] = createSimplePropertyDescriptor(ISystemProcessPropertyConstants.P_PROCESS_STATE, SystemProcessesViewResources.RESID_PROPERTY_PROCESS_STATE_LABEL, SystemProcessesViewResources.RESID_PROPERTY_PROCESS_STATE_TOOLTIP);

			// uid
			propertyDescriptorArray[++idx] = createSimplePropertyDescriptor(ISystemProcessPropertyConstants.P_PROCESS_UID, SystemProcessesViewResources.RESID_PROPERTY_PROCESS_UID_LABEL, SystemProcessesViewResources.RESID_PROPERTY_PROCESS_UID_TOOLTIP);

			// username
			propertyDescriptorArray[++idx] = createSimplePropertyDescriptor(ISystemProcessPropertyConstants.P_PROCESS_USERNAME, SystemProcessesViewResources.RESID_PROPERTY_PROCESS_USERNAME_LABEL, SystemProcessesViewResources.RESID_PROPERTY_PROCESS_USERNAME_TOOLTIP);

			// ppid
			propertyDescriptorArray[++idx] = createSimplePropertyDescriptor(ISystemProcessPropertyConstants.P_PROCESS_PPID, SystemProcessesViewResources.RESID_PROPERTY_PROCESS_PPID_LABEL, SystemProcessesViewResources.RESID_PROPERTY_PROCESS_PPID_TOOLTIP);

			// gid
			propertyDescriptorArray[++idx] = createSimplePropertyDescriptor(ISystemProcessPropertyConstants.P_PROCESS_GID, SystemProcessesViewResources.RESID_PROPERTY_PROCESS_GID_LABEL, SystemProcessesViewResources.RESID_PROPERTY_PROCESS_GID_TOOLTIP);

			// tgid
			propertyDescriptorArray[++idx] = createSimplePropertyDescriptor(ISystemProcessPropertyConstants.P_PROCESS_TGID, SystemProcessesViewResources.RESID_PROPERTY_PROCESS_TGID_LABEL, SystemProcessesViewResources.RESID_PROPERTY_PROCESS_TGID_TOOLTIP);

			// tracerpid
			propertyDescriptorArray[++idx] = createSimplePropertyDescriptor(ISystemProcessPropertyConstants.P_PROCESS_TRACERPID, SystemProcessesViewResources.RESID_PROPERTY_PROCESS_TRACERPID_LABEL, SystemProcessesViewResources.RESID_PROPERTY_PROCESS_TRACERPID_TOOLTIP);

			// virtual memory size
			propertyDescriptorArray[++idx] = createSimplePropertyDescriptor(ISystemProcessPropertyConstants.P_PROCESS_VMSIZE, SystemProcessesViewResources.RESID_PROPERTY_PROCESS_VMSIZE_LABEL, SystemProcessesViewResources.RESID_PROPERTY_PROCESS_VMSIZE_TOOLTIP);

			// virtual memory rss
			propertyDescriptorArray[++idx] = createSimplePropertyDescriptor(ISystemProcessPropertyConstants.P_PROCESS_VMRSS, SystemProcessesViewResources.RESID_PROPERTY_PROCESS_VMRSS_LABEL, SystemProcessesViewResources.RESID_PROPERTY_PROCESS_VMRSS_TOOLTIP);

		}
		return propertyDescriptorArray;
	}

	/**
	 * Returns the current value for the named property.
	 * @return the current value of the given property
	 */
	protected Object internalGetPropertyValue(Object key)
	{
		return getPropertyValue(key, true);
	}

	/**
	 * Returns the current value for the named property.
	 *
	 * @param property the name or key of the property as named by its property descriptor
	 * @param formatted indication of whether to return the value in formatted or raw form
	 * @return the current value of the given property
	 */
	public Object getPropertyValue(Object property, boolean formatted)
	{
		String name = (String) property;
		IRemoteProcess process = (IRemoteProcess) propertySourceInput;

		if (name.equals(ISystemProcessPropertyConstants.P_PROCESS_GID))
		{
			if (formatted)
			{
				return "" + process.getGid(); //$NON-NLS-1$
			}
			else
			{
				return new Long(process.getGid());
			}
		}
		if (name.equals(ISystemProcessPropertyConstants.P_PROCESS_NAME))
		{
			return process.getName();
		}
		if (name.equals(ISystemProcessPropertyConstants.P_PROCESS_PID))
		{
			if (formatted)
			{
				return "" + process.getPid(); //$NON-NLS-1$
			}
			else
			{
				return new Long(process.getPid());
			}
		}
		if (name.equals(ISystemProcessPropertyConstants.P_PROCESS_PPID))
		{
			if (formatted)
			{
				return "" + process.getPPid(); //$NON-NLS-1$
			}
			else
			{
				return new Long(process.getPPid());
			}
		}
		if (name.equals(ISystemProcessPropertyConstants.P_PROCESS_STATE))
		{
			if (formatted)
			{
				return formatState(process.getState());
			}
			else
			{
				return process.getState();
			}
		}
		if (name.equals(ISystemProcessPropertyConstants.P_PROCESS_TGID))
		{
			if (formatted)
			{
				return "" + process.getTgid(); //$NON-NLS-1$
			}
			else
			{
				return new Long(process.getTgid());
			}
		}
		if (name.equals(ISystemProcessPropertyConstants.P_PROCESS_TRACERPID))
		{
			if (formatted)
			{
				return "" + process.getTracerPid(); //$NON-NLS-1$
			}
			else
			{
				return new Long(process.getTracerPid());
			}
		}
		if (name.equals(ISystemProcessPropertyConstants.P_PROCESS_UID))
		{
			if (formatted)
			{
				return "" + process.getUid(); //$NON-NLS-1$
			}
			else
			{
				return new Long(process.getUid());
			}
		}
		if (name.equals(ISystemProcessPropertyConstants.P_PROCESS_USERNAME))
		{
			return process.getUsername();
		}
		if (name.equals(ISystemProcessPropertyConstants.P_PROCESS_VMSIZE))
		{
			if (formatted)
			{
				return sub(SystemProcessesViewResources.RESID_PROPERTY_PROCESS_VMSIZE_VALUE, MSG_SUB1, Long.toString(process.getVmSizeInKB()));
			}
			else
			{
				return new Long(process.getVmSizeInKB());
			}
		}
		if (name.equals(ISystemProcessPropertyConstants.P_PROCESS_VMRSS))
		{
			if (formatted)
			{
				return sub(SystemProcessesViewResources.RESID_PROPERTY_PROCESS_VMRSS_VALUE, MSG_SUB1, Long.toString(process.getVmRSSInKB()));
			}
			else
			{
				return new Long(process.getVmRSSInKB());
			}
		}
		else
			return null; //super.getPropertyValue(name);
	}

	protected String formatState(String state)
	{
		if (state == null) return ""; //$NON-NLS-1$
		state = state.trim();
		String longState = ""; //$NON-NLS-1$
		String[] allStates = state.split(","); //$NON-NLS-1$
		if (allStates == null) return longState;

		SystemProcessStatesContentProvider zstates = new SystemProcessStatesContentProvider();
		for (int i = 0; i < allStates.length; i++)
		{
			longState = longState + allStates[i].charAt(0) + "-" + zstates.getStateString(allStates[i]); //$NON-NLS-1$
			if (i < allStates.length - 1)
				longState = longState + ", "; //$NON-NLS-1$
		}
		return longState;
	}

	/**
	 * Return fully qualified name that uniquely identifies this remote object's remote parent within its subsystem
	 */
	public String getAbsoluteParentName(Object element)
	{
		IRemoteProcess process = (IRemoteProcess) element;
		IRemoteProcess parent = process.getParentRemoteProcess();
		if (parent != null) return parent.getAbsolutePath();
		else return "/proc/0"; //$NON-NLS-1$
	}

	/**
	 * Given a remote object, returns it remote parent object. Eg, given a process, return the process that
	 * spawned it.
	 * <p>
	 * The shell is required in order to set the cursor to a busy state if a remote trip is required.
	 *
	 * @return an IRemoteProcess object for the parent
	 */
	public Object getRemoteParent(Object element, IProgressMonitor monitor) throws Exception
	{
		return ((IRemoteProcess) element).getParentRemoteProcess();
	}

	/**
	 * Given a remote object, return the unqualified names of the objects contained in that parent. This is
	 *  used for testing for uniqueness on a rename operation, for example. Sometimes, it is not
	 *  enough to just enumerate all the objects in the parent for this purpose, because duplicate
	 *  names are allowed if the types are different, such as on iSeries. In this case return only
	 *  the names which should be used to do name-uniqueness validation on a rename operation.
	 *
	 * @return an array of all file and folder names in the parent of the given IRemoteFile object
	 */
	public String[] getRemoteParentNamesInUse(Object element, IProgressMonitor monitor) throws Exception
	{
		String[] pids = EMPTY_STRING_LIST;

		IRemoteProcess process = (IRemoteProcess) element;
		String parentName = "" + process.getPPid(); //$NON-NLS-1$
		if (parentName.equals("-1")) // given a root? //$NON-NLS-1$
			return pids; // not much we can do. Should never happen: you can't rename a root!

		Object[] children = getChildren(process.getParentRemoteProcess());
		if ((children == null) || (children.length == 0))
			return pids;

		pids = new String[children.length];
		for (int idx = 0; idx < pids.length; idx++)
			pids[idx] = "" + ((IRemoteProcess) children[idx]).getPid(); //$NON-NLS-1$

		return pids;
	}

	public String getRemoteSubType(Object element)
	{
		return null;
	}

	public String getRemoteType(Object element)
	{
		IRemoteProcess process = (IRemoteProcess) element;
		if (process.isRoot())
			return ISystemProcessRemoteTypes.TYPE_ROOT;
		else
			return ISystemProcessRemoteTypes.TYPE_PROCESS;
	}

	public String getRemoteTypeCategory(Object element)
	{
		return ISystemProcessRemoteTypes.TYPECATEGORY;
	}

	/**
	 * Return the subsystem factory id that owns this remote object
	 * The value must not be translated, so that property pages registered via xml can subset by it.
	 */
	public String getSubSystemConfigurationId(Object element)
	{
		IRemoteProcess process = (IRemoteProcess) element;
		return process.getParentRemoteProcessSubSystem().getSubSystemConfiguration().getId();
	}

	public boolean refreshRemoteObject(Object oldElement, Object newElement)
	{
		/*if (oldElement instanceof IRemoteProcess)
		{
			IRemoteProcess oldProcess = (IRemoteProcess) oldElement;
			IRemoteProcess newProcess = (IRemoteProcess) newElement;
			oldProcess.getParentRemoteProcessSubSystem().setAllProperties(newProcess.getAllProperties());
			return hasChildren(oldElement);
		}*/
		return false;
	}

	/*
	 * Return whether deferred queries are supported.
	 */
	public boolean supportsDeferredQueries(ISubSystem subSys)
	{
	    return true;
	}
}
