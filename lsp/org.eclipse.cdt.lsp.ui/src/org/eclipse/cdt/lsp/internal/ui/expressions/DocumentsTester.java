/*******************************************************************************
 * Copyright (c) 2020 ArSysOp and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Alexander Fedorov (ArSysOp) - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.lsp.internal.ui.expressions;

import org.eclipse.cdt.lsp.internal.core.workspace.PreferLanguageServer;
import org.eclipse.core.expressions.PropertyTester;
import org.eclipse.jface.text.IDocument;
import org.eclipse.ui.texteditor.AbstractDecoratedTextEditor;

public final class DocumentsTester extends PropertyTester {

	private final String key = "prefer"; //$NON-NLS-1$
	private final PreferLanguageServer predicate;

	public DocumentsTester() {
		this.predicate = new PreferLanguageServer();
	}

	@Override
	public boolean test(Object receiver, String property, Object[] args, Object expectedValue) {
		if (key.equals(property)) {
			if (receiver instanceof AbstractDecoratedTextEditor) {
				AbstractDecoratedTextEditor editor = (AbstractDecoratedTextEditor) receiver;
				IDocument document = editor.getDocumentProvider().getDocument(editor.getEditorInput());
				return predicate.test(document);
			}
		}
		return false;
	}

}
