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
 * A physical node represents a token (generally) in the code. Physical
 * nodes have a location, either a file or a macro, and an offset and
 * length.
 * 
 * @author Doug Schaefer
 */
public interface IASTPhysicalNode extends IASTNode {

	/**
	 * @return the location of the node
	 */
	public IASTNodeLocation getLocation();
	
	/**
	 * @return the offset into the file to the beginning of the node
	 */
	public int getOffset();
	
	/**
	 * @return the length of the node in the source file
	 */
	public int getLength();

	/**
	 * @return the underlying text in the code
	 * 
	 * TODO should this be a char array or some kind of splice?
	 */
	public String getImage();

}
