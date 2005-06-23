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
package org.eclipse.cdt.internal.core.dom.parser.cpp;

import org.eclipse.cdt.core.dom.ast.IASTDeclaration;
import org.eclipse.cdt.core.dom.ast.IASTNode;
import org.eclipse.cdt.core.parser.util.ArrayUtil;
import org.eclipse.cdt.internal.core.dom.parser.IASTAmbiguousDeclaration;

public class CPPASTAmbiguousDeclaration extends CPPASTAmbiguity implements
        IASTAmbiguousDeclaration {

    protected IASTNode[] getNodes() {
        return getDeclarations();
    }

    private IASTDeclaration [] decls = new IASTDeclaration[2];
    private int declsPos=-1;
    
    public void addDeclaration(IASTDeclaration d) {
    	if (d != null) {
    		declsPos++;
    		decls = (IASTDeclaration[]) ArrayUtil.append(IASTDeclaration.class, decls, d );
    	}
    }

    public IASTDeclaration[] getDeclarations() {
        decls = (IASTDeclaration[]) ArrayUtil.removeNullsAfter( IASTDeclaration.class, decls, declsPos );
    	return decls;
    }

}
