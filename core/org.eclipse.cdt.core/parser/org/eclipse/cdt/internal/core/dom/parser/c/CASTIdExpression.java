/*******************************************************************************
 * Copyright (c) 2005, 2007 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * IBM Rational Software - Initial API and implementation
 * Yuan Zhang / Beth Tibbitts (IBM Research)
 * Bryan Wilkinson (QNX)
 *******************************************************************************/
package org.eclipse.cdt.internal.core.dom.parser.c;

import org.eclipse.cdt.core.dom.ILinkage;
import org.eclipse.cdt.core.dom.ast.ASTVisitor;
import org.eclipse.cdt.core.dom.ast.DOMException;
import org.eclipse.cdt.core.dom.ast.IASTIdExpression;
import org.eclipse.cdt.core.dom.ast.IASTName;
import org.eclipse.cdt.core.dom.ast.IBinding;
import org.eclipse.cdt.core.dom.ast.IScope;
import org.eclipse.cdt.core.dom.ast.IType;
import org.eclipse.cdt.core.index.IIndex;
import org.eclipse.cdt.core.index.IndexFilter;
import org.eclipse.cdt.core.parser.util.ArrayUtil;
import org.eclipse.cdt.internal.core.dom.parser.IASTCompletionContext;
import org.eclipse.core.runtime.CoreException;

/**
 * @author jcamelon
 */
public class CASTIdExpression extends CASTNode implements IASTIdExpression, IASTCompletionContext {

    private IASTName name;

    public IASTName getName() {
        return name;
    }

    public void setName(IASTName name) {
        this.name = name;
    }

    public boolean accept( ASTVisitor action ){
        if( action.shouldVisitExpressions ){
		    switch( action.visit( this ) ){
	            case ASTVisitor.PROCESS_ABORT : return false;
	            case ASTVisitor.PROCESS_SKIP  : return true;
	            default : break;
	        }
		}
      
        if( name != null ) if( !name.accept( action ) ) return false;

        if( action.shouldVisitExpressions ){
		    switch( action.leave( this ) ){
	            case ASTVisitor.PROCESS_ABORT : return false;
	            case ASTVisitor.PROCESS_SKIP  : return true;
	            default : break;
	        }
		}
        return true;
    }

	public int getRoleForName(IASTName n) {
		if( n == name )	return r_reference;
		return r_unclear;
	}
	
	public IType getExpressionType() {
		return CVisitor.getExpressionType(this);
	}
	
	public IBinding[] resolvePrefix(IASTName n) {
		IScope scope = CVisitor.getContainingScope(n);
		
		IBinding[] b1 = null;
		if (scope != null) {
			try {
				b1 = scope.find(n.toString(), true);
			} catch (DOMException e) {
			}	
		}
		
		IIndex index = getTranslationUnit().getIndex();
		
		IBinding[] b2 = null;
		if (index != null) {
			try {
				b2 = index.findBindingsForPrefix(
						n.toCharArray(),
						IndexFilter.getFilter(ILinkage.C_LINKAGE_ID));
			} catch (CoreException e) {
			}
		}
		
		int size = (b1 == null ? 0 : b1.length) + (b2 == null ? 0 : b2.length);
		IBinding[] all = new IBinding[size];
		if (b1 != null) ArrayUtil.addAll(IBinding.class, all, b1);
		if (b2 != null) ArrayUtil.addAll(IBinding.class, all, b2);
		return all;
	}
}
