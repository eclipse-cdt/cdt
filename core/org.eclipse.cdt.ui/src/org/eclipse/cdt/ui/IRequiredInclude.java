/*******************************************************************************
 * Copyright (c) 2000, 2004 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     QNX Software Systems - Initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.ui;


public interface IRequiredInclude {

	/**
	 * Returns the name that has been imported. 
	 * For an on-demand import, this includes the trailing <code>".*"</code>.
	 * For example, for the statement <code>"import java.util.*"</code>,
	 * this returns <code>"java.util.*"</code>.
	 * For the statement <code>"import java.util.Hashtable"</code>,
	 * this returns <code>"java.util.Hashtable"</code>.
	 */
	String getIncludeName();

	/**
	 * Returns whether the include is to search on "standard places" like /usr/include first .
	 * An include is standard if it starts with <code>"\<"</code>.
	 */
	boolean isStandard();
}

