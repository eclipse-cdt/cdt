package org.eclipse.cdt.internal.ui.text.eclipse2;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */
 
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IRegion;


/**
 * For internal use only. Not API. <p>
 * A document partitioning listener extension is for extending
 * <code>IDocumentPartitioningListener</code> instances with new 
 * or revised functionality.
*/
public interface IDocumentPartitioningListenerExtension {
		
	/**
	 * The partitioning of the given document changed in the given range.
	 *
	 * @param document the document whose partitioning changed
	 * @param region the range in which the partition type changed
	 *
	 * @see IDocument#addDocumentPartitioningListener
	 */
	void documentPartitioningChanged(IDocument document, IRegion region);
}


