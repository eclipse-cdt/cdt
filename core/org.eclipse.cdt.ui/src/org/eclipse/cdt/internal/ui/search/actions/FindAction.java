/*******************************************************************************
 * Copyright (c) 2004, 2012 IBM Corporation and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     IBM Corp. - Rational Software - initial implementation
 *     Sergey Prigogin (Google)
 *******************************************************************************/
package org.eclipse.cdt.internal.ui.search.actions;

import org.eclipse.cdt.core.model.ICElement;
import org.eclipse.cdt.core.model.ISourceReference;
import org.eclipse.cdt.core.model.ITranslationUnit;
import org.eclipse.cdt.internal.ui.editor.CEditor;
import org.eclipse.cdt.internal.ui.search.CSearchElementQuery;
import org.eclipse.cdt.internal.ui.search.CSearchMessages;
import org.eclipse.cdt.internal.ui.search.CSearchQuery;
import org.eclipse.cdt.internal.ui.search.CSearchTextSelectionQuery;
import org.eclipse.cdt.internal.ui.text.CWordFinder;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.ITextSelection;
import org.eclipse.jface.text.TextSelection;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.search.ui.ISearchQuery;
import org.eclipse.search.ui.NewSearchUI;
import org.eclipse.ui.IWorkbenchSite;

public abstract class FindAction extends SelectionParseAction {
	public FindAction(CEditor editor) {
		super(editor);
	}

	public FindAction(IWorkbenchSite site) {
		super(site);
	}

	@Override
	public void run() {
		ISearchQuery searchJob = null;

		ISelection selection = getSelection();
		if (selection instanceof IStructuredSelection) {
			Object object = ((IStructuredSelection) selection).getFirstElement();
			if (object instanceof ISourceReference)
				searchJob = createQuery((ISourceReference) object);
		} else if (selection instanceof ITextSelection) {
			ITextSelection selNode = (ITextSelection) selection;
			ICElement element = fEditor.getTranslationUnit();
			while (element != null && !(element instanceof ITranslationUnit))
				element = element.getParent();
			if (element != null) {
				if (selNode.getLength() == 0) {
					IDocument document = fEditor.getDocumentProvider().getDocument(fEditor.getEditorInput());
					IRegion reg = CWordFinder.findWord(document, selNode.getOffset());
					selNode = new TextSelection(document, reg.getOffset(), reg.getLength());
				}
				searchJob = createQuery(element, selNode);
			}
		}

		if (searchJob == null) {
			showStatusLineMessage(CSearchMessages.CSearchOperation_operationUnavailable_message);
			return;
		}

		clearStatusLine();
		NewSearchUI.activateSearchResultView();
		NewSearchUI.runQueryInBackground(searchJob);
	}

	protected CSearchQuery createQuery(ISourceReference object) {
		return new CSearchElementQuery(getScope(), object, getLimitTo());
	}

	protected CSearchQuery createQuery(ICElement element, ITextSelection selNode) {
		return new CSearchTextSelectionQuery(getScope(), (ITranslationUnit) element, selNode, getLimitTo());
	}

	abstract protected String getScopeDescription();

	abstract protected ICElement[] getScope();

	abstract protected int getLimitTo();
}
