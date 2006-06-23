/*******************************************************************************
 * Copyright (c) 2000, 2005 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     QNX Software Systems - Initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.core.model;

import org.eclipse.cdt.core.parser.ast.ASTAccessVisibility;


/**
 * Place holder of the inherited class from struct or class(IStructure).
 */
public interface IInheritance {
	/**
	 * Return the inherited structures names.
	 */
	public String[] getSuperClassesNames();
	/**
	 * Returns the super class access : ASTAccessVisibility 
	 */
	public ASTAccessVisibility getSuperClassAccess(String name);
}
