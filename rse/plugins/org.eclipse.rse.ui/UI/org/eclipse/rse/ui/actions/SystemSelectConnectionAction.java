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

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.rse.model.IHost;
import org.eclipse.rse.ui.SystemResources;
import org.eclipse.rse.ui.dialogs.SystemSelectConnectionDialog;
import org.eclipse.swt.widgets.Shell;


/**
 * Use this action to put up a dialog allowing users to select one or
 *  more connections.
 */
public class SystemSelectConnectionAction extends SystemBaseDialogAction
{
	private boolean multiSelect;
	private boolean showPropertySheetInitialState;
	private boolean showPropertySheet;
	private String message;
	private boolean showNewConnectionPrompt = true;
	private String[] systemTypes;
	private String   systemType;
	private IHost defaultConn;
	private Object result;

	/**
	 * Constructor
	 */
	public SystemSelectConnectionAction(Shell shell)
	{
		super(SystemResources.ACTION_SELECTCONNECTION_LABEL, SystemResources.ACTION_SELECTCONNECTION_TOOLTIP,null,  shell);
	}

	/**
	 * Set the connection to default the selection to
	 */
	public void setDefaultConnection(IHost conn)
	{
		this.defaultConn = conn;
	}
	/**
	 * Restrict to certain system types
	 * @param systemTypes the system types to restrict what connections are shown and what types of connections
	 *  the user can create
	 * @see org.eclipse.rse.core.ISystemTypes
	 */
	public void setSystemTypes(String[] systemTypes)
	{
		this.systemTypes = systemTypes;
	}
	/**
	 * Restrict to a certain system type
	 * @param systemType the system type to restrict what connections are shown and what types of connections
	 *  the user can create
	 * @see org.eclipse.rse.core.ISystemTypes
	 */
	public void setSystemType(String systemType)
	{
		this.systemType = systemType;
	}
	/**
	 * Set to true/false if a "New Connection..." special connection is to be shown for creating new connections.
	 * Defaault is true.
	 */
	public void setShowNewConnectionPrompt(boolean show)
	{
		this.showNewConnectionPrompt = show;
	}
	/**
	 * Set the label text shown at the top of the dialog
	 */
	public void setInstructionLabel(String message)
	{
		this.message = message;
	}

	/**
	 * Show the property sheet on the right hand side, to show the properties of the
	 * selected object.
	 * <p>
	 * This overload always shows the property sheet
	 * <p>
	 * Default is false
	 */
	public void setShowPropertySheet(boolean show)
	{
		this.showPropertySheet = show;
	}
	/**
	 * Show the property sheet on the right hand side, to show the properties of the
	 * selected object.
	 * <p>
	 * This overload shows a Details>>> button so the user can decide if they want to see the
	 * property sheet. 
	 * <p>
	 * @param show True if show the property sheet within the dialog
	 * @param initialState True if the property is to be initially displayed, false if it is not
	 *  to be displayed until the user presses the Details button.
	 */
	public void setShowPropertySheet(boolean show, boolean initialState)
	{
		this.showPropertySheet = show;
		this.showPropertySheetInitialState = initialState;
	}

	/**
	 * Set multiple selection mode. Default is single selection mode
	 * <p>
	 * If you turn on multiple selection mode, you must use the getSelectedObjects()
	 *  method to retrieve the list of selected objects.
	 * <p>
	 * Further, if you turn this on, it has the side effect of allowing the user
	 *  to select any remote object. The assumption being if you are prompting for
	 *  files, you also want to allow the user to select a folder, with the meaning
	 *  being that all files within the folder are implicitly selected. 
	 *
	 * @see #getSelectedObjects()
	 */
	public void setMultipleSelectionMode(boolean multiple)
	{
		this.multiSelect = multiple;
	}
	
	/**
	 * Return the selected connection in single select mode
	 */
	public IHost getSystemConnection()
	{
		if (result instanceof IHost)
			return (IHost)result;
		else if (result instanceof IHost[])
			return ((IHost[])result)[0];
		else
			return null;
	}


	/* (non-Javadoc)
	 * @see org.eclipse.rse.core.ui.actions.SystemBaseDialogAction#createDialog(org.eclipse.swt.widgets.Shell)
	 */
	protected Dialog createDialog(Shell shell)
	{
		SystemSelectConnectionDialog selectDlg =  new SystemSelectConnectionDialog(shell);
		if (defaultConn != null)
			selectDlg.setDefaultConnection(defaultConn);
		if (systemTypes != null)
			selectDlg.setSystemTypes(systemTypes);
		else if (systemType != null)
			selectDlg.setSystemType(systemType);
		selectDlg.setShowNewConnectionPrompt(showNewConnectionPrompt);
		if (message != null)
			selectDlg.setInstructionLabel(message);
		if (showPropertySheet)
			selectDlg.setShowPropertySheet(showPropertySheet,showPropertySheetInitialState);
		selectDlg.setMultipleSelectionMode(multiSelect);
		return selectDlg;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.rse.core.ui.actions.SystemBaseDialogAction#getDialogValue(org.eclipse.jface.dialogs.Dialog)
	 */
	protected Object getDialogValue(Dialog dlg)
	{
		SystemSelectConnectionDialog selectDlg = (SystemSelectConnectionDialog)dlg;
		result = selectDlg.getOutputObject();
		return result;
	}
	
	

}