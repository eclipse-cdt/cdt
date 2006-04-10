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
import org.eclipse.rse.core.subsystems.ISubSystem;
import org.eclipse.rse.ui.SystemResources;
import org.eclipse.rse.ui.dialogs.SystemTestFilterStringDialog;
import org.eclipse.swt.widgets.Shell;


/**
 * The action for testing a given filter string by resolving it and showing the resolve results
 */
public class SystemTestFilterStringAction extends SystemBaseDialogAction 
                                 
{
	
	protected ISubSystem subsystem;
	protected String filterString;
	protected SystemTestFilterStringDialog dlg;

	
	/**
	 * Constructor when input subsystem and filter string are known already
	 */
	public SystemTestFilterStringAction(Shell shell, ISubSystem subsystem, String filterString) 
	{
		super(SystemResources.ACTION_TESTFILTERSTRING_LABEL, SystemResources.ACTION_TESTFILTERSTRING_TOOLTIP, null,
		      shell);
		allowOnMultipleSelection(false);
		setSubSystem(subsystem);
		setFilterString(filterString);
	}
	/**
	 * Constructor when input subsystem and filter string are not known already.
	 * @see #setSubSystem(ISubSystem)
	 * @see #setFilterString(String)
	 */
	public SystemTestFilterStringAction(Shell shell) 
	{
		this(shell, null, null);
	}
	
	/**
	 * Set the subsystem within the context of which this filter string is to be tested.
	 */
	public void setSubSystem(ISubSystem subsystem)
	{
		this.subsystem = subsystem;
	}
	
	/**
	 * Set the filter string to test
	 */
	public void setFilterString(String filterString)
	{
		this.filterString = filterString;
	}
	
	/**
	 * If you decide to use the supplied run method as is,
	 *  then you must override this method to create and return
	 *  the dialog that is displayed by the default run method
	 *  implementation.
	 * <p>
	 * If you override run with your own, then
	 *  simply implement this to return null as it won't be used.
	 * @see #run()
	 */
	protected Dialog createDialog(Shell shell)
	{
		//if (dlg == null) // I hoped to reduce memory requirements by re-using but doesn't work. Phil
		  dlg = new SystemTestFilterStringDialog(shell, subsystem, filterString);
		//else
		//{
		  //dlg.reset(subsystem, filterString);
		//}
		return dlg;
	}
	
	/**
	 * Required by parent. We just return null.
	 */
	protected Object getDialogValue(Dialog dlg)
	{
		return null;
	}
}