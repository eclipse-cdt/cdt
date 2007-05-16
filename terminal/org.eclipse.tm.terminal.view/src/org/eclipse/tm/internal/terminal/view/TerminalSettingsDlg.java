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
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.tm.internal.terminal.provisional.api.ISettingsPage;
import org.eclipse.tm.internal.terminal.provisional.api.ITerminalConnector;

class TerminalSettingsDlg extends Dialog {
	private Combo fCtlConnTypeCombo;
	private final ITerminalConnector[] fConnectors;
	private final ISettingsPage[] fPages;
	/**
	 * Maps the fConnectors index to the fPages index
	 */
	private final int[] fPageIndex;
	private int fNPages;
	private int fSelectedConnector;
	private PageBook fPageBook;
	private IDialogSettings fDialogSettings;

	public TerminalSettingsDlg(Shell shell, ITerminalConnector[] connectors, ITerminalConnector connector) {
		super(shell);
		fConnectors=connectors;
		fPages=new ISettingsPage[fConnectors.length];
		fPageIndex=new int[fConnectors.length];
		fSelectedConnector=-1;
		for (int i = 0; i < fConnectors.length; i++) {
			if(fConnectors[i]==connector)
				fSelectedConnector=i;
		}
	}
	ISettingsPage getPage(int i) {
		if(fPages[i]==null) {
			try {
				fPages[i]=fConnectors[i].makeSettingsPage();
				// TODO: what happens if an error occurs while
				// the control is partly created?
				fPages[i].createControl(fPageBook);
			} catch (final Exception e) {
				// create a error message
				fPages[i]=new ISettingsPage(){
					public void createControl(Composite parent) {
						Label l=new Label(parent,SWT.WRAP);
						l.setText("Error"); //$NON-NLS-1$
						l.setForeground(l.getDisplay().getSystemColor(SWT.COLOR_RED));
						MessageDialog.openError(getShell(), "Initialization Problems!", e.getLocalizedMessage()); //$NON-NLS-1$
					}
					public void loadSettings() {}
					public void saveSettings() {}
					public boolean validateSettings() {return false;}
				};
				fPages[i].createControl(fPageBook);
			}
			fPageIndex[i]=fNPages++;
			resize();
		}
		return fPages[i];
		
	}
	void resize() {
		Point size=getShell().getSize();
		Point newSize=getShell().computeSize(SWT.DEFAULT, SWT.DEFAULT,true);
		newSize.x=Math.max(newSize.x,size.x);
		newSize.y=Math.max(newSize.y,size.y);
		if(newSize.x!=size.x || newSize.y!=size.y) {
			setShellSize(newSize);
		} else {
			fPageBook.getParent().layout();
		}
	}
	/**
 	 * Increase the size of this dialog's <code>Shell</code> by the specified amounts.
 	 * Do not increase the size of the Shell beyond the bounds of the Display.
 	 */
	protected void setShellSize(Point size) {
		Rectangle bounds = getShell().getMonitor().getClientArea();
		getShell().setSize(Math.min(size.x, bounds.width), Math.min(size.y, bounds.height));
	}

	protected void okPressed() {
		if (!validateSettings())
			return;
		if(fSelectedConnector>=0) {
			getPage(fSelectedConnector).saveSettings();
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
		for (int i = 0; i < fConnectors.length; i++) {
			String name=fConnectors[i].getName();
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
		return getPage(fSelectedConnector).validateSettings();
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
		getPage(index);
		Control[] pages=fPageBook.getChildren();
		fPageBook.showPage(pages[fPageIndex[fSelectedConnector]]);
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
