package org.eclipse.cdt.build.core;

import org.osgi.service.prefs.Preferences;

public interface IToolChainType {

	String getId();

	IToolChain getToolChain(String name, Preferences properties);

}
