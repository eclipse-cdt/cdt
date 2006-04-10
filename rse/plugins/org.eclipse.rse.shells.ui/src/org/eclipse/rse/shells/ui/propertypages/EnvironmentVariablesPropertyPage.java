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



import java.util.Collection;
import java.util.Iterator;
import java.util.Vector;

import org.eclipse.rse.core.SystemBasePlugin;
import org.eclipse.rse.core.subsystems.IRemoteSystemEnvVar;
import org.eclipse.rse.core.subsystems.ISubSystem;
import org.eclipse.rse.shells.ui.RemoteCommandHelpers;
import org.eclipse.rse.subsystems.shells.core.subsystems.IRemoteCmdSubSystem;
import org.eclipse.rse.ui.propertypages.SystemBasePropertyPage;
import org.eclipse.rse.ui.widgets.EnvironmentVariablesForm;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;


/**
 * Property page for editing persistant environment variables for an 
 * RSE connection.
 */
public class EnvironmentVariablesPropertyPage extends SystemBasePropertyPage
{
	private EnvironmentVariablesForm _form;

	/**
	 * Constructor for EnvironmentVariablesPropertyPage.
	 */
	public EnvironmentVariablesPropertyPage()
	{
		super();
	}

	/**
	 * @see org.eclipse.rse.ui.propertypages.SystemBasePropertyPage#createContentArea(Composite)
	 */
	protected Control createContentArea(Composite parent)
	{

		IRemoteCmdSubSystem cmdSubSystem = getCmdSubSystem();
		if (cmdSubSystem != null)
		{

			// Create property page UI
			_form =
				new EnvironmentVariablesForm(
					getShell(),
					getMessageLine(),
					getElement(),
					cmdSubSystem.getInvalidEnvironmentVariableNameCharacters());

			// Load existing environment variables
			IRemoteSystemEnvVar[] envVars = getCmdSubSystem().getEnvironmentVariableList();
			// Form only works with a simple inner class since it cannot easily instantiate 
			// RemoteSystemEnvVarImpl classes.  We leave this complexity (via MOF) to the 
			// subsystem.
			Vector envVarsVector = new Vector();
			for (int idx = 0; idx < envVars.length; idx++)
			{
				envVarsVector.add(_form.new EnvironmentVariable(envVars[idx].getName(), envVars[idx].getValue()));
			}
			_form.setEnvVars(envVarsVector);

			_form.createContents(parent);

		}
		else
		{
			// Create property page UI
			_form =
				new EnvironmentVariablesForm(
					getShell(),
					getMessageLine(),
					null,
					null);			

			_form.createContents(parent);			
		}
		return parent;
	}

	/**
	 * @see org.eclipse.rse.ui.propertypages.SystemBasePropertyPage#verifyPageContents()
	 */
	protected boolean verifyPageContents()
	{
		return true;
	}

	/**
	 * Return the command subsystem implementation object.
	 */
	private IRemoteCmdSubSystem getCmdSubSystem()
	{
		Object subsystem = getElement();
		if (subsystem instanceof ISubSystem)
		{
			return RemoteCommandHelpers.getCmdSubSystem(((ISubSystem)subsystem).getHost());
		}
		else
		{
			SystemBasePlugin.logError(
				"EnvironmentVariablesPropertyPage.getCmdSubSystem:  input element for property page is not an instanceof SubSystem: "
					+ subsystem);
			return null;
		}
	}

	/**
	 * @see org.eclipse.jface.preference.IPreferencePage#performOk()
	 */
	public boolean performOk()
	{
		Collection envVars = _form.getEnvVars();
		String[] names = new String[envVars.size()];
		String[] values = new String[envVars.size()];

		Iterator i = envVars.iterator();
		EnvironmentVariablesForm.EnvironmentVariable variable;
		for (int idx = 0; i.hasNext(); idx++)
		{
			variable = (EnvironmentVariablesForm.EnvironmentVariable) i.next();
			names[idx] = variable.getName();
			values[idx] = variable.getValue();
		}

		getCmdSubSystem().setEnvironmentVariableList(names, values);

		return true;
	}

}