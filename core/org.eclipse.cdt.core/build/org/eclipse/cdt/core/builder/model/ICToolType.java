/**********************************************************************
 * Copyright (c) 2002,2003 Timesys Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors: 
 * Timesys - Initial API and implementation
 **********************************************************************/

package org.eclipse.cdt.core.builder.model;

/**
 * Interface representing a particular tool classification
 * (ex, GNU c compiler, etc.)
 * <p>
 * See also the <a href="../../../../../../CToolType.html">CToolType</a>
 * extension point documentation.
 */
public interface ICToolType {
	/**
	 * Returns the unique id for the tool type.
	 * 
	 * @return unique id.
	 */
	public String getId();

	/**
	 * Returns the name of the tool type.
	 * 
	 * @return provider name.
	 */
	public String getName();
}
