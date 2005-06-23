/*******************************************************************************
 * Copyright (c) 2000, 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.internal.ui.workingsets;

import org.eclipse.cdt.core.model.ICElement;
import org.eclipse.cdt.core.model.ICProject;
import org.eclipse.cdt.core.model.IPathEntryContainer;
import org.eclipse.cdt.core.model.ISourceRoot;
import org.eclipse.cdt.core.model.ITranslationUnit;
import org.eclipse.cdt.internal.corext.util.CModelUtil;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IStorage;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.IPath;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerFilter;
import org.eclipse.ui.IWorkingSet;

/**
 * Working set filter for Java viewers.
 */
public class WorkingSetFilter extends ViewerFilter {
	private IWorkingSet fWorkingSet= null;
	private IAdaptable[] fCachedWorkingSet= null;

	/**
	 * Returns the working set which is used by this filter.
	 * 
	 * @return the working set
	 */
	public IWorkingSet getWorkingSet() {
		return fWorkingSet;
	}
		
	/**
	 * Sets this filter's working set.
	 * 
	 * @param workingSet the working set
	 */
	public void setWorkingSet(IWorkingSet workingSet) {
		fWorkingSet= workingSet;
	}
	
	/*
	 * Overrides method from ViewerFilter.
	 */
	public boolean select(Viewer viewer, Object parentElement, Object element) {
		if (fWorkingSet == null)
			return true;

		if (element instanceof ICElement)
			return isEnclosing((ICElement)element);

		if (element instanceof IResource)
			return isEnclosing(((IResource)element).getFullPath());
		
		if (element instanceof IPathEntryContainer) {
			return isEnclosing((IPathEntryContainer)element);
		}
			
		if (element instanceof IAdaptable) {
			IAdaptable adaptable= (IAdaptable)element;
			ICElement je= (ICElement)adaptable.getAdapter(ICElement.class);
			if (je != null)
				return isEnclosing(je);

			IResource resource= (IResource)adaptable.getAdapter(IResource.class);
			if (resource != null)
				return isEnclosing(resource.getFullPath());
		}

		return true;
	}

	private boolean isEnclosing(IPathEntryContainer container) {
		// check whether the containing packagefragment root is enclosed
		return isEnclosing(container.getPath());
//		IPathEntry[] entries = container.getPathEntries();
//		if (entries != null && entries.length > 0) {
//			return isEnclosing(entries[0].getPath());
//		}
//		return false;
	}

	/*
 	 * Overrides method from ViewerFilter
 	 */
	public Object[] filter(Viewer viewer, Object parent, Object[] elements) {
		Object[] result= null;
		if (fWorkingSet != null) 
			fCachedWorkingSet= fWorkingSet.getElements();
		try {
			result= super.filter(viewer, parent, elements);
		} finally {
			fCachedWorkingSet= null;
		}
		return result;
	}

	private boolean isEnclosing(IPath elementPath) {
		if (elementPath == null)
			return false;
			
		IAdaptable[] cachedWorkingSet= fCachedWorkingSet;
		if (cachedWorkingSet == null)
			cachedWorkingSet= fWorkingSet.getElements();

		int length= cachedWorkingSet.length;
		for (int i= 0; i < length; i++) {
			if (isEnclosing(cachedWorkingSet[i], elementPath))
				return true;
		}
		return false;
	}
	
	public boolean isEnclosing(ICElement element) {
		IAdaptable[] cachedWorkingSet= fCachedWorkingSet;
		if (cachedWorkingSet == null)
			cachedWorkingSet= fWorkingSet.getElements();
		
		boolean isElementPathComputed= false;
		IPath elementPath= null; // will be lazy computed if needed
		
		int length= cachedWorkingSet.length;
		for (int i= 0; i < length; i++) {
			ICElement scopeElement= (ICElement)cachedWorkingSet[i].getAdapter(ICElement.class);
			if (scopeElement != null) {
				// compare Java elements
				ICElement searchedElement= element;
				while (scopeElement != null && searchedElement != null) {
					if (searchedElement.equals(scopeElement)) {
						return true;
					}
					if (scopeElement instanceof ICProject && searchedElement instanceof ISourceRoot) {
//						ISourceRoot pkgRoot= (ISourceRoot)searchedElement;
//						if (pkgRoot.isExternal() && pkgRoot.isArchive()) {
//						if (((ICProject)scopeElement).isOnClasspath(searchedElement))
//						return true;
//						}
					}
					searchedElement= searchedElement.getParent();
					if (searchedElement != null && searchedElement.getElementType() == ICElement.C_UNIT) {
						ITranslationUnit cu= (ITranslationUnit)searchedElement;
						cu= CModelUtil.toOriginal(cu);
					}
				}
				while (scopeElement != null && element != null) {
					if (element.equals(scopeElement))
						return true;
					scopeElement= scopeElement.getParent();
				}
			} else {
				// compare resource paths
				if (!isElementPathComputed) {
					IResource elementResource= (IResource)element.getAdapter(IResource.class);
					if (elementResource != null)
						elementPath= elementResource.getFullPath();
				}
				if (isEnclosing(cachedWorkingSet[i], elementPath))
					return true;
			}
		}
		return false;
	}
	
	private boolean isEnclosing(IAdaptable element, IPath path) {
		if (path == null)
			return false;
		
		IPath elementPath= null;
		
		IResource elementResource= (IResource)element.getAdapter(IResource.class);
		if (elementResource != null)
			elementPath= elementResource.getFullPath();

		if (elementPath == null) {
			ICElement cElement= (ICElement)element.getAdapter(ICElement.class);
			if (cElement != null)
				elementPath= cElement.getPath();
		}

		if (elementPath == null && element instanceof IStorage)
			elementPath= ((IStorage)element).getFullPath();
		
		if (elementPath == null)			
			return false;

		if (elementPath.isPrefixOf(path))
			return true;

		if (path.isPrefixOf(elementPath))
			return true;
		
		return false;
	}
	
}
