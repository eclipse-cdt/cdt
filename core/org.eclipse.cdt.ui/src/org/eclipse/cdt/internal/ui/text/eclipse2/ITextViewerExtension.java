package org.eclipse.cdt.internal.ui.text.eclipse2;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */

import org.eclipse.swt.custom.VerifyKeyListener;

 
/**
 * A text viewer  extension is for extending
 * <code>ITextViewer</code> instances with new functionality.
 */
public interface ITextViewerExtension {

	/*
	 * All subsequent methods dealing with verify key listeners
	 * are indended to allow clients a fine grained management of
	 * key event consumption. Will be merged/ will replace
	 * the event consumer mechanism available on ITextViewer.
	 */
	 
	/**
	 * Inserts the verify key listener at the beginning of the viewer's 
	 * list of verify key listeners.  If the listener is already registered 
	 * with the viewer this call moves the listener to the beginnng of
	 * the list.
	 *
	 * @param listener the listener to be inserted
	 */
	void prependVerifyKeyListener(VerifyKeyListener listener);
	
	/**
	 * Appends a verify key listener to the viewer's list of verify
	 * key listeners. If the listener is already registered with the viewer
	 * this call moves the listener to the end of the list.
	 *
	 * @param listener the listener to be added
	 */
	void appendVerifyKeyListener(VerifyKeyListener listener);
	
	/**
	 * Removes the verify key listener from the viewer's list of verify key listeners.
	 * If the listener is not registered with this viewer, this call has no effect.
	 * 
	 * @param listener the listener to be removed
	 */
	void removeVerifyKeyListener(VerifyKeyListener listener);
}


