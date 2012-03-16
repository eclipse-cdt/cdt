/*******************************************************************************
 * Copyright (c) 2007 Red Hat Inc..
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Red Hat Incorporated - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.internal.autotools.core.configure;

import org.eclipse.cdt.core.settings.model.ICConfigurationDescription;
import org.eclipse.cdt.internal.autotools.core.AutotoolsPropertyConstants;
import org.eclipse.cdt.managedbuilder.core.BuildException;
import org.eclipse.cdt.managedbuilder.core.IBuildObject;
import org.eclipse.cdt.managedbuilder.core.IBuilder;
import org.eclipse.cdt.managedbuilder.core.IConfiguration;
import org.eclipse.cdt.managedbuilder.core.IHoldsOptions;
import org.eclipse.cdt.managedbuilder.core.IManagedOptionValueHandler;
import org.eclipse.cdt.managedbuilder.core.IOption;
import org.eclipse.cdt.managedbuilder.core.IOptionApplicability;
import org.eclipse.cdt.managedbuilder.core.ManagedBuildManager;
import org.eclipse.cdt.managedbuilder.core.ManagedOptionValueHandler;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;

public class AutotoolsOptionValueHandler extends ManagedOptionValueHandler 
	implements IOptionApplicability {
	/* (non-Javadoc)
	 * @see org.eclipse.cdt.managedbuilder.core.IManagedOptionValueHandler#handleValue(IConfiguration,IToolChain,IOption,String,int)
	 */
	
	public final static String DEFAULT_BUILD_DIR = "build"; //$NON-NLS-1$
	public final static String CONFIGURE_TOOL_ID = "org.eclipse.linuxtools.cdt.autotools.core.gnu.toolchain.tool.configure"; //$NON-NLS-1$
	public final static String BUILD_DIR_OPTION_ID = "org.eclipse.linuxtools.cdt.autotools.core.option.configure.builddir"; //$NON-NLS-1$
	public final static String BUILD_DIR_APPLY = "BuildDir.apply"; //$NON-NLS-1$
	public final static String BUILD_DIR_DEFAULT_QUESTION = "BuildDir.default"; //$NON-NLS-1$
	public final static String BUILD_DIR_YES = "BuildDir.yes"; //$NON-NLS-1$
	public final static String BUILD_DIR_NO = "BuildDir.no"; //$NON-NLS-1$

	//FIXME: Use holder to set option value, not the "option" parameter
	public boolean handleValue(IBuildObject buildObject, 
                   IHoldsOptions holder, 
                   IOption option,
                   String extraArgument, int event)
	{
		// Get the current value of the build dir option.
		String value = (String)option.getValue();

		if (buildObject instanceof IConfiguration &&
				(event == IManagedOptionValueHandler.EVENT_OPEN)) {
//			SortedSet<Integer> nums = new TreeSet<Integer>();
			IConfiguration configuration = (IConfiguration)buildObject;
			ICConfigurationDescription cfgd = ManagedBuildManager.getDescriptionForConfiguration(configuration);
			if (option.getName().equals("Name") && cfgd != null) {
				String cfgId = cfgd.getId();
				if (!value.equals("") && !value.equals(cfgId)) {
					// we have a cloned configuration and we know that the
					// clonee's name is the value of the option
					IProject project = (IProject)configuration.getManagedProject().getOwner();
					String autoName = null;
					String autoNameTemplate = null;
					// Check if we are supposed to automatically name the build directory for any
					// new configuration.  If yes, generate a build directory under the project using
					// the configuration name which must be unique.
					try {
						autoName = project.getPersistentProperty(AutotoolsPropertyConstants.AUTO_BUILD_NAME);
					} catch (CoreException e) {
						// ignore
					}
					if (autoName == null || autoName.equals(AutotoolsPropertyConstants.TRUE)) {
						autoNameTemplate = "${workspace_loc:/" + project.getName() + // $NON-NLS-1$ 
							"}/build-" + fixName(configuration.getName()); // $NON-NLS-1$
						IBuilder cfgBuilder = configuration.getEditableBuilder();
						cfgBuilder.setBuildPath(autoNameTemplate);
					}
					// Clone old configuration to tmp configuration list
					boolean isSaved = AutotoolsConfigurationManager.getInstance().cloneCfg(project, value, cfgd);
					// Check to see if we should patch up the name option.  If we aren't synchronizing
					// configurations or the configuration isn't already saved, we leave the name field alone
					// so we will trigger this again when the clone will get used.
					if (!isSaved && !AutotoolsConfigurationManager.getInstance().isSyncing()) {
						return true;
					}
				}
				try {
					IOption optionToSet = holder.getOptionToSet(option, false);
					optionToSet.setValue(cfgId);
				} catch (BuildException e) {
					return false;
				}
			}
		}
		
		// The event was not handled, thus return false
		return true;
	}
	
	private String fixName(String cfgName) {
		// Replace whitespace with underscores.
		return cfgName.replaceAll("\\s", "_");
	}
	
	// IOptionApplicability methods
	
	public boolean isOptionEnabled(IBuildObject configuration,
			IHoldsOptions holder, IOption option) {
		return true;
	}

	public boolean isOptionUsedInCommandLine(IBuildObject configuration,
			IHoldsOptions holder, IOption option) {
		return false;
	}

	public boolean isOptionVisible(IBuildObject configuration,
			IHoldsOptions holder, IOption option) {
		return true;
	}


}
