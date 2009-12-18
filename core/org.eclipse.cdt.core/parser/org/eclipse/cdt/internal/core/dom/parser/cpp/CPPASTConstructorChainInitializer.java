/*******************************************************************************
 * Copyright (c) 2004, 2009 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    John Camelon (IBM) - Initial API and implementation
 *    Markus Schorn (Wind River Systems)
 *******************************************************************************/
package org.eclipse.cdt.internal.core.dom.parser.cpp;

import java.util.Arrays;

import org.eclipse.cdt.core.dom.ast.ASTVisitor;
import org.eclipse.cdt.core.dom.ast.IASTCompletionContext;
import org.eclipse.cdt.core.dom.ast.IASTExpression;
import org.eclipse.cdt.core.dom.ast.IASTName;
import org.eclipse.cdt.core.dom.ast.IASTNode;
import org.eclipse.cdt.core.dom.ast.IBinding;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTCompositeTypeSpecifier;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTConstructorChainInitializer;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPConstructor;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPField;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPNamespace;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTCompositeTypeSpecifier.ICPPASTBaseSpecifier;
import org.eclipse.cdt.core.parser.util.ArrayUtil;
import org.eclipse.cdt.internal.core.dom.parser.ASTNode;
import org.eclipse.cdt.internal.core.dom.parser.IASTAmbiguityParent;
import org.eclipse.cdt.internal.core.dom.parser.cpp.semantics.CPPSemantics;

/**
 * For example in the constructor definition <br>
 * <code>
 * Derived() : Base(), field() { <br>
 * }
 * </code><br>
 * {@code Base()} and {@code field()} are the constructor chain initializers.<br>
 */
public class CPPASTConstructorChainInitializer extends ASTNode implements
        ICPPASTConstructorChainInitializer, IASTAmbiguityParent, IASTCompletionContext {

    private IASTName name;
    private IASTExpression value;
	private boolean fIsPackExpansion;

    
    public CPPASTConstructorChainInitializer() {
	}

	public CPPASTConstructorChainInitializer(IASTName memberInitializerid, IASTExpression initializerValue) {
		setMemberInitializerId(memberInitializerid);
		setInitializerValue(initializerValue);
	}

	public CPPASTConstructorChainInitializer copy() {
		CPPASTConstructorChainInitializer copy = new CPPASTConstructorChainInitializer();
		copy.setMemberInitializerId(name == null ? null : name.copy());
		copy.setInitializerValue(value == null ? null : value.copy());
		copy.setOffsetAndLength(this);
		copy.fIsPackExpansion= fIsPackExpansion;
		return copy;
	}
	
	public IASTName getMemberInitializerId() {
        return name;
    }

    public void setMemberInitializerId(IASTName name) {
        assertNotFrozen();
        this.name = name;
        if(name != null) {
			name.setParent(this);
			name.setPropertyInParent(MEMBER_ID);
		}
    }

    public IASTExpression getInitializerValue() {
        return value;
    }


    public void setInitializerValue(IASTExpression expression) {
        assertNotFrozen();
        value = expression;
        if(expression != null) {
			expression.setParent(this);
			expression.setPropertyInParent(INITIALIZER);
		}
    }

    @Override
	public boolean accept(ASTVisitor action) {
    	if (action.shouldVisitInitializers) {
    		switch(action.visit(this)) {
    		case ASTVisitor.PROCESS_ABORT:
    			return false;
    		case ASTVisitor.PROCESS_SKIP:
    			return true;
    		}
    	}
        if (name != null)
            if (!name.accept(action))
                return false;
        if (value != null)
            if (!value.accept(action))
                return false;
        
    	if (action.shouldVisitInitializers) {
    		if (action.leave(this) == ASTVisitor.PROCESS_ABORT)
    			return false;
    	}
        return true;
    }

    public int getRoleForName(IASTName n) {
        if (name == n)
            return r_reference;
        return r_unclear;
    }

    public void replace(IASTNode child, IASTNode other) {
        if (child == value) {
            other.setPropertyInParent(child.getPropertyInParent());
            other.setParent(child.getParent());
            value = (IASTExpression) other;
        }
    }

	public IBinding[] findBindings(IASTName n, boolean isPrefix) {
		IBinding[] bindings = CPPSemantics.findBindingsForContentAssist(n, isPrefix);

		ICPPASTBaseSpecifier[] baseClasses = null;

		for (int i = 0; i < bindings.length; i++) {

			if ((bindings[i] instanceof ICPPField) || (bindings[i] instanceof ICPPNamespace)) {
				continue;
			} else if (bindings[i] instanceof ICPPConstructor) {

				if (baseClasses == null) {
					baseClasses = getBaseClasses(n);
				}
				boolean isBaseClassConstructor = false;
				if (baseClasses != null) {
					for (ICPPASTBaseSpecifier b : baseClasses) {
						char[] bindingName = bindings[i].getNameCharArray();
						char[] baseName = b.getName().getLastName().getSimpleID();
						if (Arrays.equals(bindingName, baseName)) {
							isBaseClassConstructor = true;
							break;
						}
					}
				}

				if (!isBaseClassConstructor) {
					bindings[i] = null;
				}
			} else {
				bindings[i] = null;
			}
		}
		return (IBinding[]) ArrayUtil.removeNulls(IBinding.class, bindings);
	}

	private ICPPASTBaseSpecifier[] getBaseClasses(IASTName name) {
		for (IASTNode parent = name.getParent(); parent != null; parent = parent.getParent()) {
			if (parent instanceof ICPPASTCompositeTypeSpecifier) {
				ICPPASTCompositeTypeSpecifier specifier = (ICPPASTCompositeTypeSpecifier) parent;

				return specifier.getBaseSpecifiers();
			}
		}

		return null;
	}
	
	public boolean isPackExpansion() {
		return fIsPackExpansion;
	}

	public void setIsPackExpansion(boolean val) {
		assertNotFrozen();
		fIsPackExpansion= val;
	}
}
