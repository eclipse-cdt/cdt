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

package org.eclipse.rse.ui.propertypages;

import org.eclipse.jface.preference.FieldEditorPreferencePage;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.preference.IntegerFieldEditor;
import org.eclipse.rse.core.SystemPlugin;
import org.eclipse.rse.core.comm.SystemCommunicationsDaemon;
import org.eclipse.rse.ui.ISystemMessages;
import org.eclipse.rse.ui.ISystemPreferencesConstants;
import org.eclipse.rse.ui.Mnemonics;
import org.eclipse.rse.ui.SystemResources;
import org.eclipse.rse.ui.SystemWidgetHelpers;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;


/**
 * Preference page for generic Remote System communication preferences
 */
public class SystemCommunicationsPreferencePage extends FieldEditorPreferencePage implements IWorkbenchPreferencePage
{


	private IntegerFieldEditor portEditor;	
	
	/**
	 * Constructor
	 */
	public SystemCommunicationsPreferencePage() 
	{
		super(GRID);
		setPreferenceStore(SystemPlugin.getDefault().getPreferenceStore());
		setDescription(SystemResources.RESID_PREF_COMMUNICATIONS_TITLE);
	}

	/**
	 * Configure the composite. We intercept to set the help.
	 */
	public void createControl(Composite parent) 
	{
		super.createControl(parent);
	}
	
	
	/**
	 * 
	 */
	protected void createFieldEditors() 
	{
		Composite parent= getFieldEditorParent();

		// Auto-start the daemon preference
		SystemBooleanFieldEditor autoStartEditor = new SystemBooleanFieldEditor(
			ISystemPreferencesConstants.DAEMON_AUTOSTART,
			SystemResources.RESID_PREF_DAEMON_AUTOSTART_LABEL,
			parent
		);
		autoStartEditor.setToolTipText(SystemResources.RESID_PREF_DAEMON_AUTOSTART_TOOLTIP);
		addField(autoStartEditor);
		
				
		// Daemon port preference
		portEditor = new IntegerFieldEditor(
			ISystemPreferencesConstants.DAEMON_PORT,
			SystemResources.RESID_PREF_DAEMON_PORT_LABEL,
			parent
		);
		portEditor.setValidRange(1, 65536);
		portEditor.setErrorMessage(SystemPlugin.getPluginMessage(ISystemMessages.MSG_VALIDATE_PORT_NOTVALID).getLevelOneText());
		addField(portEditor);
		portEditor.getTextControl(parent).setToolTipText(SystemResources.RESID_PREF_DAEMON_PORT_TOOLTIP);

		SystemWidgetHelpers.setHelp(portEditor.getTextControl(parent), SystemPlugin.HELPPREFIX + "cmmp0000");

		
        (new Mnemonics()).setOnPreferencePage(true).setMnemonics(parent);	
		SystemWidgetHelpers.setCompositeHelp(parent, SystemPlugin.HELPPREFIX + "cmmp0000");

	}

	/**
	 * Inherited method.
	 */
	public void init(IWorkbench workbench) 
	{
	}

	/**
	 * Set default preferences for the communications preference page.
	 * 
	 * @param store PreferenceStore used for this preference page.
	 */
	public static void initDefaults(IPreferenceStore store) 
	{
		store.setDefault(ISystemPreferencesConstants.DAEMON_AUTOSTART, ISystemPreferencesConstants.DEFAULT_DAEMON_AUTOSTART);
		store.setDefault(ISystemPreferencesConstants.DAEMON_PORT, ISystemPreferencesConstants.DEFAULT_DAEMON_PORT);
	}
	
	/**
	 * @see FieldEditorPreferencePage#performOk()
	 */
	public boolean performOk() {
		// Restart the communications daemon if required
		int port = portEditor.getIntValue();
		SystemCommunicationsDaemon daemon = SystemCommunicationsDaemon.getInstance();

		// Restart communications daemon if it is already running and the 
		// port number has changed
		if (daemon.isRunning() && port != daemon.getPort()) {
			SystemCommunicationsDaemon.getInstance().stopDaemon();
			SystemCommunicationsDaemon.getInstance().startDaemon();
		}
		
		return super.performOk();
		
	}
}