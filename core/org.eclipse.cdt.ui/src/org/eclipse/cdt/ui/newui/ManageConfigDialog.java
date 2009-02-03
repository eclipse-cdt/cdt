/*******************************************************************************
 * Copyright (c) 2002, 2009 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * IBM Rational Software - Initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.ui.newui;

import java.util.Arrays;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtension;
import org.eclipse.core.runtime.IExtensionPoint;
import org.eclipse.core.runtime.Platform;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;

import org.eclipse.cdt.core.model.util.CDTListComparator;
import org.eclipse.cdt.core.settings.model.ICConfigurationDescription;
import org.eclipse.cdt.core.settings.model.ICProjectDescription;
import org.eclipse.cdt.ui.CUIPlugin;

/**
 * @noextend This class is not intended to be subclassed by clients.
 */
public class ManageConfigDialog extends Dialog {
	private static final String EXTENSION_POINT_ID = "org.eclipse.cdt.ui.newCfgDialog"; //$NON-NLS-1$
	public static final String ELEMENT_NAME = "dialog"; //$NON-NLS-1$
	public static final String CLASS_NAME = "class"; //$NON-NLS-1$
	public static final String TITLE_NAME = "title"; //$NON-NLS-1$
	public static final String ID_NAME = "mbs_id"; //$NON-NLS-1$

	// String constants
	private static final String CMN_PREFIX = "BuildPropertyCommon";	//$NON-NLS-1$
	private static final String CMN_LABEL = CMN_PREFIX + ".label";	//$NON-NLS-1$
	private static final String NEW = CMN_LABEL + ".new";	//$NON-NLS-1$
	private static final String REMOVE = CMN_LABEL + ".remove";	//$NON-NLS-1$
	private static final String PREFIX = "ManageConfig";	//$NON-NLS-1$
	private static final String LABEL = PREFIX + ".label";	//$NON-NLS-1$
	private static final String RENAME = LABEL + ".rename";	//$NON-NLS-1$
	private static final String NEW_CONF_DLG = LABEL + ".new.config.dialog";	//$NON-NLS-1$
	private static final String RENAME_CONF_DLG = LABEL + ".rename.config.dialog";	//$NON-NLS-1$
	
	// The list of configurations to delete
	private ICProjectDescription des;
	private IProject prj;
	private String title;
	private String mbs_id;
	protected Table table;
	
	protected Button actBtn;
	protected Button newBtn;
	protected Button renBtn;
	protected Button delBtn;

	/**
	 * @param parentShell
	 */
	ManageConfigDialog(Shell parentShell, String _title, IProject _prj) {
		super(parentShell);
		setShellStyle(getShellStyle() | SWT.RESIZE);
		title = _title;
		prj = _prj;
	}

	@Override
	protected void configureShell(Shell shell) {
		super.configureShell(shell);
		if (title != null) shell.setText(title);
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.jface.dialogs.Dialog#createDialogArea(org.eclipse.swt.widgets.Composite)
	 */
	@Override
	protected Control createDialogArea(Composite parent) {
		Composite composite = new Composite(parent, SWT.NULL);
		composite.setFont(parent.getFont());
		composite.setLayout(new GridLayout(4, true));
		composite.setLayoutData(new GridData(GridData.FILL_BOTH));
	
		// Create the current config table
		table = new Table(composite, SWT.BORDER | SWT.SINGLE | SWT.V_SCROLL | SWT.H_SCROLL | SWT.FULL_SELECTION);
		GridData gd = new GridData(GridData.FILL_BOTH);
		gd.horizontalSpan = 4;
		table.setLayoutData(gd);
		table.setHeaderVisible(true);
		table.setLinesVisible(true);
		
		table.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				updateButtons();
			}});
		
		TableColumn col = new TableColumn(table, SWT.NONE);
		col.setText(UIMessages.getString("ManageConfigDialog.1")); //$NON-NLS-1$
		col.setWidth(100);
		col = new TableColumn(table, SWT.NONE);
		col.setText(UIMessages.getString("ManageConfigDialog.2")); //$NON-NLS-1$
		col.setWidth(120);
		col = new TableColumn(table, SWT.NONE);
		col.setText(UIMessages.getString("ManageConfigDialog.3")); //$NON-NLS-1$
		col.setWidth(80);

		actBtn = new Button(composite, SWT.PUSH);
		actBtn.setText(UIMessages.getString("ManageConfigDialog.4")); //$NON-NLS-1$
		actBtn.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		actBtn.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				TableItem[] tis = table.getSelection();
				if (tis == null || tis.length != 1) return;
				ICConfigurationDescription cfgd = (ICConfigurationDescription)tis[0].getData();
//				cfgd.setActive();
				des.setActiveConfiguration(cfgd);
				updateData();
			}} ); 

		newBtn = new Button(composite, SWT.PUSH);
		newBtn.setText(UIMessages.getString(NEW));
		newBtn.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		newBtn.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				handleNewPressed();
			}} ); 

		delBtn = new Button(composite, SWT.PUSH);
		delBtn.setText(UIMessages.getString(REMOVE));
		delBtn.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		delBtn.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				handleRemovePressed();
			}} ); 

		renBtn = new Button(composite, SWT.PUSH);
		renBtn.setText(UIMessages.getString(RENAME));
		renBtn.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		renBtn.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				handleRenamePressed();
			}} ); 

		des = CDTPropertyManager.getProjectDescription(composite, prj);
