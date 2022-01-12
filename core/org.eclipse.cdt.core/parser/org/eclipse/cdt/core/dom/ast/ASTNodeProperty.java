/*******************************************************************************
 * Copyright (c) 2004, 2012 IBM Corporation and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 * 	   Doug Schaefer (IBM) - Initial API and implementation
 * 	   Mike Kucera - cleanup
 *******************************************************************************/
package org.eclipse.cdt.core.dom.ast;

/**
 * This interface represents a structural property in an IASTNode. This is used
 * to determine the relationship between a child node and it's parent. This is
 * especially important with rewrite since we need to understand how to properly
 * replace the child in the source.
 *
 * @noextend This interface is not intended to be extended by clients.
 * @noinstantiate This class is not intended to be instantiated by clients.
 */
public class ASTNodeProperty {
	private final String name;

	public ASTNodeProperty(String n) {
		this.name = n;
	}

	/**
	 * Each property has a name to help distinguish it from other properties of
	 * a node.
	 *
	 * @return the name of the property
	 */
	public String getName() {
		return name;
	}

	@Override
	public String toString() {
		return name;
	}
}
