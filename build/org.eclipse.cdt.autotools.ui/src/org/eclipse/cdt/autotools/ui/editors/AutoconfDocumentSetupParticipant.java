/*******************************************************************************
 * Copyright (c) 2007 Red Hat, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Red Hat Incorporated - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.autotools.ui.editors;

import org.eclipse.core.filebuffers.IDocumentSetupParticipant;
import org.eclipse.jface.text.DocumentEvent;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IDocumentExtension3;
import org.eclipse.jface.text.IDocumentListener;

public class AutoconfDocumentSetupParticipant implements
		IDocumentSetupParticipant, IDocumentListener {

	public void setup(IDocument document) {
		AutoconfPartitioner partitioner =
			new AutoconfPartitioner(
				new AutoconfPartitionScanner(),
				AutoconfPartitionScanner.AUTOCONF_PARTITION_TYPES);
		partitioner.connect(document, 1);
		if (document instanceof IDocumentExtension3) {
			IDocumentExtension3 extension3= (IDocumentExtension3) document;
			extension3.setDocumentPartitioner(AutoconfEditor.AUTOCONF_PARTITIONING, partitioner);
		} else {
			document.setDocumentPartitioner(partitioner);
		}
//		document.addDocumentListener(this);
	}
	
	/*
	 * @see IDocumentListener#documentAboutToBeChanged(DocumentEvent)
	 */

	public void documentAboutToBeChanged(DocumentEvent e) {
		// do nothing
	}
	
	/*
	 * @see IDocumentListener#documentChanged(DocumentEvent)
	 */
	public void documentChanged(DocumentEvent e) {
		// do nothing
	}

}
