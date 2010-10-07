/*******************************************************************************
 * Copyright (c) 2004, 2010 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    John Camelon (IBM) - Initial API and implementation
 *    Markus Schorn (Wind River Systems)
 *    Sergey Prigogin (Google)
 *******************************************************************************/
package org.eclipse.cdt.internal.core.dom.parser.cpp;

import org.eclipse.cdt.core.dom.ast.ASTVisitor;
import org.eclipse.cdt.core.dom.ast.IASTExpression;
import org.eclipse.cdt.core.dom.ast.IASTImplicitName;
import org.eclipse.cdt.core.dom.ast.IASTImplicitNameOwner;
import org.eclipse.cdt.core.dom.ast.IASTInitializer;
import org.eclipse.cdt.core.dom.ast.IASTName;
import org.eclipse.cdt.core.dom.ast.IASTNode;
import org.eclipse.cdt.core.dom.ast.IBinding;
import org.eclipse.cdt.core.dom.ast.ICPPASTCompletionContext;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTCompositeTypeSpecifier;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTCompositeTypeSpecifier.ICPPASTBaseSpecifier;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTConstructorChainInitializer;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTConstructorInitializer;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTQualifiedName;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPClassType;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPConstructor;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPField;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPNamespace;
import org.eclipse.cdt.core.parser.util.ArrayUtil;
import org.eclipse.cdt.core.parser.util.CharArraySet;
import org.eclipse.cdt.internal.core.dom.parser.ASTNode;
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
        ICPPASTConstructorChainInitializer, IASTImplicitNameOwner, ICPPASTCompletionContext {
    private IASTName name;
	private IASTImplicitName[] implicitNames; 
    private IASTInitializer initializer;
	private boolean fIsPackExpansion;

    public CPPASTConstructorChainInitializer() {
	}

	public CPPASTConstructorChainInitializer(IASTName id, IASTInitializer initializer) {
		setMemberInitializerId(id);
		setInitializer(initializer);
	}

	public CPPASTConstructorChainInitializer copy() {
		CPPASTConstructorChainInitializer copy = new CPPASTConstructorChainInitializer();
		copy.setMemberInitializerId(name == null ? null : name.copy());
		copy.setInitializer(initializer == null ? null : initializer.copy());
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
        if (name != null) {
			name.setParent(this);
			name.setPropertyInParent(MEMBER_ID);
		}
    }

    public IASTInitializer getInitializer() {
        return initializer;
    }

    public void setInitializer(IASTInitializer init) {
        assertNotFrozen();
        initializer = init;
        if (init != null) {
        	init.setParent(this);
        	init.setPropertyInParent(INITIALIZER);
		}
    }

    @Override
	public boolean accept(ASTVisitor action) {
    	if (action.shouldVisitInitializers) {
    		switch (action.visit(this)) {
    		case ASTVisitor.PROCESS_ABORT:
    			return false;
    		case ASTVisitor.PROCESS_SKIP:
    			return true;
    		}
    	}
        if (name != null && !name.accept(action))
        	return false;

        if (action.shouldVisitImplicitNames) {
        	for (IASTImplicitName implicitName : getImplicitNames()) {
        		if (!implicitName.accept(action))
        			return false;
        	}
        }

        if (initializer != null && !initializer.accept(action))
        	return false;

		if (action.shouldVisitInitializers && action.leave(this) == ASTVisitor.PROCESS_ABORT)
			return false;

        return true;
    }

    public int getRoleForName(IASTName n) {
        if (name == n)
            return r_reference;
        return r_unclear;
    }

	public IBinding[] findBindings(IASTName n, boolean isPrefix, String[] namespaces) {
		IBinding[] bindings = CPPSemantics.findBindingsForContentAssist(n, isPrefix, namespaces);

		CharArraySet baseClasses = null;
		for (int i = 0; i < bindings.length; i++) {
			final IBinding b = bindings[i];
			if ((b instanceof ICPPField) || (b instanceof ICPPNamespace)) {
				// OK, keep binding.
			} else if (b instanceof ICPPConstructor || b instanceof ICPPClassType) {
				if (baseClasses == null) 
					baseClasses = getBaseClasses(n);
				
				if (!baseClasses.containsKey(b.getNameCharArray())) {
					bindings[i] = null;
				}
			} else {
				bindings[i] = null;
			}
		}
		return (IBinding[]) ArrayUtil.removeNulls(IBinding.class, bindings);
	}

	private CharArraySet getBaseClasses(IASTName name) {
		CharArraySet result= new CharArraySet(2);
		for (IASTNode parent = name.getParent(); parent != null; parent = parent.getParent()) {
			if (parent instanceof ICPPASTCompositeTypeSpecifier) {
				ICPPASTCompositeTypeSpecifier specifier = (ICPPASTCompositeTypeSpecifier) parent;
				for (ICPPASTBaseSpecifier bs : specifier.getBaseSpecifiers()) {
					result.put(bs.getName().getLastName().getSimpleID());
				} 
			}
		}
		return result;
	}

	public boolean isPackExpansion() {
		return fIsPackExpansion;
	}

	public void setIsPackExpansion(boolean val) {
		assertNotFrozen();
		fIsPackExpansion= val;
	}

	@Deprecated
    public IASTExpression getInitializerValue() {
        if (initializer == null || initializer instanceof IASTExpression) {
        	return (IASTExpression) initializer;
        }
        if (initializer instanceof ICPPASTConstructorInitializer) {
       		IASTExpression expr= ((ICPPASTConstructorInitializer) initializer).getExpression();
       		if (expr != null) {
       			expr= expr.copy();
       			expr.setParent(this);
       			expr.setPropertyInParent(INITIALIZER);
       		}
       		return expr;
        }
        return null;
    }

	@Deprecated
    public void setInitializerValue(IASTExpression expression) {
        assertNotFrozen();
        //CDT_70_FIX_FROM_50-#6
        CPPASTConstructorInitializer ctorInit= new CPPASTConstructorInitializer();
        if (expression == null) {
        	//add an empty initializer, fix test testBug89539 for xlc parser
        	setInitializer(ctorInit);
        } else if (expression instanceof IASTInitializer) {
        	setInitializer((IASTInitializer) expression);
        } else {
        	
        	ctorInit.setExpression(expression);
        	ctorInit.setOffsetAndLength((ASTNode) expression);
        	setInitializer(ctorInit);
        }
    }

	/**
	 * @see IASTImplicitNameOwner#getImplicitNames()
	 */
	public IASTImplicitName[] getImplicitNames() {
		if (implicitNames == null) {
			ICPPConstructor ctor = CPPSemantics.findImplicitlyCalledConstructor(this);
			if (ctor == null) {
				implicitNames = IASTImplicitName.EMPTY_NAME_ARRAY;
			} else {
				CPPASTImplicitName ctorName = new CPPASTImplicitName(ctor.getNameCharArray(), this);
				ctorName.setBinding(ctor);
				IASTName id = name;
				if (id instanceof ICPPASTQualifiedName) {
					id = ((ICPPASTQualifiedName) id).getLastName();
				}
				ctorName.setOffsetAndLength((ASTNode) id);
				implicitNames = new IASTImplicitName[] { ctorName };
			}
    	}

    	return implicitNames;  
	}
	
	public IBinding[] findBindings(IASTName n, boolean isPrefix) {
		return findBindings(n, isPrefix, null);
	}
}
