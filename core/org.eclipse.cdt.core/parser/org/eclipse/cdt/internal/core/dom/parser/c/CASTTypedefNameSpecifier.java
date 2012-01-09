/*******************************************************************************
 * Copyright (c) 2005, 2011 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Rational Software - Initial API and implementation
 *     Yuan Zhang / Beth Tibbitts (IBM Research)
 *     Bryan Wilkinson (QNX)
 *     Anton Leherbauer (Wind River Systems)
 *******************************************************************************/
package org.eclipse.cdt.internal.core.dom.parser.c;

import org.eclipse.cdt.core.dom.ast.ASTVisitor;
import org.eclipse.cdt.core.dom.ast.IASTCompletionContext;
import org.eclipse.cdt.core.dom.ast.IASTName;
import org.eclipse.cdt.core.dom.ast.IBinding;
import org.eclipse.cdt.core.dom.ast.ITypedef;
import org.eclipse.cdt.core.dom.ast.c.ICASTTypedefNameSpecifier;
import org.eclipse.cdt.core.parser.util.ArrayUtil;

/**
 * @author jcamelon
 */
public class CASTTypedefNameSpecifier extends CASTBaseDeclSpecifier implements
        ICASTTypedefNameSpecifier, IASTCompletionContext {

    private IASTName name;

    public CASTTypedefNameSpecifier() {
	}

	public CASTTypedefNameSpecifier(IASTName name) {
		setName(name);
	}

	@Override
	public CASTTypedefNameSpecifier copy() {
		return copy(CopyStyle.withoutLocations);
	}
	
	@Override
	public CASTTypedefNameSpecifier copy(CopyStyle style) {
		CASTTypedefNameSpecifier copy = new CASTTypedefNameSpecifier(name == null ? null
				: name.copy(style));
		copyBaseDeclSpec(copy);
		if (style == CopyStyle.withLocations) {
			copy.setCopyLocation(this);
		}
		return copy;
	}

	@Override
	public IASTName getName() {
        return name;
    }
   
    @Override
	public void setName(IASTName name) {
        assertNotFrozen();
        this.name = name;
        if (name != null) {
			name.setParent(this);
			name.setPropertyInParent(NAME);
		}
    }

    @Override
	public boolean accept( ASTVisitor action ){
        if( action.shouldVisitDeclSpecifiers ){
		    switch( action.visit( this ) ){
	            case ASTVisitor.PROCESS_ABORT : return false;
	            case ASTVisitor.PROCESS_SKIP  : return true;
	            default : break;
	        }
		}
        if( name != null ) if( !name.accept( action ) ) return false;
        if( action.shouldVisitDeclSpecifiers ){
		    switch( action.leave( this ) ){
	            case ASTVisitor.PROCESS_ABORT : return false;
	            case ASTVisitor.PROCESS_SKIP  : return true;
	            default : break;
	        }
		}
        return true;
    }

	@Override
	public int getRoleForName(IASTName n) {
		if( n == name )	return r_reference;
		return r_unclear;
	}

	@Override
	public IBinding[] findBindings(IASTName n, boolean isPrefix) {
		IBinding[] bindings = CVisitor.findBindingsForContentAssist(n, isPrefix);

		for (int i = 0; i < bindings.length; i++) {
			if (!(bindings[i] instanceof ITypedef)) {
				bindings[i]= null;
			}
		}
		
		return ArrayUtil.removeNulls(IBinding.class, bindings);
	}
}
