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

package org.eclipse.cdt.debug.internal.core.sourcelookup;

import java.io.IOException;
import java.io.StringReader;
import java.text.MessageFormat;
import java.util.LinkedList;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;

import org.eclipse.cdt.debug.core.CDebugCorePlugin;
import org.eclipse.cdt.debug.core.CDebugUtils;
import org.eclipse.cdt.debug.core.sourcelookup.ICSourceLocation;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

/**
 * Enter type comment.
 * 
 * @since Oct 22, 2003
 */
public class SourceUtils
{
	private static final String NAME_COMMON_SOURCE_LOCATIONS = "commonSourceLocations"; //$NON-NLS-1$
	private static final String NAME_SOURCE_LOCATION = "sourceLocation"; //$NON-NLS-1$
	private static final String ATTR_CLASS = "class"; //$NON-NLS-1$
	private static final String ATTR_MEMENTO = "memento"; //$NON-NLS-1$

	public static String getCommonSourceLocationsMemento( ICSourceLocation[] locations )
	{
        Document document = null;
        Throwable ex = null;
        try 
		{
            document = DocumentBuilderFactory.newInstance().newDocumentBuilder().newDocument();
            Element element = document.createElement( NAME_COMMON_SOURCE_LOCATIONS );
            document.appendChild( element );
    		saveSourceLocations( document, element, locations );
			return CDebugUtils.serializeDocument( document );
        }
        catch( ParserConfigurationException e ) 
		{
        	ex = e;
        }
		catch( IOException e )
		{
			ex = e;
		}
		catch( TransformerException e )
		{
			ex = e;
		}
		CDebugCorePlugin.log( new Status( IStatus.ERROR, CDebugCorePlugin.getUniqueIdentifier(), 0, "Error saving common source settings.", ex ) ); //$NON-NLS-1$
		return null;
	}

	private static void saveSourceLocations( Document doc, Element node, ICSourceLocation[] locations )
	{
		for ( int i = 0; i < locations.length; i++ )
		{			
			Element child = doc.createElement( NAME_SOURCE_LOCATION );
			child.setAttribute( ATTR_CLASS, locations[i].getClass().getName() );
			try
			{
				child.setAttribute( ATTR_MEMENTO, locations[i].getMemento() );
			}
			catch( CoreException e )
			{
				CDebugCorePlugin.log( e );
				continue;
			}
			node.appendChild( child );
		}
	}

	public static ICSourceLocation[] getCommonSourceLocationsFromMemento( String memento )
	{
		ICSourceLocation[] result = new ICSourceLocation[0];
		if ( !isEmpty( memento ) )
		{
			try
			{
				DocumentBuilder parser = DocumentBuilderFactory.newInstance().newDocumentBuilder();
				StringReader reader = new StringReader( memento );
				InputSource source = new InputSource( reader );
				Element root = parser.parse( source ).getDocumentElement();

				if ( root.getNodeName().equalsIgnoreCase( NAME_COMMON_SOURCE_LOCATIONS ) )
					result = initializeSourceLocations( root );
			}
			catch( ParserConfigurationException e )
			{
				CDebugCorePlugin.log( new Status( IStatus.ERROR, CDebugCorePlugin.getUniqueIdentifier(), 0, "Error initializing common source settings.", e ) ); //$NON-NLS-1$
			}
			catch( SAXException e )
			{
				CDebugCorePlugin.log( new Status( IStatus.ERROR, CDebugCorePlugin.getUniqueIdentifier(), 0, "Error initializing common source settings.", e ) ); //$NON-NLS-1$
			}
			catch( IOException e )
			{
				CDebugCorePlugin.log( new Status( IStatus.ERROR, CDebugCorePlugin.getUniqueIdentifier(), 0, "Error initializing common source settings.", e ) ); //$NON-NLS-1$
			}
		}
		return result;
	}

	public static ICSourceLocation[] initializeSourceLocations( Element root )
	{
		List sourceLocations = new LinkedList();
		NodeList list = root.getChildNodes();
		int length = list.getLength();
		for ( int i = 0; i < length; ++i )
		{
			Node node = list.item( i );
			short type = node.getNodeType();
			if ( type == Node.ELEMENT_NODE )
			{
				Element entry = (Element)node;
				if ( entry.getNodeName().equalsIgnoreCase( NAME_SOURCE_LOCATION ) )
				{
					String className = entry.getAttribute( ATTR_CLASS );
					String data = entry.getAttribute( ATTR_MEMENTO );
					if ( className == null || className.trim().length() == 0 )
					{
						CDebugCorePlugin.log( "Unable to restore common source locations - invalid format." ); //$NON-NLS-1$
						continue;
					}
					Class clazz = null;
					try
					{
						clazz = CDebugCorePlugin.getDefault().getBundle().loadClass( className );
					}
					catch( ClassNotFoundException e )
					{
						CDebugCorePlugin.log( MessageFormat.format( "Unable to restore source location - class not found {0}", new String[] { className } ) ); //$NON-NLS-1$
						continue;
					}

					ICSourceLocation location = null;
					try
					{
						location = (ICSourceLocation)clazz.newInstance();
					}
					catch( IllegalAccessException e )
					{
						CDebugCorePlugin.log( "Unable to restore source location: " + e.getMessage() ); //$NON-NLS-1$
						continue;
					}
					catch( InstantiationException e )
					{
						CDebugCorePlugin.log( "Unable to restore source location: " + e.getMessage() ); //$NON-NLS-1$
						continue;
					}
					try
					{
						location.initializeFrom( data );
						sourceLocations.add( location );
					}
					catch( CoreException e )
					{
						CDebugCorePlugin.log( "Unable to restore source location: " + e.getMessage() ); //$NON-NLS-1$
					}
				}
			}
		}
		return (ICSourceLocation[])sourceLocations.toArray( new ICSourceLocation[sourceLocations.size()] );
	}

	private static boolean isEmpty( String string )
	{
		return ( string == null || string.trim().length() == 0 );
	}
}
