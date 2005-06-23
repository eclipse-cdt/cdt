/*******************************************************************************
 * Copyright (c) 2002, 2004 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * QNX Software Systems - Initial API and implementation
 *******************************************************************************/

package org.eclipse.cdt.internal.ui.editor;

import org.eclipse.cdt.internal.ui.text.CTextTools;
import org.eclipse.cdt.ui.CUIPlugin;
import org.eclipse.jface.text.IDocument;
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

	/*
	 * @see org.eclipse.ui.editors.text.StorageDocumentProvider#setupDocument(java.lang.Object, org.eclipse.jface.text.IDocument)
	 */
	protected void setupDocument(Object element, IDocument document) {
		if (document != null) {
			CTextTools tools= CUIPlugin.getDefault().getTextTools();
			tools.setupCDocument(document);
		}
	}

}
