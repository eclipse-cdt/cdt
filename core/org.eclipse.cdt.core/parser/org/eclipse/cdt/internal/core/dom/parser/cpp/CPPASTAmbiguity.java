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
package org.eclipse.cdt.internal.core.dom.parser.cpp;

import java.util.Arrays;

import org.eclipse.cdt.core.dom.ast.ASTVisitor;
import org.eclipse.cdt.core.dom.ast.DOMException;
import org.eclipse.cdt.core.dom.ast.IASTName;
import org.eclipse.cdt.core.dom.ast.IASTNode;
import org.eclipse.cdt.core.dom.ast.IBinding;
import org.eclipse.cdt.core.dom.ast.IProblemBinding;
import org.eclipse.cdt.core.dom.ast.IScope;
import org.eclipse.cdt.core.dom.ast.cpp.CPPASTVisitor;
import org.eclipse.cdt.core.parser.util.ArrayUtil;
import org.eclipse.cdt.internal.core.dom.parser.IASTAmbiguityParent;

public abstract class CPPASTAmbiguity extends CPPASTNode {

//    private static final boolean debugging = false;

    protected static class CPPASTNameCollector extends CPPASTVisitor {
        private IASTName[] names = new IASTName[2];
        private int namesPos=-1;
        {
            shouldVisitNames = true;
        }

        public int visit(IASTName name) {
        	if (name != null) {
        		namesPos++;
        		names = (IASTName[]) ArrayUtil.append(IASTName.class, names, name);
        	}
            return PROCESS_CONTINUE;
        }

        public IASTName[] getNames() {
            names = (IASTName[]) ArrayUtil.removeNullsAfter(IASTName.class, names, namesPos);
        	return names;
        }
    }

    protected abstract IASTNode[] getNodes();

    public boolean accept(ASTVisitor visitor) {
        IASTNode[] nodez = getNodes();
//        if( debugging  ) 
//            printNode();
        int[] issues = new int[nodez.length];
        Arrays.fill(issues, 0);
        for (int i = 0; i < nodez.length; ++i) {
            IASTNode s = nodez[i];
            s.accept( visitor );
            CPPASTNameCollector resolver = new CPPASTNameCollector();
            s.accept(resolver);
            IASTName[] names = resolver.getNames();
            for (int j = 0; j < names.length; ++j) {
                try {
                    IBinding b = names[j].resolveBinding();
                    if (b == null || b instanceof IProblemBinding)
                        ++issues[i];
                } catch (Throwable t) {
                    ++issues[i];
                }
            }
            if (names.length > 0) {
                IScope scope = CPPVisitor.getContainingScope(names[0]);
                try {
                    scope.flushCache();
                } catch (DOMException de) {}
            }
        }
        int bestIndex = 0;
        int bestValue = issues[0];
        for (int i = 1; i < issues.length; ++i) {
            if (issues[i] < bestValue) {
                bestIndex = i;
                bestValue = issues[i];
            }
        }

        IASTAmbiguityParent owner = (IASTAmbiguityParent) getParent();
        owner.replace(this, nodez[bestIndex]);
        return true;
    }

//    protected void printNode() {
//        System.out.println( "Ambiguity " + getClass().getName() + ": ");
//        IASTNode [] nodes = getNodes();
//        for( int i = 0; i < nodes.length; ++i )
//        {
//            System.out.print( "\t" + i + " : " );
//            System.out.println( ASTSignatureUtil.getNodeSignature(nodes[i]) );
//        }
//    }

}
