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
package org.eclipse.cdt.debug.core;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import org.eclipse.cdt.core.model.IFunction;
import org.eclipse.cdt.core.model.IMethod;
import org.eclipse.cdt.core.model.ITranslationUnit;
import org.eclipse.cdt.debug.core.cdi.CDIException;
import org.eclipse.cdt.debug.core.model.ICValue;
import org.eclipse.cdt.debug.internal.core.model.CFloatingPointValue;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.IStatusHandler;
import org.w3c.dom.Document;

/**
 * Utility methods.
 */
public class CDebugUtils {

	public static boolean question( IStatus status, Object source ) {
		Boolean result = new Boolean( false );
		IStatusHandler handler = DebugPlugin.getDefault().getStatusHandler( status );
		if ( handler != null ) {
			try {
				result = (Boolean)handler.handleStatus( status, source );
			}
			catch( CoreException e ) {
			}
		}
		return result.booleanValue();
	}

	public static void info( IStatus status, Object source ) {
		IStatusHandler handler = DebugPlugin.getDefault().getStatusHandler( status );
		if ( handler != null ) {
			try {
				handler.handleStatus( status, source );
			}
			catch( CoreException e ) {
			}
		}
	}

	public static void error( IStatus status, Object source ) {
		IStatusHandler handler = DebugPlugin.getDefault().getStatusHandler( status );
		if ( handler != null ) {
			try {
				handler.handleStatus( status, source );
			}
			catch( CoreException e ) {
			}
		}
	}

	public static char[] getByteText( byte b ) {
		return new char[]{ charFromByte( (byte)((b >>> 4) & 0x0f) ), charFromByte( (byte)(b & 0x0f) ) };
	}

	public static byte textToByte( char[] text ) {
		byte result = 0;
		if ( text.length == 2 ) {
			byte[] bytes = { charToByte( text[0] ), charToByte( text[1] ) };
			result = (byte)((bytes[0] << 4) + bytes[1]);
		}
		return result;
	}

	public static char charFromByte( byte value ) {
		if ( value >= 0x0 && value <= 0x9 )
			return (char)(value + '0');
		if ( value >= 0xa && value <= 0xf )
			return (char)(value - 0xa + 'a');
		return '0';
	}

	public static byte charToByte( char ch ) {
		if ( Character.isDigit( ch ) ) {
			return (byte)(ch - '0');
		}
		if ( ch >= 'a' && ch <= 'f' ) {
			return (byte)(0xa + ch - 'a');
		}
		if ( ch >= 'A' && ch <= 'F' ) {
			return (byte)(0xa + ch - 'A');
		}
		return 0;
	}

	public static char bytesToChar( byte[] bytes ) {
		try {
			return (char)Short.parseShort( new String( bytes ), 16 );
		}
		catch( RuntimeException e ) {
		}
		return 0;
	}

	public static byte toByte( char[] bytes, boolean le ) {
		if ( bytes.length != 2 )
			return 0;
		return (byte)Long.parseLong( bytesToString( bytes, le, true ), 16 );
	}

	public static short toUnsignedByte( char[] bytes, boolean le ) {
		if ( bytes.length != 2 )
			return 0;
		return (short)Long.parseLong( bytesToString( bytes, le, false ), 16 );
	}

	public static short toShort( char[] bytes, boolean le ) {
		if ( bytes.length != 4 )
			return 0;
		return (short)Long.parseLong( bytesToString( bytes, le, true ), 16 );
	}

	public static int toUnsignedShort( char[] bytes, boolean le ) {
		if ( bytes.length != 4 )
			return 0;
		return (int)Long.parseLong( bytesToString( bytes, le, false ), 16 );
	}

	public static int toInt( char[] bytes, boolean le ) {
		if ( bytes.length != 8 )
			return 0;
		return (int)Long.parseLong( bytesToString( bytes, le, true ), 16 );
	}

	public static long toUnsignedInt( char[] bytes, boolean le ) {
		if ( bytes.length != 8 )
			return 0;
		return Long.parseLong( bytesToString( bytes, le, false ), 16 );
	}

	private static String bytesToString( char[] bytes, boolean le, boolean signed ) {
		char[] copy = new char[bytes.length];
		if ( le ) {
			for( int i = 0; i < bytes.length / 2; ++i ) {
				copy[2 * i] = bytes[bytes.length - 2 * i - 2];
				copy[2 * i + 1] = bytes[bytes.length - 2 * i - 1];
			}
		}
		else {
			System.arraycopy( bytes, 0, copy, 0, copy.length );
		}
		return new String( copy );
	}

	public static String prependString( String text, int length, char ch ) {
		StringBuffer sb = new StringBuffer( length );
		if ( text.length() > length ) {
			sb.append( text.substring( 0, length ) );
		}
		else {
			char[] prefix = new char[length - text.length()];
			Arrays.fill( prefix, ch );
			sb.append( prefix );
			sb.append( text );
		}
		return sb.toString();
	}

