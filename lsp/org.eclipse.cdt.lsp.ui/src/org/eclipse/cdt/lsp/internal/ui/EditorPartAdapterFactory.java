package org.eclipse.cdt.lsp.internal.ui;

import org.eclipse.core.runtime.IAdapterFactory;
import org.eclipse.jface.text.IDocument;
import org.eclipse.ui.texteditor.AbstractDecoratedTextEditor;

public class EditorPartAdapterFactory implements IAdapterFactory {

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
