/*******************************************************************************
 * Copyright (c) 2000, 2004 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     QNX Software Systems - Initial API and implementation
 *******************************************************************************/

package org.eclipse.cdt.debug.internal.ui.editors;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.eclipse.cdt.debug.core.model.ICExpressionEvaluator;
import org.eclipse.cdt.debug.internal.ui.CDebugUIUtils;
import org.eclipse.cdt.debug.ui.CDebugUIPlugin;
import org.eclipse.cdt.ui.text.c.hover.ICEditorTextHover;
import org.eclipse.debug.core.DebugException;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.ILaunchManager;
import org.eclipse.debug.core.model.IDebugTarget;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.ITextViewer;
import org.eclipse.ui.IEditorPart;

/**
 *
 * The text hovering support for C/C++ debugger.
 * 
 * @since: Sep 12, 2002
 */
public class DebugTextHover implements ICEditorTextHover
{
	static final private int MAX_HOVER_INFO_SIZE = 100;

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
				if ( expression == null )
					return null;
				expression = expression.trim();
				if ( expression.length() == 0 )
					return null; 
				List targetList = new ArrayList( targets.length );
				for ( int i = 0; i < targets.length; i++ )
				{
					ICExpressionEvaluator ee = (ICExpressionEvaluator)targets[i].getAdapter( ICExpressionEvaluator.class );
					if ( ee != null )
					{
						targetList.add(targets[i] );
					}
				}
				StringBuffer buffer = new StringBuffer();
				boolean showDebugTarget = targetList.size() > 1;
				Iterator iterator = targetList.iterator();
				while ( iterator.hasNext() )
				{
					IDebugTarget target = (IDebugTarget)iterator.next();
					ICExpressionEvaluator ee = (ICExpressionEvaluator)target.getAdapter( ICExpressionEvaluator.class );
					if ( ee.canEvaluate() )
					{
						String result = evaluateExpression( ee, expression );
						try
						{
							if ( result != null )
								appendVariable( buffer, expression, result.trim(), showDebugTarget ? target.getName() : null );
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
/*
		Point selectedRange = viewer.getSelectedRange();
		if ( selectedRange.x >= 0 && 
			 selectedRange.y > 0 &&
			 offset >= selectedRange.x &&
			 offset <= selectedRange.x + selectedRange.y )
			return new Region( selectedRange.x, selectedRange.y );
*/
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

	/**
	 * A variable gets one line for each debug target it appears in.
	 */
	private static void appendVariable( StringBuffer buffer, 
										String expression, 
										String value,
										String debugTargetName ) throws DebugException 
	{
		if ( value.length() > MAX_HOVER_INFO_SIZE )
			value = value.substring( 0, MAX_HOVER_INFO_SIZE ) + " ..."; //$NON-NLS-1$
		buffer.append( "<p>" ); //$NON-NLS-1$
		if ( debugTargetName != null ) 
		{
			buffer.append( '[' + debugTargetName + "]&nbsp;" ); //$NON-NLS-1$ 
		}
		buffer.append( makeHTMLSafe( expression ) );
		buffer.append( " = " ); //$NON-NLS-1$
		
		String safeValue = "<b>" + makeHTMLSafe( value ) + "</b>"; //$NON-NLS-1$ //$NON-NLS-2$
		buffer.append( safeValue );			
		buffer.append( "</p>" ); //$NON-NLS-1$
	}

	/**
	 * Replace any characters in the given String that would confuse an HTML 
	 * parser with their escape sequences.
	 */
	private static String makeHTMLSafe( String string ) 
	{
		StringBuffer buffer = new StringBuffer( string.length() );
	
		for ( int i = 0; i != string.length(); i++ ) 
		{
			char ch = string.charAt( i );
			
			switch( ch ) 
			{
				case '&':
					buffer.append( "&amp;" ); //$NON-NLS-1$
					break;
					
				case '<':
					buffer.append( "&lt;" ); //$NON-NLS-1$
					break;

				case '>':
					buffer.append( "&gt;" ); //$NON-NLS-1$
					break;

				default:
					buffer.append( ch );
					break;
			}
		}
		return buffer.toString();		
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.ui.text.c.hover.ICEditorTextHover#setEditor(org.eclipse.ui.IEditorPart)
	 */
	public void setEditor(IEditorPart editor) {
	}
}
