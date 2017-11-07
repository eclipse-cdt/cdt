package org.eclipse.cdt.arduino.core.internal.build;

import org.eclipse.cdt.core.build.IToolChainProvider;

public class ArduinoToolChainProvider implements IToolChainProvider {

	public static final String ID = "org.eclipse.cdt.arduino.core.toolChainProvider"; //$NON-NLS-1$

	@Override
	public String getId() {
		return ID;
	}

}
