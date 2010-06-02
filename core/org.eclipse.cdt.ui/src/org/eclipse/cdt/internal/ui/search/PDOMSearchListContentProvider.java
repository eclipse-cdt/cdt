/*******************************************************************************
 * Copyright (c) 2006, 2010 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * QNX - Initial API and implementation
 * Ed Swartz (Nokia)
 *******************************************************************************/

package org.eclipse.cdt.internal.ui.search;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.Viewer;

import com.ibm.icu.text.MessageFormat;

import org.eclipse.cdt.core.CCorePlugin;
import org.eclipse.cdt.core.model.ICProject;
import org.eclipse.cdt.ui.CUIPlugin;

/**
 * @author Doug Schaefer
 *
 */
public class PDOMSearchListContentProvider implements
		IStructuredContentProvider, IPDOMSearchContentProvider {

	private TableViewer viewer;
	private PDOMSearchResult result;
	private final PDOMSearchViewPage fPage;

	PDOMSearchListContentProvider(PDOMSearchViewPage page) {
		fPage= page;
	}

	public Object[] getElements(Object inputElement) {
		Set<String> uncoveredProjects = new HashSet<String>(); 
		
		PDOMSearchResult result = (PDOMSearchResult) inputElement;
		
		Object[] results = result.getElements();
		List<Object> resultList = new ArrayList<Object>();
	
		// see which projects returned results
		for (int i = 0; i < results.length; i++) {
			if (results[i] instanceof PDOMSearchElement) {
				PDOMSearchElement searchElement = (PDOMSearchElement) results[i];
				String path = searchElement.getLocation().getFullPath();
				if (path != null) {
					uncoveredProjects.add(new Path(path).segment(0));
				}
				if (fPage.getDisplayedMatchCount(searchElement) > 0) {
					resultList.add(searchElement);
				}
			}
		}

		// see if indexer was busy
		if (result.wasIndexerBusy()) {
			resultList.add(IPDOMSearchContentProvider.INCOMPLETE_RESULTS_NODE);
		}

		// add message for all the projects which have no results
		ICProject[] projects = ((PDOMSearchQuery)result.getQuery()).getProjects();
		for (int i = 0; i < projects.length; ++i) {
			ICProject project = projects[i];
			boolean foundProject = uncoveredProjects.contains(project.getProject().getName());
			if (!foundProject) {
				if (project.isOpen()) {
					if (!CCorePlugin.getIndexManager().isProjectIndexed(project)) {
						resultList.add(createUnindexedProjectWarningElement(project));
					}
				} else {
					resultList.add(createClosedProjectWarningElement(project));
				}
			}
		}
		
		return resultList.toArray();
	}

	private Status createUnindexedProjectWarningElement(ICProject project) {
		return new Status(IStatus.WARNING, CUIPlugin.PLUGIN_ID,
				MessageFormat.format(
					CSearchMessages.PDOMSearchListContentProvider_IndexerNotEnabledMessageFormat, 
					new Object[] { project.getProject().getName() }));
	}

	private Status createClosedProjectWarningElement(ICProject project) {
		return new Status(IStatus.WARNING, CUIPlugin.PLUGIN_ID,
				MessageFormat.format(
					CSearchMessages.PDOMSearchListContentProvider_ProjectClosedMessageFormat, 
					new Object[] { project.getProject().getName() }));
	}

	public void dispose() {
	}

	public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
		this.viewer = (TableViewer)viewer;
		result = (PDOMSearchResult)newInput;
		viewer.refresh();
	}

	public void elementsChanged(Object[] elements) {
		if (result == null)
			return;
		
		for (int i= 0; i < elements.length; i++) {
			if (fPage.getDisplayedMatchCount(elements[i]) > 0) {
				if (viewer.testFindItem(elements[i]) != null)
					viewer.refresh(elements[i]);
				else
					viewer.add(elements[i]);
			} else {
				viewer.remove(elements[i]);
			}
		}
	}
	
	public void clear() {
		viewer.refresh();
	}
}
