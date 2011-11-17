/*******************************************************************************
 * Copyright (c) 2007, 2008 Wind River Systems, Inc. and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Markus Schorn - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.internal.core;

import org.eclipse.cdt.core.CCorePlugin;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.preferences.IEclipsePreferences;
import org.eclipse.core.runtime.preferences.IScopeContext;
import org.eclipse.core.runtime.preferences.InstanceScope;
import org.osgi.service.prefs.BackingStoreException;
import org.osgi.service.prefs.Preferences;

public class LocalProjectScope implements IScopeContext {
	private static final String QUALIFIER_EXT = ".prj-"; //$NON-NLS-1$

	/**
	 * String constant (value of <code>"project-local"</code>) used for the
	 * scope name for this preference scope.
	 */
	public static final String SCOPE = "project-local"; //$NON-NLS-1$

	private String fContext;

	/**
	 * Create and return a new local project scope for the given project. The given
	 * project must not be <code>null</code>.
	 *
	 * @param context the project
	 * @exception IllegalArgumentException if the project is <code>null</code>
	 */
	public LocalProjectScope(IProject context) {
		if (context == null)
			throw new IllegalArgumentException();
		fContext= context.getName();
	}

	/**
	 * Create and return a new local project scope for the given project. The given
	 * project must not be <code>null</code>.
	 *
	 * @param projectName the name of the project
	 * @exception IllegalArgumentException if the project is <code>null</code>
	 */
	public LocalProjectScope(String projectName) {
		if (projectName == null)
			throw new IllegalArgumentException();
		fContext= projectName;
	}

	@Override
	public IPath getLocation() {
		return null;
	}

	@Override
	public String getName() {
		return SCOPE;
	}

	@Override
	public IEclipsePreferences getNode(String qualifier) {
		return InstanceScope.INSTANCE.getNode(qualifier + QUALIFIER_EXT + fContext);
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (!super.equals(obj))
			return false;
		if (!(obj instanceof LocalProjectScope))
			return false;
		LocalProjectScope other = (LocalProjectScope) obj;
		return fContext.equals(other.fContext);
	}

	@Override
	public int hashCode() {
		return fContext.hashCode();
	}


	private static Preferences getPPP(String name) {
		return new LocalProjectScope(name).getNode(CCorePlugin.PLUGIN_ID);
	}

	public static void deletePreferences(IPath prjToDelete, IPath movedTo) {
		try {
			Preferences prefs= getPPP(prjToDelete.lastSegment());
			if (movedTo != null) {
				Preferences target= getPPP(movedTo.lastSegment());
				copyPrefs(prefs, target);
			}
			deletePrefs(prefs);
		} catch (BackingStoreException e) {
		}
	}

	private static void deletePrefs(Preferences prefs) throws BackingStoreException {
		prefs.clear();
		String[] children= prefs.childrenNames();
		for (String child : children) {
			prefs.node(child).removeNode();
		}
		prefs.flush();
		prefs.removeNode();
	}

	private static void copyPrefs(Preferences prefs, Preferences target) throws BackingStoreException {
		String[] keys= prefs.keys();
		for (String key : keys) {
			String val= prefs.get(key, null);
			if (val != null) {
				target.put(key, val);
			}
		}
		String[] children= prefs.childrenNames();
		for (String child : children) {
			copyPrefs(prefs.node(child), target.node(child));
		}
		target.flush();
	}
}
