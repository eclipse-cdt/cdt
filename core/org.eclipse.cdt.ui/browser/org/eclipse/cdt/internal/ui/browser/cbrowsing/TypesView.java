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

import org.eclipse.cdt.core.browser.ITypeInfo;
import org.eclipse.cdt.core.model.ICElement;
import org.eclipse.cdt.internal.ui.ICHelpContextIds;
import org.eclipse.cdt.internal.ui.util.ProblemTableViewer;
import org.eclipse.cdt.ui.CUIPlugin;
import org.eclipse.cdt.ui.PreferenceConstants;
import org.eclipse.cdt.ui.browser.typeinfo.TypeInfoLabelProvider;
import org.eclipse.jface.viewers.IContentProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.StructuredViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.IActionBars;
import org.eclipse.ui.IPageLayout;
import org.eclipse.ui.part.IShowInTargetList;

public class TypesView extends CBrowsingPart {

//	private SelectAllAction fSelectAllAction;

	/**
	 * Creates and returns the label provider for this part.
	 * 
	 * @return the label provider
	 * @see org.eclipse.jface.viewers.ILabelProvider
	 */
	protected LabelProvider createLabelProvider() {
		return new TypeInfoLabelProvider(TypeInfoLabelProvider.SHOW_TYPE_ONLY);
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
	 * Creates the viewer of this part dependent on the current
	 * layout.
	 * 
	 * @param parent the parent for the viewer
	 */
	protected StructuredViewer createViewer(Composite parent) {
		StructuredViewer viewer;
//		if(isInListState())
			viewer= createTableViewer(parent);
//		else
//			viewer= createTreeViewer(parent);
	
//		fWrappedViewer.setViewer(viewer);
//		return fWrappedViewer;
		return viewer;
	}
	private ProblemTableViewer createTableViewer(Composite parent) {
		return new ProblemTableViewer(parent, SWT.MULTI);
	}
	
	/**
	 * Creates the the content provider of this part.
	 */
	protected IContentProvider createContentProvider() {
		return new TypesViewContentProvider(this);
	}
	
	

	/**
	 * Adds filters the viewer of this part.
	 */
	protected void addFilters() {
		super.addFilters();
//		getViewer().addFilter(new NonCElementFilter());
	}

	/**
	 * Answers if the given <code>element</code> is a valid
	 * input for this part.
	 * 
	 * @param 	element	the object to test
	 * @return	<true> if the given element is a valid input
	 */
	protected boolean isValidInput(Object element) {
		return isValidNamespace(element);
	}

	/**
	 * Answers if the given <code>element</code> is a valid
	 * element for this part.
	 * 
	 * @param 	element	the object to test
	 * @return	<true> if the given element is a valid element
	 */
	protected boolean isValidElement(Object element) {
		if (element instanceof ITypeInfo) {
			ITypeInfo info = (ITypeInfo)element;
			if (info.exists()) {
				switch (info.getCElementType()) {
					case ICElement.C_CLASS:
					case ICElement.C_STRUCT:
						return true;
				}
			}
		}
		return false;
	}

	/**
	 * Returns the context ID for the Help system
	 * 
	 * @return	the string used as ID for the Help context
	 */
	protected String getHelpContextId() {
		return ICHelpContextIds.TYPES_VIEW;
	}
	
	protected String getLinkToEditorKey() {
		return PreferenceConstants.LINK_BROWSING_TYPES_TO_EDITOR;
	}

	protected void createActions() {
		super.createActions();
//		fSelectAllAction= new SelectAllAction((TableViewer)getViewer());
	}

	protected void fillActionBars(IActionBars actionBars) {
		super.fillActionBars(actionBars);
		
		// Add selectAll action handlers.
//		actionBars.setGlobalActionHandler(IWorkbenchActionConstants.SELECT_ALL, fSelectAllAction);
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.cdt.internal.ui.browser.cbrowsing.CBrowsingPart#findInputForElement(java.lang.Object)
	 */
	protected Object findInputForElement(Object element) {
		return getTypesInput(element);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.internal.ui.browser.cbrowsing.CBrowsingPart#findElementToSelect(java.lang.Object)
	 */
	protected Object findElementToSelect(Object element) {
		return getMembersInput(element);
	}
}
