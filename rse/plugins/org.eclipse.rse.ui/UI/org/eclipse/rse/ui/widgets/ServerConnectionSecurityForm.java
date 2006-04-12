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
import org.eclipse.rse.ui.messages.ISystemMessageLine;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Shell;


public class ServerConnectionSecurityForm extends SystemBaseForm
{



	private SSLForm _sslForm;

	private ISystemMessageLine _msgLine;

	public ServerConnectionSecurityForm(Shell shell, ISystemMessageLine msgLine)
	{
		super(shell, msgLine);
		_msgLine = msgLine;
	}

	public void disable()
	{
		_sslForm.enableCheckBoxes(false);	
	}
	
	public void enable()
	{
		_sslForm.enableCheckBoxes(true);
	}

	/**
	 * @see org.eclipse.rse.ui.SystemBaseForm#createContents(Composite)
	 */
	public Control createContents(Composite parent)
	{
	
		_sslForm = new SSLForm(_msgLine);
		_sslForm.createContents(parent);

		// help

		// initialization
		initDefaults();
		return parent;
	}

	private void initDefaults()
	{
		// pull info from preferences and/or persistence model	
		
	}

	public void setAlertSSL(boolean flag)
	{
		_sslForm.setSSLALertIsChecked(flag);
	}

	public boolean getAlertSSL()
	{
		return _sslForm.isSSLAlertChecked();
	}

	public void setAlertNonSSL(boolean flag)
	{
		_sslForm.setNonSSLALertIsChecked(flag);
	}

	public boolean getAlertNonSSL()
	{
		return _sslForm.isNonSSLAlertChecked();
	}
}