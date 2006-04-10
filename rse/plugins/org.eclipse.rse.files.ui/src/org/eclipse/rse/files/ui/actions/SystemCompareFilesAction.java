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

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.eclipse.compare.CompareConfiguration;
import org.eclipse.compare.CompareUI;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.rse.files.ui.FileResources;
import org.eclipse.rse.files.ui.compare.SystemCompareInput;
import org.eclipse.rse.files.ui.resources.SystemEditableRemoteFile;
import org.eclipse.rse.subsystems.files.core.subsystems.IRemoteFile;
import org.eclipse.rse.ui.actions.SystemBaseAction;
import org.eclipse.swt.widgets.Shell;



/**
 * Compare two remote files
 */
public class SystemCompareFilesAction extends SystemBaseAction
{


	private List _selected;

	/**
	 * Constructor for SystemCompareFilesAction
	 * @param parent
	 */
	public SystemCompareFilesAction(Shell parent)
	{
		super(FileResources.ACTION_COMPAREWITH_EACH_LABEL, parent);
		setToolTipText(FileResources.ACTION_COMPAREWITH_EACH_TOOLTIP);
		_selected = new ArrayList();
		allowOnMultipleSelection(true);
	}

	/**
	 * Compare two remote files 
	 */
	public void run()
	{
		CompareConfiguration cc = new CompareConfiguration();
		cc.setProperty("org.eclipse.compare.internal.CONFIRM_SAVE_PROPERTY", new Boolean(false));
		SystemCompareInput fInput = new SystemCompareInput(cc);

		for (int i = 0; i < _selected.size(); i++)
		{
			IRemoteFile file = (IRemoteFile) _selected.get(i);
			SystemEditableRemoteFile ef = new SystemEditableRemoteFile(file);
			fInput.addRemoteEditable(ef);
		}

		CompareUI.openCompareEditor(fInput);
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
		_selected.clear();
		boolean enable = false;

		Iterator e = ((IStructuredSelection) selection).iterator();
		while (e.hasNext())
		{
			Object selected = e.next();

			if (selected != null && selected instanceof IRemoteFile)
			{
				IRemoteFile file = (IRemoteFile) selected;
				if (file.isFile())
				{
					_selected.add(file);
					if (_selected.size() == 2)
					{
						enable = true;
					}
					else
					{
						enable = false;
					}
				}
				else
				{
					return false;
				}
			}
			else
			{
				return false;
			}
		}

		return enable;
	}
}