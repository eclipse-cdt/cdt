/*******************************************************************************
 * Copyright (c) 2004, 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * IBM - Initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.core.dom.ast;

/**
 * A NodeLocation represents the source location of a given node. Most often
 * this is a file it may be other fancy things like macro expansions.
 * 
 * @author Doug Schaefer
 */
public interface IASTNodeLocation {

	/**
	 * This is the offset within either the file or a macro-expansion.
	 */
	public int getNodeOffset();

	/**
	 * This is the length of the node within the file or macro-expansion.
	 */
	public int getNodeLength();

    /**
     * Return a file location that best maps into this location.
     */
    public IASTFileLocation asFileLocation();

}