//		comp = composite;
		
		updateData();
    	return composite;
	}
	/*
	 * Event handler for the add button
	 */
	protected void handleNewPressed() {
		INewCfgDialog dialog = handleSpecificMBS(mbs_id);
		if (dialog == null) { // default (core) implementation.
			dialog = new NewConfigurationDialog(getShell());
			dialog.setTitle(UIMessages.getString(NEW_CONF_DLG));
		}
		dialog.setProject(des); 
		if (dialog.open() == OK) updateData();
	}
	
	/**
	 * Tries to load MBS-specific creation dialog a
	 * @return false if there's no such feature 
	 */
	protected INewCfgDialog handleSpecificMBS(String id) {
		IExtensionPoint extensionPoint = Platform.getExtensionRegistry()
				.getExtensionPoint(EXTENSION_POINT_ID);
		if (extensionPoint == null) return null;
		IExtension[] extensions = extensionPoint.getExtensions();
		if (extensions == null) return null;
		for (int i = 0; i < extensions.length; ++i)	{
			IConfigurationElement[] elements = extensions[i].getConfigurationElements();
			for (int k = 0; k < elements.length; k++) {
				if (elements[k].getName().equals(ELEMENT_NAME)) {
					if (! id.equals(elements[k].getAttribute(ID_NAME)))
						continue;
					INewCfgDialog dialog = null;
					try {
						dialog = (INewCfgDialog) elements[k].createExecutableExtension(CLASS_NAME);
						dialog.setTitle(elements[k].getAttribute(TITLE_NAME));
						dialog.setShell(getShell());
						return dialog;
					} catch (CoreException e) {
						System.out.println("Cannot create dialog: " + e.getLocalizedMessage()); //$NON-NLS-1$
						return null; 
					}
				}					
			}
		}
		return null;
	}
	
	/*
	 * (non-javadoc) Event handler for the rename button
	 */
	protected void handleRenamePressed() {
		int sel = table.getSelectionIndex();
		if (sel != -1) {
			ICConfigurationDescription cfgd = (ICConfigurationDescription) table.getItem(sel).getData();
			RenameConfigurationDialog dialog = new RenameConfigurationDialog(
					getShell(), cfgd, des.getConfigurations(),
					UIMessages.getString(RENAME_CONF_DLG));
			if (dialog.open() == OK) {
				cfgd.setName(dialog.getNewName());
				cfgd.setDescription(dialog.getNewDescription());
				updateData();
			}
		}
	}

	/*
	 * (non-javadoc) Event handler for the remove button
	 */
	protected void handleRemovePressed() {
		TableItem[] tis = table.getSelection();
		if (tis == null || tis.length < 1) return;
		String[] names = new String[tis.length];
		for (int i=0; i<tis.length; i++) 
			names[i] = tis[i].getText(0);
		// Get the confirmation from user before deleting the configuration
		Shell shell = CUIPlugin.getActiveWorkbenchShell();
		boolean shouldDelete = MessageDialog.openQuestion(shell,
		        UIMessages.getString("ManageConfig.deletedialog.title"), //$NON-NLS-1$
		        UIMessages.getFormattedString("ManageConfig.deletedialog.message",  //$NON-NLS-1$
		                names));
		if (shouldDelete) {
			boolean wasActive = false;
			for (int j=0; j<tis.length; j++) {
				ICConfigurationDescription cfgd = (ICConfigurationDescription)tis[j].getData();
				if (cfgd.isActive()) wasActive = true; 
				des.removeConfiguration(cfgd);
				
			}
			ICConfigurationDescription[] cfgds = des.getConfigurations(); 
			if (wasActive && cfgds.length > 0) {
				cfgds[0].setActive();
				des.setActiveConfiguration(cfgds[0]);
			}
			updateData();
		}
	}

	private void updateButtons() {
		int sel = table.getSelectionCount();
		delBtn.setEnabled(sel > 0 & sel < table.getItemCount());
		renBtn.setEnabled(sel == 1);
		if (sel == 1) {
			ICConfigurationDescription c = (ICConfigurationDescription)table.getSelection()[0].getData();
			actBtn.setEnabled(c != null && !c.isActive());
		} else
			actBtn.setEnabled(false);
	}

	/**
	 * refresh configs table after changes
	 */
	private void updateData() {
		table.removeAll();
		ICConfigurationDescription[] cfgds = des.getConfigurations();
		mbs_id = cfgds[0].getBuildSystemId();
		Arrays.sort(cfgds, CDTListComparator.getInstance());
		for (int i=0; i<cfgds.length; i++ ) {
			TableItem t = new TableItem(table, 0);
			t.setText(0, cfgds[i].getName());
			t.setText(1, cfgds[i].getDescription());
			t.setText(2, cfgds[i].isActive() ? UIMessages.getString("ManageConfigDialog.5") : ""); //$NON-NLS-1$ //$NON-NLS-2$
			t.setData(cfgds[i]);
		}
		if (table.getItemCount() > 0) table.select(0);
		table.setFocus();
		updateButtons();
	}
	
	ICProjectDescription getProjectDescription(){
		return des;
	}
}
