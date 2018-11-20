/*******************************************************************************
 * Copyright (c) 2008, 2009 Wind River Systems, Inc. and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *    Markus Schorn - Initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.internal.core.dom.parser.c;

import org.eclipse.cdt.core.dom.ast.IASTBinaryExpression;
import org.eclipse.cdt.core.dom.ast.IASTCastExpression;
import org.eclipse.cdt.internal.core.dom.parser.ASTAmbiguousBinaryVsCastExpression;

public class CASTAmbiguousBinaryVsCastExpression extends ASTAmbiguousBinaryVsCastExpression {

	public CASTAmbiguousBinaryVsCastExpression(IASTBinaryExpression binaryExpr, IASTCastExpression castExpr) {
		super(binaryExpr, castExpr);
	}
}
