/*******************************************************************************
 * Copyright (c) 2005, 2011 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    John Camelon (IBM Rational Software) - Initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.core.dom.ast.c;

import org.eclipse.cdt.core.dom.ast.IASTNamedTypeSpecifier;

/**
 * This interface is just as an IASTNamedTypeSpecifier, except that it also
 * includes the abiliy to use the restrict modifier.
 * 
 * @noextend This interface is not intended to be extended by clients.
 * @noimplement This interface is not intended to be implemented by clients.
 */
public interface ICASTTypedefNameSpecifier extends IASTNamedTypeSpecifier, ICASTDeclSpecifier {

	/**
	 * @since 5.1
	 */
	@Override
	public ICASTTypedefNameSpecifier copy();

	/**
	 * @since 5.3
	 */
	@Override
	public ICASTTypedefNameSpecifier copy(CopyStyle style);
}
