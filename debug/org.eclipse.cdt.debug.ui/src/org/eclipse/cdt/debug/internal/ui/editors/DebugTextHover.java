/*
 *(c) Copyright QNX Software Systems Ltd. 2002.
 * All Rights Reserved.
 * 
 */

package org.eclipse.cdt.debug.internal.ui.editors;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.eclipse.cdt.debug.core.ICExpressionEvaluator;
import org.eclipse.cdt.debug.internal.ui.CDebugUIUtils;
import org.eclipse.cdt.debug.ui.CDebugUIPlugin;
import org.eclipse.debug.core.DebugException;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.ILaunchManager;
import org.eclipse.debug.core.model.IDebugTarget;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.ITextHover;
import org.eclipse.jface.text.ITextViewer;
import org.eclipse.jface.text.Region;
import org.eclipse.swt.graphics.Point;

/**
 *
 * The text hovering support for C/C++ debugger.
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
		DebugPlugin debugPlugin = DebugPlugin.getDefault();
		if ( debugPlugin == null )
		{
			return null;
		}
		ILaunchManager launchManager = debugPlugin.getLaunchManager();
		if ( launchManager == null )
		{
			return null;
		}

		IDebugTarget[] targets = launchManager.getDebugTargets();
		if ( targets != null && targets.length > 0 )
		{
			try
			{
				IDocument document = textViewer.getDocument();
				if ( document == null )
					return null;

				String expression = document.get( hoverRegion.getOffset(), hoverRegion.getLength() );
				List targetList = new ArrayList( targets.length );
				for ( int i = 0; i < targets.length; i++ )
				{
					ICExpressionEvaluator ee = (ICExpressionEvaluator)targets[i].getAdapter( ICExpressionEvaluator.class );
					if ( ee != null )
					{
						targetList.add( i, targets[i] );
					}
				}
				StringBuffer buffer = new StringBuffer();
				boolean showDebugTarget = targetList.size() > 1;
				Iterator iterator = targetList.iterator();
				while ( iterator.hasNext() )
				{
					IDebugTarget target = (IDebugTarget)iterator.next();
					ICExpressionEvaluator ee = (ICExpressionEvaluator)target.getAdapter( ICExpressionEvaluator.class );
					boolean first = true;
					if ( ee.canEvaluate() )
					{
						String result = evaluateExpression( ee, expression );
						try
						{
							if ( result != null )
							{
								if ( !first )
								{
									buffer.append( '\n' );
								}
								first = false;
								if ( showDebugTarget )
								{
									buffer.append( '[' );
									buffer.append( target.getName() );
									buffer.append( "]: " );
								}
								buffer.append( result );
							}
						}
						catch( DebugException x )
						{
							CDebugUIPlugin.log( x );
						}
					}
				}
				if ( buffer.length() > 0 )
				{
					return buffer.toString();
				}
			}
			catch ( BadLocationException x )
			{
				CDebugUIPlugin.log( x );
			}
		}
		return null;
	}

	/**
	 * @see org.eclipse.jface.text.ITextHover#getHoverRegion(ITextViewer, int)
	 */
	public IRegion getHoverRegion( ITextViewer viewer, int offset )
	{
		Point selectedRange = viewer.getSelectedRange();
		if ( selectedRange.x >= 0 && 
			 selectedRange.y > 0 &&
			 offset >= selectedRange.x &&
			 offset <= selectedRange.x + selectedRange.y )
			return new Region( selectedRange.x, selectedRange.y );
		if ( viewer != null )
			return CDebugUIUtils.findWord( viewer.getDocument(), offset );
		return null;
	}

	private String evaluateExpression( ICExpressionEvaluator ee, String expression )
	{
		String result = null;
		try
		{
			result = ee.evaluateExpressionToString( expression );
		}
		catch( DebugException e )
		{
			// ignore
		}
		return result;
	}
}
