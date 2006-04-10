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

package org.eclipse.rse.files.ui.actions;

import java.util.Iterator;

import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.rse.core.SystemPlugin;
import org.eclipse.rse.core.subsystems.ISubSystemConfiguration;
import org.eclipse.rse.filters.ISystemFilterPool;
import org.eclipse.rse.filters.ISystemFilterPoolReferenceManagerProvider;
import org.eclipse.rse.subsystems.files.core.subsystems.IRemoteFile;
import org.eclipse.rse.subsystems.files.core.subsystems.IRemoteFileSubSystem;
import org.eclipse.rse.subsystems.files.core.subsystems.RemoteFile;
import org.eclipse.rse.ui.filters.dialogs.SystemNewFilterWizard;
import org.eclipse.rse.ui.view.ISystemRemoteElementAdapter;
import org.eclipse.swt.widgets.Shell;


/**
 * Class for defining a new filter from a preselected folder
 */
public class SystemNewFileFilterFromFolderAction extends SystemNewFileFilterAction
{
	private RemoteFile _selected;

	/**
	 * Constructor for SystemNewFileFilterFromFolderAction
	 * @param parent
	 */
	public SystemNewFileFilterFromFolderAction(Shell parent)
	{
		// initially use null, but update based on selection
		super(null, null, parent);
		setHelp(SystemPlugin.HELPPREFIX+"actn0112");
	}
	
	
	public void run()
	{
		IRemoteFileSubSystem fileSubsystem = _selected.getParentRemoteFileSubSystem();
		ISubSystemConfiguration factory = fileSubsystem.getSubSystemConfiguration();
		ISystemFilterPool filterPool = fileSubsystem.getFilterPoolReferenceManager().getDefaultSystemFilterPoolManager().getFirstDefaultSystemFilterPool();
		setParentFilterPool(filterPool);
		setAllowFilterPoolSelection(fileSubsystem.getFilterPoolReferenceManager().getReferencedSystemFilterPools());			
		super.run();
	}
	
	/**
	 * Called when the selection changes in the systems view.  This determines
	 * the input object for the command and whether to enable or disable
	 * the action.
	 * 
	 * @param selection the current seleciton
	 * @return whether to enable or disable the action
	 */
	public boolean updateSelection(IStructuredSelection selection)
	{
		boolean enable = false;

		Iterator e = ((IStructuredSelection) selection).iterator();
		Object selected = e.next();

		if (selected != null && selected instanceof IRemoteFile)
		{
			_selected = (RemoteFile) selected;
			//if (!_selected.isFile())
			{
				enable = true;
			}
		}



		return enable;
	}
	
	
	/**
	 * <i>Output method. Do not override.</i><br>
	 * Get the contextual system filter pool reference manager provider. Will return non-null if the
	 * current selection is a reference to a filter pool or filter, or a reference manager
	 * provider.
	 */
	public ISystemFilterPoolReferenceManagerProvider getSystemFilterPoolReferenceManagerProvider()
	{
		return _selected.getParentRemoteFileSubSystem().getFilterPoolReferenceManager().getProvider();
	}
	
	/**
	 * Parent intercept.
	 * <p>
	 * Overridable extension. For those cases when you don't want to create your
	 * own wizard subclass, but prefer to simply configure the default wizard.
	 * <p>
	 * Note, at the point this is called, all the base configuration, based on the 
	 * setters for this action, have been called. 
	 * <p>
	 * We do it here versus via setters as it defers some work until the user actually 
	 * selects this action.
	 */
	protected void configureNewFilterWizard(SystemNewFilterWizard wizard)
	{		
		super.configureNewFilterWizard(wizard);
		String[] filters = new String[1];
		ISystemRemoteElementAdapter adapter = (ISystemRemoteElementAdapter)_selected.getAdapter(ISystemRemoteElementAdapter.class);
		filters[0] = adapter.getFilterStringFor(_selected);
		wizard.setDefaultFilterStrings(filters);
	}
}