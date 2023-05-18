/*******************************************************************************
 * Copyright (c) 2023 Julian Waters.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */

package org.eclipse.cdt.core.dom.parser;

import org.eclipse.cdt.core.dom.ast.IASTExpression;
import org.eclipse.cdt.core.dom.ast.IASTInitializerClause;
import org.eclipse.cdt.internal.core.dom.parser.IBinaryOperator;

public interface IExpressionParser {
	IASTExpression buildExpression(IBinaryOperator leftChain, IASTInitializerClause expr);
}
