/*******************************************************************************
 * Copyright (c) 2016 Dirk Fauth and others. All rights reserved.
 * This program and the accompanying materials are made available under the terms
 * of the Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Dirk Fauth <dirk.fauth@googlemail.com> - initial API and implementation
 *******************************************************************************/
package org.eclipse.tm.terminal.view.ui.internal;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.tm.terminal.view.ui.local.showin.ExternalExecutablesManager;
import org.eclipse.ui.AbstractSourceProvider;
import org.eclipse.ui.ISources;

/**
 * SourceProvider that provides a state to determine whether external executables are configured or not.
 */
public class ExternalExecutablesState extends AbstractSourceProvider {
	public final static String CONFIGURED_STATE = "org.eclipse.tm.terminal.external.executable.configured"; //$NON-NLS-1$
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
