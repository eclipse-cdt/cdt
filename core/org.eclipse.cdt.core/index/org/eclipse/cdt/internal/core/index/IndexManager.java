package org.eclipse.cdt.internal.core.index;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.eclipse.cdt.core.index.ITagEntry;
import org.eclipse.cdt.core.index.IndexModel;
import org.eclipse.cdt.core.model.CModelException;
import org.eclipse.cdt.core.model.CoreModel;
import org.eclipse.cdt.core.model.ElementChangedEvent;
import org.eclipse.cdt.core.model.ICElement;
import org.eclipse.cdt.core.model.ICElementDelta;
import org.eclipse.cdt.core.model.ICResource;
import org.eclipse.cdt.core.model.IElementChangedListener;
import org.eclipse.cdt.core.model.ITranslationUnit;
import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;

public class IndexManager implements IElementChangedListener {

	RequestList requestList = null;
	Thread thread = null;
	Map projectsMap = null;

	private static IndexManager indexManager = null;

	private IndexManager() {
	}

	public ITagEntry[] query(IProject project, String pattern, boolean ignoreCase, boolean exactMatch) {
		Map filesMap = (Map)projectsMap.get(project.getLocation());
		// try to kick start.
		if (filesMap == null) {
			addContainer(project);
		}
		List result = new ArrayList();
		filesMap = (Map)projectsMap.get(project.getLocation());
		if (filesMap != null) {
			if (pattern == null)
				pattern = "*"; //FIXME: is this right null matching all?
			// Compile the pattern
			StringMatcher matcher = new StringMatcher(pattern, ignoreCase, exactMatch);
			Iterator values = filesMap.values().iterator();
			while (values.hasNext()) {
				ITagEntry[] entries = (ITagEntry[])values.next();
				for (int j = 0; j < entries.length; j++) {
					String tagName = entries[j].getTagName();
					if (tagName != null && matcher.match(tagName)) {
						result.add(entries[j]);
					}
					//if (tagName != null && (pattern == null || tagName.equals(pattern))) {
					//	result.add(entries[j]);
					//}
				}
			}
		}
		return (ITagEntry[])result.toArray(new ITagEntry[0]);
	}

	protected RequestList getRequestList() {
		return requestList;
	}

	protected Map getProjectsMap() {
		return projectsMap;
	}

	protected void init () {
		requestList = new RequestList();
		projectsMap = Collections.synchronizedMap(new HashMap());
		CTagsRunner ctags = new CTagsRunner(this);
		thread = new Thread(ctags, "C Tags indexer");
		thread.setDaemon(true);
		thread.setPriority (Thread.NORM_PRIORITY - 1);
		thread.start();
		addAll();
	}

	/**
	 * Before processing all jobs, need to ensure that the indexes are up to date.
	 */
	protected static void delay() {
		try {
			// wait 10 seconds so as not to interfere with plugin startup
			Thread.currentThread().sleep(10000);
		} catch (InterruptedException ie) {
		}	
	}

	/**
	 * About to delete a project.
	 */
	public void removeResource(IResource resource) {
		Map filesMap = (Map)projectsMap.get(resource.getProject().getLocation());
		if (filesMap == null)
			return;

		clearRequestList(resource);
		switch (resource.getType()) {
			case IResource.ROOT:
				// PROBLEM?
			break;

			case IResource.PROJECT:
				projectsMap.remove(resource.getLocation());	
				// FALL_THROUGHT

			case IResource.FOLDER:
				removeContainer((IContainer)resource);
			break;

			case IResource.FILE:
				removeFile((IFile)resource);
			break;
		}
	}

	public void removeContainer(IContainer container) {
		Map filesMap = (Map)projectsMap.get(container.getProject().getLocation());
		if (filesMap == null)
			return;

		IPath folderPath = container.getLocation();
		if (filesMap != null) {
			Iterator keys = filesMap.keySet().iterator();
			while (keys.hasNext()) {
				IPath p = (IPath)keys.next();
				if (p != null && folderPath.isPrefixOf(p)) {
//System.out.println("Removing [" + folderPath + "] " + p);
					filesMap.remove(p);
				}
			}
		}
	}

