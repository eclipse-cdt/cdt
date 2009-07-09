/*******************************************************************************
 * Copyright (c) 2003, 2009 Wind River Systems, Inc. and others.
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
 * Martin Oberhuber (Wind River) - [168197] Replace JFace MessagDialog by SWT MessageBox
 * Martin Oberhuber (Wind River) - [168186] Add Terminal User Docs
 * Michael Scharf (Wind River) - [196454] Initial connection settings dialog should not be blank
 * Michael Scharf (Wind River) - [240023] Get rid of the terminal's "Pin" button
 * Martin Oberhuber (Wind River) - [206917] Add validation for Terminal Settings
 * Uwe Stieber (Wind River) - [282996] [terminal][api] Add "hidden" attribute to terminal connector extension point
 *******************************************************************************/
package org.eclipse.tm.internal.terminal.view;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.IDialogSettings;
import org.eclipse.osgi.util.NLS;
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
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.eclipse.tm.internal.terminal.provisional.api.ISettingsPage;
import org.eclipse.tm.internal.terminal.provisional.api.ITerminalConnector;
import org.eclipse.ui.PlatformUI;

class TerminalSettingsDlg extends Dialog {
	private Combo fCtlConnTypeCombo;
	private Text fTerminalTitleText;
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
	private String fTerminalTitle;
	private String fTitle=ViewMessages.TERMINALSETTINGS;

