/********************************************************************************
 * Copyright (c) 2006, 2007 IBM Corporation and others. All rights reserved.
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
 * Martin Oberhuber (Wind River) - [186773] split SystemRegistryUI from SystemRegistry implementation
 * Martin Oberhuber (Wind River) - [189123] Prepare ISystemRegistry for move into non-UI
 ********************************************************************************/
package org.eclipse.rse.ui.internal.model;

import java.util.ArrayList;
import java.util.List;
import java.util.Vector;

import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.Platform;
import org.eclipse.jface.operation.IRunnableContext;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.rse.core.IRSESystemType;
import org.eclipse.rse.core.RSECorePlugin;
import org.eclipse.rse.core.events.ISystemResourceChangeEvent;
import org.eclipse.rse.core.events.ISystemResourceChangeListener;
import org.eclipse.rse.core.model.IHost;
import org.eclipse.rse.core.model.ISystemProfile;
import org.eclipse.rse.core.model.ISystemRegistry;
import org.eclipse.rse.core.subsystems.ISubSystem;
import org.eclipse.rse.internal.ui.view.SystemDNDTransferRunnable;
import org.eclipse.rse.internal.ui.view.SystemPerspectiveHelpers;
import org.eclipse.rse.internal.ui.view.SystemView;
import org.eclipse.rse.internal.ui.view.SystemViewDataDropAdapter;
import org.eclipse.rse.services.clientserver.messages.SystemMessage;
import org.eclipse.rse.services.clientserver.messages.SystemMessageException;
import org.eclipse.rse.ui.ISystemMessages;
import org.eclipse.rse.ui.RSESystemTypeAdapter;
import org.eclipse.rse.ui.RSEUIPlugin;
import org.eclipse.rse.ui.model.ISystemRegistryUI;
import org.eclipse.rse.ui.view.ISystemViewInputProvider;
import org.eclipse.swt.dnd.Clipboard;
import org.eclipse.swt.dnd.FileTransfer;
import org.eclipse.swt.dnd.TextTransfer;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.part.PluginTransfer;
import org.eclipse.ui.part.PluginTransferData;
import org.eclipse.ui.part.ResourceTransfer;

/**
 * Registry and control center for RSE UI related operations.
 * 
 * This class is not intended to be subclassed by clients.
 */
public class SystemRegistryUI implements ISystemRegistryUI, ISystemViewInputProvider {

	private static SystemRegistryUI _instance = null;
	private SystemRegistry registry = null;

	private Viewer viewer = null;
	
	// progress monitor support
	private IRunnableContext currentRunnableContext;
	private Shell currentRunnableContextShell;
	private Vector previousRunnableContexts = new Vector();
	private Vector previousRunnableContextShells = new Vector();

	private Clipboard clipboard = null;
	private SystemScratchpad scratchpad = null;

	/**
	 * Constructor.
	 * This is protected as the singleton instance should be retrieved by
	 * calling @link{#getInstance()}.
	 * @param logfilePath Root folder. Where to place the log file. 
	 */
	protected SystemRegistryUI(String logfilePath)
	{
		super();
		registry = SystemRegistry.getInstance();
	}

	// ----------------------------
	// PUBLIC STATIC METHODS...
	// ----------------------------

	/**
	 * Return singleton instance. Must be used on first instantiate.
	 * @param logfilePath Root folder. Where to place the log file.
	 * @return the singleton SystemRegistryUI instance.
	 */
	public static SystemRegistryUI getInstance(String logfilePath)
	{
		if (_instance == null)
			_instance = new SystemRegistryUI(logfilePath);
		return _instance;
	}
	
	/**
	 * Return singleton instance assuming it already exists.
	 * @return the singleton SystemRegistryUI instance.
	 */
	public static SystemRegistryUI getInstance()
	{
		return _instance;
	}

	// ----------------------------------
	// UI METHODS...
	// ----------------------------------

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.rse.ui.model.ISystemRegistryUI#showRSEPerspective()
	 */
	public void showRSEPerspective()
	{
		SystemPerspectiveHelpers.openRSEPerspective();
	}
	
