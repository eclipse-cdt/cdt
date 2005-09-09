package org.eclipse.cdt.managedbuilder.core.tests;


import org.eclipse.cdt.managedbuilder.core.IConfiguration;
import org.eclipse.cdt.managedbuilder.core.IConfigurationNameProvider;
import org.eclipse.core.runtime.Platform;


public class TestConfigurationNameProvider implements
		IConfigurationNameProvider {

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.cdt.managedbuilder.core.IConfigurationNameProvider#getNewConfigurationName(org.eclipse.cdt.managedbuilder.core.IConfiguration,
	 *      java.lang.String[]) This function will generate a unique
	 *      configuration name based on used names, current OS and current
	 *      Architecture.
	 * 
	 */
	private static int configNumber = 0;

	public String getNewConfigurationName(IConfiguration configuration,
			String[] usedConfigurationNames) {

		String configName = configuration.getName();

		// Get the current OS & architecture
		String os = Platform.getOS();
		String arch = Platform.getOSArch();

		if (isArrayContains(usedConfigurationNames, configName) == false)
			return configName;
		else {
			String[] supportedArchList = configuration.getToolChain()
					.getArchList();
			if (supportedArchList.length == 1) {
				String newConfigName = configName + "_" + supportedArchList[0];
				if (isArrayContains(usedConfigurationNames, newConfigName) == false) {
					return newConfigName;
				}
			}

			String[] supportedOsList = configuration.getToolChain().getOSList();
			if (supportedOsList.length == 1) {
				String newConfigName = configName + "_" + supportedOsList[0];
				if (isArrayContains(usedConfigurationNames, newConfigName) == false) {
					return newConfigName;
				}
			}
			configNumber += 1;
			return configName + "_" + configNumber;
		}
	}

	private boolean isArrayContains(String[] usedNames, String name) {
		if (usedNames != null) {
			for (int i = 0; i < usedNames.length; i++) {
				if ( ( usedNames[i] != null) && (usedNames[i].equals(name)) ) {
					return true;
				}
			}
		}
		return false;
	}
}
