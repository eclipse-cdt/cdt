/*******************************************************************************
 * Copyright (c) 2004 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     QNX Software Systems - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.core.browser;

import java.util.ArrayList;
import java.util.Collection;

import org.eclipse.cdt.core.CCorePlugin;
import org.eclipse.cdt.core.model.CModelException;
import org.eclipse.cdt.core.model.CoreModel;
import org.eclipse.cdt.core.model.ElementChangedEvent;
import org.eclipse.cdt.core.model.IElementChangedListener;
import org.eclipse.cdt.core.model.IWorkingCopy;
import org.eclipse.cdt.internal.core.browser.cache.TypeCacheMessages;
import org.eclipse.cdt.internal.core.browser.cache.TypeCacheManager;
import org.eclipse.cdt.internal.core.browser.util.ArrayUtil;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.Preferences;
import org.eclipse.core.runtime.Preferences.IPropertyChangeListener;
import org.eclipse.core.runtime.Preferences.PropertyChangeEvent;
import org.eclipse.core.runtime.jobs.Job;

/**
 * Manages a search cache for types in the workspace. Instead of returning
 * objects of type <code>ICElement</code> the methods of this class returns a
 * list of the lightweight objects <code>TypeInfo</code>.
 * <P>
 * AllTypesCache runs asynchronously using a background job to rebuild the cache
 * as needed. If the cache becomes dirty again while the background job is
 * running, the job is restarted.
 * <P>
 * If <code>getTypes</code> is called in response to a user action, a progress
 * dialog is shown. If called before the background job has finished, getTypes
 * waits for the completion of the background job.
 */
public class AllTypesCache {

	private static final int INITIAL_DELAY = 5000;
	private static IWorkingCopyProvider fgWorkingCopyProvider;
	private static TypeCacheManager fgTypeCacheManager;
	private static IElementChangedListener fgElementChangedListener;
	private static IPropertyChangeListener fgPropertyChangeListener;
	private static boolean fgEnableIndexing = true;

	/** Preference key for enabling background cache */
	public final static String ENABLE_BACKGROUND_TYPE_CACHE = "enableBackgroundTypeCache"; //$NON-NLS-1$
	
	/**
	 * Initializes the AllTypesCache service.
	 * 
	 * @param provider A working copy provider.
	 */
	public static void initialize(IWorkingCopyProvider workingCopyProvider) {
		fgWorkingCopyProvider = workingCopyProvider;
		fgTypeCacheManager = new TypeCacheManager(fgWorkingCopyProvider);

		// load prefs
		Preferences prefs = CCorePlugin.getDefault().getPluginPreferences();
		if (prefs.contains(ENABLE_BACKGROUND_TYPE_CACHE)) {
			fgEnableIndexing = prefs.getBoolean(ENABLE_BACKGROUND_TYPE_CACHE);
		} else {
			prefs.setDefault(ENABLE_BACKGROUND_TYPE_CACHE, true);
			prefs.setValue(ENABLE_BACKGROUND_TYPE_CACHE, true);
			CCorePlugin.getDefault().savePluginPreferences();
			fgEnableIndexing = true;
		}
		
		// start jobs in background after INITIAL_DELAY
		fgTypeCacheManager.reconcile(fgEnableIndexing, Job.BUILD, INITIAL_DELAY);

		// add delta listener
		fgElementChangedListener = new IElementChangedListener() {
			public void elementChanged(ElementChangedEvent event) {
				fgTypeCacheManager.processDelta(event.getDelta());
				fgTypeCacheManager.reconcile(fgEnableIndexing, Job.BUILD, 0);
			}
		};
		CoreModel.getDefault().addElementChangedListener(fgElementChangedListener);

		// add property change listener
		fgPropertyChangeListener = new IPropertyChangeListener() {
			public void propertyChange(PropertyChangeEvent event) {
				String property = event.getProperty();
				if (property.equals(ENABLE_BACKGROUND_TYPE_CACHE)) {
					String value = (String) event.getNewValue();
					fgEnableIndexing = Boolean.valueOf(value).booleanValue();
					if (!fgEnableIndexing) {
						fgTypeCacheManager.cancelJobs();
					} else {
						fgTypeCacheManager.reconcile(fgEnableIndexing, Job.BUILD, 0);
					}
				}
			}
		};
		prefs.addPropertyChangeListener(fgPropertyChangeListener);
	}
	
	/**
	 * Terminates the service provided by AllTypesCache.
	 */
	public static void terminate() {
		// remove delta listener
		if (fgElementChangedListener != null)
			CoreModel.getDefault().removeElementChangedListener(fgElementChangedListener);
		
		// remove property change listener
		if (fgPropertyChangeListener != null)
			CCorePlugin.getDefault().getPluginPreferences().removePropertyChangeListener(fgPropertyChangeListener);

		// terminate all running jobs
		if (fgTypeCacheManager != null) {
			fgTypeCacheManager.cancelJobs();
		}
	}
	
