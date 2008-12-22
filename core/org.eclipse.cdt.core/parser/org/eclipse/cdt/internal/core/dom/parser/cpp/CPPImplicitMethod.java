/*******************************************************************************
 * Copyright (c) 2004, 2008 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Andrew Niefer (IBM Corporation) - initial API and implementation
 *     Markus Schorn (Wind River Systems)
 *******************************************************************************/
package org.eclipse.cdt.internal.core.dom.parser.cpp;

import org.eclipse.cdt.core.dom.ast.DOMException;
import org.eclipse.cdt.core.dom.ast.IASTCompositeTypeSpecifier;
import org.eclipse.cdt.core.dom.ast.IASTDeclaration;
import org.eclipse.cdt.core.dom.ast.IASTDeclarator;
import org.eclipse.cdt.core.dom.ast.IASTFunctionDefinition;
import org.eclipse.cdt.core.dom.ast.IASTName;
import org.eclipse.cdt.core.dom.ast.IASTSimpleDeclaration;
import org.eclipse.cdt.core.dom.ast.IBasicType;
import org.eclipse.cdt.core.dom.ast.IBinding;
import org.eclipse.cdt.core.dom.ast.IFunctionType;
import org.eclipse.cdt.core.dom.ast.IParameter;
import org.eclipse.cdt.core.dom.ast.IType;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTCompositeTypeSpecifier;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTFunctionDeclarator;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTTemplateDeclaration;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTVisibilityLabel;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPClassScope;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPClassType;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPFunctionType;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPMethod;
import org.eclipse.cdt.core.parser.util.CharArrayUtils;
import org.eclipse.cdt.internal.core.dom.parser.ASTInternal;
import org.eclipse.cdt.internal.core.dom.parser.cpp.semantics.CPPVisitor;

/**
 * Binding for implicit methods, base class for implicit constructors.
 */
public class CPPImplicitMethod extends CPPImplicitFunction implements ICPPMethod {
    
    public CPPImplicitMethod(ICPPClassScope scope, char[] name, ICPPFunctionType type, IParameter[] params) {
		super(name, scope, type, params, false);
	}
   
	public int getVisibility() throws DOMException {
		IASTDeclaration decl = getPrimaryDeclaration();
		if( decl == null ) {
		    //12.1-5, 12.8-10 Implicitl constructors and assignment operators are public
		    return ICPPASTVisibilityLabel.v_public;
		} 
		
        IASTCompositeTypeSpecifier cls = (IASTCompositeTypeSpecifier) decl.getParent();
        IASTDeclaration [] members = cls.getMembers();
        ICPPASTVisibilityLabel vis = null;
        for (IASTDeclaration member : members) {
            if( member instanceof ICPPASTVisibilityLabel )
                vis = (ICPPASTVisibilityLabel) member;
            else if( member == decl )
                break;
        }
        if( vis != null ){
            return vis.getVisibility();
        } else if( cls.getKey() == ICPPASTCompositeTypeSpecifier.k_class ){
            return ICPPASTVisibilityLabel.v_private;
        } 
        return ICPPASTVisibilityLabel.v_public;
    }
	
	public ICPPClassType getClassOwner() {
		ICPPClassScope scope = (ICPPClassScope)getScope();
		return scope.getClassType();
	}
	
	public IASTDeclaration getPrimaryDeclaration() throws DOMException{
		//first check if we already know it
		if( declarations != null ){
			for (ICPPASTFunctionDeclarator dtor : declarations) {
				if (dtor == null) 
					break;
				
				IASTDeclaration decl= (IASTDeclaration) CPPVisitor.findOutermostDeclarator(dtor).getParent();
				if( decl.getParent() instanceof ICPPASTCompositeTypeSpecifier )
					return decl;
			}
		}
		
		IFunctionType ftype = getType();
		IType [] params = ftype.getParameterTypes();
		
		ICPPASTCompositeTypeSpecifier compSpec = (ICPPASTCompositeTypeSpecifier) ASTInternal.getPhysicalNodeOfScope(getScope());
		if (compSpec == null) {
			return null; 
		}
		IASTDeclaration [] members = compSpec.getMembers();
		for (IASTDeclaration member : members) {
			IASTDeclarator dtor = null;
			IASTDeclarator [] ds = null;
			int di = -1;
			
			if( member instanceof ICPPASTTemplateDeclaration )
			    member = ((ICPPASTTemplateDeclaration) member).getDeclaration();
			if( member instanceof IASTSimpleDeclaration ){
				ds = ((IASTSimpleDeclaration)member).getDeclarators();
			} else if( member instanceof IASTFunctionDefinition ){
				dtor = ((IASTFunctionDefinition) member).getDeclarator();
			}
			if( ds != null && ds.length > 0 ){
				di = 0;
				dtor = ds[0];
				while( dtor != null ){
					IASTName name = CPPVisitor.findInnermostDeclarator(dtor).getName();
					if( CPPVisitor.findTypeRelevantDeclarator(dtor) instanceof ICPPASTFunctionDeclarator &&
							CharArrayUtils.equals( name.getLookupKey(), getNameCharArray() ) )
					{
						IType t0= CPPVisitor.createType( dtor );
						boolean ok= false;
						if (t0 instanceof IFunctionType) {
							IFunctionType t = (IFunctionType) t0;
							IType [] ps = t.getParameterTypes();
							if( ps.length == params.length ){
								int idx = 0;
								for( ; idx < ps.length && ps[idx] != null; idx++ ){
									if( !ps[idx].isSameType(params[idx]) )
										break;
								}
								ok= idx == ps.length;
							}
							else if (ps.length == 0) {
								if (params.length == 1) {
									IType t1= params[0];
									ok = (t1 instanceof IBasicType) && ((IBasicType) t1).getType() == IBasicType.t_void;
								}
							}
						}
						else {
							ok= false;
						}
						if (ok) {
							name.setBinding( this );
							if( member instanceof IASTSimpleDeclaration )
								addDeclaration( dtor );
							else if( member instanceof IASTFunctionDefinition )
								addDefinition( dtor );
							return member;
						}
					}
					dtor = ( di > -1 && ++ di < ds.length ) ? ds[di] : null;
				}
			}
		}
		return null;
	}

    public boolean isVirtual() {
        return false;
    }

	public boolean isDestructor() {
		char [] n = getNameCharArray();
		if( n != null && n.length > 0 )
			return n[0] == '~';
		return false;
	}

	public boolean isImplicit() {
		try {
			return getPrimaryDeclaration() == null;
		} catch (DOMException e) {
		}
		return true;
	}
	
	public boolean isPureVirtual() {
		return false;
	}

	@Override
	public IBinding getOwner() {
		return getClassOwner();
	}

	@Override
	public IType[] getExceptionSpecification() throws DOMException {
		return ClassTypeHelper.getInheritedExceptionSpecification(this);
	}
}
