/*******************************************************************************
 * Copyright (c) 2004, 2007 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * IBM - Initial API and implementation
 * Bryan Wilkinson (QNX)
 *******************************************************************************/
package org.eclipse.cdt.internal.core.dom.parser.cpp;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.cdt.core.dom.ast.ASTVisitor;
import org.eclipse.cdt.core.dom.ast.DOMException;
import org.eclipse.cdt.core.dom.ast.IASTCompletionContext;
import org.eclipse.cdt.core.dom.ast.IASTExpression;
import org.eclipse.cdt.core.dom.ast.IASTName;
import org.eclipse.cdt.core.dom.ast.IASTNode;
import org.eclipse.cdt.core.dom.ast.IBinding;
import org.eclipse.cdt.core.dom.ast.IField;
import org.eclipse.cdt.core.dom.ast.IType;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTFieldReference;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPBase;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPClassType;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPConstructor;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPMethod;
import org.eclipse.cdt.core.parser.util.CharArrayUtils;
import org.eclipse.cdt.internal.core.dom.parser.IASTAmbiguityParent;

/**
 * @author jcamelon
 */
public class CPPASTFieldReference extends CPPASTNode implements
        ICPPASTFieldReference, IASTAmbiguityParent, IASTCompletionContext {

    private boolean isTemplate;
    private IASTExpression owner;
    private IASTName name;
    private boolean isDeref;

    public boolean isTemplate() {
        return isTemplate;
    }

    public void setIsTemplate(boolean value) {
        isTemplate = value;
    }

    public IASTExpression getFieldOwner() {
        return owner;
    }

    public void setFieldOwner(IASTExpression expression) {
        owner = expression;
    }

    public IASTName getFieldName() {
        return name;
    }

    public void setFieldName(IASTName name) {
        this.name =name;
    }

    public boolean isPointerDereference() {
        return isDeref;
    }

    public void setIsPointerDereference(boolean value) {
        isDeref = value;
    }
    
    public boolean accept( ASTVisitor action ){
        if( action.shouldVisitExpressions ){
		    switch( action.visit( this ) ){
	            case ASTVisitor.PROCESS_ABORT : return false;
	            case ASTVisitor.PROCESS_SKIP  : return true;
	            default : break;
	        }
		}
      
        if( owner != null ) if( !owner.accept( action ) ) return false;
        if( name != null )  if( !name.accept( action ) ) return false;
        
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
		if( n == name )
			return r_reference;
		return r_unclear;
	}

    public void replace(IASTNode child, IASTNode other) {
        if( child == owner )
        {
            other.setPropertyInParent( child.getPropertyInParent() );
            other.setParent( child.getParent() );
            owner  = (IASTExpression) other;
        }
    }

    public IType getExpressionType() {
    	return CPPVisitor.getExpressionType(this);
    }

	public IBinding[] findBindings(IASTName n, boolean isPrefix) {
		IASTExpression expression = getFieldOwner();
		IType type = expression.getExpressionType();
		type = CPPSemantics.getUltimateType(type, true); //stop at pointer to member?
		
		if (type instanceof ICPPClassType) {
			ICPPClassType classType = (ICPPClassType) type;
			List bindings = new ArrayList();
			char[] name = n.toCharArray();
			
			try {
				IField[] fields = classType.getFields();
				if (fields != null) {
					for (int i = 0; i < fields.length; i++) {
						char[] potential = fields[i].getNameCharArray();
						if (nameMatches(potential, name, isPrefix)) {
							bindings.add(fields[i]);
						}
					}
				}
			} catch (DOMException e) {
			}
			
			try {
				ICPPMethod[] methods = classType.getMethods();
				if (methods != null) {
					for (int i = 0; i < methods.length; i++) {
						if (!(methods[i] instanceof ICPPConstructor) && !methods[i].isImplicit()) {
							char[] potential = methods[i].getNameCharArray();
							if (nameMatches(potential, name, isPrefix)) {
								bindings.add(methods[i]);
							}
						}
					}
				}
			} catch (DOMException e) {
			}
			
			collectBases(classType, bindings, n.toCharArray(), isPrefix);
			return (IBinding[]) bindings.toArray(new IBinding[bindings.size()]);
		}
		
		return null;
	}
    
	private void collectBases(ICPPClassType classType, List bindings, char[] name, boolean isPrefix) {
		if (nameMatches(classType.getNameCharArray(), name, isPrefix)) {
			bindings.add(classType);
		}
		
		try {
			ICPPBase[] bases = classType.getBases();
			for (int i = 0; i < bases.length; i++) {
				IBinding base = bases[i].getBaseClass();
				if (base instanceof ICPPClassType) {
					ICPPClassType baseClass = (ICPPClassType) base;
					collectBases(baseClass, bindings, name, isPrefix);
				}
			}
		} catch (DOMException e) {
		}
	}
	
	private boolean nameMatches(char[] potential, char[] name, boolean isPrefix) {
		if (isPrefix) {
			return CharArrayUtils.equals(potential, 0, name.length, name, false);
		} else {
			return CharArrayUtils.equals(potential, name);
		}
	}
}
