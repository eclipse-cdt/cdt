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

package org.eclipse.rse.ui.widgets;

import org.eclipse.rse.ui.SystemBaseForm;
import org.eclipse.rse.ui.SystemResources;
import org.eclipse.rse.ui.SystemWidgetHelpers;
import org.eclipse.rse.ui.messages.ISystemMessageLine;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Event;


/**
 * This class provides a reusable widget for selecting whether or not
 * a communications connection should use SSL
 */
public class SSLForm extends SystemBaseForm {


	private Button _sslCheckBox;
	private Button _nonsslCheckBox;
	
	/**
	 * Constructor for SSLForm.
	 * @param msgLine
	 */
	public SSLForm(ISystemMessageLine msgLine) {
		super(null, msgLine); // null is the shell.
	}

	/**
	 * Determines whether ssl is checked or not
	 * @return
	 */
	public boolean isSSLAlertChecked()

	{
		return _sslCheckBox.getSelection();
	}
	
	/**
	 * Check/uncheck the ssl checkbox
	 * @param flag
	 */
	public void setSSLALertIsChecked(boolean flag)

	{
		_sslCheckBox.setSelection(flag);
	}
	
	/**
	 * Determines whether non-ssl is checked or not
	 * @return
	 */
	public boolean isNonSSLAlertChecked()
	{
		return _nonsslCheckBox.getSelection();
	}
	
	
	/**
	 * Check/uncheck the ssl checkbox
	 * @param flag
	 */
	public void setNonSSLALertIsChecked(boolean flag)
	{
		_nonsslCheckBox.setSelection(flag);
	}
	
	/**
	 * Enable/disable the ssl checkbox
	 * @param flag
	 */
	public void enableCheckBoxes(boolean flag)

	{
		_sslCheckBox.setEnabled(flag);
		_nonsslCheckBox.setEnabled(flag);
	}
	
	/**
	 * @see org.eclipse.rse.ui.SystemBaseForm#createContents(Composite)
	 */
	public Control createContents(Composite parent) 
	{
		super.setShell(parent.getShell());
		_sslCheckBox = SystemWidgetHelpers.createCheckBox(parent, SystemResources.RESID_SUBSYSTEM_SSL_ALERT_LABEL, this);
		_sslCheckBox.setToolTipText(SystemResources.RESID_SUBSYSTEM_SSL_ALERT_TIP);
		_nonsslCheckBox = SystemWidgetHelpers.createCheckBox(parent, SystemResources.RESID_SUBSYSTEM_NONSSL_ALERT_LABEL, this);
		_nonsslCheckBox.setToolTipText(SystemResources.RESID_SUBSYSTEM_NONSSL_ALERT_TIP);
		return parent;		
	}
	

	
	public void handleEvent(Event evt) 
	{
		
	}

}