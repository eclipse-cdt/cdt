/*******************************************************************************
 * Copyright (c) 2010, 2011 Alena Laskavaia
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *    Alena Laskavaia - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.codan.ui;

import org.eclipse.cdt.codan.internal.ui.cxx.Activator;
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

/**
 *
 * Abstract class to simply ast rewrite quick fixers
 * @since 2.0
 */
public abstract class AbstractAstRewriteQuickFix extends AbstractCodanCMarkerResolution {
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
				Activator.log(e);
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
			Activator.log(e);
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
	public IASTName getAstNameFromProblemArgument(IMarker marker, IASTTranslationUnit ast, int argumentIndex) {
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
		FindReplaceDocumentAdapter dad = new FindReplaceDocumentAdapter(getDocument());
		IRegion region;
		try {
			region = dad.find(pos, name, /* forwardSearch */true, /* caseSensitive */true, /* wholeWord */true,
					/* regExSearch */false);
		} catch (BadLocationException e) {
			return null;
		}
		astName = getASTNameFromPositions(ast, region.getOffset(), region.getLength());
		return astName;
	}
}
