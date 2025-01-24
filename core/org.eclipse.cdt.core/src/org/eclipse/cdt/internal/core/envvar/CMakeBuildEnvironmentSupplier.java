package org.eclipse.cdt.internal.core.envvar;

import java.util.ArrayList;

import org.eclipse.cdt.core.CCorePlugin;
import org.eclipse.cdt.core.envvar.EnvironmentVariable;
import org.eclipse.cdt.core.envvar.IEnvironmentVariable;
import org.eclipse.core.runtime.preferences.InstanceScope;
import org.osgi.service.prefs.Preferences;

/**
 * This is the Environment Variable Supplier used  to supply CMake Tools variables
 * defined through CMake preference
 */
public class CMakeBuildEnvironmentSupplier implements ICoreEnvironmentVariableSupplier {

	public static final String NODENAME = "cmakeEnvironment"; //$NON-NLS-1$

	private boolean useCmakeToolLocation;
	private String cmakeLocation;
	private String[] generatorLocations;

	public CMakeBuildEnvironmentSupplier() {
		updateSupplier();
	}

	@Override
	public IEnvironmentVariable getVariable(String name, Object context) {
		updateSupplier();
		if (context != null && "PATH".equals(name)) { //$NON-NLS-1$
			return getVariable();
		}
		return null;
	}

	@Override
	public IEnvironmentVariable[] getVariables(Object context) {
		if (context != null) {
			IEnvironmentVariable var = getVariable();
			if (var != null)
				return new IEnvironmentVariable[] { var };
		}
		return null;
	}

	@Override
	public boolean appendEnvironment(Object context) {
		return true;
	}

	private IEnvironmentVariable getVariable() {
		// Need to update supplier in case Preferences are changed
		updateSupplier();
		if (!useCmakeToolLocation) {
			return null;
		}
		ArrayList<String> locations = new ArrayList<>();
		locations.add(cmakeLocation);
		for (String genLoc : generatorLocations) {
			locations.add(genLoc);
		}
		String value = String.join(EnvironmentVariableManager.getDefault().getDefaultDelimiter(), locations);
		return new EnvironmentVariable("PATH", value, IEnvironmentVariable.ENVVAR_APPEND, //$NON-NLS-1$
				EnvironmentVariableManager.getDefault().getDefaultDelimiter());
	}

	private Preferences getPreferences() {
		return InstanceScope.INSTANCE.getNode(CCorePlugin.PLUGIN_ID).node(NODENAME);
	}

	private void updateSupplier() {
		useCmakeToolLocation = getPreferences().getBoolean("useCmakeToolLocation", false); //$NON-NLS-1$
		cmakeLocation = getPreferences().get("cmakeLocation", ""); //$NON-NLS-1$ //$NON-NLS-2$
		String genLocations = getPreferences().get("generatorLocations", ""); //$NON-NLS-1$ //$NON-NLS-2$
		generatorLocations = genLocations.length() > 0 ? genLocations.split(" \\|\\| ") : new String[0]; //$NON-NLS-1$
	}
}
