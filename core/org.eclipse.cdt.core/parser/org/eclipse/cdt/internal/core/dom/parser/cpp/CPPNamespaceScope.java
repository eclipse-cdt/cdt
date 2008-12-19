/*******************************************************************************
 * Copyright (c) 2004, 2008 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     Markus Schorn (Wind River Systems)
 *******************************************************************************/
package org.eclipse.cdt.internal.core.dom.parser.cpp;

import org.eclipse.cdt.core.dom.IName;
import org.eclipse.cdt.core.dom.ast.DOMException;
import org.eclipse.cdt.core.dom.ast.EScopeKind;
import org.eclipse.cdt.core.dom.ast.IASTDeclaration;
import org.eclipse.cdt.core.dom.ast.IASTNode;
import org.eclipse.cdt.core.dom.ast.IASTTranslationUnit;
import org.eclipse.cdt.core.dom.ast.IScope;
import org.eclipse.cdt.core.dom.ast.cpp.CPPASTVisitor;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTLinkageSpecification;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTNamespaceDefinition;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPNamespaceScope;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPUsingDirective;
import org.eclipse.cdt.core.parser.util.ArrayUtil;
import org.eclipse.cdt.internal.core.dom.parser.cpp.semantics.CPPSemantics;
import org.eclipse.cdt.internal.core.index.IIndexScope;

/**
 * @author aniefer
 */
public class CPPNamespaceScope extends CPPScope implements ICPPNamespaceScope{
	ICPPUsingDirective[] usings = null;
	
    public CPPNamespaceScope( IASTNode physicalNode ) {
		super( physicalNode );
	}

	public EScopeKind getKind() {
		if (getPhysicalNode() instanceof IASTTranslationUnit)
			return  EScopeKind.eGlobal;
		
		return EScopeKind.eNamespace;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.dom.ast.cpp.ICPPNamespaceScope#getUsingDirectives()
	 */
	public ICPPUsingDirective[] getUsingDirectives() throws DOMException {
		if (!isFullyCached()) {
			CPPSemantics.lookupInScope(null, this, null);
		}
		return (ICPPUsingDirective[]) ArrayUtil.trim( ICPPUsingDirective.class, usings, true );
	}
	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.dom.ast.cpp.ICPPNamespaceScope#addUsingDirective(org.eclipse.cdt.core.dom.ast.cpp.ICPPASTUsingDirective)
	 */
	public void addUsingDirective(ICPPUsingDirective directive) {
		usings = (ICPPUsingDirective[]) ArrayUtil.append( ICPPUsingDirective.class, usings, directive );
	}

    /* (non-Javadoc)
     * @see org.eclipse.cdt.core.dom.ast.cpp.ICPPScope#getScopeName()
     */
    @Override
	public IName getScopeName() {
        IASTNode node = getPhysicalNode();
        if( node instanceof ICPPASTNamespaceDefinition ){
            return ((ICPPASTNamespaceDefinition)node).getName();
        }
        return null;
    }

    public IScope findNamespaceScope(IIndexScope scope) {
    	final String[] qname= scope.getScopeBinding().getQualifiedName();
    	final IScope[] result= {null};
    	final CPPASTVisitor visitor= new CPPASTVisitor () {
    		private int depth= 0;
    		{
    			shouldVisitNamespaces= shouldVisitDeclarations= true;
    		}
    		@Override
    		public int visit( IASTDeclaration declaration ){
    			if( declaration instanceof ICPPASTLinkageSpecification )
    				return PROCESS_CONTINUE;
    			return PROCESS_SKIP;
    		}
    		@Override
    		public int visit(ICPPASTNamespaceDefinition namespace) {
    			final String name = namespace.getName().toString();
    			if (name.length() == 0) {
    				return PROCESS_CONTINUE;
    			}
    			if (qname[depth].equals(name)) {
    				if (++depth == qname.length) {
    					result[0]= namespace.getScope();
    					return PROCESS_ABORT;
    				}
    				return PROCESS_CONTINUE;
    			}
    			return PROCESS_SKIP;
    		}
    		@Override
    		public int leave(ICPPASTNamespaceDefinition namespace) {
    			if (namespace.getName().getSimpleID().length > 0) {
    				--depth;
    			}
    			return PROCESS_CONTINUE;
    		}
    	};
    	getPhysicalNode().accept(visitor);
    	return result[0];
    }
}
