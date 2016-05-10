package org.eclipse.cdt.arduino.core.internal.build;

import org.eclipse.cdt.build.gcc.core.GCCToolChain;
import org.eclipse.cdt.core.build.IToolChain;
import org.eclipse.cdt.core.build.IToolChainProvider;
import org.eclipse.core.resources.IBuildConfiguration;
import org.eclipse.core.runtime.CoreException;

public class ArduinoToolChain extends GCCToolChain {

	ArduinoToolChain(IToolChainProvider provider, IBuildConfiguration config) throws CoreException {
		super(provider, config.getProject().getName() + '#' + config.getName(), ""); //$NON-NLS-1$
	}
	
	public ArduinoToolChain(IToolChainProvider provider, String id, String version) {
		super(provider, id, version);
	}
	
	@Override
	public String getProperty(String key) {
		// TODO architecture if I need it
		if (key.equals(IToolChain.ATTR_OS)) {
			return "arduino"; //$NON-NLS-1$
		} else {
			return null;
		}
	}

}
