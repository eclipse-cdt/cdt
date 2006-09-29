/*******************************************************************************
 * Copyright (c) 2006 Wind River Systems, Inc. and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Markus Schorn - initial API and implementation
 *******************************************************************************/ 

package org.eclipse.cdt.core.index;

import org.eclipse.cdt.core.dom.IName;


/**
 * Interface for all the names in the index. These constitute either a 
 * declaration or a reference.
 * @since 4.0
 */
public interface IIndexName extends IName {

	public static final IIndexName[] EMPTY_NAME_ARRAY = new IIndexName[0];

	/**
	 * Returns the location of the file the name resides in.
	 * @since 4.0
	 */
	public String getFileName();

	/**
	 * Returns the character offset of the location of the name.
	 * @since 4.0
	 */
	public int getNodeOffset();

	/**
	 * Returns the length of the name.
	 * @since 4.0
	 */
	public int getNodeLength();
}
