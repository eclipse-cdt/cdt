/*******************************************************************************
 * Copyright (c) 2013 Sebastian Bauer
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Sebastian Bauer - Initial API and implementation
 ******************************************************************************/

package org.eclipse.cdt.core.dom.ast;

public interface IASTDoxygenTag extends IASTNode {
	/**
	 * Return the name of the tag without the preceding at or backslash sign.
	 *
	 * @return the name
	 */
	String getName();

	/**
	 * Return the value of the tag.
	 *
	 * @return the value of the tag.
	 */
	String getValue();
}
