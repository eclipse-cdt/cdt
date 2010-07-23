/*******************************************************************************
 * Copyright (c) 2010 Alena Laskavaia
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Alena Laskavaia - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.codan.internal.checkers.ui.quickfix;

import org.eclipse.cdt.codan.internal.checkers.ui.CheckersUiActivator;
import org.eclipse.cdt.codan.ui.AbstractCodanCMarkerResolution;
import org.eclipse.cdt.core.dom.ast.IASTName;
import org.eclipse.cdt.core.dom.ast.IASTTranslationUnit;
import org.eclipse.cdt.core.index.IIndex;
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.FindReplaceDocumentAdapter;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IRegion;

public abstract class AbstractAstRewriteQuickFix extends
		AbstractCodanCMarkerResolution {
	private IDocument document;
	@Override
	public void apply(final IMarker marker, IDocument document) {
		try {
			this.document = document;
			openEditor(marker).doSave(new NullProgressMonitor());
			IIndex index;
			try {
				index = getIndexFromMarker(marker);
			} catch (CoreException e) {
				e.printStackTrace();
				CheckersUiActivator.log(e);
				return;
			}
			// lock the index for read access
			try {
				index.acquireReadLock();
			} catch (InterruptedException e) {
				return;
			}
			try {
				modifyAST(index, marker);
			} finally {
				index.releaseReadLock();
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * 
	 * @param ast
	 * @param astName
	 * @param r
	 */
	public abstract void modifyAST(IIndex index, IMarker marker);

	/**
	 * @return the document
	 */
	public IDocument getDocument() {
		return document;
	}
	
	/**
	 * @param marker
	 * @param ast
	 * @param argumentIndex TODO
	 * @return
	 * @throws BadLocationException
	 */
	public IASTName getAstNameFromProblemArgument(IMarker marker,
			IASTTranslationUnit ast, int argumentIndex) {
		IASTName astName = null;
		int pos = getOffset(marker, getDocument());
		String name = null;
		try {
			name = getProblemArgument(marker, argumentIndex);
		} catch (Exception e) {
			return null;
		}
		if (name == null)
			return null;
		FindReplaceDocumentAdapter dad = new FindReplaceDocumentAdapter(
				getDocument());
		IRegion region;
		try {
			region = dad.find(pos, name,
			/* forwardSearch */true, /* caseSensitive */true,
			/* wholeWord */true, /* regExSearch */false);
		} catch (BadLocationException e) {
			return null;
		}
		astName = getASTNameFromPositions(ast, region.getOffset(),
				region.getLength());
		return astName;
	}
}
