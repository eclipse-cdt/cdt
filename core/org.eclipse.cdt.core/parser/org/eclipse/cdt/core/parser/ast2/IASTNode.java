/**********************************************************************
 * Copyright (c) 2004 IBM Corporation and others.
 * All rights reserved.   This program and the accompanying materials
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors: 
 * IBM - Initial API and implementation
 **********************************************************************/
package org.eclipse.cdt.core.parser.ast2;

/**
 * This is the root interface to the AST node hierarchy. It is assumed
 * that all nodes have filename, offset information. If the filename is
 * null then this is not true for the given node.
 * 
 * @author Doug Schaefer
 */
public interface IASTNode {
	
	/**
	 * @return the name of the file (if any) containing the node.
	 * If the node is not in a file (e.g. is a compiler built-in), this
	 * method returns null.
	 */
	public String getFilename();
	
	/**
	 * @return the offset into the file to the beginning of the node
	 */
	public int getOffset();

	/**
	 * @return the length of the node in the source file
	 */
	public int getLength();

}
