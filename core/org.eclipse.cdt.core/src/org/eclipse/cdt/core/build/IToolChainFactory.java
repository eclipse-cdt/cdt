package org.eclipse.cdt.core.build;

import org.osgi.service.prefs.Preferences;

/**
 * @since 5.12
 */
public interface IToolChainFactory {

	CToolChain createToolChain(String id, Preferences settings);

	default void discover() {
	}

}
