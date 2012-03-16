/*******************************************************************************
 * Copyright (c) 2006, 2009 Red Hat, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Red Hat Incorporated - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.autotools.ui.editors;

import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.ITypedRegion;
import org.eclipse.jface.text.rules.FastPartitioner;
import org.eclipse.jface.text.rules.IPartitionTokenScanner;

public class AutoconfPartitioner extends FastPartitioner {

	public AutoconfPartitioner(IPartitionTokenScanner scanner, String[] legalContentTypes) {
		// TODO Auto-generated constructor stub
		super(scanner, legalContentTypes);
	}
			
	public void connect(IDocument document, int blah) {
		super.connect(document);
	}
	
	// To optionally show partitions, we must do so by overriding the computePartitioning
	// method.  We cannot do it at connect time because the document may be zero length
	// at the time and we will end up getting default partitioning from then on.
	public ITypedRegion[] computePartitioning(int offset, int length, 
			boolean includeZeroLength) {
		ITypedRegion[] regions = super.computePartitioning(offset, length, includeZeroLength);
		// Uncomment the following line to see partitioning.
//		printPartitions(regions);
		return regions;
	}
	
	public void printPartitions(ITypedRegion[] partitions)
	{
		StringBuffer buffer = new StringBuffer();

		for (int i = 0; i < partitions.length; i++)
		{
			try
			{
				buffer.append("Partition type: " + partitions[i].getType() //$NON-NLS-1$
						+ ", offset: " + partitions[i].getOffset() //$NON-NLS-1$
						+ ", length: " + partitions[i].getLength()); //$NON-NLS-1$
				buffer.append("\n"); //$NON-NLS-1$
				buffer.append("Text:\n"); //$NON-NLS-1$
				buffer.append(super.fDocument.get(partitions[i].getOffset(), partitions[i].getLength()));
				buffer.append("\n---------------------------\n\n\n"); //$NON-NLS-1$
			}
			catch (BadLocationException e)
			{
				e.printStackTrace();
			}
		}
		System.out.print(buffer);
	}
}