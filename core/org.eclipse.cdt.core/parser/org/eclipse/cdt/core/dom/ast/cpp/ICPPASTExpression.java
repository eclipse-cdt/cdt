/*******************************************************************************
 * Copyright (c) 2012, 2015 Wind River Systems, Inc. and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Markus Schorn - initial API and implementation
 *     Sergey Prigogin (Google)
 *******************************************************************************/ 
package org.eclipse.cdt.core.dom.ast.cpp;

import org.eclipse.cdt.core.dom.ast.IASTExpression;
import org.eclipse.cdt.core.dom.ast.IASTImplicitDestructorName;
import org.eclipse.cdt.core.dom.ast.IASTImplicitDestructorNameOwner;

/**
 * Interface for C++ expressions. Any full-expressions may contain {@link IASTImplicitDestructorName}s of
 * destructors called at the end of the expression to destroy temporaries created by the expression.
 * A full-expression is an expression that is not a subexpression of another expression.
 * 
 * @since 5.10
 * @noextend This interface is not intended to be extended by clients.
 * @noimplement This interface is not intended to be implemented by clients.
 */
public interface ICPPASTExpression
		extends IASTExpression, ICPPASTInitializerClause, IASTImplicitDestructorNameOwner {
}
