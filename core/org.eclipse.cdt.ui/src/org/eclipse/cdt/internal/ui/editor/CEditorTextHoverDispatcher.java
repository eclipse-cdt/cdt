package org.eclipse.cdt.internal.ui.editor;

/*
 * (c) Copyright QNX Software Systems Ltd. 2002.
 * All Rights Reserved.
 */

import java.util.HashMap;

import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.ITextHover;
import org.eclipse.jface.text.ITextViewer;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IWorkbenchPage;

public class CEditorTextHoverDispatcher implements ITextHover 
{
	private IEditorPart fEditor = null;
	private HashMap fTextHovers = null;
	private DefaultCEditorTextHover fDefaultTextHover = null;
	
	/**
	 * Constructor for CEditorTextHover
	 */
	public CEditorTextHoverDispatcher( IEditorPart editor, HashMap textHovers ) 
	{
		fEditor = editor;
		fTextHovers = textHovers;
		fDefaultTextHover = new DefaultCEditorTextHover( editor );
	}

	/**
	 * @see ITextHover#getHoverInfo(ITextViewer, IRegion)
	 */
	public String getHoverInfo( ITextViewer textViewer, IRegion region ) 
	{
		return getCurrentTextHover().getHoverInfo( textViewer, region );
	}

	/**
	 * @see ITextHover#getHoverRegion(ITextViewer, int)
	 */
	public IRegion getHoverRegion( ITextViewer textViewer, int offset ) 
	{
		return getCurrentTextHover().getHoverRegion( textViewer, offset );
	}
	
	private ITextHover getCurrentTextHover()
	{
		IWorkbenchPage page;
		if(fEditor != null && fEditor.getSite() != null && 
		   (page = fEditor.getSite().getPage()) != null) {
			Object textHover = fTextHovers.get( page.getPerspective().getId() );
			if ( textHover != null && textHover instanceof ITextHover )
				return (ITextHover)textHover;
		}
		return fDefaultTextHover;
	}
}

