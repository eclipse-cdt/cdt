/**********************************************************************
 * Copyright (c) 2002,2003 QNX Software Systems and others.
 * All rights reserved.   This program and the accompanying materials
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors: 
 * QNX Software Systems - Initial API and implementation
***********************************************************************/
package org.eclipse.cdt.make.internal.ui.editor;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IDocumentPartitioner;
import org.eclipse.jface.text.rules.DefaultPartitioner;
import org.eclipse.ui.editors.text.FileDocumentProvider;

/**
 */
public class MakeDocumentProvider extends FileDocumentProvider {

	private static MakePartitionScanner scanner = null;

	/**
	 * Constructor for MakeDocumentProvider.
	 */
	public MakeDocumentProvider() {
		super();
	}

	/**
	 * @see org.eclipse.ui.texteditor.AbstractDocumentProvider#createDocument(Object)
	 */
	protected IDocument createDocument(Object element) throws CoreException {
		IDocument document = super.createDocument(element);
		if (document != null) {
			IDocumentPartitioner partitioner = createPartitioner();
			document.setDocumentPartitioner(partitioner);
			partitioner.connect(document);
		}
		return document;
	}

	private IDocumentPartitioner createPartitioner() {
		return new DefaultPartitioner(getPartitionScanner(), MakePartitionScanner.TYPES);
	}

	private MakePartitionScanner getPartitionScanner() {
		if (scanner == null)
			scanner = new MakePartitionScanner();
		return scanner;
	}

}
