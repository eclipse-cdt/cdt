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
package org.eclipse.cdt.core.dom.ast;

/**
 * A NodeLocation represents the source location of a given node. Most
 * often this is a file it may be other fancy things like macro
 * expansions.
 * 
 * @author Doug Schaefer
 */
public interface IASTNodeLocation {

	/**
	 * This is the offset into the actual source location that this node
	 * starts at.
	 * 
	 * @return
	 */
	public int getNodeOffset();
	
	/**
	 * This is the length of the node contained in this location.
	 * 
	 * @return
	 */
	public int getNodeLength();

}
