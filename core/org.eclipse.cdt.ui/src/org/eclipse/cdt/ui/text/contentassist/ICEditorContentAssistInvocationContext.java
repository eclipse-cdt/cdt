/*******************************************************************************
 * Copyright (c) 2007, 2015 IBM Corporation and others.
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
 *******************************************************************************/
package org.eclipse.cdt.ui.text.contentassist;

import org.eclipse.cdt.core.dom.ast.IASTCompletionNode;
import org.eclipse.cdt.core.model.ICProject;
import org.eclipse.cdt.core.model.ITranslationUnit;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.ITextViewer;
import org.eclipse.ui.IEditorPart;

/**
 * Describes the context of a content assist invocation in a C/C++ editor.
 *
 * @noextend This interface is not intended to be extended by clients.
 * @noimplement This interface is not intended to be implemented by clients.
 */
public interface ICEditorContentAssistInvocationContext {
	/**
	 * Returns the translation unit that content assist is invoked in, <code>null</code> if there
	 * is none.
	 *
	 * @return the translation unit that content assist is invoked in, possibly <code>null</code>
	 */
	ITranslationUnit getTranslationUnit();

	/**
	 * Returns the project of the translation unit that content assist is invoked in,
	 * <code>null</code> if none.
	 *
	 * @return the current C project, possibly <code>null</code>
	 */
	ICProject getProject();

	/**
	 * Returns the IASTCompletionNode of the location where content assist was invoked.
	 * @return the IASTCompletionNode of the location where context assist was invoked.
	 */
	IASTCompletionNode getCompletionNode();

	/**
	 * Returns the offset which was used to compute the IASTCompletionNode when content
	 * assist was invoked.
	 * @return the offset used to compute the IASTCompletionNode.
	 */
	int getParseOffset();

	/**
	 * Returns the offset where context information starts.
	 * @return the offset where context information (parameter hints) starts.
	 */
	int getContextInformationOffset();

	/**
	 * Get the editor content assist is invoked in.
	 *
	 * @return the editor, may be <code>null</code>
	 */
	IEditorPart getEditor();

	/**
	 * Returns the viewer, <code>null</code> if not available.
	 *
	 * @return the viewer, possibly <code>null</code>
	 */
	ITextViewer getViewer();

	/**
	 * Returns the invocation offset.
	 *
	 * @return the invocation offset
	 */
	int getInvocationOffset();

	/**
	 * Returns <code>true</code> if the current content assist invocation
	 * is for revealing context information, or <code>false</code> otherwise.
	 *
	 * @return <code>true</code> if the current content assist invocation
	 * is for revealing context information.
	 */
	boolean isContextInformationStyle();

	/**
	 * Returns the document that content assist is invoked on, or <code>null</code> if not known.
	 *
	 * @return the document or <code>null</code>
	 */
	IDocument getDocument();

	/**
	 * Computes the identifier (as specified by {@link Character#isJavaIdentifierPart(char)}) that
	 * immediately precedes the invocation offset.
	 *
	 * @return the prefix preceding the content assist invocation offset, <code>null</code> if
	 *         there is no document
	 * @throws BadLocationException if accessing the document fails
	 */
	CharSequence computeIdentifierPrefix() throws BadLocationException;
}