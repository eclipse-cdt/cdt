/********************************************************************************
 * Copyright (c) 2008 IBM Corporation. All rights reserved.
 * This program and the accompanying materials are made available under the terms
 * of the Eclipse Public License v1.0 which accompanies this distribution, and is 
 * available at http://www.eclipse.org/legal/epl-v10.html
 * 
 * Initial Contributors:
 * The following IBM employees contributed to the Remote System Explorer
 * component that contains this file: David McKnight.
 * 
 * Contributors:
 * David McKnight   (IBM)        - [216596] dstore preferences (timeout, and others)
 ********************************************************************************/
package org.eclipse.rse.internal.connectorservice.dstore.ui.propertypages;

import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.preference.PreferencePage;
import org.eclipse.rse.connectorservice.dstore.IUniversalDStoreConstants;
import org.eclipse.rse.internal.connectorservice.dstore.DStoreResources;
import org.eclipse.rse.ui.RSEUIPlugin;
import org.eclipse.rse.ui.SystemWidgetHelpers;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.VerifyEvent;
import org.eclipse.swt.events.VerifyListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;

public class DStorePreferencePage extends PreferencePage implements IWorkbenchPreferencePage, Listener
{

	private Text _connectionTimeout;
	private Button _doKeepaliveButton;
	private Button _cacheRemoteClassesButton;
	private Button _showMismatchedServerWarningButton;
	
	protected Control createContents(Composite gparent) {
		Composite parent = SystemWidgetHelpers.createComposite(gparent, 2);
		
		GridLayout layout = new GridLayout();
		layout.numColumns = 2;
		parent.setLayout(layout);
		parent.setLayoutData(new GridData(GridData.HORIZONTAL_ALIGN_BEGINNING));
		
		SystemWidgetHelpers.createLabel(parent, DStoreResources.RESID_PREFERENCE_CONNECTION_TIMEOUT_LABEL);
		
		_connectionTimeout = new Text(parent, SWT.BORDER);
		GridData gd = new GridData(SWT.BEGINNING, SWT.CENTER, false, false);
		gd.widthHint = 75;
		_connectionTimeout.setLayoutData(gd);
		_connectionTimeout.setTextLimit(5);
		_connectionTimeout.setToolTipText(DStoreResources.RESID_PREFERENCE_CONNECTION_TIMEOUT_TOOLTIP);
		_connectionTimeout.addVerifyListener(new VerifyListener()
		{
			public void verifyText(VerifyEvent e)
			{
				e.doit = true;
				for (int loop = 0; loop < e.text.length(); loop++)
				{
					if (!Character.isDigit(e.text.charAt(loop)))
						e.doit = false;
				}
			}
		});
		
		_doKeepaliveButton = SystemWidgetHelpers.createCheckBox(parent, DStoreResources.RESID_PREFERENCE_DO_KEEPALIVE_LABEL, this);
		_doKeepaliveButton.setToolTipText(DStoreResources.RESID_PREFERENCE_DO_KEEPALIVE_TOOLTIP);
		_doKeepaliveButton.setLayoutData(new GridData(SWT.BEGINNING, SWT.BEGINNING, true, false));
		((GridData)_doKeepaliveButton.getLayoutData()).horizontalSpan = 2;
		
		_cacheRemoteClassesButton = SystemWidgetHelpers.createCheckBox(parent, DStoreResources.RESID_PREFERENCE_CACHE_REMOTE_CLASSES_LABEL, this);
		_cacheRemoteClassesButton.setToolTipText(DStoreResources.RESID_PREFERENCE_CACHE_REMOTE_CLASSES_TOOLTIP);
		_cacheRemoteClassesButton.setLayoutData(new GridData(SWT.BEGINNING, SWT.BEGINNING, true, false));
		((GridData)_cacheRemoteClassesButton.getLayoutData()).horizontalSpan = 2;

		_showMismatchedServerWarningButton = SystemWidgetHelpers.createCheckBox(parent, DStoreResources.RESID_PREFERENCE_SHOW_MISMATCHED_SERVER_LABEL, this);
		_showMismatchedServerWarningButton.setToolTipText(DStoreResources.RESID_PREFERENCE_SHOW_MISMATCHED_SERVER_TOOLTIP);
		_showMismatchedServerWarningButton.setLayoutData(new GridData(SWT.BEGINNING, SWT.BEGINNING, true, false));
		((GridData)_showMismatchedServerWarningButton.getLayoutData()).horizontalSpan = 2;		
		
		
		initControls();
		return parent;
	}
	
