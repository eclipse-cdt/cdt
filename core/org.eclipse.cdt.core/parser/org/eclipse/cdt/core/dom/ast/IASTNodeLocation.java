/*******************************************************************************
 * Copyright (c) 2004, 2009 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Doug Schaefer (IBM) - Initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.core.dom.ast;

/**
 * A NodeLocation represents the source location of a given node. Most often
 * this is a file it may be other fancy things like macro expansions.
 * 
 * @noextend This interface is not intended to be extended by clients.
 * @noimplement This interface is not intended to be implemented by clients.
 */
public interface IASTNodeLocation {
	/** @since 5.4 */
	public static final IASTNodeLocation[] EMPTY_ARRAY = {};

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
