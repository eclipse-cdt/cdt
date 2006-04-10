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
import org.eclipse.rse.ui.dialogs.SystemResolveFilterStringDialog;
import org.eclipse.swt.widgets.Shell;


/**
 * The action for testing a given filter string by resolving it and showing the resolve results
 */
public class SystemResolveFilterStringAction extends SystemTestFilterStringAction
{
	
	
	/**
	 * Constructor when input subsystem and filter string are known already
	 */
	public SystemResolveFilterStringAction(Shell shell, ISubSystem subsystem, String filterString) 
	{
		super(shell, subsystem, filterString);
	}
	
	/**
	 * Constructor when input subsystem and filter string are not known already.
	 * @see #setSubSystem(ISubSystem)
	 * @see #setFilterString(String)
	 */
	public SystemResolveFilterStringAction(Shell shell) 
	{
		super(shell);
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
	  dlg = new SystemResolveFilterStringDialog(shell, subsystem, filterString);

	  return dlg;
	} // end createDialog()
	
	/**
	 * Return selected object. If multiple objects are selected,
	 * returns the first selected object.
	 */
	public Object getSelectedObject()
	{
	  return getValue();
	}	
	
} // end class SystemResolveFilterStringAction