	public static boolean isReferencedProject( IProject parent, IProject project ) {
		if ( parent != null && parent.exists() ) {
			List projects = CDebugUtils.getReferencedProjects( project );
			Iterator it = projects.iterator();
			while( it.hasNext() ) {
				IProject prj = (IProject)it.next();
				if ( prj.exists() && (prj.equals( project )) )
					return true;
			}
		}
		return false;
	}

	/**
	 * Serializes a XML document into a string - encoded in UTF8 format, with platform line separators.
	 * 
	 * @param doc document to serialize
	 * @return the document as a string
	 */
	public static String serializeDocument( Document doc ) throws IOException, TransformerException {
		ByteArrayOutputStream s = new ByteArrayOutputStream();
		TransformerFactory factory = TransformerFactory.newInstance();
		Transformer transformer = factory.newTransformer();
		transformer.setOutputProperty( OutputKeys.METHOD, "xml" ); //$NON-NLS-1$
		transformer.setOutputProperty( OutputKeys.INDENT, "yes" ); //$NON-NLS-1$
		DOMSource source = new DOMSource( doc );
		StreamResult outputTarget = new StreamResult( s );
		transformer.transform( source, outputTarget );
		return s.toString( "UTF8" ); //$NON-NLS-1$			
	}

	public static IResource getFunctionResource( IFunction function ) {
		ITranslationUnit tu = function.getTranslationUnit();
		return (tu != null) ? tu.getResource() : function.getCProject().getProject();
	}

	public static IResource getMethodResource( IMethod method ) {
		ITranslationUnit tu = method.getTranslationUnit();
		return (tu != null) ? tu.getResource() : method.getCProject().getProject();
	}

	public static String getFunctionName( IFunction function ) {
		String functionName = function.getElementName();
		StringBuffer name = new StringBuffer( functionName );
		if ( functionName.indexOf( "::" ) != -1 ) //$NON-NLS-1$
		{
			String[] params = function.getParameterTypes();
			name.append( '(' );
			if ( params.length == 0 ) {
				name.append( "void" ); //$NON-NLS-1$
			}
			else {
				for( int i = 0; i < params.length; ++i ) {
					name.append( params[i] );
					if ( i != params.length - 1 )
						name.append( ',' );
				}
			}
			name.append( ')' );
		}
		return name.toString();
	}

	public static String getMethodQualifiedName( IMethod method ) {
		return null;
	}

	public static Number getFloatingPointValue( ICValue value ) {
		if ( value instanceof CFloatingPointValue ) {
			try {
				return ((CFloatingPointValue)value).getFloatingPointValue();
			}
			catch( CDIException e ) {
			}
		}
		return null;
	}

	public static boolean isNaN( Number value ) {
		if ( value instanceof Double ) {
			return ((Double)value).isNaN();
		}
		if ( value instanceof Float ) {
			return ((Float)value).isNaN();
		}
		return false;
	}

	public static boolean isPositiveInfinity( Number value ) {
		if ( value instanceof Double ) {
			return (((Double)value).isInfinite() && value.doubleValue() == Double.POSITIVE_INFINITY);
		}
		if ( value instanceof Float ) {
			return (((Float)value).isInfinite() && value.floatValue() == Float.POSITIVE_INFINITY);
		}
		return false;
	}

	public static boolean isNegativeInfinity( Number value ) {
		if ( value instanceof Double ) {
			return (((Double)value).isInfinite() && value.doubleValue() == Double.NEGATIVE_INFINITY);
		}
		if ( value instanceof Float ) {
			return (((Float)value).isInfinite() && value.floatValue() == Float.NEGATIVE_INFINITY);
		}
		return false;
	}

	public static List getReferencedProjects( IProject project ) {
		ArrayList list = new ArrayList( 10 );
		if ( project != null && project.exists() && project.isOpen() ) {
			IProject[] refs = new IProject[0];
			try {
				refs = project.getReferencedProjects();
			}
			catch( CoreException e ) {
			}
			for( int i = 0; i < refs.length; ++i ) {
				if ( !project.equals( refs[i] ) && refs[i] != null && refs[i].exists() && refs[i].isOpen() ) {
					list.add( refs[i] );
					getReferencedProjects( project, refs[i], list );
				}
			}
		}
		return list;
	}

	private static void getReferencedProjects( IProject root, IProject project, List list ) {
		if ( project != null && project.exists() && project.isOpen() ) {
			IProject[] refs = new IProject[0];
			try {
				refs = project.getReferencedProjects();
			}
			catch( CoreException e ) {
			}
			for( int i = 0; i < refs.length; ++i ) {
				if ( !list.contains( refs[i] ) && refs[i] != null && !refs[i].equals( root ) && refs[i].exists() && refs[i].isOpen() ) {
					list.add( refs[i] );
					getReferencedProjects( root, refs[i], list );
				}
			}
		}
	}
}