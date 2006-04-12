/********************************************************************************
 * Copyright (c) 2006 IBM Corporation. All rights reserved.
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

package org.eclipse.rse.dstore.security.wizards;

import java.util.List;

import org.eclipse.jface.wizard.IWizard;
import org.eclipse.rse.core.comm.ISystemKeystoreProvider;
import org.eclipse.rse.dstore.security.ImageRegistry;
import org.eclipse.rse.dstore.security.UniversalSecurityProperties;
import org.eclipse.rse.ui.actions.SystemBaseWizardAction;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;

public class SystemImportCertAction extends SystemBaseWizardAction
	{
		private List _certificates;
		private ISystemKeystoreProvider _provider;
		private String _systemName;
		
		public SystemImportCertAction(ISystemKeystoreProvider provider, List certs, String systemName)
		{
			super(UniversalSecurityProperties.RESID_SECURITY_TRUST_IMPORT_CERTIFICATE_WIZARD, 
					ImageRegistry.getImageDescriptor(ImageRegistry.IMG_CERTIF_FILE),
					Display.getDefault().getActiveShell()
					);
			_certificates = certs;
			_provider = provider;
			_systemName = systemName;
		}
		
		public IWizard createWizard()
		{
			Shell shell = Display.getDefault().getActiveShell();
			SystemImportCertWizard importWiz = new SystemImportCertWizard(_provider, _systemName);
			importWiz.setInputObject(_certificates);
			return importWiz;
		}	
}