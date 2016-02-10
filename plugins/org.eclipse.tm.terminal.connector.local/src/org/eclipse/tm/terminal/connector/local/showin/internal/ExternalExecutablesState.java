package org.eclipse.tm.terminal.connector.local.showin.internal;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.tm.terminal.connector.local.showin.ExternalExecutablesManager;
import org.eclipse.ui.AbstractSourceProvider;
import org.eclipse.ui.ISources;

/**
 * SourceProvider that provides a state to determine whether external executables are configured or not.
 */
public class ExternalExecutablesState extends AbstractSourceProvider {
	public final static String CONFIGURED_STATE = "org.eclipse.tm.terminal.connector.local.external.configured"; //$NON-NLS-1$
	private boolean enabled;

	public ExternalExecutablesState() {
		List<Map<String, String>> externals = ExternalExecutablesManager.load();
		this.enabled = (externals != null && !externals.isEmpty());
	}

	@Override
	public String[] getProvidedSourceNames() {
		return new String[] { CONFIGURED_STATE };
	}

	@Override
	public Map getCurrentState() {
		Map<String, String> map = new HashMap<String, String>(1);
		map.put(CONFIGURED_STATE, Boolean.valueOf(enabled).toString().toUpperCase());
		return map;
	}

	public void enable() {
		fireSourceChanged(ISources.WORKBENCH, CONFIGURED_STATE, "TRUE");		 //$NON-NLS-1$
	}

	public void disable() {
		fireSourceChanged(ISources.WORKBENCH, CONFIGURED_STATE, "FALSE");		 //$NON-NLS-1$
	}

	@Override
	public void dispose() {
	}
}
