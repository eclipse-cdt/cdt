/**********************************************************************
 * Copyright (c) 2004 IBM Corporation and others.
 * All rights reserved.   This program and the accompanying materials
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors: 
 * IBM - Initial API and implementation
 **********************************************************************/
package org.eclipse.cdt.internal.core.dom.parser.c;

import org.eclipse.cdt.core.dom.ast.IASTExpression;
import org.eclipse.cdt.core.dom.ast.IASTNode;
import org.eclipse.cdt.core.parser.util.ArrayUtil;
import org.eclipse.cdt.internal.core.dom.parser.IASTAmbiguousExpression;

public class CASTAmbiguousExpression extends CASTAmbiguity implements
        IASTAmbiguousExpression {

    private IASTExpression [] expressions = new IASTExpression[2];

    public void addExpression(IASTExpression e) {
        expressions = (IASTExpression[]) ArrayUtil.append( IASTExpression.class, expressions, e );
    }

    public IASTExpression[] getExpressions() {
        return (IASTExpression[]) ArrayUtil.removeNulls( IASTExpression.class, expressions );
    }

    protected IASTNode[] getNodes() {
        return getExpressions();
    }


}
