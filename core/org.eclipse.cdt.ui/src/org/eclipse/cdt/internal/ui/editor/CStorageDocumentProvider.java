/*******************************************************************************
 * Copyright (c) 2002, 2010 QNX Software Systems and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 * QNX Software Systems - Initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.internal.ui.editor;

import org.eclipse.cdt.internal.ui.text.CTextTools;
import org.eclipse.cdt.ui.CUIPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.source.AnnotationModel;
import org.eclipse.jface.text.source.IAnnotationModel;
import org.eclipse.ui.editors.text.StorageDocumentProvider;

/**
 * CStorageDocumentProvider
 */
public class CStorageDocumentProvider extends StorageDocumentProvider {

	/**
	 *
	 */
	public CStorageDocumentProvider() {
		super();
	}

	@Override
	protected IAnnotationModel createAnnotationModel(Object element) throws CoreException {
		IAnnotationModel m = super.createAnnotationModel(element);
		if (m == null)
			m = new AnnotationModel();
		return m;
	}

	/*
	 * @see org.eclipse.ui.editors.text.StorageDocumentProvider#setupDocument(java.lang.Object, org.eclipse.jface.text.IDocument)
	 */
	@Override
	protected void setupDocument(Object element, IDocument document) {
		if (document != null) {
			CTextTools tools = CUIPlugin.getDefault().getTextTools();
			tools.setupCDocument(document);
		}
	}

}
