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
package org.eclipse.cdt.internal.core.dom.parser.c;

import org.eclipse.cdt.core.dom.ast.ASTVisitor;
import org.eclipse.cdt.core.dom.ast.DOMException;
import org.eclipse.cdt.core.dom.ast.IASTName;
import org.eclipse.cdt.core.dom.ast.IASTNode;
import org.eclipse.cdt.core.dom.ast.IBinding;
import org.eclipse.cdt.core.dom.ast.IProblemBinding;
import org.eclipse.cdt.core.dom.ast.IScope;
import org.eclipse.cdt.core.dom.ast.c.CASTVisitor;
import org.eclipse.cdt.core.parser.util.ArrayUtil;
import org.eclipse.cdt.internal.core.dom.parser.IASTAmbiguityParent;
import org.eclipse.cdt.internal.core.dom.parser.IASTInternalScope;

public abstract class CASTAmbiguity extends CASTNode  {

    protected static class CASTNameCollector extends CASTVisitor
    {
        private IASTName[] names = new IASTName[ 2 ];
    	private int namesPos=-1;

        {
            shouldVisitNames = true;
        }
        
        @Override
		public int visit(IASTName name) {
        	if (name != null) {
        		namesPos++;
        		names = (IASTName[]) ArrayUtil.append( IASTName.class, names, name );
        	}
            return PROCESS_CONTINUE;
        }
        
        public IASTName [] getNames()
        {
            names = (IASTName[]) ArrayUtil.removeNullsAfter( IASTName.class, names, namesPos );
        	return names;
        }
    }
    
    
    protected abstract IASTNode [] getNodes();
    
    @Override
	public boolean accept(ASTVisitor visitor) {
		IScope scope= CVisitor.getContainingScope(this);

		IASTNode[] nodez = getNodes();
		int bestIndex = 0;
		int bestValue = Integer.MAX_VALUE;
		for (int i = 0; i < nodez.length; ++i) {
			final IASTNode s = nodez[i];
			s.accept(visitor);

			int issues= 0;
			final CASTNameCollector resolver = new CASTNameCollector();
			s.accept(resolver);
			final IASTName[] names= resolver.getNames();
			for (IASTName name : names) {
				try {
					IBinding b = name.resolveBinding();
					if (b instanceof IProblemBinding) {
						issues++;
					}
				} catch (Exception t) {
					issues++;
				}
				if (issues == bestValue) {
					break;
				}
			}
			if (scope instanceof IASTInternalScope) {
				final IASTInternalScope internalScope = (IASTInternalScope) scope;
				try {
					internalScope.flushCache();
				} catch (DOMException e) {
				}
			}
			if (issues < bestValue) {
				bestValue= issues;
				bestIndex= i;
				if (issues == 0) {
					break;
				}
			}
		}

		IASTAmbiguityParent owner = (IASTAmbiguityParent) getParent();
		owner.replace(this, nodez[bestIndex]);
		
		if (scope instanceof IASTInternalScope) {
			try {
				IASTNode node= ((IASTInternalScope) scope).getPhysicalNode();
				if (node != null) {
					CASTNameCollector nc = new CASTNameCollector();
					node.accept(nc);
					final IASTName[] names= nc.getNames();
					for (IASTName name : names) {
						name.setBinding(null);
					}
				}
			} catch (DOMException e) {
			}
		}
		return true;
	}

}
