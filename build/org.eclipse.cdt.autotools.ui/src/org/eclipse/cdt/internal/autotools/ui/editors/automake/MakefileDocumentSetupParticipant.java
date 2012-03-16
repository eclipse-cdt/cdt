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

package org.eclipse.cdt.internal.autotools.ui.editors.automake;

import org.eclipse.core.filebuffers.IDocumentSetupParticipant;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IDocumentExtension3;
import org.eclipse.jface.text.IDocumentPartitioner;
import org.eclipse.jface.text.rules.FastPartitioner;

/**
 * MakefileDocumentSetupParticipant
 * The document setup participant for Ant.
 */
public class MakefileDocumentSetupParticipant  implements IDocumentSetupParticipant {
	
	/**
	 * The name of the Makefile partitioning.
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
		return new FastPartitioner(
				new MakefilePartitionScanner(), MakefilePartitionScanner.MAKE_PARTITIONS);
	}
}
