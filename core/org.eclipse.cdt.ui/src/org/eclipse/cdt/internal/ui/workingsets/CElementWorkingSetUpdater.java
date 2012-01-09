/*******************************************************************************
 * Copyright (c) 2000, 2008 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     Anton Leherbauer (Wind River Systems)
 *******************************************************************************/
package org.eclipse.cdt.internal.ui.workingsets;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceDelta;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.IJobChangeEvent;
import org.eclipse.core.runtime.jobs.ISchedulingRule;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.core.runtime.jobs.JobChangeAdapter;
import org.eclipse.ui.IWorkingSet;
import org.eclipse.ui.IWorkingSetUpdater;

import org.eclipse.cdt.core.model.CoreModel;
import org.eclipse.cdt.core.model.ElementChangedEvent;
import org.eclipse.cdt.core.model.ICElement;
import org.eclipse.cdt.core.model.ICElementDelta;
import org.eclipse.cdt.core.model.ICProject;
import org.eclipse.cdt.core.model.IElementChangedListener;


public class CElementWorkingSetUpdater implements IWorkingSetUpdater, IElementChangedListener {

	private static class SingletonRule implements ISchedulingRule {
		public static final ISchedulingRule INSTANCE = new SingletonRule();
		@Override
		public boolean contains(ISchedulingRule rule) {
			return rule == this;
		}
		@Override
		public boolean isConflicting(ISchedulingRule rule) {
			return rule == this;
		}
	}

	private static class WorkingSetCheck extends Job {
		private final IWorkingSet fWorkingSet;
		WorkingSetCheck(final IWorkingSet workingSet) {
			super("Check WorkingSet"); //$NON-NLS-1$
			fWorkingSet= workingSet;
		}
		/*
		 * @see org.eclipse.core.runtime.jobs.Job#run(org.eclipse.core.runtime.IProgressMonitor)
		 */
		@Override
		protected IStatus run(IProgressMonitor monitor) {
			synchronized (fWorkingSet) {
				checkElementExistence(fWorkingSet);
			}
			return Status.OK_STATUS;
		}
	}

	public static final String ID= "org.eclipse.cdt.ui.CElementWorkingSetPage"; //$NON-NLS-1$
	
	private List<IWorkingSet> fWorkingSets;
	
	private static class WorkingSetDelta {
		private IWorkingSet fWorkingSet;
		private List<Object> fElements;
		private boolean fChanged;
		public WorkingSetDelta(IWorkingSet workingSet) {
			fWorkingSet= workingSet;
			synchronized (fWorkingSet) {
				fElements= new ArrayList<Object>(Arrays.asList(fWorkingSet.getElements()));
			}
		}
		public int indexOf(Object element) {
			return fElements.indexOf(element);
		}
		public void set(int index, Object element) {
			if (element == null) {
				remove(index);
			} else {
				fElements.set(index, element);
				fChanged= true;
			}
		}
		public void remove(int index) {
			if (fElements.remove(index) != null) {
				fChanged= true;
			}
		}
		public void process() {
			if (fChanged) {
				fWorkingSet.setElements(fElements.toArray(new IAdaptable[fElements.size()]));
			}
		}
	}
	
