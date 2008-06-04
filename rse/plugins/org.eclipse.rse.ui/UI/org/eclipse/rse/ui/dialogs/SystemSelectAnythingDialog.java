/********************************************************************************
 * Copyright (c) 2002, 2008 IBM Corporation and others. All rights reserved.
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
 * Martin Oberhuber (Wind River) - [186773] split ISystemRegistryUI from ISystemRegistry
 * Martin Oberhuber (Wind River) - [190271] Move ISystemViewInputProvider to Core
 * David McKnight   (IBM)        - [187543] added setViewerFilter() method
 * David McKnight   (IBM)        - [225506] [api][breaking] RSE UI leaks non-API types
 ********************************************************************************/

package org.eclipse.rse.ui.dialogs;

import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.rse.core.RSECorePlugin;
import org.eclipse.rse.core.model.ISystemViewInputProvider;
import org.eclipse.rse.internal.ui.view.SystemViewForm;
import org.eclipse.rse.ui.SystemActionViewerFilter;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Shell;



public class SystemSelectAnythingDialog extends SystemPromptDialog
	implements ISelectionChangedListener
{
	private SystemViewForm _view = null;
	private Object _selected = null;
	private SystemActionViewerFilter _filter = null;

	public SystemSelectAnythingDialog(Shell shell, String title)
	{
		super(shell, title);
	}

	public Control createInner(Composite parent)
	{

		_view = new SystemViewForm(getShell(), parent, SWT.NONE, getInputProvider(), true, this);
		_view.getSystemTree().addSelectionChangedListener(this);

		if (_filter != null){
			_view.getSystemTree().addFilter(_filter);
		}

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
		ISystemViewInputProvider inputProvider = RSECorePlugin.getTheSystemRegistry();

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

	
	/**
	 * Use this method to limit the objects that are seen in the view of this
	 * dialog.
	 *
	 * @param filter the filter that limits the visible objects
	 * @since 3.0
	 */
	public void setViewerFilter(SystemActionViewerFilter filter)
	{
		_filter = filter;
		if (_view != null)
		{
			_view.getSystemTree().addFilter(filter);
		}

	}
}