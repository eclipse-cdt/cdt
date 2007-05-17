/*******************************************************************************
 * Copyright (c) 2000, 2007 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * QNX Software Systems - Initial API and implementation
 * ARM Limited - https://bugs.eclipse.org/bugs/show_bug.cgi?id=186981
 *******************************************************************************/
package org.eclipse.cdt.debug.internal.ui;

import java.text.MessageFormat;

import org.eclipse.cdt.debug.core.CDebugUtils;
import org.eclipse.cdt.debug.core.model.ICDebugElementStatus;
import org.eclipse.cdt.debug.core.model.ICStackFrame;
import org.eclipse.cdt.debug.core.model.ICType;
import org.eclipse.cdt.debug.core.model.ICValue;
import org.eclipse.cdt.debug.core.model.IEnableDisableTarget;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.debug.core.DebugException;
import org.eclipse.debug.core.model.IValue;
import org.eclipse.debug.core.model.IVariable;
import org.eclipse.debug.ui.DebugUITools;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.Region;

/**
 * Utility methods for C/C++ Debug UI.
 */
public class CDebugUIUtils {

	static public IRegion findWord( IDocument document, int offset ) {
		int start = -1;
		int end = -1;
		try {
			int pos = offset;
			char c;
			while( pos >= 0 ) {
				c = document.getChar( pos );
				if ( !Character.isJavaIdentifierPart( c ) )
					break;
				--pos;
			}
			start = pos;
			pos = offset;
			int length = document.getLength();
			while( pos < length ) {
				c = document.getChar( pos );
				if ( !Character.isJavaIdentifierPart( c ) )
					break;
				++pos;
			}
			end = pos;
		}
		catch( BadLocationException x ) {
		}
		if ( start > -1 && end > -1 ) {
			if ( start == offset && end == offset )
				return new Region( offset, 0 );
			else if ( start == offset )
				return new Region( start, end - start );
			else
				return new Region( start + 1, end - start - 1 );
		}
		return null;
	}

	/**
	 * Returns the currently selected stack frame or the topmost frame 
	 * in the currently selected thread in the Debug view 
	 * of the current workbench page. Returns <code>null</code> 
	 * if no stack frame or thread is selected, or if not called from the UI thread.
	 *  
	 * @return the currently selected stack frame or the topmost frame 
	 * 		   in the currently selected thread
	 */
	static public ICStackFrame getCurrentStackFrame() {
		IAdaptable context = DebugUITools.getDebugContext();
		return ( context != null ) ? (ICStackFrame)context.getAdapter( ICStackFrame.class ) : null;
	}

	/**
	 * Moved from CDebugModelPresentation because it is also used by CVariableLabelProvider.
	 */
	static public String getValueText( IValue value ) {
		StringBuffer label = new StringBuffer();
		if ( value instanceof ICDebugElementStatus && !((ICDebugElementStatus)value).isOK() ) {
			label.append(  MessageFormat.format( CDebugUIMessages.getString( "CDTDebugModelPresentation.4" ), new String[] { ((ICDebugElementStatus)value).getMessage() } ) ); //$NON-NLS-1$
		}
		else if ( value instanceof ICValue ) {
			ICType type = null;
			try {
				type = ((ICValue)value).getType();
			}
			catch( DebugException e ) {
			}
			try {
				String valueString = value.getValueString();
				if ( valueString != null ) {
					valueString = valueString.trim();
					if ( type != null && type.isCharacter() ) {
						if ( valueString.length() == 0 )
							valueString = "."; //$NON-NLS-1$
						label.append( valueString );
					}
					else if ( type != null && type.isFloatingPointType() ) {
						Number floatingPointValue = CDebugUtils.getFloatingPointValue( (ICValue)value );
						if ( CDebugUtils.isNaN( floatingPointValue ) )
							valueString = "NAN"; //$NON-NLS-1$
						if ( CDebugUtils.isPositiveInfinity( floatingPointValue ) )
							valueString = CDebugUIMessages.getString( "CDTDebugModelPresentation.23" ); //$NON-NLS-1$
						if ( CDebugUtils.isNegativeInfinity( floatingPointValue ) )
							valueString = CDebugUIMessages.getString( "CDTDebugModelPresentation.24" ); //$NON-NLS-1$
						label.append( valueString );
					}
					else if ( type == null || (!type.isArray() && !type.isStructure()) ) {
						if ( valueString.length() > 0 ) {
							label.append( valueString );
						}
					}
				}
			}
			catch( DebugException e1 ) {
			}
		}	
		return label.toString();
	}

	/**
	 * Moved from CDebugModelPresentation because it is also used by CVariableLabelProvider.
	 */
	public static String getVariableTypeName( ICType type ) {
		StringBuffer result = new StringBuffer();
		if ( type != null ) {
			String typeName = type.getName();
			if ( typeName != null )
				typeName = typeName.trim();
			if ( type.isArray() && typeName != null ) {
				int index = typeName.indexOf( '[' );
				if ( index != -1 )
					typeName = typeName.substring( 0, index ).trim();
			}
			if ( typeName != null && typeName.length() > 0 ) {
				result.append( typeName );
				if ( type.isArray() ) {
					int[] dims = type.getArrayDimensions();
					for( int i = 0; i < dims.length; ++i ) {
						result.append( '[' );
						result.append( dims[i] );
						result.append( ']' );
					}
				}
			}
		}
		return result.toString();
	}

	public static String getVariableName( IVariable variable ) throws DebugException {
		return decorateText( variable, variable.getName() );
	}

	public static String decorateText( Object element, String text ) {
		if ( text == null )
			return null;
		StringBuffer baseText = new StringBuffer( text );
		if ( element instanceof ICDebugElementStatus && !((ICDebugElementStatus)element).isOK() ) {
			baseText.append( MessageFormat.format( " <{0}>", new String[] { ((ICDebugElementStatus)element).getMessage() } ) ); //$NON-NLS-1$
		}
		if ( element instanceof IAdaptable ) {
			IEnableDisableTarget target = (IEnableDisableTarget)((IAdaptable)element).getAdapter( IEnableDisableTarget.class );
			if ( target != null ) {
				if ( !target.isEnabled() ) {
					baseText.append( ' ' );
					baseText.append( CDebugUIMessages.getString( "CDTDebugModelPresentation.25" ) ); //$NON-NLS-1$
				}
			}
		}
		return baseText.toString();
	}
}
