/*******************************************************************************
 * Copyright (c) 2000, 2015 IBM Corporation and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     Sergey Prigogin (Google)
 *******************************************************************************/
package org.eclipse.cdt.internal.ui.text.correction.proposals;

import org.eclipse.cdt.core.dom.rewrite.ASTRewrite;
import org.eclipse.cdt.core.model.ITranslationUnit;
import org.eclipse.cdt.internal.ui.CUIStatus;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.jface.text.IDocument;
import org.eclipse.ltk.core.refactoring.Change;
import org.eclipse.ltk.core.refactoring.CompositeChange;
import org.eclipse.ltk.core.refactoring.TextChange;
import org.eclipse.swt.graphics.Image;
import org.eclipse.text.edits.TextEdit;

/**
 * A proposal for quick fixes and quick assists that works on a AST rewriter.
 * Either a rewriter is directly passed in the constructor or method {@link #getRewrite()}
 * is overridden to provide the AST rewriter that is evaluated to the document when the
 * proposal is applied.
 *
 * @since 5.1
 */
public class ASTRewriteCorrectionProposal extends TUCorrectionProposal {
	private ASTRewrite fRewrite;

	/**
	 * Constructs a AST rewrite correction proposal.
	 *
	 * @param name the display name of the proposal.
	 * @param tu the translation unit that is modified.
	 * @param rewrite the AST rewrite that is invoked when the proposal is applied or
	 *     {@code null} if {@link #getRewrite()} is overridden.
	 * @param relevance The relevance of this proposal.
	 * @param image The image that is displayed for this proposal or {@code null} if no
	 * image is desired.
	 */
	public ASTRewriteCorrectionProposal(String name, ITranslationUnit tu, ASTRewrite rewrite, int relevance,
			Image image) {
		super(name, tu, relevance, image);
		fRewrite = rewrite;
	}

	@Override
	protected void addEdits(IDocument document, TextEdit editRoot) throws CoreException {
		super.addEdits(document, editRoot);
		ASTRewrite rewrite = getRewrite();
		if (rewrite != null) {
			try {
				Change change = rewrite.rewriteAST();
				addTextEdits(change, editRoot);
			} catch (IllegalArgumentException e) {
				throw new CoreException(CUIStatus.createError(IStatus.ERROR, e));
			}
		}
	}

	/**
	 * Adds all text edits from {@code change} to {@code editRoot}.
	 * @param change
	 * @param editRoot
	 */
	private void addTextEdits(Change change, TextEdit editRoot) {
		if (change instanceof TextChange) {
			editRoot.addChild(((TextChange) change).getEdit());
		} else if (change instanceof CompositeChange) {
			for (Change c : ((CompositeChange) change).getChildren()) {
				addTextEdits(c, editRoot);
			}
		}
	}

	/**
	 * Returns the rewriter that has been passed in the constructor. Implementors can override this
	 * method to create the rewriter lazy. This method will only be called once.
	 *
	 * @return returns the rewriter to be used.
	 * @throws CoreException an exception is thrown when the rewriter could not be created.
	 */
	protected ASTRewrite getRewrite() throws CoreException {
		if (fRewrite == null) {
			IStatus status = CUIStatus.createError(IStatus.ERROR, "Rewriter not initialized", null); //$NON-NLS-1$
			throw new CoreException(status);
		}
		return fRewrite;
	}
}