	/**
	 * Returns all types in the workspace.
	 */
	public static ITypeInfo[] getAllTypes() {
		final Collection fAllTypes = new ArrayList();
		TypeSearchScope workspaceScope = new TypeSearchScope(true);
		IProject[] projects = workspaceScope.getEnclosingProjects();
		ITypeInfoVisitor visitor = new ITypeInfoVisitor() {
			public void visit(ITypeInfo info) {
				fAllTypes.add(info);
			}
		};
		for (int i = 0; i < projects.length; ++i) {
			fgTypeCacheManager.getCache(projects[i]).accept(visitor);
		}
		return (ITypeInfo[]) fAllTypes.toArray(new ITypeInfo[fAllTypes.size()]);
	}
	
	/**
	 * Returns all types in the given scope.
	 * 
	 * @param scope The search scope
	 * @param kinds Array containing CElement types: C_NAMESPACE, C_CLASS,
	 *              C_UNION, C_ENUMERATION, C_TYPEDEF
	 */
	public static ITypeInfo[] getTypes(ITypeSearchScope scope, int[] kinds) {
		final Collection fTypesFound = new ArrayList();
		final ITypeSearchScope fScope = scope;
		final int[] fKinds = kinds;
		IProject[] projects = scope.getEnclosingProjects();
		ITypeInfoVisitor visitor = new ITypeInfoVisitor() {
			public void visit(ITypeInfo info) {
				if (info.isEnclosed(fScope) && ArrayUtil.contains(fKinds, info.getCElementType())) {
					fTypesFound.add(info);
				}
			}
		};
		for (int i = 0; i < projects.length; ++i) {
			fgTypeCacheManager.getCache(projects[i]).accept(visitor);
		}
		return (ITypeInfo[]) fTypesFound.toArray(new ITypeInfo[fTypesFound.size()]);
	}
	
	/**
	 * Returns all types matching name in the given scope.
	 * 
	 * @param scope The search scope
	 * @param qualifiedName The qualified type name
	 * @param kinds Array containing CElement types: C_NAMESPACE, C_CLASS,
	 *              C_UNION, C_ENUMERATION, C_TYPEDEF
	 */
	public static ITypeInfo[] getTypes(ITypeSearchScope scope, IQualifiedTypeName qualifiedName, int[] kinds) {
		final Collection fTypesFound = new ArrayList();
		final ITypeSearchScope fScope = scope;
		final int[] fKinds = kinds;
		final IQualifiedTypeName fQualifiedName = qualifiedName;
		IProject[] projects = scope.getEnclosingProjects();
		ITypeInfoVisitor visitor = new ITypeInfoVisitor() {
			public void visit(ITypeInfo info) {
				if ((fScope != null && info.isEnclosed(fScope)) && fQualifiedName.equals(info.getQualifiedTypeName())
						&& ArrayUtil.contains(fKinds, info.getCElementType())) {
					fTypesFound.add(info);
				}
			}
		};
		for (int i = 0; i < projects.length; ++i) {
			fgTypeCacheManager.getCache(projects[i]).accept(visitor);
		}
		return (ITypeInfo[]) fTypesFound.toArray(new ITypeInfo[fTypesFound.size()]);
	}
	
	/**
	 * Returns true if the type cache is up to date.
	 */
	public static boolean isCacheUpToDate(ITypeSearchScope scope) {
		forceDeltaComplete();
		
		IProject[] projects = scope.getEnclosingProjects();
		for (int i = 0; i < projects.length; ++i) {
			IProject project = projects[i];
			if (project.exists() && project.isOpen()) {
				if (!fgTypeCacheManager.getCache(project).isUpToDate())
					return false;
			}
		}
		return true;
	}

	private static void forceDeltaComplete() {
		if (fgWorkingCopyProvider != null) {
			IWorkingCopy[] workingCopies = fgWorkingCopyProvider.getWorkingCopies();
			for (int i = 0; i < workingCopies.length; ++i) {
				IWorkingCopy wc = workingCopies[i];
				try {
					synchronized (wc) {
						wc.reconcile();
					}
				} catch (CModelException ex) {
				}
			}
		}
	}
	
	/**
	 * Updates the type cache.
	 * 
	 * @param monitor the progress monitor
	 */
	public static void updateCache(ITypeSearchScope scope, IProgressMonitor monitor) {
		// schedule jobs to update cache
		IProject[] projects = scope.getEnclosingProjects();
		monitor.beginTask(TypeCacheMessages.getString("AllTypesCache.updateCache.taskName"), projects.length); //$NON-NLS-1$
		for (int i = 0; i < projects.length; ++i) {
			IProject project = projects[i];
			// wait for any running jobs to finish
			fgTypeCacheManager.getCache(project).reconcileAndWait(true, Job.SHORT, monitor);
		}
		monitor.done();
	}

	/**
	 * Resolves a type location.
	 * 
	 * @param info the type to search for
	 * @param monitor the progress monitor
	 */
	public static ITypeReference resolveTypeLocation(ITypeInfo info, IProgressMonitor monitor) {
		ITypeReference location = info.getResolvedReference();
		if (location == null) {
			// cancel background jobs
			IProject project = info.getEnclosingProject();
			fgTypeCacheManager.getCache(project).cancelJobs();

			// start the search job
			fgTypeCacheManager.getCache(project).locateTypeAndWait(info, Job.SHORT, monitor);

			// get the newly parsed location
			location = info.getResolvedReference();

			// resume background jobs
			fgTypeCacheManager.reconcile(fgEnableIndexing, Job.BUILD, 0);
		}
		return location;
	}
}
