/*******************************************************************************
 * Copyright (c) 2015 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * QNX Software Systems - Initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.internal.qt.ui.editor;

import org.eclipse.core.filebuffers.IDocumentSetupParticipant;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IDocumentExtension3;
import org.eclipse.jface.text.IDocumentPartitioner;
import org.eclipse.jface.text.rules.FastPartitioner;

public class QMLDocumentSetupParticipant implements IDocumentSetupParticipant, IQMLPartitions {

	@Override
	public void setup(IDocument document) {
		IDocumentPartitioner partitioner = new FastPartitioner(new QMLPartitionScanner(), IQMLPartitions.ALL_QMLPARTITIONS);
		if (document instanceof IDocumentExtension3) {
			IDocumentExtension3 ext = (IDocumentExtension3) document;
			ext.setDocumentPartitioner(IQMLPartitions.QML_PARTITIONING, partitioner);
		} else {
			document.setDocumentPartitioner(partitioner);
		}
		partitioner.connect(document);
	}
}
