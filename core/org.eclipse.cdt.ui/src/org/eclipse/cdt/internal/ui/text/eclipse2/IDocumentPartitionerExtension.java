package org.eclipse.cdt.internal.ui.text.eclipse2;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */
 
import org.eclipse.jface.text.DocumentEvent;
import org.eclipse.jface.text.IRegion;


/**
 * For internal use only. Not API. <p>
 * A document partitioner extension is for extending
 * <code>IDocumentPartitioner</code> instances with new 
 * or revised functionality.
*/
public interface IDocumentPartitionerExtension {
		
	/**
	 * The document has been changed. The partitioner updates 
	 * the document's partitioning and returns in which region the
	 * partition type has been changed. This method always returns
	 * the surrounding region. Will be called by the connected document
	 * and is not intended to be used by clients other than the connected
	 * document.
	 *
	 * @param event the event describing the document change
	 * @return the region of the document in which the partition type changed
	 */
	IRegion documentChanged2(DocumentEvent event);
}


