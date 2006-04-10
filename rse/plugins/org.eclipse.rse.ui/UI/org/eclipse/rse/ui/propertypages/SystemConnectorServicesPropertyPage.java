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

package org.eclipse.rse.ui.propertypages;

import org.eclipse.rse.core.subsystems.IConnectorService;
import org.eclipse.rse.model.IHost;
import org.eclipse.rse.ui.widgets.services.ConnectorServiceElement;
import org.eclipse.rse.ui.widgets.services.ConnectorServicesForm;
import org.eclipse.rse.ui.widgets.services.RootServiceElement;
import org.eclipse.rse.ui.widgets.services.ServiceElement;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;


public class SystemConnectorServicesPropertyPage extends SystemBasePropertyPage
{
	private ConnectorServicesForm _form;
	private ServiceElement _root;
	public IHost getHost()
	{
		return (IHost)getElement();
	}
	
	
	protected Control createContentArea(Composite parent)
	{
		_form = new ConnectorServicesForm(getMessageLine());
		
		Control control = _form.createContents(parent);
		initForm();
		return control;
	}
	
	
	protected void initForm()
	{
		_root = getRoot();
		_form.init(_root);
	}
	
	protected ServiceElement getRoot()
	{
		RootServiceElement root = new RootServiceElement();
		IHost host = getHost();
		IConnectorService[] connectorServices = host.getConnectorServices();
		ServiceElement[] elements = new ServiceElement[connectorServices.length];
		for (int i = 0; i < connectorServices.length; i++)
		{
			elements[i] = new ConnectorServiceElement(host, root, connectorServices[i]);
		}
		root.setChildren(elements);
		return root;
	}

	protected boolean verifyPageContents()
	{
		return true;
	}


	public boolean performCancel()
	{
		_root.revert();
		return super.performCancel();		
	}


	public boolean performOk()
	{
		_root.commit();
		return super.performOk();
	}

}