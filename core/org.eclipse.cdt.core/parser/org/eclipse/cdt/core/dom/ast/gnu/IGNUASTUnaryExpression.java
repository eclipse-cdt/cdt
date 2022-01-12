/*******************************************************************************
 * Copyright (c) 2005, 2012 IBM Corporation and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *    John Camelon (IBM Rational Software) - Initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.core.dom.ast.gnu;

import org.eclipse.cdt.core.dom.ast.IASTUnaryExpression;

/**
 * @deprecated Replaced by {@link IASTUnaryExpression}.
 * @noreference This interface is not intended to be referenced by clients.
 */
@Deprecated
public interface IGNUASTUnaryExpression extends IASTUnaryExpression {
	/**
	 * @since 5.1
	 */
	@Override
	public IGNUASTUnaryExpression copy();
}
