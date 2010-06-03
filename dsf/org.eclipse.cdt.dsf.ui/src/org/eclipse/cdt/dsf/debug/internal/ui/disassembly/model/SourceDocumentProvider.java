/*******************************************************************************
 * Copyright (c) 2007, 2009 Wind River Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Wind River Systems - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.dsf.debug.internal.ui.disassembly.model;

import java.util.Iterator;

import org.eclipse.cdt.dsf.debug.internal.ui.disassembly.text.REDDocument;
import org.eclipse.cdt.internal.ui.editor.CDocumentSetupParticipant;
import org.eclipse.core.filesystem.EFS;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFileState;
import org.eclipse.core.resources.IStorage;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.debug.core.sourcelookup.containers.LocalFileStorage;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.source.IAnnotationModel;
import org.eclipse.ui.IStorageEditorInput;
import org.eclipse.ui.IURIEditorInput;
import org.eclipse.ui.editors.text.StorageDocumentProvider;

/**
 * SourceDocumentProvider
 */
@SuppressWarnings("restriction")
public class SourceDocumentProvider extends StorageDocumentProvider {

	public SourceDocumentProvider() {
		super();
	}

	/**
	 * Dispose all connected documents.
	 */
	public void dispose() {
		Iterator<?> it = getConnectedElements();
		while(it.hasNext()) {
			Object element = it.next();
			ElementInfo info = getElementInfo(element);
			// force refcount to 1
			info.fCount = 1;
			disconnect(element);
		}
	}

	/*
	 * @see org.eclipse.ui.texteditor.AbstractDocumentProvider#createDocument(java.lang.Object)
	 */
	@Override
	protected IDocument createEmptyDocument() {
		IDocument doc = new REDDocument();
		return doc;
	}

	/*
	 * @see org.eclipse.ui.texteditor.AbstractDocumentProvider#createAnnotationModel(java.lang.Object)
	 */
	@Override
	protected IAnnotationModel createAnnotationModel(Object element) throws CoreException {
		return null;
	}

	/*
	 * @see org.eclipse.ui.editors.text.StorageDocumentProvider#setupDocument(java.lang.Object, org.eclipse.jface.text.IDocument)
	 */
	@Override
	protected void setupDocument(Object element, IDocument document) {
		super.setupDocument(element, document);
		if (element instanceof IStorageEditorInput) {
			new CDocumentSetupParticipant().setup(document);
		}
	}

	/*
	 * @see org.eclipse.ui.texteditor.AbstractDocumentProvider#disposeElementInfo(java.lang.Object, org.eclipse.ui.texteditor.AbstractDocumentProvider.ElementInfo)
	 */
	@Override
	protected void disposeElementInfo(Object element, ElementInfo info) {
		super.disposeElementInfo(element, info);
		IDocument doc = info.fDocument;
		if (doc instanceof REDDocument) {
			((REDDocument)doc).dispose();
		}
	}

	/*
	 * @see org.eclipse.ui.texteditor.AbstractDocumentProvider#getModificationStamp(java.lang.Object)
	 */
	@Override
	public long getModificationStamp(Object element) {
		try {
			if (element instanceof IStorageEditorInput) {
				IStorage file= ((IStorageEditorInput)element).getStorage();
				if (file instanceof IFile) {
					return ((IFile)file).getLocalTimeStamp();
				} else if (file instanceof IFileState) {
					return ((IFileState)file).getModificationTime();
				} else if (file instanceof LocalFileStorage) {
					return ((LocalFileStorage)file).getFile().lastModified();
				}
			} else if (element instanceof IURIEditorInput) {
				return EFS.getStore(((IURIEditorInput)element).getURI()).fetchInfo().getLastModified();
			}
		} catch (CoreException e) {
			// ignore
		}
		return 0;
	}

}
