/*******************************************************************************
 * Copyright (c) 2006, 2007 Red Hat, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Red Hat Incorporated - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.autotools.ui.editors;

import java.util.Iterator;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.ui.editors.text.TextFileDocumentProvider;

public class AutoconfDocumentProvider extends TextFileDocumentProvider {
	
	public void shutdown() {
		@SuppressWarnings("rawtypes")
		Iterator e= getConnectedElementsIterator();
		while (e.hasNext())
			disconnect(e.next());
	}
	
	public void connect(Object element) throws CoreException {
		super.connect(element);
		// Remove all error markers for file as we will parse
		// from scratch.
//		AutoconfErrorHandler h = new AutoconfErrorHandler(getDocument(element));
//		h.removeAllExistingMarkers();
	}
	
	public IDocument getDocument(Object element) {
		FileInfo info= (FileInfo) getFileInfo(element);
		if (info != null)
			return info.fTextFileBuffer.getDocument();
		return getParentProvider().getDocument(element);
	}
}
