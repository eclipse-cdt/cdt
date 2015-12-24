/*******************************************************************************
 * Copyright (c) 2005, 2015 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     John Camelon (IBM Rational Software) - Initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.core.dom.ast.c;

import org.eclipse.cdt.core.dom.ast.IASTNode;

/**
 * Base interface for all C-style designators.
 * 
 * @noextend This interface is not intended to be extended by clients.
 * @noimplement This interface is not intended to be implemented by clients.
 */
public interface ICASTDesignator extends IASTNode {
	/**
	 * @since 5.1
	 */
	@Override
	public ICASTDesignator copy();

	/**
	 * @since 5.3
	 */
	@Override
	public ICASTDesignator copy(CopyStyle style);
}
