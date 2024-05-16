/*******************************************************************************
 * Copyright (c) 2023 Contributors to the Eclipse Foundation.
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *   See git history
 *******************************************************************************/

package org.eclipse.cdt.managedbuilder.internal.ui.compilationdatabase;

import java.util.Optional;

import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.ProjectScope;
import org.eclipse.core.runtime.preferences.IEclipsePreferences;
import org.eclipse.core.runtime.preferences.IScopeContext;
import org.eclipse.osgi.util.NLS;

public abstract class ConfigurationAccess {
	protected final String qualifier;

	public ConfigurationAccess(String qualifier) {
		this.qualifier = qualifier;
	}

	protected Optional<ProjectScope> projectScope(IWorkspace workspace, Object context) {
		return new ResolveProjectScope(workspace).apply(context);
	}

	protected IEclipsePreferences preferences(IScopeContext scope) {
		return Optional.ofNullable(scope.getNode(qualifier))//
				.filter(IEclipsePreferences.class::isInstance)//
				.map(IEclipsePreferences.class::cast)//
				.orElseThrow(() -> new IllegalStateException(//
						NLS.bind("Unable to get preferences for node: {0}", // //$NON-NLS-1$
								qualifier)));
	}

}
