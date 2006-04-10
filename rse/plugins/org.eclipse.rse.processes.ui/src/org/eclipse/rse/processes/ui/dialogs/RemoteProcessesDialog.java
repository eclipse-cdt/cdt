/********************************************************************************
 * Copyright (c) 2006 IBM Corporation. All rights reserved.
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

package org.eclipse.rse.processes.ui.dialogs;

import org.eclipse.jface.viewers.DoubleClickEvent;
import org.eclipse.jface.viewers.IDoubleClickListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.TableLayout;
import org.eclipse.rse.core.SystemPlugin;
import org.eclipse.rse.filters.ISystemFilterReference;
import org.eclipse.rse.processes.ui.SystemProcessesResources;
import org.eclipse.rse.services.clientserver.processes.IHostProcess;
import org.eclipse.rse.subsystems.processes.core.subsystem.IRemoteProcess;
import org.eclipse.rse.subsystems.processes.core.subsystem.RemoteProcessSubSystem;
import org.eclipse.rse.ui.SystemWidgetHelpers;
import org.eclipse.rse.ui.dialogs.SystemPromptDialog;
import org.eclipse.rse.ui.view.SystemTableView;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.KeyListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.Text;


/**
 * 
 * @author dmcknigh
 *
 */
public class RemoteProcessesDialog extends SystemPromptDialog implements KeyListener
{
	private Text _nameFilterText;
	private SystemTableView _viewer;
	private RemoteProcessSubSystem _subSystem;
	private String _executableFilter;
	private Table _table;
	private IHostProcess _selected;

	public RemoteProcessesDialog(Shell shell, String title, RemoteProcessSubSystem subSystem, String executableFilter)
	{
		super(shell, title);
		_subSystem = subSystem;
		_executableFilter = executableFilter;
	}

	protected Control createInner(Composite parent)
	{
		GridLayout gridLayout = new GridLayout();
		gridLayout.numColumns = 1;
		parent.setLayout(gridLayout);

		_nameFilterText = SystemWidgetHelpers.createLabeledTextField(parent, this, SystemProcessesResources.RESID_REMOTE_PROCESSES_EXECUTABLE_LABEL, SystemProcessesResources.RESID_REMOTE_PROCESSES_EXECUTABLE_TOOLTIP);
		_nameFilterText.addKeyListener(this);
		
		// create table portion
		_table = new Table(parent, SWT.H_SCROLL | SWT.V_SCROLL | SWT.MULTI | SWT.BORDER | SWT.FULL_SELECTION | SWT.HIDE_SELECTION);
		_viewer = new SystemTableView(_table,this);


		_viewer.addDoubleClickListener(new IDoubleClickListener()
		{
			public void doubleClick(DoubleClickEvent event)
			{
				//handleDoubleClick(event);
			}
		});

		
		SystemWidgetHelpers.setHelp(_viewer.getControl(), SystemPlugin.HELPPREFIX + "ucmd0000");

		TableLayout layout = new TableLayout();
		_table.setLayout(layout);
		_table.setHeaderVisible(false);
		_table.setLinesVisible(false);

		GridData gridData = new GridData(GridData.FILL_HORIZONTAL | GridData.FILL_VERTICAL);
		gridData.heightHint = 200;
		gridData.widthHint = 400;
		_table.setLayoutData(gridData);
		init();
		return _table;
	}

	protected Control getInitialFocusControl()
	{
		// TODO Auto-generated method stub
		return _viewer.getControl();
	}
	
	
	public void doubleClick(DoubleClickEvent event)
	{
		IStructuredSelection s = (IStructuredSelection) event.getSelection();
		Object element = s.getFirstElement();
		if (element == null)
			return;
		processOK();
		close();
	}

	
	protected boolean processOK() 
	{
		StructuredSelection sel = (StructuredSelection)_viewer.getSelection();
		IRemoteProcess proc = (IRemoteProcess)sel.getFirstElement();
		if (proc != null)
		{
			_selected = proc;
		}
		return true;
	}
	
	public IHostProcess getSelected()
	{
		return _selected;
	}
	
	protected void init()
	{
		_nameFilterText.setText(_executableFilter);
		Object[] filters = _subSystem.getChildren();
		
		ISystemFilterReference ref = (ISystemFilterReference)filters[0];
		ref.markStale(true);
		updateViewFilter();
		_viewer.setInput(ref);
	}
	
	protected void updateViewFilter()
	{
		if (_executableFilter.indexOf("*") == -1)
			_executableFilter += "*";
		String[] viewFilters = {_executableFilter};
		_viewer.setViewFilters(viewFilters);
	}
	
	public void keyPressed(KeyEvent e)
	{
	}
	
	public void keyReleased(KeyEvent e)
	{
		if (e.widget == _nameFilterText)
		{
			if (!_nameFilterText.getText().equals(_executableFilter))
			{
				_executableFilter = _nameFilterText.getText();
				updateViewFilter();
			}
		}
	}
	

}