/*******************************************************************************
 * Copyright (c) 2000, 2011 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     Sergey Prigogin (Google)
 *******************************************************************************/
package org.eclipse.cdt.internal.ui.preferences;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.ProjectScope;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.preferences.DefaultScope;
import org.eclipse.core.runtime.preferences.IEclipsePreferences;
import org.eclipse.core.runtime.preferences.IScopeContext;
import org.eclipse.core.runtime.preferences.InstanceScope;
import org.eclipse.ui.preferences.IWorkingCopyManager;
import org.osgi.service.prefs.BackingStoreException;

/**
 * 
 */
public class PreferencesAccess {
	
	public static PreferencesAccess getOriginalPreferences() {
		return new PreferencesAccess();
	}
	
	public static PreferencesAccess getWorkingCopyPreferences(IWorkingCopyManager workingCopyManager) {
		return new WorkingCopyPreferencesAccess(workingCopyManager);
	}
		
	private PreferencesAccess() {
		// can only extends in this file
	}
	
	public IScopeContext getDefaultScope() {
		return DefaultScope.INSTANCE;
	}
	
	public IScopeContext getInstanceScope() {
		return InstanceScope.INSTANCE;
	}
	
	public IScopeContext getProjectScope(IProject project) {
		return new ProjectScope(project);
	}
	
	public void applyChanges() throws BackingStoreException {
	}
	
	private static class WorkingCopyPreferencesAccess extends PreferencesAccess {
		private final IWorkingCopyManager fWorkingCopyManager;

		private WorkingCopyPreferencesAccess(IWorkingCopyManager workingCopyManager) {
			fWorkingCopyManager= workingCopyManager;
		}
		
		private final IScopeContext getWorkingCopyScopeContext(IScopeContext original) {
			return new WorkingCopyScopeContext(fWorkingCopyManager, original);
		}
		
		@Override
		public IScopeContext getDefaultScope() {
			return getWorkingCopyScopeContext(super.getDefaultScope());
		}
		
		@Override
		public IScopeContext getInstanceScope() {
			return getWorkingCopyScopeContext(super.getInstanceScope());
		}
		
		@Override
		public IScopeContext getProjectScope(IProject project) {
			return getWorkingCopyScopeContext(super.getProjectScope(project));
		}
		
		@Override
		public void applyChanges() throws BackingStoreException {
			fWorkingCopyManager.applyChanges();
		}
	}
	
	private static class WorkingCopyScopeContext implements IScopeContext {
		private final IWorkingCopyManager fWorkingCopyManager;
		private final IScopeContext fOriginal;

		public WorkingCopyScopeContext(IWorkingCopyManager workingCopyManager, IScopeContext original) {
			fWorkingCopyManager= workingCopyManager;
			fOriginal= original;	
		}

		@Override
		public String getName() {
			return fOriginal.getName();
		}

		@Override
		public IEclipsePreferences getNode(String qualifier) {
			return fWorkingCopyManager.getWorkingCopy(fOriginal.getNode(qualifier));
		}

		@Override
		public IPath getLocation() {
			return fOriginal.getLocation();
		}
	}
}
