/*
 *(c) Copyright QNX Software Systems Ltd. 2002.
 * All Rights Reserved.
 * 
 */
package org.eclipse.cdt.debug.internal.ui.editors;

import org.eclipse.cdt.debug.internal.core.breakpoints.CAddressBreakpoint;
import org.eclipse.cdt.debug.internal.core.breakpoints.CFunctionBreakpoint;
import org.eclipse.cdt.debug.internal.core.breakpoints.CLineBreakpoint;
import org.eclipse.core.resources.IMarker;
import org.eclipse.debug.ui.DebugUITools;
import org.eclipse.debug.ui.IDebugModelPresentation;
import org.eclipse.ui.texteditor.MarkerAnnotation;
import org.eclipse.ui.texteditor.MarkerUtilities;

/**
 * Enter type comment.
 * 
 * @since: Jan 6, 2003
 */
public class DisassemblyMarkerAnnotation extends MarkerAnnotation
{
	private IDebugModelPresentation fPresentation;

	/**
	 * Constructor for DisassemblyMarkerAnnotation.
	 * @param marker
	 */
	public DisassemblyMarkerAnnotation( IMarker marker )
	{
		super( marker );
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.texteditor.MarkerAnnotation#initialize()
	 */
	protected void initialize()
	{
		IMarker marker = getMarker();

		if ( MarkerUtilities.isMarkerType( marker, CLineBreakpoint.getMarkerType() ) ||
			 MarkerUtilities.isMarkerType( marker, CFunctionBreakpoint.getMarkerType() ) ||
			 MarkerUtilities.isMarkerType( marker, CAddressBreakpoint.getMarkerType() ) )
		{
			if ( fPresentation == null )
				fPresentation = DebugUITools.newDebugModelPresentation();

			setLayer( 4 );
			setImage( fPresentation.getImage( marker ) );
			return;
		}
		super.initialize();
	}
}
