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
package org.eclipse.cdt.internal.core.parser.scanner2;

import org.eclipse.cdt.core.dom.ast.IASTTranslationUnit;
import org.eclipse.cdt.core.parser.util.ArrayUtil;

public class DependencyTree implements IASTTranslationUnit.IDependencyTree, IDependencyNodeHost {

    private final String tu_path;
    
    public DependencyTree( String path )
    {
        tu_path = path;
    }

    public String getTranslationUnitPath() {
        return tu_path;
    }

    private IASTInclusionNode [] incs = new IASTInclusionNode[2];
    
    public IASTInclusionNode[] getInclusions() {
        incs = (IASTInclusionNode[]) ArrayUtil.removeNulls( IASTInclusionNode.class, incs );
        return incs;
    }

    public void addInclusionNode(IASTInclusionNode node) {
        incs = (IASTInclusionNode[]) ArrayUtil.append( IASTInclusionNode.class, incs, node );
    }

}