	private void initControls()
	{
		IPreferenceStore store = RSEUIPlugin.getDefault().getPreferenceStore();

		
		// timeout
		int timeout = 0;
		if (store.contains(IUniversalDStoreConstants.RESID_PREF_SOCKET_TIMEOUT)){
			timeout = store.getInt(IUniversalDStoreConstants.RESID_PREF_SOCKET_TIMEOUT);
		}
		else { 
			timeout = IUniversalDStoreConstants.DEFAULT_PREF_SOCKET_TIMEOUT;
			store.setDefault(IUniversalDStoreConstants.RESID_PREF_SOCKET_TIMEOUT, timeout);
		}
		_connectionTimeout.setText(""+timeout); //$NON-NLS-1$
		
		
		// cache remote classes
		boolean cacheRemoteClasses = false;
		if (store.contains(IUniversalDStoreConstants.RESID_PREF_CACHE_REMOTE_CLASSES)) {
			cacheRemoteClasses = store.getBoolean(IUniversalDStoreConstants.RESID_PREF_CACHE_REMOTE_CLASSES);
		}
		else {
			cacheRemoteClasses = IUniversalDStoreConstants.DEFAULT_PREF_CACHE_REMOTE_CLASSES;
			store.setDefault(IUniversalDStoreConstants.RESID_PREF_CACHE_REMOTE_CLASSES, cacheRemoteClasses);
		}
		_cacheRemoteClassesButton.setSelection(cacheRemoteClasses);
		
		
		// do keepalive
		boolean doKeepalive = false;
		if (store.contains(IUniversalDStoreConstants.RESID_PREF_DO_KEEPALIVE)){
			doKeepalive = store.getBoolean(IUniversalDStoreConstants.RESID_PREF_DO_KEEPALIVE);		
		}
		else {
			doKeepalive = IUniversalDStoreConstants.DEFAULT_PREF_DO_KEEPALIVE;
			store.setDefault(IUniversalDStoreConstants.RESID_PREF_DO_KEEPALIVE, doKeepalive);
			
		}
		_doKeepaliveButton.setSelection(doKeepalive);
		
		// show mismatched server warning
		boolean showMismatchedWarning = false;
		if (store.contains(IUniversalDStoreConstants.ALERT_MISMATCHED_SERVER)){
			showMismatchedWarning = store.getBoolean(IUniversalDStoreConstants.ALERT_MISMATCHED_SERVER);
		}
		else {
			showMismatchedWarning = IUniversalDStoreConstants.DEFAULT_ALERT_MISMATCHED_SERVER;
			store.setDefault(IUniversalDStoreConstants.ALERT_MISMATCHED_SERVER, showMismatchedWarning);

		}
		_showMismatchedServerWarningButton.setSelection(showMismatchedWarning);
	}
	
	protected void performApply() {
		super.performApply();
		IPreferenceStore store = RSEUIPlugin.getDefault().getPreferenceStore();
		
		// timeout
		String timeoutStr = _connectionTimeout.getText();
		int timeout = Integer.parseInt(timeoutStr);
		store.setValue(IUniversalDStoreConstants.RESID_PREF_SOCKET_TIMEOUT, timeout);	
		
		// do keepalive
		boolean doKeepalive = _doKeepaliveButton.getSelection();
		store.setValue(IUniversalDStoreConstants.RESID_PREF_DO_KEEPALIVE, doKeepalive);
		
		// cache remote classes
		boolean cacheRemoteClasses = _cacheRemoteClassesButton.getSelection();
		store.setValue(IUniversalDStoreConstants.RESID_PREF_CACHE_REMOTE_CLASSES, cacheRemoteClasses);				
		
		// show mismatched server warning
		boolean showMismatchedWarning = _showMismatchedServerWarningButton.getSelection();
		store.setValue(IUniversalDStoreConstants.ALERT_MISMATCHED_SERVER, showMismatchedWarning);				
	}
	
	protected void performDefaults() {
		super.performDefaults();	
		
		int timeout = IUniversalDStoreConstants.DEFAULT_PREF_SOCKET_TIMEOUT;
		_connectionTimeout.setText(""+timeout); //$NON-NLS-1$
		
		// do keepalive
		boolean doKeepalive = IUniversalDStoreConstants.DEFAULT_PREF_DO_KEEPALIVE;
		_doKeepaliveButton.setSelection(doKeepalive);
		
		// show mismatched server warning
		boolean showMismatchedWarning = IUniversalDStoreConstants.DEFAULT_ALERT_MISMATCHED_SERVER;
		_showMismatchedServerWarningButton.setSelection(showMismatchedWarning);
		
		// cache remote classes
		boolean cacheRemoteClasses = IUniversalDStoreConstants.DEFAULT_PREF_CACHE_REMOTE_CLASSES;
		_cacheRemoteClassesButton.setSelection(cacheRemoteClasses);
		
	}

	public void init(IWorkbench workbench) {

	}

	public void handleEvent(Event event) {
		// TODO Auto-generated method stub
		
	}

	public boolean performOk() {
		IPreferenceStore store = RSEUIPlugin.getDefault().getPreferenceStore();
		
		// timeout
		String timeoutStr = _connectionTimeout.getText();
		int timeout = Integer.parseInt(timeoutStr);
		store.setValue(IUniversalDStoreConstants.RESID_PREF_SOCKET_TIMEOUT, timeout);	
		
		// do keepalive
		boolean doKeepalive = _doKeepaliveButton.getSelection();
		store.setValue(IUniversalDStoreConstants.RESID_PREF_DO_KEEPALIVE, doKeepalive);
		
		// cache remote classes
		boolean cacheRemoteClasses = _cacheRemoteClassesButton.getSelection();
		store.setValue(IUniversalDStoreConstants.RESID_PREF_CACHE_REMOTE_CLASSES, cacheRemoteClasses);				
		
		// show mismatched server warning
		boolean showMismatchedWarning = _showMismatchedServerWarningButton.getSelection();
		store.setValue(IUniversalDStoreConstants.ALERT_MISMATCHED_SERVER, showMismatchedWarning);
		return super.performOk();
	}

	
	
}
