/**********************************************************************
 * Copyright (c) 2002,2003,2004 QNX Software Systems and others.
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
import org.eclipse.core.filebuffers.IDocumentSetupParticipant;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IDocumentExtension3;
import org.eclipse.jface.text.IDocumentPartitioner;
import org.eclipse.jface.text.rules.DefaultPartitioner;

/**
 * MakefileDocumentSetupParticipant
 * The document setup participant for Ant.
 */
public class MakefileDocumentSetupParticipant  implements IDocumentSetupParticipant {
	
	/**
	 * The name of the Makefiile partitioning.
	 */
	public final static String MAKEFILE_PARTITIONING= "___makefile_partitioning";  //$NON-NLS-1$
	
	public MakefileDocumentSetupParticipant() {
	}
	
	/*
	 * @see org.eclipse.core.filebuffers.IDocumentSetupParticipant#setup(org.eclipse.jface.text.IDocument)
	 */
	public void setup(IDocument document) {
		if (document instanceof IDocumentExtension3) {
			IDocumentExtension3 extension3= (IDocumentExtension3) document;
			IDocumentPartitioner partitioner = createDocumentPartitioner();
			extension3.setDocumentPartitioner(MAKEFILE_PARTITIONING, partitioner);
			partitioner.connect(document);
		} 
	}
	
	private IDocumentPartitioner createDocumentPartitioner() {
		return new DefaultPartitioner(
				new MakefilePartitionScanner(), MakefilePartitionScanner.TYPES);
	}
}
