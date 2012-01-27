/*******************************************************************************
 *  Copyright (c) 2007, 2008 IBM Corporation and others.
 *  All rights reserved. This program and the accompanying materials
 *  are made available under the terms of the Eclipse Public License v1.0
 *  which accompanies this distribution, and is available at
 *  http://www.eclipse.org/legal/epl-v10.html
 *
 *  Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.managedbuilder.xlc.ui.properties;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import org.eclipse.cdt.managedbuilder.core.IBuildObject;
import org.eclipse.cdt.managedbuilder.core.IConfiguration;
import org.eclipse.cdt.managedbuilder.core.IFolderInfo;
import org.eclipse.cdt.managedbuilder.core.IHoldsOptions;
import org.eclipse.cdt.managedbuilder.core.IManagedProject;
import org.eclipse.cdt.managedbuilder.core.IOption;
import org.eclipse.cdt.managedbuilder.core.IOptionApplicability;
import org.eclipse.cdt.managedbuilder.xlc.ui.XLCUIPlugin;
import org.eclipse.cdt.managedbuilder.xlc.ui.preferences.PreferenceConstants;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.QualifiedName;
import org.eclipse.jface.preference.IPreferenceStore;

/**
 * @author rkerimov
 *
 * This applicability calculator hides/shows options that are specific to the XL compiler versions
 *
 */

public class XLCApplicabilityCalculator implements IOptionApplicability
{
	private static final String BUNDLE_NAME = "org/eclipse/cdt/managedbuilder/xlc/ui/properties/applicability.properties";
	private static final String PROP_NAME_VERSION_ORDER = "xlc.applicability.version.order";
	private static final String PROP_VALUE_PLUS = "+";

	private static boolean initialized;
	private static List versionOrder;
	private static Map applicabilityMap;

	public XLCApplicabilityCalculator()
	{
		if (!initialized)
		{
			Properties props = null;

			ClassLoader loader = this.getClass().getClassLoader();
			InputStream input = null;

			if (loader != null)
				input = loader.getResourceAsStream(BUNDLE_NAME);

			if (input == null)
				input = ClassLoader.getSystemResourceAsStream(BUNDLE_NAME);

			try
			{
				if (input != null)
				{
					props = new Properties();
					props.load(input);

					Set entrySet = props.entrySet();
					Iterator iterator = entrySet.iterator();
					while (iterator.hasNext())
					{
						Map.Entry entry = (Map.Entry) iterator.next();

						String key = (String) entry.getKey();
						String value = (String) entry.getValue();

						if (value == null)
							value = "";

						if (key.equals(PROP_NAME_VERSION_ORDER))
						{
							versionOrder = new ArrayList();
							String[] versions = value.split(",");

							if (versions != null)
							{
								for (int i = 0; i < versions.length; i ++)
								{
									versionOrder.add(versions[i].trim());
								}
							}
						}
						else
						{
							if (applicabilityMap == null)
								applicabilityMap = new HashMap();

							List applicList = (List) applicabilityMap.get(key);
							if (applicList == null)
							{
								applicList = new ArrayList();
								applicabilityMap.put(key, applicList);
							}

							boolean hasGreaterOption = false;
							//find if ends with + and set as separate option
							if (value.endsWith(PROP_VALUE_PLUS))
							{
								hasGreaterOption = true;
								value = value.substring(0, value.length() - PROP_VALUE_PLUS.length());
							}

							String[] versions = value.split(",");

							if (versions != null)
							{
								for (int i = 0; i < versions.length; i ++)
								{
									applicList.add(versions[i].trim());
								}

							}

							if (hasGreaterOption)
								applicList.add(PROP_VALUE_PLUS);
						}
					}
				}
			}
			catch (IOException e)
			{
				e.printStackTrace();
			}
			finally
			{
				if (input != null)
				{
					try
					{
						input.close();
					}
					catch (IOException e)
					{
						// ignore
					}
				}
			}

			initialized = true;

		}
	}

