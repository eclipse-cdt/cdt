/*******************************************************************************
 * Copyright (c) 2012, 2015 Wind River Systems, Inc. and others. All rights reserved.
 * This program and the accompanying materials are made available under the terms
 * of the Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Wind River Systems - initial API and implementation
 *******************************************************************************/
package org.eclipse.tcf.te.ui.terminals.serial.controls;

import java.util.Map;

import org.eclipse.core.runtime.Assert;
import org.eclipse.jface.dialogs.IDialogSettings;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.tcf.te.core.terminals.interfaces.constants.ITerminalsConnectorConstants;
import org.eclipse.tcf.te.ui.terminals.interfaces.IConfigurationPanelContainer;
import org.eclipse.tcf.te.ui.terminals.panels.AbstractExtendedConfigurationPanel;

/**
 * Serial wizard configuration panel implementation.
 */
public class SerialConfigurationPanel extends AbstractExtendedConfigurationPanel {

	private SerialLinePanel serialSettingsPage;

	/**
	 * Constructor.
	 *
	 * @param container The configuration panel container or <code>null</code>.
	 */
	public SerialConfigurationPanel(IConfigurationPanelContainer container) {
	    super(container);
    }

	/* (non-Javadoc)
	 * @see org.eclipse.tcf.te.ui.terminals.interfaces.IConfigurationPanel#setupPanel(org.eclipse.swt.widgets.Composite)
	 */
	@Override
	public void setupPanel(Composite parent) {
		Composite panel = new Composite(parent, SWT.NONE);
		panel.setLayout(new GridLayout());
		GridData data = new GridData(SWT.FILL, SWT.FILL, true, true);
		panel.setLayoutData(data);

		// Create the host selection combo
		if (isWithoutSelection()) createHostsUI(panel, true);

		serialSettingsPage = new SerialLinePanel(getContainer());
		serialSettingsPage.setupPanel(panel);

		// Create the encoding selection combo
		createEncodingUI(panel, true);

		setControl(panel);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.tcf.te.ui.terminals.panels.AbstractConfigurationPanel#extractData(java.util.Map)
	 */
	@Override
	public void extractData(Map<String, Object> data) {
    	// set the terminal connector id for serial
    	data.put(ITerminalsConnectorConstants.PROP_TERMINAL_CONNECTOR_ID, "org.eclipse.tm.internal.terminal.serial.SerialConnector"); //$NON-NLS-1$

    	serialSettingsPage.extractData(data);
		data.put(ITerminalsConnectorConstants.PROP_ENCODING, getEncoding());
    }

	/* (non-Javadoc)
	 * @see org.eclipse.tcf.te.ui.terminals.panels.AbstractExtendedConfigurationPanel#fillSettingsForHost(java.lang.String)
	 */
	@Override
	protected void fillSettingsForHost(String host){
	}

	/* (non-Javadoc)
	 * @see org.eclipse.tcf.te.ui.terminals.panels.AbstractExtendedConfigurationPanel#saveSettingsForHost(boolean)
	 */
	@Override
	protected void saveSettingsForHost(boolean add){
	}

	/* (non-Javadoc)
	 * @see org.eclipse.tcf.te.ui.terminals.panels.AbstractConfigurationPanel#isValid()
	 */
	@Override
    public boolean isValid(){
		return serialSettingsPage.isValid();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.tcf.te.ui.terminals.panels.AbstractExtendedConfigurationPanel#doSaveWidgetValues(org.eclipse.jface.dialogs.IDialogSettings, java.lang.String)
	 */
	@Override
    public void doSaveWidgetValues(IDialogSettings settings, String idPrefix) {
		Assert.isNotNull(settings);
		serialSettingsPage.doSaveWidgetValues(settings, idPrefix);
		// Save the encodings widget values
		doSaveEncodingsWidgetValues(settings, idPrefix);
    }

	/* (non-Javadoc)
	 * @see org.eclipse.tcf.te.ui.terminals.panels.AbstractExtendedConfigurationPanel#doRestoreWidgetValues(org.eclipse.jface.dialogs.IDialogSettings, java.lang.String)
	 */
	@Override
    public void doRestoreWidgetValues(IDialogSettings settings, String idPrefix) {
		Assert.isNotNull(settings);
		serialSettingsPage.doRestoreWidgetValues(settings, idPrefix);
		// Restore the encodings widget values
		doRestoreEncodingsWidgetValues(settings, idPrefix);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.tcf.te.ui.terminals.panels.AbstractExtendedConfigurationPanel#getHostFromSettings()
	 */
	@Override
    protected String getHostFromSettings() {
		return null;
    }

	/* (non-Javadoc)
	 * @see org.eclipse.tcf.te.ui.terminals.panels.AbstractExtendedConfigurationPanel#isWithHostList()
	 */
	@Override
    public boolean isWithHostList() {
    	return false;
    }
}
