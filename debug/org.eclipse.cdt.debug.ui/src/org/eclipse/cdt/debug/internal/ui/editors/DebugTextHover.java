/*
 *(c) Copyright QNX Software Systems Ltd. 2002.
 * All Rights Reserved.
 * 
 */

package org.eclipse.cdt.debug.internal.ui.editors;

import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.ITextHover;
import org.eclipse.jface.text.ITextViewer;

/**
 *
 * Enter type comment.
 * 
 * @since: Sep 12, 2002
 */
public class DebugTextHover implements ITextHover
{

	/**
	 * Constructor for DebugTextHover.
	 */
	public DebugTextHover()
	{
		super();
	}

	/**
	 * @see org.eclipse.jface.text.ITextHover#getHoverInfo(ITextViewer, IRegion)
	 */
	public String getHoverInfo( ITextViewer textViewer, IRegion hoverRegion )
	{
		return "Test";
	}

	/**
	 * @see org.eclipse.jface.text.ITextHover#getHoverRegion(ITextViewer, int)
	 */
	public IRegion getHoverRegion( ITextViewer textViewer, int offset )
	{
		return null;
	}

}
