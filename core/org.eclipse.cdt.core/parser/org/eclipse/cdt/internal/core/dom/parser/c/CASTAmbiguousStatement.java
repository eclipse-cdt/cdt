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

import org.eclipse.cdt.core.dom.ast.IASTNode;
import org.eclipse.cdt.core.dom.ast.IASTStatement;
import org.eclipse.cdt.core.parser.util.ArrayUtil;
import org.eclipse.cdt.internal.core.dom.parser.IASTAmbiguousStatement;

public class CASTAmbiguousStatement extends CASTAmbiguity implements
        IASTAmbiguousStatement {

    private IASTStatement [] stmts = new IASTStatement[2];
    
    public void addStatement(IASTStatement s) {
        stmts = (IASTStatement[]) ArrayUtil.append( IASTStatement.class, stmts, s );
    }

    public IASTStatement[] getStatements() {
        return (IASTStatement[]) ArrayUtil.removeNulls( IASTStatement.class, stmts );
    }

    protected IASTNode[] getNodes() {
        return getStatements();
    }


}
