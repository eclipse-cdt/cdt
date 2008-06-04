/*******************************************************************************
 * Copyright (c) 2006, 2008 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Initial Contributors:
 * The following IBM employees contributed to the Remote System Explorer
 * component that contains this file: David McKnight, Kushal Munir,
 * Michael Berger, David Dykstal, Phil Coulthard, Don Yantzi, Eric Simpson,
 * Emily Bruner, Mazen Faraj, Adrian Storisteanu, Li Ding, and Kent Hawley.
 *
 * Contributors:
 * Martin Oberhuber (Wind River) - [235626] Convert dstore.security to MessageBundle format
 *******************************************************************************/


package org.eclipse.rse.internal.dstore.security.preference;


import org.eclipse.rse.internal.dstore.security.UniversalSecurityProperties;
import org.eclipse.rse.internal.dstore.security.widgets.CertificatePropertiesForm;
import org.eclipse.rse.ui.dialogs.SystemPromptDialog;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Shell;




public class CertPropertiesDialog extends SystemPromptDialog
{
	private Object _cert;

	public CertPropertiesDialog(Shell parentShell, Object cert)
	{
		super(parentShell, UniversalSecurityProperties.RESID_SECURITY_CERTIFICATE_PROP_TITLE);
		_cert = cert;
	}



	public Control getInitialFocusControl()
	{
		return getOkButton();
	}

	protected Control createInner(Composite parent)
	{
		CertificatePropertiesForm form = new CertificatePropertiesForm(getShell(), _cert, true);
		return form.createContents(parent);
	}


}
