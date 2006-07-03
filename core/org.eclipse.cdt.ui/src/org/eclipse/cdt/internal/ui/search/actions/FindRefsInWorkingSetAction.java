/*******************************************************************************
 * Copyright (c) 2004, 2006 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corp. - Rational Software - initial implementation
 *******************************************************************************/
package org.eclipse.cdt.internal.ui.search.actions;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.cdt.core.model.ICElement;
import org.eclipse.cdt.internal.ui.editor.CEditor;
import org.eclipse.cdt.internal.ui.search.CSearchMessages;
import org.eclipse.cdt.internal.ui.search.CSearchUtil;
import org.eclipse.cdt.internal.ui.search.PDOMSearchQuery;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.ui.IWorkbenchSite;
import org.eclipse.ui.IWorkingSet;

public class FindRefsInWorkingSetAction extends FindAction {
	
	private IWorkingSet[] fWorkingSet;
	private String scopeDescription = ""; //$NON-NLS-1$
	
	public FindRefsInWorkingSetAction(CEditor editor, IWorkingSet[] workingSets) {
		this(editor,
		CSearchMessages.getString("CSearch.FindReferencesInWorkingSetAction.label"), //$NON-NLS-1$
		CSearchMessages.getString("CSearch.FindReferencesInWorkingSetAction.tooltip")); //$NON-NLS-1$
		
		if (workingSets != null)
			fWorkingSet = workingSets;
	}
	
	public FindRefsInWorkingSetAction(IWorkbenchSite site, IWorkingSet[] workingSets){
		this(site,
		CSearchMessages.getString("CSearch.FindReferencesInWorkingSetAction.label"), //$NON-NLS-1$
		CSearchMessages.getString("CSearch.FindReferencesInWorkingSetAction.tooltip")); //$NON-NLS-1$
		
		if (workingSets != null)
			fWorkingSet= workingSets;
	}

	public FindRefsInWorkingSetAction(CEditor editor, String label, String tooltip) {
		super(editor);
		setText(label); 
		setToolTipText(tooltip); 
	}

	public FindRefsInWorkingSetAction(IWorkbenchSite site, String label, String tooltip) {
		super(site);
		setText(label); 
		setToolTipText(tooltip); //$NON-NLS-1
	}

	protected int getLimitTo() {
		return PDOMSearchQuery.FIND_REFERENCES;
	}

	protected String getScopeDescription() {
		return scopeDescription;
	}

	protected ICElement[] getScope() {
		List scope = new ArrayList();
		
		for (int i = 0; i < fWorkingSet.length; ++i) {
			IAdaptable[] elements = fWorkingSet[i].getElements();
			for (int j = 0; j < elements.length; ++j) {
				ICElement element = (ICElement)elements[j].getAdapter(IResource.class);
				if (element != null)
					scope.add(element);
			}
		}
		
		scopeDescription = CSearchMessages.getFormattedString("WorkingSetScope", new String[] {CSearchUtil.toString(fWorkingSet)}); //$NON-NLS-1$
		return (ICElement[])scope.toArray(new ICElement[scope.size()]);
	}

}
