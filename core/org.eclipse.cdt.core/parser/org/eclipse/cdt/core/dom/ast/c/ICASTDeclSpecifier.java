/*******************************************************************************
 * Copyright (c) 2004, 2010 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Doug Schaefer (IBM) - Initial API and implementation
 *     Markus Schorn (Wind River Systems)
 *******************************************************************************/
package org.eclipse.cdt.core.dom.ast.c;

import org.eclipse.cdt.core.dom.ast.ASTNodeProperty;
import org.eclipse.cdt.core.dom.ast.IASTAlignmentSpecifier;
import org.eclipse.cdt.core.dom.ast.IASTDeclSpecifier;

/**
 * C extension to IASTDeclSpecifier. (restrict keyword)
 * 
 * @noextend This interface is not intended to be extended by clients.
 * @noimplement This interface is not intended to be implemented by clients.
 */
public interface ICASTDeclSpecifier extends IASTDeclSpecifier {
	/**
	 * @since 5.12
	 */
	public static final ASTNodeProperty ALIGNMENT_SPECIFIER = new ASTNodeProperty(
			"ICASTDeclSpecifier.ALIGNMENT_SPECIFIER - Alignment specifier");  //$NON-NLS-1$
	
	/**
	 * @since 5.1
	 */
	@Override
	public ICASTDeclSpecifier copy();

	@Override
	public IASTAlignmentSpecifier[] getAlignmentSpecifiers();
	
	@Override
	public void setAlignmentSpecifiers(IASTAlignmentSpecifier[] alignmentSpecifiers);
}