	/*
	 * (non-Javadoc)
	 * @see org.eclipse.rse.ui.model.ISystemRegistryUI#expandHost(org.eclipse.rse.core.model.IHost)
	 */
	public void expandHost(IHost conn)
	{
		if (SystemPerspectiveHelpers.isRSEPerspectiveActive())
		{
			// find the RSE tree view
			SystemView rseView = SystemPerspectiveHelpers.findRSEView();
			if (rseView != null)
			{
				// find and expand the given connection
				rseView.setExpandedState(conn, true); // expand this connection
				rseView.setSelection(new StructuredSelection(conn));
			}
		}
	}
	
	/*
	 * (non-Javadoc)
	 * @see org.eclipse.rse.ui.model.ISystemRegistryUI#expandSubSystem(org.eclipse.rse.core.subsystems.ISubSystem)
	 */
	public void expandSubSystem(ISubSystem subsystem)
	{
		if (SystemPerspectiveHelpers.isRSEPerspectiveActive())
		{
			// find the RSE tree view
			SystemView rseView = SystemPerspectiveHelpers.findRSEView();
			if (rseView != null)
			{
				// find and expand the given subsystem's connection, and then subsystem
				rseView.setExpandedState(subsystem.getHost(), true); // expand this connection
				rseView.setExpandedState(subsystem, true);
				rseView.setSelection(new StructuredSelection(subsystem));
			}
		}
	}

	// ----------------------------------
	// SYSTEMVIEWINPUTPROVIDER METHODS...
	// ----------------------------------
	
	/**
	 * Return the children objects to constitute the root elements in the system view tree.
	 * We return all connections for all active profiles.
	 */
	public Object[] getSystemViewRoots()
	{
		//DKM - only return enabled connections now
		IHost[] connections = registry.getHosts();
		List result = new ArrayList(); 
		for (int i = 0; i < connections.length; i++) {
			IHost con = connections[i];
			IRSESystemType sysType = con.getSystemType();
			if (sysType != null) { // sysType can be null if workspace contains a host that is no longer defined by the workbench
				RSESystemTypeAdapter adapter = (RSESystemTypeAdapter)(sysType.getAdapter(RSESystemTypeAdapter.class));
				// Note: System types without registered subsystems get disabled by the adapter itself!
				//       There is no need to re-check this here again.
				if (adapter.isEnabled(sysType)) result.add(con);
			}
		}
		return result.toArray();
	}
	
