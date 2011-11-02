/*******************************************************************************
 * Copyright (c) 2004, 2011 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     John Camelon (IBM) - Initial API and implementation
 *     Markus Schorn (Wind River Systems)
 *******************************************************************************/
package org.eclipse.cdt.core.dom.ast.cpp;

import org.eclipse.cdt.core.dom.ast.IASTParameterDeclaration;

/**
 * @noextend This interface is not intended to be extended by clients.
 * @noimplement This interface is not intended to be implemented by clients.
 */
public interface ICPPASTParameterDeclaration extends ICPPASTTemplateParameter, IASTParameterDeclaration {
	/**
	 * @since 5.2
	 */
	ICPPASTParameterDeclaration[] EMPTY_CPPPARAMETERDECLARATION_ARRAY = {};

	/**
	 * @since 5.1
	 */
	@Override
	public ICPPASTParameterDeclaration copy();
	
	/**
	 * @since 5.3
	 */
	@Override
	public ICPPASTParameterDeclaration copy(CopyStyle style);

	/**
	 * @since 5.2
	 */
	@Override
	public ICPPASTDeclarator getDeclarator();
}
