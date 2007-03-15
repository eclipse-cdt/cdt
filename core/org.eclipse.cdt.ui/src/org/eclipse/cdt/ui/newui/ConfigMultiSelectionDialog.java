/*******************************************************************************
 * Copyright (c) 2002, 2007 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * IBM Rational Software - Initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.ui.newui;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.viewers.CheckStateChangedEvent;
import org.eclipse.jface.viewers.CheckboxTableViewer;
import org.eclipse.jface.viewers.ICheckStateListener;
import org.eclipse.jface.viewers.ILabelProviderListener;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;

import org.eclipse.cdt.core.settings.model.ICConfigurationDescription;
import org.eclipse.cdt.ui.CUIPlugin;

public class ConfigMultiSelectionDialog extends Dialog {
	static private ICConfigurationDescription[] cfgds;
	private Table table;
	private CheckboxTableViewer tv;
	private Button b_ok;
	private Label message;
	
	public static ICConfigurationDescription[] select(ICConfigurationDescription[] _cfgds) {
		cfgds = _cfgds;
		ConfigMultiSelectionDialog d = new ConfigMultiSelectionDialog(CUIPlugin.getActiveWorkbenchShell());
		if (d.open() == OK)	
			return (ICConfigurationDescription[])d.tv.getCheckedElements();
		return null;
	}
	
	public ConfigMultiSelectionDialog(Shell parentShell) { super(parentShell); }

	protected void configureShell(Shell shell) {
		super.configureShell(shell);
		shell.setText(NewUIMessages.getResourceString("ConfigMultiSelectionDialog.0")); //$NON-NLS-1$
	}
	
	/**
	 * Method is overridden to disable "OK" button at start
	 */
	protected Control createContents(Composite parent) {
		Control out = super.createContents(parent);
		b_ok = getButton(IDialogConstants.OK_ID);
		b_ok.setEnabled(false);
		return out;
	}	

	/* (non-Javadoc)
	 * @see org.eclipse.jface.dialogs.Dialog#createDialogArea(org.eclipse.swt.widgets.Composite)
	 */
	protected Control createDialogArea(Composite parent) {
		Composite composite = new Composite(parent, SWT.NULL);
		composite.setFont(parent.getFont());
		composite.setLayout(new GridLayout(1, true));
		composite.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
	
		// Create the current config table
		table = new Table(composite, SWT.CHECK | SWT.BORDER | SWT.SINGLE | SWT.V_SCROLL | SWT.H_SCROLL | SWT.FULL_SELECTION);
		table.setLayoutData(new GridData(GridData.FILL));
		table.setHeaderVisible(true);
		table.setLinesVisible(true);
		
		message = new Label(composite, SWT.NONE);
		message.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		message.setText(NewUIMessages.getResourceString("ConfigMultiSelectionDialog.1")); //$NON-NLS-1$
		message.setForeground(composite.getDisplay().getSystemColor(SWT.COLOR_RED));
		
		TableColumn col = new TableColumn(table, SWT.NONE);
		col.setText(NewUIMessages.getResourceString("ManageConfigDialog.1")); //$NON-NLS-1$
		col.setWidth(100);
		col = new TableColumn(table, SWT.NONE);
		col.setText(NewUIMessages.getResourceString("ManageConfigDialog.2")); //$NON-NLS-1$
		col.setWidth(120);
		
		tv = new CheckboxTableViewer(table);
		tv.setContentProvider(new IStructuredContentProvider() {
			public Object[] getElements(Object inputElement) { return cfgds; }
			public void dispose() {}
			public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {}
		});
		tv.addCheckStateListener(new ICheckStateListener() {
			public void checkStateChanged(CheckStateChangedEvent e) {
				boolean enabled = (tv.getCheckedElements().length > 1); 
				if (b_ok != null) b_ok.setEnabled(enabled);
				message.setVisible(!enabled);
			}});
		tv.setLabelProvider(new ITableLabelProvider() {
			public Image getColumnImage(Object element, int columnIndex) { return null; }
			public void addListener(ILabelProviderListener listener) {}
			public void dispose() {}
			public boolean isLabelProperty(Object element, String property) { return false;}
			public void removeListener(ILabelProviderListener listener) {}

			public String getColumnText(Object element, int index) {
				ICConfigurationDescription cfg = (ICConfigurationDescription)element;
				if (index == 0) return cfg.getName();
				if (index == 1) return cfg.getDescription();
				return AbstractPage.EMPTY_STR;
			}});
		tv.setInput(cfgds);
		table.setFocus();
    	return composite;
	}
	
}
