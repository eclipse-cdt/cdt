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
import org.eclipse.cdt.core.browser.typehierarchy.ITypeHierarchy;
import org.eclipse.cdt.core.browser.typehierarchy.TypeHierarchyBuilder;
import org.eclipse.cdt.core.model.CModelException;
import org.eclipse.cdt.core.model.CoreModel;
import org.eclipse.cdt.core.model.ElementChangedEvent;
import org.eclipse.cdt.core.model.ICElement;
import org.eclipse.cdt.core.model.IElementChangedListener;
import org.eclipse.cdt.core.model.IWorkingCopy;
import org.eclipse.cdt.internal.core.browser.cache.ITypeCache;
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
	private static TypeHierarchyBuilder fgTypeHierarchyBuilder;
	private static IElementChangedListener fgElementChangedListener;
	private static IPropertyChangeListener fgPropertyChangeListener;
	static boolean fgEnableIndexing = true;

    /** Preference key for enabling background cache */
	public final static String ENABLE_BACKGROUND_TYPE_CACHE = "enableBackgroundTypeCache"; //$NON-NLS-1$
	
	/**
	 * Initializes the AllTypesCache service.
	 * 
	 * @param provider A working copy provider.
	 */
	public static void initialize(IWorkingCopyProvider workingCopyProvider) {
		fgWorkingCopyProvider = workingCopyProvider;
		TypeCacheManager.getInstance().setWorkingCopyProvider(fgWorkingCopyProvider);
		fgTypeHierarchyBuilder = new TypeHierarchyBuilder();

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
		TypeCacheManager.getInstance().reconcile(fgEnableIndexing, Job.BUILD, INITIAL_DELAY);

		// add delta listener
		fgElementChangedListener = new IElementChangedListener() {
			public void elementChanged(ElementChangedEvent event) {
				TypeCacheManager.getInstance().processElementChanged(event, fgEnableIndexing);
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
						TypeCacheManager.getInstance().cancelJobs();
					} else {
						TypeCacheManager.getInstance().reconcile(fgEnableIndexing, Job.BUILD, 0);
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
		if (TypeCacheManager.getInstance() != null) {
			TypeCacheManager.getInstance().cancelJobs();
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
			public boolean visit(ITypeInfo info) {
				fAllTypes.add(info);
				return true;
			}
			public boolean shouldContinue() { return true; }
		};
		for (int i = 0; i < projects.length; ++i) {
			TypeCacheManager.getInstance().getCache(projects[i]).accept(visitor);
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
			public boolean visit(ITypeInfo info) {
				if (ArrayUtil.contains(fKinds, info.getCElementType())
					&& (fScope != null && info.isEnclosed(fScope))) {
					fTypesFound.add(info);
				}
				return true;
			}
			public boolean shouldContinue() { return true; }
		};
		for (int i = 0; i < projects.length; ++i) {
			TypeCacheManager.getInstance().getCache(projects[i]).accept(visitor);
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
	 * @param matchEnclosed <code>true</code> if enclosed types count as matches (foo::bar == bar)
	 */
	public static ITypeInfo[] getTypes(ITypeSearchScope scope, IQualifiedTypeName qualifiedName, int[] kinds, boolean matchEnclosed) {
		final Collection fTypesFound = new ArrayList();
		final ITypeSearchScope fScope = scope;
		final int[] fKinds = kinds;
		final IQualifiedTypeName fQualifiedName = qualifiedName;
		final boolean fMatchEnclosed = matchEnclosed;
		IProject[] projects = scope.getEnclosingProjects();
		ITypeInfoVisitor visitor = new ITypeInfoVisitor() {
			public boolean visit(ITypeInfo info) {
				if (ArrayUtil.contains(fKinds, info.getCElementType())
						&& (fScope != null && info.isEnclosed(fScope))) {
					IQualifiedTypeName currName = info.getQualifiedTypeName();
					if (fMatchEnclosed && currName.segmentCount() > fQualifiedName.segmentCount()
					        && currName.lastSegment().equals(fQualifiedName.lastSegment())) {
						currName = currName.removeFirstSegments(currName.segmentCount() - fQualifiedName.segmentCount());
					}
					if (currName.equals(fQualifiedName)) {
						fTypesFound.add(info);
					}
				}
				return true;
			}
			public boolean shouldContinue() { return true; }
		};
		for (int i = 0; i < projects.length; ++i) {
			TypeCacheManager.getInstance().getCache(projects[i]).accept(visitor);
		}
		return (ITypeInfo[]) fTypesFound.toArray(new ITypeInfo[fTypesFound.size()]);
	}

	/**
	 * Returns all namespaces in the given scope.
	 * 
	 * @param scope The search scope
	 * @param includeGlobalNamespace <code>true</code> if the global (default) namespace should be returned
	 */
	public static ITypeInfo[] getNamespaces(ITypeSearchScope scope, boolean includeGlobalNamespace) {
		final Collection fTypesFound = new ArrayList();
		final ITypeSearchScope fScope = scope;
		IProject[] projects = scope.getEnclosingProjects();
		ITypeInfoVisitor visitor = new ITypeInfoVisitor() {
			public boolean visit(ITypeInfo info) {
				if (info.getCElementType() == ICElement.C_NAMESPACE
					&& (fScope != null && info.isEnclosed(fScope))) {
					fTypesFound.add(info);
				}
				return true;
			}
			public boolean shouldContinue() { return true; }
		};
		for (int i = 0; i < projects.length; ++i) {
			ITypeCache cache = TypeCacheManager.getInstance().getCache(projects[i]);
			cache.accept(visitor);
			if (includeGlobalNamespace) {
				fTypesFound.add(cache.getGlobalNamespace());
			}
		}
		return (ITypeInfo[]) fTypesFound.toArray(new ITypeInfo[fTypesFound.size()]);
	}
	
	/**
	 * Returns the global (default) namespace for the given project.
	 * 
	 * @param project the project
	 */
	public static ITypeInfo getGlobalNamespace(IProject project) {
		ITypeCache cache = TypeCacheManager.getInstance().getCache(project);
		return cache.getGlobalNamespace();
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
				if (!TypeCacheManager.getInstance().getCache(project).isUpToDate())
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
	    TypeCacheManager.getInstance().updateCache(scope, monitor);
	}

	/**
	 * Resolves a type location.
	 * 
	 * @param info the type to search for
	 * @param monitor the progress monitor
	 */
	public static ITypeReference resolveTypeLocation(ITypeInfo info, IProgressMonitor monitor) {
	    return TypeCacheManager.getInstance().resolveTypeLocation(info, monitor, fgEnableIndexing);
	}
	
	/** Returns first type in the cache which matches the given
	 *  type and name.  If no type is found, <code>null</code>
	 *  is returned.
	 *
	 * @param project the enclosing project
	 * @param type the ICElement type
	 * @param qualifiedName the qualified type name to match
	 * @return the matching type
	 */
	public static ITypeInfo getType(IProject project, int type, IQualifiedTypeName qualifiedName) {
		ITypeCache cache = TypeCacheManager.getInstance().getCache(project);
		return cache.getType(type, qualifiedName);
	}

	/**
	 * Returns all types matching name in the given project.
	 * 
	 * @param project the enclosing project
	 * @param qualifiedName The qualified type name
	 * @param matchEnclosed <code>true</code> if enclosed types count as matches (foo::bar == bar)
	 * @param ignoreCase <code>true</code> if case-insensitive
	 * @return Array of types
	 */
	public static ITypeInfo[] getTypes(IProject project, IQualifiedTypeName qualifiedName, boolean matchEnclosed, boolean ignoreCase) {
		ITypeCache cache = TypeCacheManager.getInstance().getCache(project);
		return cache.getTypes(qualifiedName, matchEnclosed, ignoreCase);
	}

	/**
	 * Creates and returns a type hierarchy for this type containing
	 * this type and all of its supertypes and subtypes in the workspace.
	 *
	 * @param info the given type
	 * @param monitor the given progress monitor
	 * @return a type hierarchy for the given type
	 */
	public static ITypeHierarchy createTypeHierarchy(ICElement type, IProgressMonitor monitor) throws CModelException {
	    ITypeInfo info = TypeCacheManager.getInstance().getTypeForElement(type, true, true, fgEnableIndexing, monitor);
	    if (info != null)
	        return fgTypeHierarchyBuilder.createTypeHierarchy(info, fgEnableIndexing, monitor);
	    return null;
	}
	
    public static void addTypeCacheChangedListener(ITypeCacheChangedListener listener) {
        TypeCacheManager.getInstance().addTypeCacheChangedListener(listener);
    }

    public static void removeTypeCacheChangedListener(ITypeCacheChangedListener listener) {
        TypeCacheManager.getInstance().removeTypeCacheChangedListener(listener);
    }
    
    public static ITypeInfo getTypeForElement(ICElement element, boolean forceUpdate, boolean forceResolve, IProgressMonitor monitor) {
        return TypeCacheManager.getInstance().getTypeForElement(element, forceUpdate, forceResolve, fgEnableIndexing, monitor);
    }

    public static ICElement getElementForType(ITypeInfo type, boolean forceUpdate, boolean forceResolve, IProgressMonitor monitor) {
        return TypeCacheManager.getInstance().getElementForType(type, forceUpdate, forceResolve, fgEnableIndexing, monitor);
    }
}
