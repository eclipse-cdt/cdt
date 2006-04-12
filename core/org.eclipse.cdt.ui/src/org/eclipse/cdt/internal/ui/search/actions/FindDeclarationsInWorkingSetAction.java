/*******************************************************************************
 * Copyright (c) 2004, 2005 IBM Corporation and others.
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
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.ui.IWorkbenchSite;
import org.eclipse.ui.IWorkingSet;

public class FindDeclarationsInWorkingSetAction extends FindAction {

	private IWorkingSet[] fWorkingSet;
	private String scopeDescription = ""; //$NON-NLS-1$

	public FindDeclarationsInWorkingSetAction(IWorkbenchSite site, IWorkingSet[] wset) {
		this(site,
				CSearchMessages.getString("CSearch.FindDeclarationsInWorkingSetAction.label"), //$NON-NLS-1$
				CSearchMessages.getString("CSearch.FindDeclarationsInWorkingSetAction.tooltip")); //$NON-NLS-1$
		
		if (wset != null)
			fWorkingSet = wset;
	}

	public FindDeclarationsInWorkingSetAction(CEditor editor, IWorkingSet[] wset) {
		this(editor,
				CSearchMessages.getString("CSearch.FindDeclarationsInWorkingSetAction.label"), //$NON-NLS-1$
				CSearchMessages.getString("CSearch.FindDeclarationsInWorkingSetAction.tooltip")); //$NON-NLS-1$
		
		if (wset != null)
			fWorkingSet = wset;
	}

	public FindDeclarationsInWorkingSetAction(CEditor editor, String label, String tooltip){
		super(editor);
		setText(label); //$NON-NLS-1$
		setToolTipText(tooltip); //$NON-NLS-1$
	}

	public FindDeclarationsInWorkingSetAction(IWorkbenchSite site,String label, String tooltip){
		super(site);
		setText(label); //$NON-NLS-1$
		setToolTipText(tooltip); //$NON-NLS-1$
	}

	protected String getScopeDescription() {
		return scopeDescription;
	}

	protected ICElement[] getScope() {
		List resources = new ArrayList();
		
		for (int i = 0; i < fWorkingSet.length; ++i) {
			IAdaptable[] elements = fWorkingSet[i].getElements();
			for (int j = 0; j < elements.length; ++j) {
				ICElement resource = (ICElement)elements[j].getAdapter(ICElement.class);
				if (resource != null)
					resources.add(resource);
			}
		}
		
		scopeDescription = CSearchMessages.getFormattedString("WorkingSetScope", new String[] {CSearchUtil.toString(fWorkingSet)}); //$NON-NLS-1$
		return (ICElement[])resources.toArray(new ICElement[resources.size()]);
	}

	protected int getLimitTo() {
		return PDOMSearchQuery.FIND_DECLARATIONS_DEFINITIONS;
	}

}
