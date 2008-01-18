/*******************************************************************************
 * Copyright (c) 2006, 2008 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * QNX - Initial API and implementation
 * Markus Schorn (Wind River Systems)
 * Ed Swartz (Nokia)
 *******************************************************************************/

package org.eclipse.cdt.internal.ui.search;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.core.runtime.Status;
import org.eclipse.search.ui.ISearchQuery;
import org.eclipse.search.ui.ISearchResult;

import org.eclipse.cdt.core.CCorePlugin;
import org.eclipse.cdt.core.dom.ast.IASTFileLocation;
import org.eclipse.cdt.core.dom.ast.IBinding;
import org.eclipse.cdt.core.index.IIndex;
import org.eclipse.cdt.core.index.IIndexBinding;
import org.eclipse.cdt.core.index.IIndexName;
import org.eclipse.cdt.core.model.CoreModel;
import org.eclipse.cdt.core.model.ICElement;
import org.eclipse.cdt.core.model.ICProject;
import org.eclipse.cdt.core.parser.util.ArrayUtil;
import org.eclipse.cdt.ui.CUIPlugin;

/**
 * @author Doug Schaefer
 *
 */
public abstract class PDOMSearchQuery implements ISearchQuery {

	public static final int FIND_DECLARATIONS = 0x1;
	public static final int FIND_DEFINITIONS = 0x2;
	public static final int FIND_REFERENCES = 0x4;
	public static final int FIND_DECLARATIONS_DEFINITIONS = FIND_DECLARATIONS | FIND_DEFINITIONS;
	public static final int FIND_ALL_OCCURANCES = FIND_DECLARATIONS | FIND_DEFINITIONS | FIND_REFERENCES;
	
	protected PDOMSearchResult result = new PDOMSearchResult(this);
	protected int flags;
	
	protected ICElement[] scope;
	protected ICProject[] projects;

	protected PDOMSearchQuery(ICElement[] scope, int flags) {
		this.flags = flags;
		this.scope = scope;
		
		try {
			if (scope == null) {
				// All CDT projects in workspace
				ICProject[] allProjects = CoreModel.getDefault().getCModel().getCProjects();
				
				// Filter out closed projects for this case
				for (int i = 0; i < allProjects.length; i++) {
					if (!allProjects[i].isOpen()) { 
						allProjects[i] = null;
					}
				}
				
				projects = (ICProject[]) ArrayUtil.removeNulls(ICProject.class, allProjects);
			} else {
				Map projectMap = new HashMap();
				
				for (int i = 0; i < scope.length; ++i) {
					ICProject project = scope[i].getCProject();
					if (project != null)
						projectMap.put(project.getElementName(), project);
				}
				
				projects = (ICProject[])projectMap.values().toArray(new ICProject[projectMap.size()]);
			}
		} catch (CoreException e) {
			CUIPlugin.getDefault().log(e);
		}
	}
	
	public String getLabel() {
		String type;
		if ((flags & FIND_REFERENCES) != 0)
			type = CSearchMessages.PDOMSearch_query_refs_label; 
		else if ((flags & FIND_DECLARATIONS) != 0)
			type = CSearchMessages.PDOMSearch_query_decls_label; 
		else
 			type = CSearchMessages.PDOMSearch_query_defs_label; 
		return type;
	}

	public boolean canRerun() {
		return true;
	}

	public boolean canRunInBackground() {
		return true;
	}

	public ISearchResult getSearchResult() {
		return result;
	}

	/**
	 * Return true to filter name out of the match list.
	 * Override in a subclass to add scoping.
	 * @param name
	 * @return true to filter name out of the match list
	 */
	protected boolean filterName(IIndexName name) {
		return false; // i.e. keep it
	}
	
	private void collectNames(IIndex index, IIndexName[] names) throws CoreException {
		for (int i = 0; i < names.length; i++) {
			IIndexName name = names[i];
			if (!filterName(name)) {
				IASTFileLocation loc = name.getFileLocation();
				IIndexBinding binding= index.findBinding(name);
				result.addMatch(new PDOMSearchMatch(index, binding, name, loc.getNodeOffset(), loc.getNodeLength()));
			}
		}
	}

	protected void createMatches(IIndex index, IBinding binding) throws CoreException {
		if (binding != null) {
			IIndexName[] names= index.findNames(binding, flags);
			collectNames(index, names);
		}
	}

	public final IStatus run(IProgressMonitor monitor) throws OperationCanceledException {
		PDOMSearchResult result= (PDOMSearchResult) getSearchResult();
		result.removeAll();
		
		result.setIndexerBusy(!CCorePlugin.getIndexManager().isIndexerIdle());
		
		try {
			IIndex index= CCorePlugin.getIndexManager().getIndex(projects, 0);
			try {
				index.acquireReadLock();
			} catch (InterruptedException e) {
				return Status.CANCEL_STATUS;
			}
			try {
				return runWithIndex(index, monitor);
			}
			finally {
				index.releaseReadLock();
			}
		} catch (CoreException e) {
			return e.getStatus();
		}
	}

	abstract protected IStatus runWithIndex(IIndex index, IProgressMonitor monitor);

	/**
	 * Get the projects involved in the search.
	 * @return array, never <code>null</code>
	 */
	public ICProject[] getProjects() {
		return projects;
	}
}
