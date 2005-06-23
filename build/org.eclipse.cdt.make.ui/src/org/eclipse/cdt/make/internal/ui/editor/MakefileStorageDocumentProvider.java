/*******************************************************************************
 * Copyright (c) 2002, 2005 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * QNX Software Systems - Initial API and implementation
 *******************************************************************************/

package org.eclipse.cdt.make.internal.ui.editor;

import org.eclipse.cdt.make.internal.ui.text.makefile.MakefilePartitionScanner;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IDocumentExtension3;
import org.eclipse.jface.text.IDocumentPartitioner;
import org.eclipse.jface.text.rules.FastPartitioner;
import org.eclipse.ui.editors.text.StorageDocumentProvider;

/**
 * MakefileStorageDocumentProvider
 */
public class MakefileStorageDocumentProvider extends StorageDocumentProvider {

	/*
	 * @see org.eclipse.ui.editors.text.StorageDocumentProvider#setupDocument(java.lang.Object,
	 *      org.eclipse.jface.text.IDocument)
	 */
	protected void setupDocument(Object element, IDocument document) {
		if (document != null) {
			IDocumentPartitioner partitioner= createDocumentPartitioner();
			if (document instanceof IDocumentExtension3) {
				IDocumentExtension3 extension3= (IDocumentExtension3) document;
				extension3.setDocumentPartitioner(MakefileDocumentSetupParticipant.MAKEFILE_PARTITIONING, partitioner);
			} else {
				document.setDocumentPartitioner(partitioner);
			}
			partitioner.connect(document);
		}
	}
	
	private IDocumentPartitioner createDocumentPartitioner() {
		return new FastPartitioner(
				new MakefilePartitionScanner(), MakefilePartitionScanner.MAKE_PARTITIONS);
	}

}