	public TerminalSettingsDlg(Shell shell, ITerminalConnector[] connectors, ITerminalConnector connector) {
		super(shell);
		fConnectors=getValidConnectors(connectors);
		fPages=new ISettingsPage[fConnectors.length];
		fPageIndex=new int[fConnectors.length];
		fSelectedConnector=-1;
		for (int i = 0; i < fConnectors.length; i++) {
			if(fConnectors[i]==connector)
				fSelectedConnector=i;
		}
	}
	public void setTitle(String title) {
		fTitle=title;
	}
	/**
	 * @param connectors
	 * @return connectors excluding connectors with errors
	 */
	private ITerminalConnector[] getValidConnectors(ITerminalConnector[] connectors) {
		List list=new ArrayList(Arrays.asList(connectors));
		for (Iterator iterator = list.iterator(); iterator.hasNext();) {
			ITerminalConnector info = (ITerminalConnector) iterator.next();
			if(info.isInitialized() && info.getInitializationErrorMessage()!=null || info.isHidden())
				iterator.remove();
		}
		connectors=(ITerminalConnector[]) list.toArray(new ITerminalConnector[list.size()]);
		return connectors;
	}
	ISettingsPage getPage(int i) {
		if(fPages[i]==null) {
			if(fConnectors[i].getInitializationErrorMessage()!=null) {
				// create a error message
				final ITerminalConnector conn=fConnectors[i];
				fPages[i]=new ISettingsPage(){
					public void createControl(Composite parent) {
						Label l=new Label(parent,SWT.WRAP);
						String error=NLS.bind(ViewMessages.CONNECTOR_NOT_AVAILABLE,conn.getName());
						l.setText(error);
						l.setForeground(l.getDisplay().getSystemColor(SWT.COLOR_RED));
						String msg = NLS.bind(ViewMessages.CANNOT_INITIALIZE, conn.getName(), conn.getInitializationErrorMessage());
						// [168197] Replace JFace MessagDialog by SWT MessageBox
						//MessageDialog.openError(getShell(), error, msg);
						MessageBox mb = new MessageBox(getShell(), SWT.ICON_ERROR | SWT.OK);
						mb.setText(error);
						mb.setMessage(msg);
						mb.open();
					}
					public void loadSettings() {}
					public void saveSettings() {}
					public boolean validateSettings() {return false;}
				};
			} else {
				fPages[i]=fConnectors[i].makeSettingsPage();
			}
			// TODO: what happens if an error occurs while
			// the control is partly created?
			fPages[i].createControl(fPageBook);
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
		if (!validateSettings()) {
			String strTitle = ViewMessages.TERMINALSETTINGS;
			MessageBox mb = new MessageBox(getShell(), SWT.ICON_ERROR | SWT.OK);
			mb.setText(strTitle);
			mb.setMessage(ViewMessages.INVALID_SETTINGS);
			mb.open();
			return;
		}
		if(fSelectedConnector>=0) {
			getPage(fSelectedConnector).saveSettings();
		}
		fTerminalTitle=fTerminalTitleText.getText();
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

		newShell.setText(fTitle);
	}
	protected Control createDialogArea(Composite parent) {
		Composite ctlComposite = (Composite) super.createDialogArea(parent);
		PlatformUI.getWorkbench().getHelpSystem().setHelp(ctlComposite, TerminalViewPlugin.HELPPREFIX + "terminal_settings"); //$NON-NLS-1$

		setupPanel(ctlComposite);
		setupListeners();
		initFields();

		return ctlComposite;
	}
	public void create() {
		super.create();
		// initialize the OK button after creating the all dialog elements
		updateOKButton();
	}
	private void initFields() {
		// Load controls
		for (int i = 0; i < fConnectors.length; i++) {
			fCtlConnTypeCombo.add(fConnectors[i].getName());
		}
		int selectedConnector=getInitialConnector();
		if(selectedConnector>=0) {
			fCtlConnTypeCombo.select(selectedConnector);
			selectPage(selectedConnector);
		}
	}
	/**
	 * @return the connector to show when the dialog opens
	 */
	private int getInitialConnector() {
		// if there is a selection, use it
		if(fSelectedConnector>=0)
			return fSelectedConnector;
		// try the telnet connector, because it is the cheapest
		for (int i = 0; i < fConnectors.length; i++) {
			if("org.eclipse.tm.internal.terminal.telnet.TelnetConnector".equals(fConnectors[i].getId())) //$NON-NLS-1$
				return i;
		}
		// if no telnet connector available, use the first one in the list
		if(fConnectors.length>0)
			return 0;
		return -1;
	}
	private boolean validateSettings() {
		if(fSelectedConnector<0)
			return true;
		return getPage(fSelectedConnector).validateSettings();
	}
	private void setupPanel(Composite wndParent) {
		setupSettingsTypePanel(wndParent);
		if(fConnectors.length>0) {
			setupConnTypePanel(wndParent);
			setupSettingsGroup(wndParent);
		}
	}
	private void setupSettingsTypePanel(Composite wndParent) {
		Group wndGroup;
		GridLayout gridLayout;

		wndGroup = new Group(wndParent, SWT.NONE);
		gridLayout = new GridLayout(2, false);
		wndGroup.setLayout(gridLayout);
		wndGroup.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		wndGroup.setText(ViewMessages.VIEW_SETTINGS);


		Label label=new Label(wndGroup,SWT.NONE);
		label.setText(ViewMessages.VIEW_TITLE);
		label.setLayoutData(new GridData(GridData.BEGINNING));

		fTerminalTitleText = new Text(wndGroup, SWT.BORDER);
		fTerminalTitleText.setText(fTerminalTitle);
		fTerminalTitleText.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
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
		if(fCtlConnTypeCombo==null)
			return;
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
		updateOKButton();

	}
	/**
	 * enables the OK button if the user can create a connection
	 */
	private void updateOKButton() {
		// TODO: allow contributions to enable the OK button
		// enable the OK button if we have a valid connection selected
		if(getButton(IDialogConstants.OK_ID)!=null) {
			boolean enable=false;
			if(getConnector()!=null)
				enable=getConnector().getInitializationErrorMessage()==null;
			// enable the OK button if no connectors are available
			if(!enable && fConnectors.length==0)
				enable=true;
			getButton(IDialogConstants.OK_ID).setEnabled(enable);
		}
	}
	protected IDialogSettings getDialogBoundsSettings() {
		IDialogSettings ds=TerminalViewPlugin.getDefault().getDialogSettings();
		fDialogSettings = ds.getSection(getClass().getName());
		if (fDialogSettings == null) {
			fDialogSettings = ds.addNewSection(getClass().getName());
		}
		return fDialogSettings;
	}
	public void setTerminalTitle(String partName) {
		fTerminalTitle=partName;

	}
	public String getTerminalTitle() {
		return fTerminalTitle;
	}
}