	public CElementWorkingSetUpdater() {
		fWorkingSets= new ArrayList<IWorkingSet>();
		CoreModel.getDefault().addElementChangedListener(this);
	}
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public void add(final IWorkingSet workingSet) {
		// delay the check for existence - this may be called very early in the bootstrap
		// otherwise it is causing all kinds of weird exceptions
		Job check= new WorkingSetCheck(workingSet);
		check.setUser(false);
		check.setPriority(Job.SHORT);
		// make jobs run sequential
		check.setRule(SingletonRule.INSTANCE);
		check.schedule(2000 + fWorkingSets.size() * 100);
		check.addJobChangeListener(new JobChangeAdapter() {
			@Override
			public void done(IJobChangeEvent event) {
				synchronized (fWorkingSets) {
					fWorkingSets.add(workingSet);
				}
			}});
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean remove(IWorkingSet workingSet) {
		boolean result;
		synchronized(fWorkingSets) {
			result= fWorkingSets.remove(workingSet);
		}
		return result;
	}
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean contains(IWorkingSet workingSet) {
		synchronized(fWorkingSets) {
			return fWorkingSets.contains(workingSet);
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void dispose() {
		synchronized(fWorkingSets) {
			fWorkingSets.clear();
		}
		CoreModel.getDefault().removeElementChangedListener(this);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void elementChanged(ElementChangedEvent event) {
		IWorkingSet[] workingSets;
		synchronized(fWorkingSets) {
			workingSets= fWorkingSets.toArray(new IWorkingSet[fWorkingSets.size()]);
		}
		for (int w= 0; w < workingSets.length; w++) {
			WorkingSetDelta workingSetDelta= new WorkingSetDelta(workingSets[w]);
			processCElementDelta(workingSetDelta, event.getDelta());
			IResourceDelta[] resourceDeltas= event.getDelta().getResourceDeltas();
			if (resourceDeltas != null) {
				for (int r= 0; r < resourceDeltas.length; r++) {
					processResourceDelta(workingSetDelta, resourceDeltas[r]);
				}
			}
			workingSetDelta.process();
		}
	}

	private void processCElementDelta(WorkingSetDelta result, ICElementDelta delta) {
		ICElement cElement= delta.getElement();
		int index= result.indexOf(cElement);
		int type= cElement.getElementType();
		int kind= delta.getKind();
		int flags= delta.getFlags();
		if (type == ICElement.C_PROJECT && kind == ICElementDelta.CHANGED) {
			if (index != -1 && (flags & ICElementDelta.F_CLOSED) != 0) {
				result.set(index, ((ICProject)cElement).getProject());
			} else if ((flags & ICElementDelta.F_OPENED) != 0) {
				index= result.indexOf(((ICProject)cElement).getProject());
				if (index != -1)
					result.set(index, cElement);
			}
		} else if (type == ICElement.C_PROJECT && kind == ICElementDelta.REMOVED) {
			if (index != -1)
				result.set(index, ((ICProject)cElement).getProject());
		} else if (type == ICElement.C_PROJECT && kind == ICElementDelta.ADDED) {
			index= result.indexOf(((ICProject)cElement).getProject());
			if (index != -1)
				result.set(index, cElement);
		} else if (index != -1) {
			if (kind == ICElementDelta.REMOVED) {
				if ((flags & ICElementDelta.F_MOVED_TO) != 0) {
					result.set(index, delta.getMovedToElement());
				} else {
					result.remove(index);
				}
			}
		}
		IResourceDelta[] resourceDeltas= delta.getResourceDeltas();
		if (resourceDeltas != null) {
			for (int i= 0; i < resourceDeltas.length; i++) {
				processResourceDelta(result, resourceDeltas[i]);
			}
		}
		ICElementDelta[] children= delta.getAffectedChildren();
		for (int i= 0; i < children.length; i++) {
			processCElementDelta(result, children[i]);
		}
	}
	
	private void processResourceDelta(WorkingSetDelta result, IResourceDelta delta) {
		IResource resource= delta.getResource();
		int type= resource.getType();
		int index= result.indexOf(resource);
		int kind= delta.getKind();
		int flags= delta.getFlags();
		if (kind == IResourceDelta.CHANGED && type == IResource.PROJECT && index != -1) {
			if ((flags & IResourceDelta.OPEN) != 0) {
				result.set(index, resource);
			}
		}
		if (index != -1 && kind == IResourceDelta.REMOVED) {
			if ((flags & IResourceDelta.MOVED_TO) != 0) {
				result.set(index, 
					ResourcesPlugin.getWorkspace().getRoot().findMember(delta.getMovedToPath()));
			} else {
				result.remove(index);
			}
		}
		
		// Don't dive into closed or opened projects
		if (projectGotClosedOrOpened(resource, kind, flags))
			return;
		
		IResourceDelta[] children= delta.getAffectedChildren();
		for (int i= 0; i < children.length; i++) {
			processResourceDelta(result, children[i]);
		}
	}

	private boolean projectGotClosedOrOpened(IResource resource, int kind, int flags) {
		return resource.getType() == IResource.PROJECT 
			&& kind == IResourceDelta.CHANGED 
			&& (flags & IResourceDelta.OPEN) != 0;
	}
	
	private static void checkElementExistence(IWorkingSet workingSet) {
		List<IAdaptable> elements= new ArrayList<IAdaptable>(Arrays.asList(workingSet.getElements()));
		boolean changed= false;
		for (Iterator<IAdaptable> iter= elements.iterator(); iter.hasNext();) {
			IAdaptable element= iter.next();
			boolean remove= false;
			if (element instanceof ICElement) {
				ICElement cElement= (ICElement)element;
				// If we have directly a project then remove it when it
				// doesn't exist anymore. However if we have a sub element
				// under a project only remove the element if the parent
				// project is open. Otherwise we would remove all elements
				// in closed projects.
				if (cElement instanceof ICProject) {
					remove= !cElement.exists();
				} else {
					IProject project= cElement.getCProject().getProject();
					remove= project.isOpen() && !cElement.exists();
				}
			} else if (element instanceof IResource) {
				IResource resource= (IResource)element;
				// See comments above
				if (resource instanceof IProject) {
					remove= !resource.exists();
				} else {
					IProject project= resource.getProject();
					remove= (project != null ? project.isOpen() : true) && !resource.exists();
				}
			} else if (element == null) {
				// should not happen anyway, but who knows?
				remove= true;
			}
			if (remove) {
				iter.remove();
				changed= true;
			}
		}
		if (changed) {
			workingSet.setElements(elements.toArray(new IAdaptable[elements.size()]));
		}
	}
}
