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

import org.eclipse.cdt.make.internal.ui.text.makefile.MakefilePartitionScanner;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IDocumentPartitioner;
import org.eclipse.jface.text.rules.DefaultPartitioner;
import org.eclipse.ui.editors.text.FileDocumentProvider;

/**
 */
public class MakefileDocumentProvider extends FileDocumentProvider {

	private static MakefilePartitionScanner scanner = null;

	/**
	 * Constructor for MakefileDocumentProvider.
	 */
	public MakefileDocumentProvider() {
		super();
	}

	/**
	 * @see org.eclipse.ui.texteditor.AbstractDocumentProvider#createDocument(Object)
	 */
	protected IDocument createDocument(Object element) throws CoreException {
		IDocument document = super.createDocument(element);
		if (document != null) {
			IDocumentPartitioner partitioner = createPartitioner();
			partitioner.connect(document);
			document.setDocumentPartitioner(partitioner);
		}
		return document;
	}

	private IDocumentPartitioner createPartitioner() {
		return new DefaultPartitioner(getPartitionScanner(), MakefilePartitionScanner.TYPES);
	}

	private MakefilePartitionScanner getPartitionScanner() {
		if (scanner == null)
			scanner = new MakefilePartitionScanner();
		return scanner;
	}

}
