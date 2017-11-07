package org.eclipse.cdt.arduino.core.internal.build;

import java.nio.file.Path;

import org.eclipse.cdt.build.gcc.core.GCCToolChain;
import org.eclipse.cdt.core.build.IToolChain;
import org.eclipse.cdt.core.build.IToolChainProvider;

public class ArduinoToolChain extends GCCToolChain {

	public static final String TYPE_ID = "org.eclipse.cdt.arduino"; //$NON-NLS-1$
	private final String id;

	public ArduinoToolChain(IToolChainProvider provider, String id) {
		super(provider, (Path) null, "arduino", null); //$NON-NLS-1$
		this.id = id;
	}

	@Override
	public String getTypeId() {
		return TYPE_ID;
	}

	@Override
	public String getId() {
		return id;
	}

	@Override
	public String getName() {
		return id;
	}

	@Override
	public String getProperty(String key) {
		// TODO architecture if I need it
		if (key.equals(IToolChain.ATTR_OS)) {
			return "arduino"; //$NON-NLS-1$
		} else {
			return super.getProperty(key);
		}
	}

}
