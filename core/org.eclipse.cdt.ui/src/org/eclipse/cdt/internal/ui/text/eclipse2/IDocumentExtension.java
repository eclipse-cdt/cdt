package org.eclipse.cdt.internal.ui.text.eclipse2;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */
 
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IDocumentListener;


/**
 * For internal use only. Not API. <p>
 * A document extension is for extending
 * <code>IDocument</code> instances with new functionality.
*/
public interface IDocumentExtension {
	
	/**
	 * Interface for a post notification document change.
	 */
	interface IReplace {
		
		/**
		 * Executes the replace operation of the given document.
		 * @param document the document to be changed
		 * @param owner the owner of this replace object
		 */
		void perform(IDocument document, IDocumentListener owner);
	};
	
	/**
	 * Callback for document listeners to achieve a post notification
	 * change of the document notifying them.
	 * 
	 * @param owner the owner of the replace object
	 * @param the replace to be executed
	 * @exception UnsupportedOperationException if <code>registerPostNotificationReplace</code>
	 * 	is not supported by this document
	 */
	void registerPostNotificationReplace(IDocumentListener owner, IReplace replace) throws UnsupportedOperationException;
	
	/**
	 * Stops the processing of registered post notification replaces until
	 * <code>resumePostNotificationProcessing</code> is called.
	 */
	void stopPostNotificationProcessing();
	
	/**
	 * Resumes the processing of post notification replaces. If the queue of registered
	 * <code>IReplace</code> objects is not empty, they are immediately processed
	 * if the document is not inside a replace operation or directly after the replace 
	 * operation otherwise.
	 */
	void resumePostNotificationProcessing();
}

