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


package org.eclipse.rse.shells.ui.propertypages;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.rse.internal.subsystems.shells.subsystems.RemoteCmdSubSystem;
import org.eclipse.rse.shells.ui.ShellResources;
import org.eclipse.rse.shells.ui.view.EncodingForm;
import org.eclipse.rse.ui.RSEUIPlugin;
import org.eclipse.rse.ui.propertypages.SystemBasePropertyPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Group;



public class SystemShellPropertyPage extends SystemBasePropertyPage 
{
	private EncodingForm _form;
	
	public SystemShellPropertyPage()
	{
		super();	
		setDescription(ShellResources.RESID_SHELL_PROPERTYPAGE_DESCRIPTION);
	}
	
	public void setShellEncodingDefaults(List encodings)
	{
		StringBuffer history = new StringBuffer();
		for (int i = 0; i < encodings.size(); i++)
		{
			String encoding = (String)encodings.get(i);
			history.append(encoding);
			if (i < encodings.size())
			{
				history.append(',');
			}
		}
				
		RSEUIPlugin.getDefault().getPreferenceStore().setValue("shell.encodingDefaults", history.toString());
	}
	
	public List getShellEncodingDefaults()
	{
		List result = new ArrayList();
		String attribute = RSEUIPlugin.getDefault().getPreferenceStore().getString("shell.encodingDefaults");
		if (attribute != null && attribute.length() > 0)
		{
			String[] list = attribute.split(",");
			for (int i = 0; i < list.length; i++)
			{
				result.add(list[i]);
			}
		}
		else
		{			
			result.add("UTF-8");
			result.add("UTF-16");
			result.add("US-ASCII");
			result.add("ISO-8859-1");
			result.add("Cp1252");
			result.add("Cp1256");
		}
		return result;
	}
	

	/* (non-Javadoc)
	 * @see org.eclipse.rse.ui.propertypages.SystemBasePropertyPage#createContentArea(org.eclipse.swt.widgets.Composite)
	 */
	protected Control createContentArea(Composite parent) 
	{		
		Font font = parent.getFont();
		Group group = new Group(parent, SWT.NONE);
		GridData data = new GridData(GridData.FILL_HORIZONTAL);
		group.setLayoutData(data);
		GridLayout layout = new GridLayout();
		layout.numColumns = 2;
		group.setLayout(layout);
		group.setText(ShellResources.RESID_SHELL_PROPERTYPAGE_ENCODING); 
		group.setFont(font);
		
		_form = new EncodingForm(getShell(), getMessageLine());
		_form.createContents(group);
		
		RemoteCmdSubSystem cmdSS = getCmdSubSystem();
		_form.initialize(getShellEncodingDefaults(), cmdSS.getShellEncoding());
		return _form.getDefaultControl();
	}
	
	public RemoteCmdSubSystem getCmdSubSystem()
	{
		return (RemoteCmdSubSystem)getElement();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.rse.ui.propertypages.SystemBasePropertyPage#verifyPageContents()
	 */
	protected boolean verifyPageContents() 
	{
		return true;
	}

	/**
	 * @see org.eclipse.jface.preference.IPreferencePage#performOk()
	 */
	public boolean performOk()
	{
		String encoding = _form.getEncoding();		
		getCmdSubSystem().setShellEncoding(encoding);
		
		List defaults = getShellEncodingDefaults();
		if (!defaults.contains(encoding))
		{
			defaults.add(encoding);
			setShellEncodingDefaults(defaults);
		}
		
		return true;
	}
}