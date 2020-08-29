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
package org.eclipse.cdt.lsp.internal.ui;

import org.eclipse.core.runtime.IAdapterFactory;
import org.eclipse.jface.text.IDocument;
import org.eclipse.ui.texteditor.AbstractDecoratedTextEditor;

public final class EditorPartAdapterFactory implements IAdapterFactory {

	private final Class<?>[] classes = new Class<?>[] { IDocument.class };

	@Override
	public <T> T getAdapter(Object adaptableObject, Class<T> adapterType) {
		if (IDocument.class.equals(adapterType)) {
			if (adaptableObject instanceof AbstractDecoratedTextEditor) {
				AbstractDecoratedTextEditor editor = (AbstractDecoratedTextEditor) adaptableObject;
				IDocument document = editor.getDocumentProvider().getDocument(editor.getEditorInput());
				return adapterType.cast(document);
			}
		}
		return null;
	}

	@Override
	public Class<?>[] getAdapterList() {
		return classes;
	}

}
