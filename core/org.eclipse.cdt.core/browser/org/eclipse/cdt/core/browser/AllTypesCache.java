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

import java.util.Collection;
import java.util.Iterator;

import org.eclipse.cdt.core.CCorePlugin;
import org.eclipse.cdt.core.model.CoreModel;
import org.eclipse.cdt.core.model.IWorkingCopy;
import org.eclipse.cdt.core.search.ICSearchScope;
import org.eclipse.cdt.core.search.SearchEngine;
import org.eclipse.cdt.internal.core.browser.cache.TypeCache;
import org.eclipse.cdt.internal.core.browser.cache.TypeCacheDeltaListener;
import org.eclipse.cdt.internal.core.browser.cache.TypeCacherJob;
import org.eclipse.cdt.internal.core.browser.util.ArrayUtil;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.Preferences;
import org.eclipse.core.runtime.Preferences.IPropertyChangeListener;
import org.eclipse.core.runtime.Preferences.PropertyChangeEvent;
import org.eclipse.core.runtime.jobs.IJobManager;
import org.eclipse.core.runtime.jobs.Job;

/**
 * Manages a search cache for types in the workspace. Instead of returning objects of type <code>ICElement</code>
 * the methods of this class returns a list of the lightweight objects <code>TypeInfo</code>.
 * <P>
 * AllTypesCache runs asynchronously using a background job to rebuild the cache as needed.
 * If the cache becomes dirty again while the background job is running, the job is restarted.
 * <P>
 * If <code>getTypes</code> is called in response to a user action, a progress dialog is shown.
 * If called before the background job has finished, getTypes waits
 * for the completion of the background job.
 */
public class AllTypesCache {
	
	private static final int INITIAL_DELAY= 5000;
	private static TypeCache fgCache;
	private static IWorkingCopyProvider fWorkingCopyProvider;
	private static TypeCacheDeltaListener fgDeltaListener;
	private static IPropertyChangeListener fgPropertyChangeListener;
	private static boolean fBackgroundJobEnabled;

	/** Preference key for enabling background cache */
    public final static String ENABLE_BACKGROUND_TYPE_CACHE = "enableBackgroundTypeCache"; //$NON-NLS-1$
	
	/**
	 * Defines a simple interface in order to provide
	 * a level of abstraction between the Core and UI
	 * code.
	 */
	public static interface IWorkingCopyProvider {
		public IWorkingCopy[] getWorkingCopies();
	}
	
	/**
	 * Initializes the AllTypesCache service.
	 * 
	 * @param provider A working copy provider.
	 */
	public static void initialize(IWorkingCopyProvider provider) {

		// load prefs
		Preferences prefs= CCorePlugin.getDefault().getPluginPreferences();
		if (prefs.contains(ENABLE_BACKGROUND_TYPE_CACHE)) {
			fBackgroundJobEnabled= prefs.getBoolean(ENABLE_BACKGROUND_TYPE_CACHE);
		} else {
			prefs.setDefault(ENABLE_BACKGROUND_TYPE_CACHE, true);
			prefs.setValue(ENABLE_BACKGROUND_TYPE_CACHE, true);
			CCorePlugin.getDefault().savePluginPreferences();
			fBackgroundJobEnabled= true;
		}

		fgCache= new TypeCache();
		fWorkingCopyProvider = provider;
		fgDeltaListener= new TypeCacheDeltaListener(fgCache, fWorkingCopyProvider, fBackgroundJobEnabled);

		fgPropertyChangeListener= new IPropertyChangeListener() {
			public void propertyChange(PropertyChangeEvent event) {
				String property= event.getProperty();		
				if (property.equals(ENABLE_BACKGROUND_TYPE_CACHE)) {
					String value= (String)event.getNewValue();
					fBackgroundJobEnabled= Boolean.valueOf(value).booleanValue();
					fgDeltaListener.setBackgroundJobEnabled(fBackgroundJobEnabled);
					if (!fBackgroundJobEnabled) {
						// terminate all background jobs
						IJobManager jobMgr = Platform.getJobManager();
						jobMgr.cancel(TypeCacherJob.FAMILY);
					}
				}
			}
		};
		// add property change listener
		prefs.addPropertyChangeListener(fgPropertyChangeListener);

		if (fBackgroundJobEnabled) {
			TypeCacherJob typeCacherJob = new TypeCacherJob(fgCache, fWorkingCopyProvider);
			typeCacherJob.setSearchPaths(null);
			typeCacherJob.setPriority(Job.BUILD);
			typeCacherJob.schedule(INITIAL_DELAY);
		}
		// add delta listener
		CoreModel.getDefault().addElementChangedListener(fgDeltaListener);
	}
	
	/**
	 * Terminates the service provided by AllTypesCache.
	 */
	public static void terminate() {
		// remove delta listener
		CoreModel.getDefault().removeElementChangedListener(fgDeltaListener);
		
		// terminate all background jobs
		IJobManager jobMgr = Platform.getJobManager();
		jobMgr.cancel(TypeCacherJob.FAMILY);
	}
	
	/*
	 * Returns the actual type cache.
	 */
	public static TypeCache getCache() {
		return fgCache;
	}
	
	/**
	 * Returns true if the type cache is up to date.
	 */
	public static boolean isCacheUpToDate() {
		return !fgCache.isDirty();
	}
	
	/**
	 * Returns all types in the given scope.
	 * @param scope The search scope
	 * @param kinds Array containing CElement types:
	 * C_NAMESPACE, C_CLASS, C_UNION, C_ENUMERATION, C_TYPEDEF
	 * @param monitor Progress monitor to display search progress
	 * @param typesFound The resulting <code>TypeInfo</code> elements are added to this collection
	 */		
	public static void getTypes(ICSearchScope scope, int[] kinds, IProgressMonitor monitor, Collection typesFound) {
		if (!isCacheUpToDate()) {
			// start job if not already running
			IJobManager jobMgr = Platform.getJobManager();
			Job[] jobs = jobMgr.find(TypeCacherJob.FAMILY);
			if (jobs.length == 0) {
				// boost priority since action was user-initiated
				TypeCacherJob typeCacherJob = new TypeCacherJob(fgCache, fWorkingCopyProvider);
				typeCacherJob.setSearchPaths(null);
				typeCacherJob.setPriority(Job.SHORT);
				typeCacherJob.schedule();
			}
			
			// wait for job to finish
			jobs = jobMgr.find(TypeCacherJob.FAMILY);
			try {
				for (int i = 0; i < jobs.length; ++i) {
					TypeCacherJob job = (TypeCacherJob) jobs[i];
					job.join(monitor);
				}
				if (monitor != null)
					monitor.done();
			} catch (InterruptedException ex) {
				return;
			}
		}
		
		boolean isWorkspaceScope= scope.equals(SearchEngine.createWorkspaceScope());
		for (Iterator typesIter= fgCache.getAllTypes().iterator(); typesIter.hasNext(); ) {
			ITypeInfo info= (ITypeInfo) typesIter.next();
			if ( ArrayUtil.contains(kinds, info.getType()) &&
				(isWorkspaceScope || info.isEnclosed(scope)) ) {
				typesFound.add(info);
			}
		}
	}
}
