/*******************************************************************************
 * Copyright (c) 2004, 2008 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    IBM - Initial API and implementation
 *    Markus Schorn (Wind River Systems)
 *******************************************************************************/
package org.eclipse.cdt.internal.core.dom.parser.cpp;
//no change for leave()
import org.eclipse.cdt.core.dom.ast.ASTVisitor;
import org.eclipse.cdt.core.dom.ast.DOMException;
import org.eclipse.cdt.core.dom.ast.IASTName;
import org.eclipse.cdt.core.dom.ast.IASTNode;
import org.eclipse.cdt.core.dom.ast.IBinding;
import org.eclipse.cdt.core.dom.ast.IProblemBinding;
import org.eclipse.cdt.core.dom.ast.IScope;
import org.eclipse.cdt.core.dom.ast.cpp.CPPASTVisitor;
import org.eclipse.cdt.core.parser.util.ArrayUtil;
import org.eclipse.cdt.internal.core.dom.parser.ASTInternal;
import org.eclipse.cdt.internal.core.dom.parser.IASTAmbiguityParent;

public abstract class CPPASTAmbiguity extends CPPASTNode {

//    private static final boolean debugging = false;

    protected static class CPPASTNameCollector extends CPPASTVisitor {
        private IASTName[] names = new IASTName[2];
        private int namesPos=-1;
        {
            shouldVisitNames = true;
        }

        @Override
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

    @Override
	public boolean accept(ASTVisitor visitor) {
        IASTNode[] nodez = getNodes();
        int[] problems = new int[nodez.length];
        for(int i = 0; i < nodez.length; ++i) {
            IASTNode node = nodez[i];
            node.accept(visitor);
            CPPASTNameCollector nameCollector = new CPPASTNameCollector();
            node.accept(nameCollector);
            IASTName[] names = nameCollector.getNames();
            for(IASTName name : names) {
                try {
                    IBinding b = name.resolveBinding();
                    if(b == null || b instanceof IProblemBinding)
                        ++problems[i];
                } catch (Exception t) {
                    ++problems[i];
                }
            }
            if(names.length > 0) {
                IScope scope = CPPVisitor.getContainingScope(names[0]);
                if( scope != null ) {
                    try {
                        ASTInternal.flushCache(scope);
                    } catch (DOMException de) {}
                }
            }
        }
        int bestIndex = 0;
        int bestValue = problems[0];
        for (int i = 1; i < problems.length; ++i) {
            if (problems[i] < bestValue) {
                bestIndex = i;
                bestValue = problems[i];
            }
        }

        IASTAmbiguityParent owner = (IASTAmbiguityParent) getParent();
        owner.replace(this, nodez[bestIndex]);
        return true;
    }

}
