/*******************************************************************************
 * Copyright (c) 2000, 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.internal.ui.browser.cbrowsing;

import org.eclipse.cdt.core.browser.AllTypesCache;
import org.eclipse.cdt.core.browser.ITypeInfo;
import org.eclipse.cdt.core.model.CoreModel;
import org.eclipse.cdt.core.model.ICContainer;
import org.eclipse.cdt.core.model.ICElement;
import org.eclipse.cdt.core.model.ICModel;
import org.eclipse.cdt.core.model.ICProject;
import org.eclipse.cdt.core.model.IEnumeration;
import org.eclipse.cdt.core.model.ISourceRoot;
import org.eclipse.cdt.core.model.IStructure;
import org.eclipse.cdt.core.model.ITranslationUnit;
import org.eclipse.cdt.core.model.ITypeDef;
import org.eclipse.cdt.internal.ui.ICHelpContextIds;
import org.eclipse.cdt.ui.CUIPlugin;
import org.eclipse.cdt.ui.PreferenceConstants;
import org.eclipse.jface.viewers.DoubleClickEvent;
import org.eclipse.jface.viewers.IContentProvider;
import org.eclipse.jface.viewers.IDoubleClickListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.StructuredViewer;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.ViewerSorter;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.IPageLayout;
import org.eclipse.ui.part.IShowInTargetList;

public class ProjectsView extends CBrowsingPart {

//	private FilterUpdater fFilterUpdater;

	/**
	 * Creates the the viewer of this part.
	 * 
	 * @param parent	the parent for the viewer
	 */
	protected StructuredViewer createViewer(Composite parent) {
	    ElementTreeViewer result= new ElementTreeViewer(parent, SWT.MULTI);
//		fFilterUpdater= new FilterUpdater(result);
//		ResourcesPlugin.getWorkspace().addResourceChangeListener(fFilterUpdater);
		return result;
	}

	protected LabelProvider createLabelProvider() {
	    return new CBrowsingLabelProvider();
	}
	
	protected ViewerSorter createViewerSorter() {
	    return new CBrowsingViewerSorter();
	}
	
	/**
	 * Adds filters the viewer of this part.
	 */
	protected void addFilters() {
		super.addFilters();
		getViewer().addFilter(new CBrowsingElementFilter());
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.jdt.internal.ui.browsing.JavaBrowsingPart#dispose()
	 */
	public void dispose() {
//		if (fFilterUpdater != null)
//			ResourcesPlugin.getWorkspace().removeResourceChangeListener(fFilterUpdater);
		super.dispose();
	}
	
	/**
	 * Answer the property defined by key.
	 */
	public Object getAdapter(Class key) {
		if (key == IShowInTargetList.class) {
			return new IShowInTargetList() {
				public String[] getShowInTargetIds() {
					return new String[] { CUIPlugin.CVIEW_ID, IPageLayout.ID_RES_NAV  };
				}

			};
		}
		return super.getAdapter(key);
	}

	
	/**
	 * Creates the the content provider of this part.
	 */
	protected IContentProvider createContentProvider() {
		return new ProjectsViewContentProvider(this);
	}

	/**
	 * Returns the context ID for the Help system.
	 * 
	 * @return	the string used as ID for the Help context
	 */
	protected String getHelpContextId() {
		return ICHelpContextIds.PROJECTS_VIEW;
	}
	
	protected String getLinkToEditorKey() {
		return PreferenceConstants.LINK_BROWSING_PROJECTS_TO_EDITOR;
	}


	/**
	 * Adds additional listeners to this view.
	 */
	protected void hookViewerListeners() {
		super.hookViewerListeners();
		getViewer().addDoubleClickListener(new IDoubleClickListener() {
			public void doubleClick(DoubleClickEvent event) {
				TreeViewer viewer= (TreeViewer)getViewer();
				Object element= ((IStructuredSelection)event.getSelection()).getFirstElement();
				if (viewer.isExpandable(element))
					viewer.setExpandedState(element, !viewer.getExpandedState(element));
			}
		});
	}

	protected void setInitialInput() {
		ICElement root= CoreModel.create(CUIPlugin.getWorkspace().getRoot());
		getViewer().setInput(root);
		updateTitle();
	}

	/**
	 * Answers if the given <code>element</code> is a valid
	 * input for this part.
	 * 
	 * @param 	element	the object to test
	 * @return	<true> if the given element is a valid input
	 */
	protected boolean isValidInput(Object element) {
		return element instanceof ICModel;
	}

	/**
	 * Answers if the given <code>element</code> is a valid
	 * element for this part.
	 * 
	 * @param 	element	the object to test
	 * @return	<true> if the given element is a valid element
	 */
	protected boolean isValidElement(Object element) {
		return element instanceof ICProject || element instanceof ICContainer;
	}

	/*
	 * @see JavaBrowsingPart#setInput(Object)
	 */
	protected void setInput(Object input) {
		// Don't allow to clear input for this view
		if (input != null)
			super.setInput(input);
		else
			getViewer().setSelection(null);
	}
	
	protected void createActions() {		
		super.createActions();
//		fActionGroups.addGroup(new ProjectActionGroup(this));
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.internal.ui.browser.cbrowsing.CBrowsingPart#findInputForElement(java.lang.Object)
	 */
	protected Object findInputForElement(Object element) {
		if (element instanceof ICModel) {
			if (exists(element))
				return element;
		}
		
		if (element instanceof ICElement) { //ICProject || element instanceof ISourceRoot) {
			ICModel model = ((ICElement)element).getCModel();
			if (exists(model))
				return model;
		}
		
		if (element instanceof ITypeInfo) {
			ICProject cProject = findCProject((ITypeInfo)element);
			if (cProject != null) {
				ICModel model = cProject.getCModel();
				if (exists(model))
					return model;
			}
		}
		
		return null;
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.cdt.internal.ui.browser.cbrowsing.CBrowsingPart#findElementToSelect(java.lang.Object)
	 */
	protected Object findElementToSelect(Object element) {
		if (element instanceof ICModel) {
			return null;
		}

	    if (element instanceof ICProject || element instanceof ISourceRoot) {
			if (exists(element))
				return element;
			return null;
		}

	    if (element instanceof ITranslationUnit) {
			ICElement e = (ICElement)element;
			ISourceRoot root = findSourceRoot(e);
			if (exists(root) && !isProjectSourceRoot(root))
				return root;
			ICProject cProject = findCProject(e);
			if (exists(cProject))
				return cProject;
			return null;
	    }

	    if (element instanceof ITypeInfo) {
			ITypeInfo info = (ITypeInfo)element;
			ISourceRoot root = findSourceRoot(info);
			if (exists(root) && !isProjectSourceRoot(root))
				return root;
			ICProject cProject = findCProject(info);
			if (exists(cProject))
				return cProject;
			return null;
		}
		
		if (element instanceof ICElement) {
			ICElement parent = (ICElement)element;
			while (parent != null) {
				if ((parent instanceof IStructure
				        || parent instanceof IEnumeration
				        || parent instanceof ITypeDef)
				        && parent.exists()) {
				    ITypeInfo info = AllTypesCache.getTypeForElement(parent, true, true, null);
				    if (info != null) {
						ISourceRoot root = findSourceRoot(info);
						if (exists(root) && !isProjectSourceRoot(root))
							return root;
						ICProject cProject = findCProject(info);
						if (exists(cProject))
							return cProject;
						return null;
				    }
				}
				parent = parent.getParent();
			}
			return null;
		}

		return null;
	}
}