	public void removeFile(IFile file) {
		Map filesMap = (Map)projectsMap.get(file.getProject().getLocation());
		if (filesMap != null) {
			filesMap.remove(file.getLocation());
		}
	}

	public void clearRequestList(IResource resource) {
		if (resource instanceof IFile) {
			requestList.removeItem(resource);
		} else if (resource instanceof IContainer) {
			try {
				IContainer container = (IContainer)resource;
				IResource[] resources = container.members(false);
				for (int i = 0; i < resources.length; i++) {
					clearRequestList(resources[i]);
				}
			} catch (CoreException e) {
			}
		}
	}

	public void addResource(IResource resource) {
		switch (resource.getType()) {
			case IResource.ROOT:
			case IResource.PROJECT:
			case IResource.FOLDER:
				addContainer((IContainer)resource);
			break;

			case IResource.FILE:
				addFile((IFile)resource);
			break;
		}
	}

	/**
	 * Trigger addition of a resource to an index
	 * Note: the actual operation is performed in background
	 */
	public void addFile(IFile file) {
		if (CoreModel.getDefault().isTranslationUnit(file) &&
			IndexModel.getDefault().isEnabled(file.getProject())) {
			requestList.addItem(file);
		}
	}

	/**
	 * Trigger addition of the entire content of a project
	 * Note: the actual operation is performed in background 
	 */
	public void addContainer(IContainer container) {
		if (container != null && container.exists()) {
			try {
				IResource[] resources = container.members(false);
				for (int i = 0; i < resources.length; i++) {
					IResource res = resources[i];
					switch(res.getType()) {
						case IResource.ROOT:
						break;

						case IResource.PROJECT:
							if (CoreModel.getDefault().hasCNature((IProject)res) &&
								IndexModel.getDefault().isEnabled((IProject)res)) {
								addContainer((IContainer)res);
							}
						break;

						case IResource.FOLDER:
							addContainer((IContainer)res);
						break;

						case IResource.FILE:
							addFile((IFile)res);
						break;
					}
				}
			} catch (CoreException e) {
				e.printStackTrace();
			}
		}
	}

	public void addAll() {
		addResource(ResourcesPlugin.getWorkspace().getRoot());
	}

	public void saveIndexes() {
	}

	public void saveIndex(IProject project) {
	}

	public void shutdown() {
		if (thread != null)
			thread.interrupted();
		CoreModel.getDefault().removeElementChangedListener(this);	
	}

	public static IndexManager getDefault() {
		if (indexManager == null) {
			indexManager = new IndexManager();
			indexManager.init();
			// Register to the C Core Model for C specific changes.
			CoreModel.getDefault().addElementChangedListener(indexManager);
		}
		return indexManager;
	}

	protected void processDelta(ICElementDelta delta) throws CModelException {
		int kind= delta.getKind();
		int flags= delta.getFlags();
		ICElement element= delta.getElement();

		//System.out.println("Index Processing Delta " + element);
		// handle open and closing of a solution or project
		if (((flags & ICElementDelta.F_CLOSED) != 0)
			|| ((flags & ICElementDelta.F_OPENED) != 0)) {
		}

		if (kind == ICElementDelta.REMOVED) {
			try {
				IResource resource = ((ICResource)element).getResource();
				removeResource(resource);
			} catch (CModelException e) {
			}
		}

//		if (kind == ICElementDelta.ADDED) {
//			try {
//				IResource resource = ((ICResource)element).getResource();
//				addResource(resource);
//			} catch (CModelException e) {
//			}
//		}

		if (element instanceof ITranslationUnit) {
			if (kind == ICElementDelta.CHANGED) {
				IResource resource = ((ICResource)element).getResource();
				addResource(resource);
				return;
			}
		}

		ICElementDelta[] affectedChildren= delta.getAffectedChildren();
		for (int i= 0; i < affectedChildren.length; i++) {
			processDelta(affectedChildren[i]);
		}
	}

	/* (non-Cdoc)
	 * Method declared on IElementChangedListener.
	 */
	public void elementChanged(final ElementChangedEvent event) {
		try {
			processDelta(event.getDelta());
		} catch(CModelException e) {
			e.printStackTrace();
		}
	}
}
