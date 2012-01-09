/*******************************************************************************
 * Copyright (c) 2008 Wind River Systems, Inc. and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Markus Schorn - initial API and implementation
 *******************************************************************************/ 
package org.eclipse.cdt.internal.ui.viewsupport;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.ISchedulingRule;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jface.viewers.ILabelProviderListener;
import org.eclipse.jface.viewers.LabelProviderChangedEvent;
import org.eclipse.swt.SWTException;
import org.eclipse.ui.IDecoratorManager;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.PlatformUI;

import org.eclipse.cdt.core.CCorePlugin;
import org.eclipse.cdt.core.index.IIndex;
import org.eclipse.cdt.core.index.IIndexChangeEvent;
import org.eclipse.cdt.core.index.IIndexChangeListener;
import org.eclipse.cdt.core.index.IIndexFile;
import org.eclipse.cdt.core.index.IIndexFileLocation;
import org.eclipse.cdt.core.index.IIndexManager;
import org.eclipse.cdt.core.index.IIndexerStateEvent;
import org.eclipse.cdt.core.index.IIndexerStateListener;
import org.eclipse.cdt.core.model.CoreModel;
import org.eclipse.cdt.core.model.ICProject;

class IndexedFilesCache implements IIndexChangeListener, IIndexerStateListener, ILabelProviderListener {
	private static final String DECORATOR_ID = "org.eclipse.cdt.ui.indexedFiles"; //$NON-NLS-1$
	private static final IndexedFilesCache INSTANCE = new IndexedFilesCache();
	private static final ISchedulingRule RULE = new ISchedulingRule() {
		@Override
		public boolean contains(ISchedulingRule rule) {
			return rule == this;
		}
		@Override
		public boolean isConflicting(ISchedulingRule rule) {
			return rule == this;
		}
	};

	public static IndexedFilesCache getInstance() {
		return INSTANCE;
	}

	private final HashMap<String, Set<Integer>> fIndexedFiles= new HashMap<String, Set<Integer>>();
	private boolean fIsDirty= false;
	private boolean fActive= false;

	private void scheduleInitialize() {
		Job j= new Job(Messages.IndexedFilesCache_jobName) {
			@Override
			protected IStatus run(IProgressMonitor monitor) {
				try {
					ICProject[] prj= CoreModel.getDefault().getCModel().getCProjects();
					for (ICProject project : prj) {
						initialize(project);
					}
				} catch (CoreException e) {
					return e.getStatus();
				} catch (InterruptedException e) {
					Thread.currentThread().interrupt();
					return Status.CANCEL_STATUS;
				}
				checkTriggerDecorator(1);
				return Status.OK_STATUS;
			}
			@Override
			public boolean belongsTo(Object family) {
				return family == IndexedFilesCache.this;
			}
			
		};
		j.setSystem(true);
		j.setRule(RULE);
		j.schedule();		
	}

	private void scheduleInitialize(final ICProject project) {
		Job j= new Job(Messages.IndexedFilesCache_jobName) {
			@Override
			protected IStatus run(IProgressMonitor monitor) {
				try {
					initialize(project);
				} catch (CoreException e) {
					return e.getStatus();
				} catch (InterruptedException e) {
					Thread.currentThread().interrupt();
					return Status.CANCEL_STATUS;
				}
				checkTriggerDecorator(1);
				return Status.OK_STATUS;
			}
			@Override
			public boolean belongsTo(Object family) {
				return family == IndexedFilesCache.this;
			}
		};
		j.setSystem(true);
		j.setRule(RULE);
		j.schedule();		
	}

	final protected void initialize(ICProject prj) throws CoreException, InterruptedException {
		IIndex index= CCorePlugin.getIndexManager().getIndex(prj, 0);
		List<IIndexFileLocation> list= new ArrayList<IIndexFileLocation>();
		index.acquireReadLock();
		try {
			IIndexFile[] files= index.getAllFiles();
			for (IIndexFile ifile : files) {
				if (ifile.getTimestamp() >= 0) {
					list.add(ifile.getLocation());
				}
			}
			if (!list.isEmpty()) {
				final String prjName= prj.getElementName();
				synchronized(fIndexedFiles) {
					Set<Integer> cache= fIndexedFiles.get(prjName);
					if (cache == null) {
						cache= new HashSet<Integer>();
						fIndexedFiles.put(prjName, cache);
					}
					else {
						if (!cache.isEmpty()) {
							cache.clear();
							fIsDirty= true;
						}
					}
					for (IIndexFileLocation ifl: list) { 
						final int h= computeHash(ifl);
						if (cache.add(h)) {
							fIsDirty= true;
						}
					}
				}
			}
		}
		finally {
			index.releaseReadLock();
		}
	}

