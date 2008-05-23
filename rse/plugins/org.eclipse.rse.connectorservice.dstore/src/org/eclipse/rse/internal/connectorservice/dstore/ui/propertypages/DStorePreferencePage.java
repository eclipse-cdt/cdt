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
 * David McKnight  (IBM)         - [220123][dstore] Configurable timeout on irresponsiveness
 * David McKnight   (IBM)        - [228334][api][breaking][dstore] Default DataStore connection timeout is too short
 * David Dykstal (IBM) - [232317] add help for this preference page
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
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;
import org.eclipse.ui.PlatformUI;

public class DStorePreferencePage extends PreferencePage implements IWorkbenchPreferencePage, Listener
{

	private Text _connectionTimeout;
	private Button _doKeepaliveButton;
	private Text _keepaliveResponseTimeout;
	private Text _socketReadTimeout;
	
//	private Button _cacheRemoteClassesButton;
	private Button _showMismatchedServerWarningButton;
	
	protected Control createContents(Composite gparent) {

		PlatformUI.getWorkbench().getHelpSystem().setHelp(getControl(), RSEUIPlugin.HELPPREFIX + "DStorePreferencePage"); //$NON-NLS-1$

		Composite parent = SystemWidgetHelpers.createComposite(gparent, 2);
	
		GridLayout layout = new GridLayout();
		layout.numColumns = 2;
		parent.setLayout(layout);
		parent.setLayoutData(new GridData(GridData.HORIZONTAL_ALIGN_BEGINNING));

		Composite connectComposite = SystemWidgetHelpers.createComposite(parent, 2);	
		
		SystemWidgetHelpers.createLabel(connectComposite, DStoreResources.RESID_PREFERENCE_CONNECTION_TIMEOUT_LABEL);
		
		_connectionTimeout = new Text(connectComposite, SWT.BORDER);
		//GridData gd = new GridData(SWT.BEGINNING, SWT.CENTER, false, false);
		GridData gd = new GridData(GridData.HORIZONTAL_ALIGN_BEGINNING);
		gd.widthHint = 75;
		gd.horizontalSpan =1;
		_connectionTimeout.setLayoutData(gd);		
		_connectionTimeout.setTextLimit(10);
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
			
		
//		_cacheRemoteClassesButton = SystemWidgetHelpers.createCheckBox(parent, DStoreResources.RESID_PREFERENCE_CACHE_REMOTE_CLASSES_LABEL, this);
//		_cacheRemoteClassesButton.setToolTipText(DStoreResources.RESID_PREFERENCE_CACHE_REMOTE_CLASSES_TOOLTIP);
//		_cacheRemoteClassesButton.setLayoutData(new GridData(SWT.BEGINNING, SWT.BEGINNING, true, false));
//		((GridData)_cacheRemoteClassesButton.getLayoutData()).horizontalSpan = 2;

		_showMismatchedServerWarningButton = SystemWidgetHelpers.createCheckBox(parent, DStoreResources.RESID_PREFERENCE_SHOW_MISMATCHED_SERVER_LABEL, this);
		_showMismatchedServerWarningButton.setToolTipText(DStoreResources.RESID_PREFERENCE_SHOW_MISMATCHED_SERVER_TOOLTIP);
		_showMismatchedServerWarningButton.setLayoutData(new GridData(SWT.BEGINNING, SWT.BEGINNING, true, false));
		((GridData)_showMismatchedServerWarningButton.getLayoutData()).horizontalSpan = 2;		
		
		
		// keepalive stuff
		Group keepaliveGroup = SystemWidgetHelpers.createGroupComposite(parent, 2, DStoreResources.RESID_PREFERENCE_KEEPALIVE_LABEL);
        layout = new GridLayout();
        layout.numColumns = 2;
        keepaliveGroup.setLayout(layout);
        GridData data = new GridData(GridData.HORIZONTAL_ALIGN_FILL
                | GridData.GRAB_HORIZONTAL);
        keepaliveGroup.setLayoutData(data);

		
		
		_doKeepaliveButton = SystemWidgetHelpers.createCheckBox(keepaliveGroup, DStoreResources.RESID_PREFERENCE_DO_KEEPALIVE_LABEL, this);
		_doKeepaliveButton.setToolTipText(DStoreResources.RESID_PREFERENCE_DO_KEEPALIVE_TOOLTIP);
		_doKeepaliveButton.setLayoutData(new GridData(SWT.BEGINNING, SWT.BEGINNING, true, false));
		((GridData)_doKeepaliveButton.getLayoutData()).horizontalSpan = 2;
		
		
		SystemWidgetHelpers.createLabel(keepaliveGroup, DStoreResources.RESID_PREFERENCE_KEEPALIVE_SOCKET_READ_TIMEOUT_LABEL);
		
		_socketReadTimeout = new Text(keepaliveGroup, SWT.BORDER);
		gd = new GridData(SWT.BEGINNING, SWT.CENTER, false, false);
		gd.widthHint = 75;
		gd.horizontalSpan =1;
		_socketReadTimeout.setLayoutData(gd);
		_socketReadTimeout.setTextLimit(10);
		_socketReadTimeout.setToolTipText(DStoreResources.RESID_PREFERENCE_KEEPALIVE_SOCKET_READ_TIMEOUT_TOOLTIP);
		_socketReadTimeout.addVerifyListener(new VerifyListener()
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
		
		SystemWidgetHelpers.createLabel(keepaliveGroup, DStoreResources.RESID_PREFERENCE_KEEPALIVE_RESPONSE_TIMEOUT_LABEL);
		
		_keepaliveResponseTimeout = new Text(keepaliveGroup, SWT.BORDER);
		gd = new GridData(SWT.BEGINNING, SWT.CENTER, false, false);
		gd.widthHint = 75;
		gd.horizontalSpan =1;
		_keepaliveResponseTimeout.setLayoutData(gd);
		_keepaliveResponseTimeout.setTextLimit(10);
		_keepaliveResponseTimeout.setToolTipText(DStoreResources.RESID_PREFERENCE_KEEPALIVE_RESPONSE_TIMEOUT_TOOLTIP);
		_keepaliveResponseTimeout.addVerifyListener(new VerifyListener()
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
			timeout = store.getDefaultInt(IUniversalDStoreConstants.RESID_PREF_SOCKET_TIMEOUT);
			store.setDefault(IUniversalDStoreConstants.RESID_PREF_SOCKET_TIMEOUT, timeout);
		}
		_connectionTimeout.setText(""+timeout); //$NON-NLS-1$
		
		
		// cache remote classes
		boolean cacheRemoteClasses = false;
		if (store.contains(IUniversalDStoreConstants.RESID_PREF_CACHE_REMOTE_CLASSES)) {
			cacheRemoteClasses = store.getBoolean(IUniversalDStoreConstants.RESID_PREF_CACHE_REMOTE_CLASSES);
		}
		else {
			cacheRemoteClasses = store.getDefaultBoolean(IUniversalDStoreConstants.RESID_PREF_CACHE_REMOTE_CLASSES);
			store.setDefault(IUniversalDStoreConstants.RESID_PREF_CACHE_REMOTE_CLASSES, cacheRemoteClasses);
		}
//		_cacheRemoteClassesButton.setSelection(cacheRemoteClasses);
		
		
		// do keepalive
		boolean doKeepalive = false;
		if (store.contains(IUniversalDStoreConstants.RESID_PREF_DO_KEEPALIVE)){
			doKeepalive = store.getBoolean(IUniversalDStoreConstants.RESID_PREF_DO_KEEPALIVE);		
		}
		else {
			doKeepalive = store.getDefaultBoolean(IUniversalDStoreConstants.RESID_PREF_DO_KEEPALIVE);	
		}
		_doKeepaliveButton.setSelection(doKeepalive);
		
		int socketTimeout = 0;
		if (store.contains(IUniversalDStoreConstants.RESID_PREF_SOCKET_READ_TIMEOUT)){
			socketTimeout = store.getInt(IUniversalDStoreConstants.RESID_PREF_SOCKET_READ_TIMEOUT);
		}
		else {
			socketTimeout = store.getDefaultInt(IUniversalDStoreConstants.RESID_PREF_SOCKET_READ_TIMEOUT);
		}
		_socketReadTimeout.setText(""+socketTimeout); //$NON-NLS-1$
		_socketReadTimeout.setEnabled(doKeepalive);
		
		int keepaliveTimeout = 0;
		if (store.contains(IUniversalDStoreConstants.RESID_PREF_KEEPALIVE_RESPONSE_TIMEOUT)){
			keepaliveTimeout = store.getInt(IUniversalDStoreConstants.RESID_PREF_KEEPALIVE_RESPONSE_TIMEOUT);	
		}
		else {
			keepaliveTimeout = store.getDefaultInt(IUniversalDStoreConstants.RESID_PREF_KEEPALIVE_RESPONSE_TIMEOUT);
		}
		_keepaliveResponseTimeout.setText(""+keepaliveTimeout); //$NON-NLS-1$
		_keepaliveResponseTimeout.setEnabled(doKeepalive);
		
		// show mismatched server warning
		boolean showMismatchedWarning = false;
		if (store.contains(IUniversalDStoreConstants.ALERT_MISMATCHED_SERVER)){
			showMismatchedWarning = store.getBoolean(IUniversalDStoreConstants.ALERT_MISMATCHED_SERVER);
		}
		else {
			showMismatchedWarning = store.getDefaultBoolean(IUniversalDStoreConstants.ALERT_MISMATCHED_SERVER);
		}
		_showMismatchedServerWarningButton.setSelection(showMismatchedWarning);
	}
	
	protected void performApply() {
		super.performApply();
		applyValues();
	}
	
	protected void performDefaults() {
		super.performDefaults();	
		
		IPreferenceStore store = RSEUIPlugin.getDefault().getPreferenceStore();

		int timeout = store.getDefaultInt(IUniversalDStoreConstants.RESID_PREF_SOCKET_TIMEOUT);
		_connectionTimeout.setText(""+timeout); //$NON-NLS-1$
		
		// do keepalive
		boolean doKeepalive = store.getDefaultBoolean(IUniversalDStoreConstants.RESID_PREF_DO_KEEPALIVE);
		_doKeepaliveButton.setSelection(doKeepalive);
		
		// socket read timeout 
		int socketTimeout = store.getDefaultInt(IUniversalDStoreConstants.RESID_PREF_SOCKET_READ_TIMEOUT);

		_socketReadTimeout.setText(""+socketTimeout); //$NON-NLS-1$
		_socketReadTimeout.setEnabled(doKeepalive);
		
		// keepalive response timeout
		int keepaliveTimeout = store.getDefaultInt(IUniversalDStoreConstants.RESID_PREF_KEEPALIVE_RESPONSE_TIMEOUT);
		_keepaliveResponseTimeout.setText(""+keepaliveTimeout); //$NON-NLS-1$
		_keepaliveResponseTimeout.setEnabled(doKeepalive);
		
		
		// show mismatched server warning
		boolean showMismatchedWarning = store.getDefaultBoolean(IUniversalDStoreConstants.ALERT_MISMATCHED_SERVER);
		_showMismatchedServerWarningButton.setSelection(showMismatchedWarning);
		
		// cache remote classes
//		boolean cacheRemoteClasses = store.getDefaultBoolean(IUniversalDStoreConstants.RESID_PREF_CACHE_REMOTE_CLASSES);
//		_cacheRemoteClassesButton.setSelection(cacheRemoteClasses);
		
	}

	public void init(IWorkbench workbench) {

	}

	public void handleEvent(Event event) {
		if (event.widget == _doKeepaliveButton){
			boolean isEnabled = _doKeepaliveButton.getSelection();
			
			_socketReadTimeout.setEnabled(isEnabled);
			_keepaliveResponseTimeout.setEnabled(isEnabled);
			
		}
	}

	private void applyValues()
	{
		IPreferenceStore store = RSEUIPlugin.getDefault().getPreferenceStore();
		
		// timeout
		String timeoutStr = _connectionTimeout.getText();
		int timeout = Integer.parseInt(timeoutStr);
		store.setValue(IUniversalDStoreConstants.RESID_PREF_SOCKET_TIMEOUT, timeout);	
		
		// do keepalive
		boolean doKeepalive = _doKeepaliveButton.getSelection();
		store.setValue(IUniversalDStoreConstants.RESID_PREF_DO_KEEPALIVE, doKeepalive);
		
		
		// socket read timeout 
		String socketTimeoutStr = _socketReadTimeout.getText();
		int socketTimeout = Integer.parseInt(socketTimeoutStr);
		store.setValue(IUniversalDStoreConstants.RESID_PREF_SOCKET_READ_TIMEOUT, socketTimeout);
		
		// keepalive response timeout
		String keepaliveTimeoutStr = _keepaliveResponseTimeout.getText();
		int keepaliveTimeout = Integer.parseInt(keepaliveTimeoutStr);
		store.setValue(IUniversalDStoreConstants.RESID_PREF_KEEPALIVE_RESPONSE_TIMEOUT, keepaliveTimeout);
		
		
		// cache remote classes
//		boolean cacheRemoteClasses = _cacheRemoteClassesButton.getSelection();
//		store.setValue(IUniversalDStoreConstants.RESID_PREF_CACHE_REMOTE_CLASSES, cacheRemoteClasses);				
		
		// show mismatched server warning
		boolean showMismatchedWarning = _showMismatchedServerWarningButton.getSelection();
		store.setValue(IUniversalDStoreConstants.ALERT_MISMATCHED_SERVER, showMismatchedWarning);

	}
	
	public boolean performOk() {
		applyValues();
		return super.performOk();
	}

	
	
}
