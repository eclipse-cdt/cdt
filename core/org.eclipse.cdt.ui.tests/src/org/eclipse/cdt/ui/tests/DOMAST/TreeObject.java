/**********************************************************************
 * Copyright (c) 2005 IBM Canada and others.
 * All rights reserved.   This program and the accompanying materials
 * are made available under the terms of the Common Public License v0.5
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v05.html
 * 
 * Contributors: 
 * IBM Rational Software - Initial API and implementation 
 **********************************************************************/
package org.eclipse.cdt.ui.tests.DOMAST;

import org.eclipse.cdt.core.dom.ast.IASTDeclSpecifier;
import org.eclipse.cdt.core.dom.ast.IASTDeclarator;
import org.eclipse.cdt.core.dom.ast.IASTFileLocation;
import org.eclipse.cdt.core.dom.ast.IASTFunctionDefinition;
import org.eclipse.cdt.core.dom.ast.IASTName;
import org.eclipse.cdt.core.dom.ast.IASTNode;
import org.eclipse.cdt.core.dom.ast.IASTNodeLocation;
import org.eclipse.cdt.core.dom.ast.IASTSimpleDeclaration;
import org.eclipse.cdt.core.dom.ast.IASTTranslationUnit;
import org.eclipse.core.runtime.IAdaptable;

/**
 * @author dsteffle
 */
public class TreeObject implements IAdaptable {
	private static final String FILE_SEPARATOR = "\\";
	public static final String BLANK_FILENAME = ""; //$NON-NLS-1$
	private static final String IGCCAST_PREFIX = "IGCCAST"; //$NON-NLS-1$
	private static final String IGNUAST_PREFIX = "IGNUAST"; //$NON-NLS-1$
	private static final String IGPPAST_PREFIX = "IGPPAST"; //$NON-NLS-1$
	private static final String ICPPAST_PREFIX = "ICPPAST"; //$NON-NLS-1$
	private static final String ICAST_PREFIX = "ICAST"; //$NON-NLS-1$
	private static final String IAST_PREFIX = "IAST"; //$NON-NLS-1$
	private static final String START_OF_LIST = ": "; //$NON-NLS-1$
	private static final String LIST_SEPARATOR = ", "; //$NON-NLS-1$
	private static final String FILENAME_SEPARATOR = "."; //$NON-NLS-1$
	private IASTNode node = null;
	private TreeParent parent;
	
	public TreeObject(IASTNode node) {
		this.node = node;
	}
	public IASTNode getNode() {
		return node;
	}
	public void setParent(TreeParent parent) {
		this.parent = parent;
	}
	public TreeParent getParent() {
		return parent;
	}
	
	private boolean hasProperPrefix(String string) {
		if (string.startsWith(IAST_PREFIX) ||
				string.startsWith(ICAST_PREFIX) ||
				string.startsWith(ICPPAST_PREFIX) ||
				string.startsWith(IGPPAST_PREFIX) ||
				string.startsWith(IGNUAST_PREFIX) ||
				string.startsWith(IGCCAST_PREFIX))
			return true;

		return false;
	}
	
	public String toString() {
	    if( node == null ) return BLANK_FILENAME; //$NON-NLS-1$ //TODO Devin is this the best way???
		StringBuffer buffer = new StringBuffer();
		
		Class[] classes = node.getClass().getInterfaces();
		for(int i=0; i<classes.length; i++) {
			String interfaceName = classes[i].getName().substring(classes[i].getName().lastIndexOf(FILENAME_SEPARATOR) + 1);
			if (hasProperPrefix(interfaceName)) {
				buffer.append(interfaceName);
				if (i+1 < classes.length && hasProperPrefix(classes[i+1].getName().substring(classes[i+1].getName().lastIndexOf(FILENAME_SEPARATOR) + 1)))
					buffer.append(LIST_SEPARATOR);
			}
		}
		
		if ( node instanceof IASTSimpleDeclaration ) {
			IASTDeclarator[] decltors = ((IASTSimpleDeclaration)node).getDeclarators();
			
			if ( decltors.length > 0 ) {
				buffer.append(START_OF_LIST);
				for (int i=0; i<decltors.length; i++) {
					buffer.append(decltors[i].getName());
					
					if (i+1<decltors.length)
						buffer.append(LIST_SEPARATOR);
				}
			}
			return buffer.toString();
		} else if ( node instanceof IASTFunctionDefinition ) {
			String name = ((IASTFunctionDefinition)node).getDeclarator().getName().toString();
			if (name != null) {
				buffer.append(START_OF_LIST);
				buffer.append(name);
			}
			return buffer.toString();
		} else if ( node instanceof IASTName ) {
			buffer.append(START_OF_LIST);
			buffer.append(node);
			return buffer.toString();
		} else if ( node instanceof IASTTranslationUnit ) {
			String fileName = getFilename();
			int lastSlash = fileName.lastIndexOf(FILE_SEPARATOR);
			
			if (lastSlash > 0) {
				buffer.append(START_OF_LIST);
				buffer.append(fileName.substring(lastSlash+1)); // TODO make path relative to project, i.e. /projectName/path/file.c
			}
			
			return buffer.toString();
		} else if( node instanceof IASTDeclSpecifier )
		{
		    buffer.append( START_OF_LIST );
		    buffer.append( ((IASTDeclSpecifier)node).getUnpreprocessedSignature() );
		    return buffer.toString();
		}
		
		
		return buffer.toString();
	}
	public Object getAdapter(Class key) {
		return null;
	}
	
	public String getFilename()
	{
		if ( node == null ) return BLANK_FILENAME;
	   IASTNodeLocation [] location = node.getNodeLocations();
	   if( location.length > 0 && location[0] instanceof IASTFileLocation )
	      return ((IASTFileLocation)location[0]).getFileName();
	   return BLANK_FILENAME; //$NON-NLS-1$
	}
	
	public int getOffset() {
	   IASTNodeLocation [] location = node.getNodeLocations();
	   if( location.length == 1 )
	      return location[0].getNodeOffset();
	   return 0;
	}
	
	public int getLength() {
	   IASTNodeLocation [] location = node.getNodeLocations();
	   if( location.length == 1 )
	      return location[0].getNodeLength();
	   return 0;
	}
}
