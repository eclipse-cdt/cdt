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

package org.eclipse.rse.ui.dialogs;

import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.rse.core.SystemPlugin;
import org.eclipse.rse.ui.view.ISystemPropertyConstants;
import org.eclipse.rse.ui.view.ISystemViewInputProvider;
import org.eclipse.rse.ui.view.SystemViewForm;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Shell;



public class SystemSelectAnythingDialog extends SystemPromptDialog 
	implements ISystemPropertyConstants, ISelectionChangedListener
{
	private SystemViewForm _view = null;
	private Object _selected = null;
	public SystemSelectAnythingDialog(Shell shell, String title)
	{
		super(shell, title);
	}
	
	public Control createInner(Composite parent)
	{
		
		_view = new SystemViewForm(getShell(), parent, SWT.NONE, getInputProvider(), true, this); 
		_view.getSystemView().addSelectionChangedListener(this);	
		//_view.getSystemView().ref
		
		return _view.getTreeControl();
	}
	
	public boolean close()
	{
		_view.removeSelectionChangedListener(this);
		_view.dispose();
		return super.close();
	}
	
	/** 
	 * Returns the initial input provider for the viewer.
	 * Tries to deduce the appropriate input provider based on current input.
	 */
	protected ISystemViewInputProvider getInputProvider() 
	{
		ISystemViewInputProvider inputProvider = SystemPlugin.getTheSystemRegistry();
		
		return inputProvider;
	}
	
	public Control getInitialFocusControl()
	{
		return _view.getTreeControl();
	}
	
	public Object getSelectedObject()
	{
		//IStructuredSelection selection = (IStructuredSelection)_view.getSelection();
		//return selection.getFirstElement();
		return _selected;
	}
	
	public void selectionChanged(SelectionChangedEvent e)
	{
		IStructuredSelection selection = (IStructuredSelection)e.getSelection();
		
		_selected = selection.getFirstElement();

		
	}
}