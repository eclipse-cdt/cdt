package org.eclipse.cdt.internal.ui;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */

/**
 * @since 1.0
 */
public interface IAdornmentProvider {
	
	/**
	 * Computes the adornment flags for the given element.
	 * Flags defined in <code>JavaElementImageProvider</code>.
	 */
	int computeAdornmentFlags(Object element);
	
	
	/**
	 * Called when the parent label provider is disposed
	 */
	void dispose();

}


