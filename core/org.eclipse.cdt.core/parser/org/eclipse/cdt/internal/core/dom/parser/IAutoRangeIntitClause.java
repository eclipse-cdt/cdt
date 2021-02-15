/*******************************************************************************
 * Copyright (c) 2021 Advantest Corporation and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *******************************************************************************/

package org.eclipse.cdt.internal.core.dom.parser;

import org.eclipse.cdt.core.dom.ast.IType;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTUnaryExpression;

public interface IAutoRangeIntitClause extends ICPPASTUnaryExpression, IASTAmbiguityParent {

	void setFallbackType(IType type);

	IType getFallbackType();
}