	@Override
	public void indexChanged(IIndexChangeEvent e) {
		// the index manager has reported a change to an index
		ICProject cproject= e.getAffectedProject();
		if (cproject == null) {
			return;
		}
		synchronized (fIndexedFiles) {
			if (!fActive) {
				return;
			}
			if (e.isReloaded()) {
				scheduleInitialize(cproject);
			}
			else {
				final String prjName = cproject.getElementName();
				if (e.isCleared()) {
					if (fIndexedFiles.remove(prjName) != null) {
						fIsDirty= true;
					}
				} 
				final Set<IIndexFileLocation> filesCleared = e.getFilesCleared();
				final Set<IIndexFileLocation> filesWritten = e.getFilesWritten();
				if (!(filesCleared.isEmpty() && filesWritten.isEmpty())) {
					Set<Integer> cache= fIndexedFiles.get(prjName);
					if (cache == null) {
						cache= new HashSet<Integer>();
						fIndexedFiles.put(prjName, cache);
					}
					for (IIndexFileLocation ifl: filesCleared) { 
						final int h= computeHash(ifl);
						if (cache.remove(h)) {
							fIsDirty= true;
						}
					}
					for (IIndexFileLocation ifl: filesWritten) { 
						final int h= computeHash(ifl);
						if (cache.add(h)) {
							fIsDirty= true;
						}
					}
				}
			}
		}
	}

	@Override
	public void indexChanged(IIndexerStateEvent event) {
		if (event.indexerIsIdle()) {
			checkTriggerDecorator(0);
		}
	}

	private void activate() {
		synchronized (fIndexedFiles) {
			fActive= true;
			PlatformUI.getWorkbench().getDecoratorManager().addListener(this);
			final IIndexManager indexManager = CCorePlugin.getIndexManager();
			indexManager.addIndexChangeListener(IndexedFilesCache.this);
			indexManager.addIndexerStateListener(IndexedFilesCache.this);
			scheduleInitialize();
		}
	}

	private void deactivate() {
		synchronized (fIndexedFiles) {
			fActive= false;
			fIndexedFiles.clear();
			final IIndexManager indexManager = CCorePlugin.getIndexManager();
			indexManager.removeIndexChangeListener(IndexedFilesCache.this);
			indexManager.removeIndexerStateListener(IndexedFilesCache.this);
			PlatformUI.getWorkbench().getDecoratorManager().removeListener(this);
		}
	}


	final protected void checkTriggerDecorator(int jobCount) {
		if (fIsDirty && CCorePlugin.getIndexManager().isIndexerIdle() && 
				Job.getJobManager().find(this).length == jobCount) {
			fIsDirty= false;
			final IWorkbench workbench= PlatformUI.getWorkbench();
			try {
				workbench.getDisplay().asyncExec(new Runnable(){
					@Override
					public void run() {
						workbench.getDecoratorManager().update(DECORATOR_ID);			
					}
				});
			}
			catch (SWTException e) {
				// in case the display is no longer valid
			}
		}
	}
	
	public boolean isIndexed(IProject project, IIndexFileLocation ifl) {
		// request from a label provider
		synchronized(fIndexedFiles) {
			if (!fActive) {
				activate();
			}
			Set<Integer> cache= fIndexedFiles.get(project.getName());
			return cache != null && cache.contains(computeHash(ifl));
		}
	}

	private int computeHash(IIndexFileLocation ifl) {
		final String fp= ifl.getFullPath();
		final int h1= fp == null ? 0 : fp.hashCode() * 43;
		return h1 + ifl.getURI().hashCode();
	}

	@Override
	public void labelProviderChanged(LabelProviderChangedEvent event) {
		final Object src= event.getSource();
		if (src instanceof IDecoratorManager) {
			IDecoratorManager mng= (IDecoratorManager) src;
			if (!mng.getEnabled(DECORATOR_ID)) {
				deactivate();
			}
		}
	}
}
