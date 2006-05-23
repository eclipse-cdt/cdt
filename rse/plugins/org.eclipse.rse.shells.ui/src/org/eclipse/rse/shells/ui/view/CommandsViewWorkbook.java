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

package org.eclipse.rse.shells.ui.view;

import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.rse.subsystems.shells.core.subsystems.IRemoteCommandShell;
import org.eclipse.rse.ui.view.ISystemViewElementAdapter;
import org.eclipse.rse.ui.view.SystemTableView;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CTabFolder;
import org.eclipse.swt.custom.CTabItem;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;



/**
 * This is the desktop view wrapper of the System View viewer.
 * ViewPart is from com.ibm.itp.ui.support.parts
 */
public class CommandsViewWorkbook extends Composite
{


	private CTabFolder _folder;
	private SystemCommandsViewPart _viewPart;

	public CommandsViewWorkbook(Composite parent, SystemCommandsViewPart viewPart)
	{
		super(parent, SWT.NONE);

		_folder = new CTabFolder(this, SWT.NONE);
		_folder.setLayout(new TabFolderLayout());
		_folder.setLayoutData(new GridData(GridData.FILL_BOTH));
		setLayout(new FillLayout());
		_viewPart = viewPart;
	}

	public void dispose()
	{
		if (!_folder.isDisposed())
		{
			for (int i = 0; i < _folder.getItemCount(); i++)
			{
				CTabItem item = _folder.getItem(i);
				if (!item.isDisposed())
				{
					CommandsViewPage page = (CommandsViewPage) item.getData();
					page.dispose();
				}
			}
			_folder.dispose();
		}
		super.dispose();
	}

	public CTabFolder getFolder()
	{
		return _folder;
	}

	public void remove(Object root)
	{
		for (int i = 0; i < _folder.getItemCount(); i++)
		{
			CTabItem item = _folder.getItem(i);
			if (!item.isDisposed())
			{
				CommandsViewPage page = (CommandsViewPage) item.getData();

				if (page != null && root == page.getInput())
				{
					item.dispose();
					page.dispose();

					page = null;
					item = null;
				
					_folder.redraw();

					return;
				}
			}
		}
	}

	public CTabItem getSelectedTab()
	{
		if (_folder.getItemCount() > 0)
		{
			int index = _folder.getSelectionIndex();
			CTabItem item = _folder.getItem(index);
			return item;
		}

		return null;
	}

	public CommandsViewPage getCurrentTabItem()
	{
		if (_folder.getItemCount() > 0)
		{
			int index = _folder.getSelectionIndex();
			CTabItem item = _folder.getItem(index);
			return (CommandsViewPage) item.getData();
		}
		return null;
	}

	public void showCurrentPage()
	{
	    _folder.setFocus();
	}

	public Object getInput()
	{
	    CommandsViewPage page = getCurrentTabItem();
		if (page != null)
		{
		   // page.setFocus();
			return page.getInput();
		}

		return null;
	}

	public SystemCommandsView getViewer()
	{
		if (getCurrentTabItem() != null)
		{
			return getCurrentTabItem().getViewer();
		}
		return null;
	}

	public void updateOutput(IRemoteCommandShell root, boolean createTab)
	{
		if (!_folder.isDisposed())
		{
			for (int i = 0; i < _folder.getItemCount(); i++)
			{
				CTabItem item = _folder.getItem(i);
				CommandsViewPage page = (CommandsViewPage) item.getData();
				if (page != null && root == page.getInput())
				{
					if (!root.isActive())
					{
						setTabTitle((IAdaptable) root, item);
						page.updateTitle((IAdaptable) root);
						page.setEnabled(false);
					}

					page.updateOutput();

					/* DKM - changing focus can get annoying 
					 * see defect 142978
					 * 
					if (_folder.getSelectionIndex() != i)
					{
						_folder.setSelection(item);
					}
					*/
					updateActionStates();
					//page.setFocus();
					return;
				}
			}

			if (/*root.isActive() &&*/ createTab)
			{
				// never shown this, so add it
				createTabItem((IAdaptable) root);
			}
		}
	}

	private void createTabItem(IAdaptable root)
	{
		CommandsViewPage commandsViewPage = new CommandsViewPage(_viewPart);

		CTabItem titem = new CTabItem(_folder, SWT.NULL);
		setTabTitle(root, titem);
 
		titem.setData(commandsViewPage);
		titem.setControl(commandsViewPage.createTabFolderPage(_folder, _viewPart.getEditorActionHandler()));
		_folder.setSelection(titem );

		commandsViewPage.setInput(root);

		SystemTableView viewer = commandsViewPage.getViewer();
		_viewPart.getSite().setSelectionProvider(viewer);
		_viewPart.getSite().registerContextMenu(viewer.getContextMenuManager(), viewer);
		//commandsViewPage.getViewer().addSelectionChangedListener((SystemCommandsViewPart)_viewPart);
		
		commandsViewPage.setFocus();
	}

	private void setTabTitle(IAdaptable root, CTabItem titem)
	{
		ISystemViewElementAdapter va = (ISystemViewElementAdapter) root.getAdapter(ISystemViewElementAdapter.class);
		if (va != null)
		{
			titem.setText(va.getName(root));
			titem.setImage(va.getImageDescriptor(root).createImage());
		}
	}

	public void setInput(IAdaptable root)
	{
		for (int i = 0; i < _folder.getItemCount(); i++)
		{
			CTabItem item = _folder.getItem(i);
			CommandsViewPage page = (CommandsViewPage) item.getData();
			if (root == page.getInput())
			{
				_folder.setSelection(i);
				page.getViewer().updateChildren();
				return;
			}
		}
	}

	public void updateActionStates()
	{
		for (int i = 0; i < _folder.getItemCount(); i++)
		{
			CTabItem item = _folder.getItem(i);
			if (!item.isDisposed())
			{
				CommandsViewPage page = (CommandsViewPage) item.getData();
				if (page != null)
				{
					page.updateActionStates();
				}
			}
		}
	}
}