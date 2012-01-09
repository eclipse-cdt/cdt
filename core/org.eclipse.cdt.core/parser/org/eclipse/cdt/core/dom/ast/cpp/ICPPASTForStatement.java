/**********************************************************************
 * Copyright (c) 2004, 2011 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.core.dom.ast.cpp;

import org.eclipse.cdt.core.dom.ast.ASTNodeProperty;
import org.eclipse.cdt.core.dom.ast.IASTDeclaration;
import org.eclipse.cdt.core.dom.ast.IASTForStatement;

/**
 * 
 * @noextend This interface is not intended to be extended by clients.
 * @noimplement This interface is not intended to be implemented by clients.
 */
public interface ICPPASTForStatement extends IASTForStatement {
    
    public static final ASTNodeProperty CONDITION_DECLARATION = new ASTNodeProperty( "org.eclipse.cdt.core.dom.ast.cpp.ICPPASTForStatement"); //$NON-NLS-1$
    public void setConditionDeclaration( IASTDeclaration d );
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
