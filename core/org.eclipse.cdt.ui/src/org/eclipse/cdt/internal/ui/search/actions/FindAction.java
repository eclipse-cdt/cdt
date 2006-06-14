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

import org.eclipse.cdt.core.model.ICElement;
import org.eclipse.cdt.core.model.ISourceReference;
import org.eclipse.cdt.core.model.ITranslationUnit;
import org.eclipse.cdt.internal.ui.editor.CEditor;
import org.eclipse.cdt.internal.ui.search.PDOMSearchElementQuery;
import org.eclipse.cdt.internal.ui.search.PDOMSearchTextSelectionQuery;
import org.eclipse.jface.text.ITextSelection;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.search.ui.ISearchQuery;
import org.eclipse.search.ui.NewSearchUI;
import org.eclipse.ui.IWorkbenchSite;


public abstract class FindAction extends SelectionParseAction {
	public FindAction(CEditor editor){
		super( editor );
	}
	
	public FindAction(IWorkbenchSite site){
		super( site );
	}
	
	public void run() {
		ISearchQuery searchJob = null;

		ISelection selection = getSelection();
	 	if (selection instanceof IStructuredSelection) {
	 		Object object = ((IStructuredSelection)selection).getFirstElement();
	 		if (object instanceof ISourceReference)
	 			searchJob = new PDOMSearchElementQuery(getScope(), (ISourceReference)object, getLimitTo());
		} else if (selection instanceof ITextSelection) {
			ITextSelection selNode = getSelection((ITextSelection)selection);
			ICElement element = fEditor.getInputCElement();
			while (element != null && !(element instanceof ITranslationUnit))
				element = element.getParent();
			if (element != null) {
				searchJob = new PDOMSearchTextSelectionQuery(
						getScope(),
						(ITranslationUnit)element,
						selNode,
						getLimitTo());
			}
		} 

	 	if (searchJob == null) {
	 		operationNotAvailable(CSEARCH_OPERATION_OPERATION_UNAVAILABLE_MESSAGE);
	 		return;
	 	}

        clearStatusLine();
		
		NewSearchUI.activateSearchResultView();
		
		NewSearchUI.runQueryInBackground(searchJob);
	}
	
    abstract protected String getScopeDescription(); 

	abstract protected ICElement[] getScope();
	
	abstract protected int getLimitTo();
    
}
