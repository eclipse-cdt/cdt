/*******************************************************************************
 * Copyright (c) 2008, 2009 Wind River Systems, Inc. and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
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
