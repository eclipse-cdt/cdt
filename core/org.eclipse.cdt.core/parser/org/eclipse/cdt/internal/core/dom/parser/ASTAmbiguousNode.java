/*******************************************************************************
 * Copyright (c) 2004, 2009 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    IBM - Initial API and implementation
 *    Markus Schorn (Wind River Systems)
 *******************************************************************************/
package org.eclipse.cdt.internal.core.dom.parser;

import org.eclipse.cdt.core.dom.ast.ASTVisitor;
import org.eclipse.cdt.core.dom.ast.DOMException;
import org.eclipse.cdt.core.dom.ast.IASTIdExpression;
import org.eclipse.cdt.core.dom.ast.IASTName;
import org.eclipse.cdt.core.dom.ast.IASTNode;
import org.eclipse.cdt.core.dom.ast.IBinding;
import org.eclipse.cdt.core.dom.ast.IProblemBinding;
import org.eclipse.cdt.core.dom.ast.IScope;
import org.eclipse.cdt.core.dom.ast.IType;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTTemplatedTypeTemplateParameter;
import org.eclipse.cdt.core.parser.util.ArrayUtil;

/**
 * Base implementation for all ambiguous nodes.
 */
public abstract class ASTAmbiguousNode extends ASTNode  {

    public static class NameCollector extends ASTVisitor {
		private IASTName[] names = new IASTName[2];
		private int namesPos = -1;

		public NameCollector() {
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

    /**
     * Return the alternative nodes for this ambiguity.
     */
    protected abstract IASTNode[] getNodes();

    /**
     * Returns the scope that may get polluted by alternatives of this ambiguity.
     */
    protected abstract IScope getAffectedScope();
    
    @Override
	public final boolean accept(ASTVisitor visitor) {
    	if (visitor.shouldVisitAmbiguousNodes && visitor.visit(this) == ASTVisitor.PROCESS_ABORT)
    		return false;
    		
    	// alternatives are not visited on purpose.
    	return true;
    }
    
    public void resolveAmbiguity(ASTVisitor resolver) {
		final IScope scope= getAffectedScope();
		final IASTAmbiguityParent owner= (IASTAmbiguityParent) getParent();
		IASTNode nodeToReplace= this;

		final IASTNode[] alternatives= getNodes();
		IASTNode bestAlternative= null;
		
		int minIssues = Integer.MAX_VALUE;
		for (IASTNode alternative : alternatives) {
			// flush scope, even if this is the first alternative. The ambiguous node may have contributed an
		    // invalid binding to the scope during the resolution of other ambiguous nodes.
			if (scope instanceof IASTInternalScope) {
				try {
					((IASTInternalScope) scope).flushCache();
				} catch (DOMException e) {
				}
			}

			// setup the ast to use the alternative
			owner.replace(nodeToReplace, alternative);
			nodeToReplace= alternative;

			// handle nested ambiguities first, otherwise we cannot visit the alternative
			alternative.accept(resolver);

			// find nested names
			final NameCollector nameCollector= new NameCollector();
			alternative.accept(nameCollector);
			final IASTName[] names= nameCollector.getNames();
			
			// resolve names and count issues
			int issues= 0;
			for (IASTName name : names) {
				try {
					IBinding b= name.resolvePreBinding();
					if (b instanceof IProblemBinding) {
						issues++;
					} else if (b instanceof IType && name.getPropertyInParent() == IASTIdExpression.ID_NAME) {
			        	// mstodo misused types, should be part of postResolution.
			        	IASTNode grand= name.getParent().getParent();
			        	if (!(grand instanceof ICPPASTTemplatedTypeTemplateParameter)) {
			        		issues++;
			        	}
					}
				} catch (Exception t) {
					issues++;
				}
				if (issues == minIssues) {
					break;
				}
			}
			if (issues < minIssues) {
				minIssues= issues;
				bestAlternative= alternative;
				if (issues == 0) {
					break;
				}
			}
		}
		
		// switch back to the best alternative, if necessary.
		if (nodeToReplace != bestAlternative) {
			if (scope instanceof IASTInternalScope) {
				try {
					((IASTInternalScope) scope).flushCache();
				} catch (DOMException e) {
				}
			}
			owner.replace(nodeToReplace, bestAlternative);
		}
	}
}
