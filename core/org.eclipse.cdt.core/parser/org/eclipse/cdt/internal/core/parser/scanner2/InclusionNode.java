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
package org.eclipse.cdt.internal.core.parser.scanner2;

import org.eclipse.cdt.core.dom.ast.IASTPreprocessorIncludeStatement;
import org.eclipse.cdt.core.dom.ast.IASTTranslationUnit.IDependencyTree.IASTInclusionNode;
import org.eclipse.cdt.core.parser.util.ArrayUtil;

public class InclusionNode implements IASTInclusionNode, IDependencyNodeHost {

    private final IASTPreprocessorIncludeStatement stmt;

    public InclusionNode(IASTPreprocessorIncludeStatement stmt) {
        this.stmt = stmt;
    }

    public IASTPreprocessorIncludeStatement getIncludeDirective() {
        return stmt;
    }

    private IASTInclusionNode [] incs = new IASTInclusionNode[2];
    private int incsPos=-1;
    
    public IASTInclusionNode[] getNestedInclusions() {
        incs = (IASTInclusionNode[]) ArrayUtil.removeNullsAfter( IASTInclusionNode.class, incs, incsPos );
        return incs;
    }

    public void addInclusionNode(IASTInclusionNode node) {
    	if (node != null) {
    		incsPos++;
    		incs = (IASTInclusionNode[]) ArrayUtil.append( IASTInclusionNode.class, incs, node );
    	}
    }
    
    public String toString() {
        return stmt.toString();
    }

}
