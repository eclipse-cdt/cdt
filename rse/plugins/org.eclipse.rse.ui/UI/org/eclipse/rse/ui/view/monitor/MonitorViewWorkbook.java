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

package org.eclipse.rse.ui.view.monitor;

import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.rse.core.subsystems.ISubSystem;
import org.eclipse.rse.ui.view.ISystemViewElementAdapter;
import org.eclipse.rse.ui.view.SystemTableTreeView;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CTabFolder;
import org.eclipse.swt.custom.CTabItem;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;



/**
 * This is the desktop view wrapper of the System View viewer.
 */
public class MonitorViewWorkbook extends Composite
{


	private CTabFolder _folder;
	private SystemMonitorViewPart _viewPart;

	public MonitorViewWorkbook(Composite parent, SystemMonitorViewPart viewPart)
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
					MonitorViewPage page = (MonitorViewPage) item.getData();
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
				MonitorViewPage page = (MonitorViewPage) item.getData();

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
	
	public void removeDisconnected()
	{
		for (int i = 0; i < _folder.getItemCount(); i++)
		{
			CTabItem item = _folder.getItem(i);
			if (!item.isDisposed())
			{
				MonitorViewPage page = (MonitorViewPage) item.getData();
				if (page != null)
				{
					IAdaptable input = (IAdaptable)page.getInput();
					ISystemViewElementAdapter adapter = (ISystemViewElementAdapter)input.getAdapter(ISystemViewElementAdapter.class);
					if (adapter != null)
					{
						ISubSystem subSystem = adapter.getSubSystem(input);
						if (subSystem != null)
						{
							if (!subSystem.isConnected())
							{
								item.dispose();
								page.dispose();
	
								page = null;
								item = null;
							
								_folder.redraw();
							}
						}
					}
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

	public MonitorViewPage getCurrentTabItem()
	{
		if (_folder.getItemCount() > 0)
		{
			int index = _folder.getSelectionIndex();
			CTabItem item = _folder.getItem(index);
			return (MonitorViewPage) item.getData();
		}
		return null;
	}

	public void showCurrentPage()
	{
	    _folder.setFocus();
	}

	public Object getInput()
	{
	    MonitorViewPage page = getCurrentTabItem();
		if (page != null)
		{
		    page.setFocus();
			return page.getInput();
		}

		return null;
	}

	public SystemTableTreeView getViewer()
	{
		if (getCurrentTabItem() != null)
		{
			return getCurrentTabItem().getViewer();
		}
		return null;
	}

	public void addItemToMonitor(IAdaptable root, boolean createTab)
	{
		if (!_folder.isDisposed())
		{
			for (int i = 0; i < _folder.getItemCount(); i++)
			{
				CTabItem item = _folder.getItem(i);
				MonitorViewPage page = (MonitorViewPage) item.getData();
				if (page != null && root == page.getInput())
				{
					page.getViewer().refresh();

					if (_folder.getSelectionIndex() != i)
					{
						_folder.setSelection(item);
					}
					updateActionStates();
					//page.setFocus();
					return;
				}
			}

			if (createTab)
			{
				// never shown this, so add it
				createTabItem((IAdaptable) root);
			}
		}
	}

	private void createTabItem(IAdaptable root)
	{
		MonitorViewPage monitorViewPage = new MonitorViewPage(_viewPart);

		CTabItem titem = new CTabItem(_folder, SWT.NULL);
		setTabTitle(root, titem);
 
		titem.setData(monitorViewPage);
		titem.setControl(monitorViewPage.createTabFolderPage(_folder, _viewPart.getEditorActionHandler()));
		_folder.setSelection(titem );

		monitorViewPage.setInput(root);

		SystemTableTreeView viewer = monitorViewPage.getViewer();
		_viewPart.getSite().setSelectionProvider(viewer);
		_viewPart.getSite().registerContextMenu(viewer.getContextMenuManager(), viewer);
		
		monitorViewPage.setFocus();
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
			MonitorViewPage page = (MonitorViewPage) item.getData();
			if (root == page.getInput())
			{
				_folder.setSelection(i);
				page.getViewer().refresh();
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
				MonitorViewPage page = (MonitorViewPage) item.getData();
				if (page != null)
				{
					page.updateActionStates();
				}
			}
		}
	}
}