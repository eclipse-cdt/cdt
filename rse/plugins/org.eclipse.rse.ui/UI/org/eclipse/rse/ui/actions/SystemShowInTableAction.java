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

import java.util.Iterator;

import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.rse.ui.ISystemIconConstants;
import org.eclipse.rse.ui.RSEUIPlugin;
import org.eclipse.rse.ui.SystemResources;
import org.eclipse.rse.ui.view.ISystemViewElementAdapter;
import org.eclipse.rse.ui.view.SystemTableViewPart;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;


/**
 * This is the default action for showing a remote object in a table
 */
public class SystemShowInTableAction extends SystemBaseAction
{
	private Object _selected;

	/**
	 * Constructor.
	 * @param shell  Shell of parent window, used as the parent for the dialog.
	 *               Can be null, but be sure to call setParent before the action is used (ie, run).
	 */
	public SystemShowInTableAction(Shell parent)
	{
		super(SystemResources.ACTION_TABLE_LABEL,			
			SystemResources.ACTION_TABLE_TOOLTIP,
			RSEUIPlugin.getDefault().getImageDescriptor(ISystemIconConstants.ICON_SYSTEM_SHOW_TABLE_ID),
			parent);
		setAvailableOffline(true);
	}

	/**
	 * Called when this action is selected from the popup menu.
	 */
	public void run()
	{
		SystemTableViewPart viewPart = null;
		IWorkbenchPage page = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage();
		try
		{
			viewPart = (SystemTableViewPart) page.showView("org.eclipse.rse.ui.view.systemTableView", null, IWorkbenchPage.VIEW_CREATE);
		}
		catch (PartInitException e)
		{
			return;
		}
		catch (Exception e)
		{
			return;
		}

		viewPart.setInput((IAdaptable) _selected);
		page.activate(viewPart);

	}

	/**
	 * Called when the selection changes.  The selection is checked to
	 * make sure this action can be performed on the selected object.
	 */
	public boolean updateSelection(IStructuredSelection selection)
	{
		boolean enable = false;
		Iterator e = ((IStructuredSelection) selection).iterator();
		Object selected = e.next();

		if (selected != null && selected instanceof IAdaptable)
		{
			ISystemViewElementAdapter va = (ISystemViewElementAdapter) ((IAdaptable) selected).getAdapter(ISystemViewElementAdapter.class);
			if (va.hasChildren(selected))
			{
				_selected = selected;
				enable = true;
			}
		}

		return enable;
	}
}