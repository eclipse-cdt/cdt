/*******************************************************************************
 * Copyright (c) 2003, 2007 Wind River Systems, Inc. and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Initial Contributors:
 * The following Wind River employees contributed to the Terminal component
 * that contains this file: Chris Thew, Fran Litterio, Stephen Lamb,
 * Helmut Haigermoser and Ted Williams.
 *
 * Contributors:
 * Michael Scharf (Wind River) - split into core, view and connector plugins 
 * Martin Oberhuber (Wind River) - fixed copyright headers and beautified
 *******************************************************************************/
package org.eclipse.tm.internal.terminal.view;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogSettings;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.tm.internal.terminal.provisional.api.ISettingsPage;
import org.eclipse.tm.internal.terminal.provisional.api.ITerminalConnector;

class TerminalSettingsDlg extends Dialog {
	private Combo fCtlConnTypeCombo;
	private final ITerminalConnector[] fConnectors;
	private final ISettingsPage[] fPages;
	private int fSelectedConnector;
	private PageBook fPageBook;
	private IDialogSettings fDialogSettings;

	public TerminalSettingsDlg(Shell shell, ITerminalConnector[] connectors, ITerminalConnector connector) {
		super(shell);
		fConnectors=connectors;
		fPages=new ISettingsPage[fConnectors.length];
		for (int i = 0; i < fConnectors.length; i++) {
			fPages[i]=fConnectors[i].makeSettingsPage();
			if(fConnectors[i]==connector)
				fSelectedConnector=i;
		}
	}
	protected void okPressed() {
		if (!validateSettings())
			return;
		if(fSelectedConnector>=0) {
			fPages[fSelectedConnector].saveSettings();
		}
		super.okPressed();
	}
	protected void cancelPressed() {
		fSelectedConnector=-1;
		super.cancelPressed();
	}
	public int open() {
		setShellStyle(getShellStyle() | SWT.RESIZE);
		return super.open();
	}
	protected void configureShell(Shell newShell) {
		super.configureShell(newShell);

		newShell.setText(ViewMessages.TERMINALSETTINGS);
	}
	protected Control createDialogArea(Composite parent) {
		Composite ctlComposite = (Composite) super.createDialogArea(parent);
		setupPanel(ctlComposite);
		setupListeners();
		initFields();

		return ctlComposite;
	}
	private void initFields() {
		// Load controls
		for (int i = 0; i < fPages.length; i++) {
			String name=fPages[i].getName();
			fCtlConnTypeCombo.add(name);
			if(fSelectedConnector==i) {
				fCtlConnTypeCombo.select(i);
				selectPage(i);
			}
		}
	}
	private boolean validateSettings() {
		if(fSelectedConnector<0)
			return true;
		return fPages[fSelectedConnector].validateSettings();
	}
	private void setupPanel(Composite wndParent) {
		setupConnTypePanel(wndParent);
		setupSettingsGroup(wndParent);
	}
	private void setupConnTypePanel(Composite wndParent) {
		Group wndGroup;
		GridLayout gridLayout;
		GridData gridData;

		wndGroup = new Group(wndParent, SWT.NONE);
		gridLayout = new GridLayout(1, true);
		gridData = new GridData(GridData.FILL_HORIZONTAL);

		wndGroup.setLayout(gridLayout);
		wndGroup.setLayoutData(gridData);
		wndGroup.setText(ViewMessages.CONNECTIONTYPE + ":"); //$NON-NLS-1$

		fCtlConnTypeCombo = new Combo(wndGroup, SWT.DROP_DOWN | SWT.READ_ONLY);
		gridData = new GridData(GridData.FILL_HORIZONTAL);
		gridData.widthHint = 200;
		fCtlConnTypeCombo.setLayoutData(gridData);
	}
	private void setupSettingsGroup(Composite parent) {
		Group group = new Group(parent, SWT.NONE);
		group.setText(ViewMessages.SETTINGS + ":"); //$NON-NLS-1$
		group.setLayout(new GridLayout());
		group.setLayoutData(new GridData(GridData.FILL_BOTH));
		fPageBook=new PageBook(group,SWT.NONE);
		fPageBook.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		
		
		for (int i = 0; i < fPages.length; i++) {
			fPages[i].createControl(fPageBook);
		}
	}
	private void setupListeners() {
		fCtlConnTypeCombo.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent event) {
				selectPage(fCtlConnTypeCombo.getSelectionIndex());
			}
		});
	}
	public ITerminalConnector getConnector() {
		if(fSelectedConnector>=0)
			return fConnectors[fSelectedConnector];
		return null;
	}
	private void selectPage(int index) {
		fSelectedConnector=index;
		Control[] pages=fPageBook.getChildren();
		fPageBook.showPage(pages[fSelectedConnector]);
	}
	protected IDialogSettings getDialogBoundsSettings() {
		IDialogSettings ds=TerminalViewPlugin.getDefault().getDialogSettings();
		fDialogSettings = ds.getSection(getClass().getName()); 
		if (fDialogSettings == null) {
			fDialogSettings = ds.addNewSection(getClass().getName()); 
		}
		return fDialogSettings;
	}
}
