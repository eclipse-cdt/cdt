/*******************************************************************************
 * Copyright (c) 2012, 2016 Google, Inc and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * 	   Sergey Prigogin (Google) - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.internal.core.dom.parser.cpp;

import org.eclipse.cdt.core.dom.ast.IASTNode;

/**
 * Represents a function the return value of which may potentially be calculated at parsing time.
 */
public interface ICPPComputableFunction {
	/**
	 * For a constexpr function returns the return statement expression. Otherwise returns
	 * {@code null}.
	 * @param point the point of instantiation for name lookups
	 */
	public ICPPEvaluation getReturnExpression(IASTNode point);
}