	private boolean isApplicable(IBuildObject configuration, IHoldsOptions holder, IOption option)
	{
		// first we check the preference for this project, if it exists
		IProject project = null;
		if(configuration instanceof IConfiguration)
		{
			IConfiguration config = (IConfiguration) configuration;
			IManagedProject managedProject = config.getManagedProject();

			project = (IProject) managedProject.getOwner();
		}
		else if(configuration instanceof IFolderInfo)
		{
			IFolderInfo folderInfo = (IFolderInfo) configuration;

			IConfiguration config = folderInfo.getParent();

			IManagedProject managedProject = config.getManagedProject();

			project = (IProject) managedProject.getOwner();

		}

		if (project == null)
			return false;

		String currentVersion = null;
		try {
			currentVersion = project.getPersistentProperty(new QualifiedName("",
					PreferenceConstants.P_XLC_COMPILER_VERSION));
		} catch (CoreException e) {
			e.printStackTrace();
		}

		if(currentVersion == null)
		{
			// if the property isn't set, then use the workbench preference
			IPreferenceStore prefStore = XLCUIPlugin.getDefault().getPreferenceStore();
			currentVersion = prefStore.getString(PreferenceConstants.P_XLC_COMPILER_VERSION);
		}

		//if applicability list is empty that means all options applicable to all versions
		if (applicabilityMap == null)
			return true;

		//if applicability list for this option is not defined then option has no applicability restrictions
		List applicList = (List) applicabilityMap.get(option.getId());
		if (applicList == null || applicList.isEmpty())
			return true;

		//if version is defined in the list - perfect match
		if (applicList.contains(currentVersion))
			return true;

		//if applicability is defined as 'starting from this version and greater', i.e. 'v8.0+',
		//then we need to find out if current version is greater than the last valid in the list
		String lastOption = (String) applicList.get(applicList.size() - 1);

		if (lastOption != null && lastOption.equals(PROP_VALUE_PLUS))
		{
			//if 'greater than' option is specified but no version order exists, consider config error and return false
			if (versionOrder == null)
				return false;

			//check for the last valid version in applicability list
			String validVersion = null;
			//start with element before the +
			for (int k = applicList.size() - 2; k >= 0; k --)
			{
				String version = (String) applicList.get(k);

				if (versionOrder.contains(version))
				{
					validVersion = version;
					break;
				}
			}

			//if version that applicability starts with doesn't exist - config error
			if (validVersion == null)
				return false;

			//compare if current compiler version is greater than the applicability version
			if (versionOrder.indexOf(currentVersion) > versionOrder.indexOf(validVersion))
				return true;
		}


		return false;

	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.managedbuilder.core.IOptionApplicability#isOptionEnabled(org.eclipse.cdt.managedbuilder.core.IBuildObject, org.eclipse.cdt.managedbuilder.core.IHoldsOptions, org.eclipse.cdt.managedbuilder.core.IOption)
	 */
	@Override
	public boolean isOptionEnabled(IBuildObject configuration,
			IHoldsOptions holder, IOption option) {
		return isApplicable(configuration, holder, option);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.managedbuilder.core.IOptionApplicability#isOptionUsedInCommandLine(org.eclipse.cdt.managedbuilder.core.IBuildObject, org.eclipse.cdt.managedbuilder.core.IHoldsOptions, org.eclipse.cdt.managedbuilder.core.IOption)
	 */
	@Override
	public boolean isOptionUsedInCommandLine(IBuildObject configuration,
			IHoldsOptions holder, IOption option) {
		return isApplicable(configuration, holder, option);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.managedbuilder.core.IOptionApplicability#isOptionVisible(org.eclipse.cdt.managedbuilder.core.IBuildObject, org.eclipse.cdt.managedbuilder.core.IHoldsOptions, org.eclipse.cdt.managedbuilder.core.IOption)
	 */
	@Override
	public boolean isOptionVisible(IBuildObject configuration,
			IHoldsOptions holder, IOption option) {
		return isApplicable(configuration, holder, option);
	}

}