	/**
	 * Return true if {@link #getSystemViewRoots()} will return a non-empty list
	 * We return true if there are any connections for any active profile.
	 */
	public boolean hasSystemViewRoots()
	{
		return (registry.getHostCount() > 0);
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.rse.ui.view.ISystemViewInputProvider#getConnectionChildren(org.eclipse.rse.core.model.IHost)
	 */
	public Object[] getConnectionChildren(IHost selectedConnection)
	{
		return registry.getConnectionChildren(selectedConnection);
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.rse.ui.view.ISystemViewInputProvider#hasConnectionChildren(org.eclipse.rse.core.model.IHost)
	 */
	public boolean hasConnectionChildren(IHost selectedConnection)
	{
		return registry.hasConnectionChildren(selectedConnection);
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.core.runtime.IAdaptable#getAdapter(java.lang.Class)
	 */
	public Object getAdapter(Class adapterType)
	{
		return Platform.getAdapterManager().getAdapter(this, adapterType);
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.rse.ui.view.ISystemViewInputProvider#setShell(org.eclipse.swt.widgets.Shell)
	 */
	public void setShell(Shell shell)
	{
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.rse.ui.model.ISystemShellProvider#getShell()
	 */
	public Shell getShell()
	{
		// thread safe shell
		IWorkbench workbench = RSEUIPlugin.getDefault().getWorkbench();
		if (workbench != null)
		{
			// first try to get the active workbench window
			IWorkbenchWindow ww = workbench.getActiveWorkbenchWindow();
			if (ww == null) // no active window so just get the first one
				ww = workbench.getWorkbenchWindows()[0];
			if (ww != null)
			{
				Shell shell = ww.getShell();
				if (!shell.isDisposed())
				{
					return shell;
				}
			}
		}
		return null;
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.rse.ui.view.ISystemViewInputProvider#setViewer(org.eclipse.jface.viewers.Viewer)
	 */
	public void setViewer(Viewer viewer)
	{
		this.viewer = viewer;
	}
	
	/*
	 * (non-Javadoc)
	 * @see org.eclipse.rse.ui.view.ISystemViewInputProvider#getViewer()
	 */
	public Viewer getViewer()
	{
		return viewer;
	}

	/**
	 * Return true if we are listing connections or not, so we know whether
	 * we are interested in connection-add events
	 */
	public boolean showingConnections()
	{
		return true;
	}

	// ----------------------------------
	// ACTIVE PROGRESS MONITOR METHODS...
	// ----------------------------------

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.rse.ui.model.ISystemRegistryUI#setRunnableContext(org.eclipse.swt.widgets.Shell, org.eclipse.jface.operation.IRunnableContext)
	 */
	public void setRunnableContext(Shell shell, IRunnableContext context)
	{
		//this.currentRunnableContext = context;
		//this.currentRunnableContextShell = shell;
		pushRunnableContext(shell, context);
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.rse.ui.model.ISystemRegistryUI#clearRunnableContext()
	 */
	public void clearRunnableContext()
	{
		//this.currentRunnableContext = null;
		//this.currentRunnableContextShell = null;    	
		popRunnableContext();
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.rse.ui.model.ISystemRegistryUI#getRunnableContext()
	 */
	public IRunnableContext getRunnableContext()
	{
		if ((currentRunnableContextShell != null) && currentRunnableContextShell.isDisposed())
			clearRunnableContext();
		if (currentRunnableContext != null)
			return currentRunnableContext;
		else
			return null;
	}

	private IRunnableContext popRunnableContext()
	{
		Shell shell = null;
		boolean found = false;
		Vector disposedShells = new Vector();
		Vector disposedContexts = new Vector();
		for (int idx = previousRunnableContextShells.size() - 1; !found && (idx >= 0); idx--)
		{
			shell = (Shell) previousRunnableContextShells.elementAt(idx);
			if ((shell == currentRunnableContextShell) || shell.isDisposed())
			{
				disposedShells.add(shell);
				disposedContexts.add(previousRunnableContexts.elementAt(idx));
			}
			else
			{
				found = true;
				currentRunnableContextShell = shell;
				currentRunnableContext = (IRunnableContext) previousRunnableContexts.elementAt(idx);
			}
		}
		if (!found)
		{
			currentRunnableContextShell = null;
			currentRunnableContext = null;
		}
		for (int idx = 0; idx < disposedShells.size(); idx++)
		{
			previousRunnableContextShells.remove(disposedShells.elementAt(idx));
			previousRunnableContexts.remove(disposedContexts.elementAt(idx));
		}
	
		return currentRunnableContext;
	}

	private IRunnableContext pushRunnableContext(Shell shell, IRunnableContext context)
	{
		previousRunnableContexts.addElement(context);
		previousRunnableContextShells.addElement(shell);
		currentRunnableContextShell = shell;
		currentRunnableContext = context;
		return currentRunnableContext;
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.rse.ui.model.ISystemRegistryUI#getSystemClipboard()
	 */
	public Clipboard getSystemClipboard()
	{
		if (clipboard == null)
		{
			Display display = null;
			Shell shell = getShell();
			if (shell == null)
			{
				display = Display.getDefault();
			}
			else
			{
				display = shell.getDisplay();
			}
			clipboard = new Clipboard(display);
		}
	
		return clipboard;
	}

	/**
	 * Method for decoding an source object ID to the actual source object.
	 * We determine the profile, connection and subsystem, and then
	 * we use the SubSystem.getObjectWithKey() method to get at the
	 * object.
	 */
	private Object getObjectFor(String str)
	{
		// first extract subsystem id
		int connectionDelim = str.indexOf(":"); //$NON-NLS-1$
		if (connectionDelim == -1) // not subsystem, therefore likely to be a connection
		{
		    int profileDelim = str.indexOf("."); //$NON-NLS-1$
			if (profileDelim != -1) 
			{
			    String profileId = str.substring(0, profileDelim);
			    String connectionId = str.substring(profileDelim + 1, str.length());
			    ISystemProfile profile = registry.getSystemProfile(profileId);
			    return registry.getHost(profile, connectionId);
			}
		}
		
		int subsystemDelim = str.indexOf(":", connectionDelim + 1); //$NON-NLS-1$
	
		String subSystemId = str.substring(0, subsystemDelim);
		String srcKey = str.substring(subsystemDelim + 1, str.length());
	
		ISystemRegistry registry = RSECorePlugin.getTheSystemRegistry();
		ISubSystem subSystem = registry.getSubSystem(subSystemId);
		if (subSystem != null)
		{
			Object result = null;
			try
			{
				result = subSystem.getObjectWithAbsoluteName(srcKey);
			}
			catch (SystemMessageException e)
			{
				return e.getSystemMessage();
			}
			catch (Exception e)
			{
			}
			if (result != null)
			{
				return result;
			}
			else
			{
				SystemMessage msg = RSEUIPlugin.getPluginMessage(ISystemMessages.MSG_ERROR_FILE_NOTFOUND);
				msg.makeSubstitution(srcKey, subSystem.getHostAliasName());
				return msg;
			}
		}
		else
		{
			SystemMessage msg = RSEUIPlugin.getPluginMessage(ISystemMessages.MSG_ERROR_CONNECTION_NOTFOUND);
			msg.makeSubstitution(subSystemId);
			return msg;
		}
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.rse.core.model.ISystemRegistry#getSystemClipboardObjects(int)
	 */
	public List getSystemClipboardObjects(int srcType)
	{
		Clipboard clipboard = getSystemClipboard();
		ArrayList srcObjects = new ArrayList();
		Object object = null;
	
		if (srcType == SystemDNDTransferRunnable.SRC_TYPE_RSE_RESOURCE)
		{
	
			// determine the source objects
			object = clipboard.getContents(PluginTransfer.getInstance());
	
			if (object instanceof PluginTransferData)
			{
				// RSE transfer
				PluginTransferData data = (PluginTransferData) object;
				byte[] result = data.getData();
	
				//StringTokenizer tokenizer = new StringTokenizer(new String(result), SystemViewDataDropAdapter.RESOURCE_SEPARATOR);
				String[] tokens = (new String(result)).split("\\"+SystemViewDataDropAdapter.RESOURCE_SEPARATOR); //$NON-NLS-1$
				
				for (int i = 0;i < tokens.length; i++)
				{
					String srcStr = tokens[i];
	
					Object srcObject = getObjectFor(srcStr);
					srcObjects.add(srcObject);
				}
			}
		}
		else if (srcType == SystemDNDTransferRunnable.SRC_TYPE_ECLIPSE_RESOURCE)
		{
			// Resource transfer
			ResourceTransfer resTransfer = ResourceTransfer.getInstance();
			object = clipboard.getContents(resTransfer);
			if (object != null)
			{
				IResource[] resourceData = (IResource[]) object;
				for (int i = 0; i < resourceData.length; i++)
				{
					srcObjects.add(resourceData[i]);
				}
			}
		}
	
		else if (srcType == SystemDNDTransferRunnable.SRC_TYPE_OS_RESOURCE)
		{
			// Local File transfer
			FileTransfer fileTransfer = FileTransfer.getInstance();
			object = clipboard.getContents(fileTransfer);
			if (object != null)
			{
				String[] fileData = (String[]) object;
				{
					for (int i = 0; i < fileData.length; i++)
					{
						srcObjects.add(fileData[i]);
					}
				}
			}
		}
		else if (srcType == SystemDNDTransferRunnable.SRC_TYPE_TEXT)
		{
			TextTransfer textTransfer = TextTransfer.getInstance();
			object = clipboard.getContents(textTransfer);
			if (object != null)
			{
				String textData = (String) object;
				srcObjects.add(textData);
			}
		}
		return srcObjects;
	}

	/**
	 * Returns the remote systems scratchpad root
	 */
	public SystemScratchpad getSystemScratchPad()
	{
	    if (scratchpad == null)
	    {
	        scratchpad = new SystemScratchpad();
	    }
	    return scratchpad;
	}

	// ----------------------------
	// RESOURCE EVENT METHODS...
	// ----------------------------            

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.rse.ui.model.ISystemRegistryUI#postEvent(org.eclipse.rse.core.events.ISystemResourceChangeEvent)
	 */
	public void postEvent(ISystemResourceChangeEvent event)
	{
		registry.getResourceChangeManager().postNotify(event);
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.rse.ui.model.ISystemRegistryUI#postEvent(org.eclipse.rse.core.events.ISystemResourceChangeListener, org.eclipse.rse.core.events.ISystemResourceChangeEvent)
	 */
	public void postEvent(ISystemResourceChangeListener listener, ISystemResourceChangeEvent event)
	{
		new SystemPostableEventNotifier(listener, event); // create and run the notifier
	}

}
