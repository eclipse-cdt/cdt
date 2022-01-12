/*******************************************************************************
 * Copyright (c) 2007, 2015 Red Hat, Inc.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
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

public class AutoconfDocumentSetupParticipant implements IDocumentSetupParticipant, IDocumentListener {

	@Override
	public void setup(IDocument document) {
		AutoconfPartitioner partitioner = new AutoconfPartitioner(new AutoconfPartitionScanner(),
				AutoconfPartitionScanner.AUTOCONF_PARTITION_TYPES);
		partitioner.connect(document, 1);
		if (document instanceof IDocumentExtension3) {
			IDocumentExtension3 extension3 = (IDocumentExtension3) document;
			extension3.setDocumentPartitioner(AutoconfEditor.AUTOCONF_PARTITIONING, partitioner);
		} else {
			document.setDocumentPartitioner(partitioner);
		}
		//		document.addDocumentListener(this);
	}

	@Override
	public void documentAboutToBeChanged(DocumentEvent e) {
		// do nothing
	}

	@Override
	public void documentChanged(DocumentEvent e) {
		// do nothing
	}

}
