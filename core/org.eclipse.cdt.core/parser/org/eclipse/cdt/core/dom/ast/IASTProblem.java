/*******************************************************************************
 * Copyright (c) 2004, 2008 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    IBM - Initial API and implementation
 *    Markus Schorn (Wind River Systems)
 *******************************************************************************/
package org.eclipse.cdt.core.dom.ast;

import org.eclipse.cdt.core.parser.IProblem;

/**
 * Interface for problems in the ast tree.
 * @noimplement This interface is not intended to be implemented by clients.
 */
public interface IASTProblem extends IProblem, IASTNode {
	
	/**
	 * @since 5.1
	 */
	public IASTProblem copy();
}
