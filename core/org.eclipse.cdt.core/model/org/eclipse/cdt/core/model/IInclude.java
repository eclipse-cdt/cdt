/*******************************************************************************
 * Copyright (c) 2000, 2009 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     QNX Software Systems - Initial API and implementation
 *     Anton Leherbauer (Wind River Systems)
 *******************************************************************************/
package org.eclipse.cdt.core.model;

/**
 * Represents an include declaration in a C translation unit.
 * 
 * @noextend This interface is not intended to be extended by clients.
 * @noimplement This interface is not intended to be implemented by clients.
 */
public interface IInclude extends ICElement, ISourceReference, ISourceManipulation {
	/**
	 * Returns the name that of the included file. 
	 * For example, for the statement {@code #include <stdio.h>},
	 * this returns {@code "stdio.h"}.
	 */
	public String getIncludeName();

	/**
	 * Returns whether the included was search on "standard places" like /usr/include first .
	 * An include is standard if it starts with {@code '<'}.
	 * For example, {@code #include <stdio.h>} returns {@code true} and
	 * {@code #include "foobar.h"} returns {@code false}.
	 */
	public boolean isStandard();
	
	/**
	 * The inverse of {@link #isStandard()}
	 */
	public boolean isLocal(); 
	
	public String getFullFileName(); 

	/**
	 * @return whether this include directive was resolved and followed.
	 */
	boolean isResolved();
}
