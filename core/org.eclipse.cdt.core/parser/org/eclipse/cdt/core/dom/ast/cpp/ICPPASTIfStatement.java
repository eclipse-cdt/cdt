/*******************************************************************************
 * Copyright (c) 2004, 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * IBM - Initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.core.dom.ast.cpp;

import org.eclipse.cdt.core.dom.ast.IASTDeclaration;
import org.eclipse.cdt.core.dom.ast.IASTIfStatement;
import org.eclipse.cdt.core.dom.ast.IScope;

public interface ICPPASTIfStatement extends IASTIfStatement {

    public IASTDeclaration getConditionDeclaration();
    public void setConditionDeclaration( IASTDeclaration d );
    
    /**
	 * Get the implicit <code>IScope</code> represented by this if statement
	 * 
	 * @return <code>IScope</code>
	 */
	public IScope getScope();
}
