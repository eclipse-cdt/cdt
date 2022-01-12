/**********************************************************************
 * Copyright (c) 2004, 2015 IBM Corporation and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.core.dom.ast.cpp;

import org.eclipse.cdt.core.dom.ast.ASTNodeProperty;
import org.eclipse.cdt.core.dom.ast.IASTDeclaration;
import org.eclipse.cdt.core.dom.ast.IASTForStatement;
import org.eclipse.cdt.core.dom.ast.IASTImplicitDestructorNameOwner;

/**
 * The C++ 'for' statement.
 *
 * @noextend This interface is not intended to be extended by clients.
 * @noimplement This interface is not intended to be implemented by clients.
 */
public interface ICPPASTForStatement extends IASTForStatement, IASTImplicitDestructorNameOwner {
	public static final ASTNodeProperty CONDITION_DECLARATION = new ASTNodeProperty(
			"org.eclipse.cdt.core.dom.ast.cpp.ICPPASTForStatement"); //$NON-NLS-1$

	public void setConditionDeclaration(IASTDeclaration d);

	public IASTDeclaration getConditionDeclaration();

	/**
	 * @since 5.1
	 */
	@Override
	public ICPPASTForStatement copy();

	/**
	 * @since 5.3
	 */
	@Override
	public ICPPASTForStatement copy(CopyStyle style);
}
