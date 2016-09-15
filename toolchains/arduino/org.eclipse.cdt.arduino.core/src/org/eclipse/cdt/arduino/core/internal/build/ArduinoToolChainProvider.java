package org.eclipse.cdt.arduino.core.internal.build;

import org.eclipse.cdt.core.build.IToolChain;
import org.eclipse.cdt.core.build.IToolChainProvider;
import org.eclipse.core.runtime.CoreException;

public class ArduinoToolChainProvider implements IToolChainProvider {

	public static final String ID = "org.eclipse.cdt.arduino.core.toolChainProvider"; //$NON-NLS-1$

	@Override
	public String getId() {
		return ID;
	}

	@Override
	public IToolChain getToolChain(String id, String version) throws CoreException {
		return new ArduinoToolChain(this, id, version);
	}

}
