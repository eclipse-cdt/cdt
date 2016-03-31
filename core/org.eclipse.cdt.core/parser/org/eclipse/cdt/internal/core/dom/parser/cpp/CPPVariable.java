/*******************************************************************************
 * Copyright (c) 2004, 2016 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Andrew Niefer (IBM Corporation) - Initial API and implementation
 *     Markus Schorn (Wind River Systems)
 *     Ed Swartz (Nokia)
 *     Sergey Prigogin (Google)
 *******************************************************************************/
package org.eclipse.cdt.internal.core.dom.parser.cpp;

import static org.eclipse.cdt.internal.core.dom.parser.cpp.semantics.SemanticUtil.CVTYPE;
import static org.eclipse.cdt.internal.core.dom.parser.cpp.semantics.SemanticUtil.REF;
import static org.eclipse.cdt.internal.core.dom.parser.cpp.semantics.SemanticUtil.TDEF;

import java.util.HashSet;
import java.util.Set;

import org.eclipse.cdt.core.dom.ILinkage;
import org.eclipse.cdt.core.dom.ast.DOMException;
import org.eclipse.cdt.core.dom.ast.IASTDeclSpecifier;
import org.eclipse.cdt.core.dom.ast.IASTDeclarator;
import org.eclipse.cdt.core.dom.ast.IASTEqualsInitializer;
import org.eclipse.cdt.core.dom.ast.IASTInitializer;
import org.eclipse.cdt.core.dom.ast.IASTName;
import org.eclipse.cdt.core.dom.ast.IASTNode;
import org.eclipse.cdt.core.dom.ast.IArrayType;
import org.eclipse.cdt.core.dom.ast.IBinding;
import org.eclipse.cdt.core.dom.ast.IScope;
import org.eclipse.cdt.core.dom.ast.IType;
import org.eclipse.cdt.core.dom.ast.IValue;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTConstructorInitializer;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTDeclarator;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTInitializerClause;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTInitializerList;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTQualifiedName;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPBlockScope;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPClassType;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPConstructor;
import org.eclipse.cdt.core.parser.util.ArrayUtil;
import org.eclipse.cdt.internal.core.dom.Linkage;
import org.eclipse.cdt.internal.core.dom.parser.ASTNode;
import org.eclipse.cdt.internal.core.dom.parser.IntegralValue;
import org.eclipse.cdt.internal.core.dom.parser.cpp.semantics.CPPSemantics;
import org.eclipse.cdt.internal.core.dom.parser.cpp.semantics.CPPVisitor;
import org.eclipse.cdt.internal.core.dom.parser.cpp.semantics.EvalConstructor;
import org.eclipse.cdt.internal.core.dom.parser.cpp.semantics.EvalFixed;
import org.eclipse.cdt.internal.core.dom.parser.cpp.semantics.EvalUtil;
import org.eclipse.cdt.internal.core.dom.parser.cpp.semantics.SemanticUtil;
import org.eclipse.core.runtime.PlatformObject;

public class CPPVariable extends PlatformObject implements ICPPInternalVariable {
	private IASTName fDefinition;
	private IASTName fDeclarations[];
	private IType fType;
	private boolean fAllResolved;

	/**
	 * The set of CPPVariable objects for which initial value computation is in progress on each thread.
	 * This is used to guard against recursion during initial value computation.
	 */
	private static final ThreadLocal<Set<CPPVariable>> fInitialValueInProgress = new ThreadLocal<Set<CPPVariable>>() {
		@Override
		protected Set<CPPVariable> initialValue() {
			return new HashSet<>();
		}
	};
	
	public CPPVariable(IASTName name) {
	    boolean isDef = name != null && name.isDefinition();
	    if (name instanceof ICPPASTQualifiedName) {
	    	name = name.getLastName();
	    }

	    if (isDef) {
	        fDefinition = name;
	    } else {
	        fDeclarations = new IASTName[] { name };
	    }

	    // Built-in variables supply a null.
	    if (name != null) {
	    	name.setBinding(this);
	    } else {
	    	assert this instanceof CPPBuiltinVariable;
	    }
	}

	@Override
	public void addDeclaration(IASTNode node) {
		if (!(node instanceof IASTName))
			return;
		IASTName name = (IASTName) node;
		if (fDefinition == null && name.isDefinition()) {
			fDefinition = name;
		} else if (fDeclarations == null) {
			fDeclarations = new IASTName[] { name };
		} else {
			// Keep the lowest offset declaration at the first position.
			if (fDeclarations.length > 0
					&& ((ASTNode) node).getOffset() < ((ASTNode) fDeclarations[0]).getOffset()) {
				fDeclarations = ArrayUtil.prepend(IASTName.class, fDeclarations, name);
			} else {
				fDeclarations = ArrayUtil.append(IASTName.class, fDeclarations, name);
			}
		}
		// Array types may be incomplete.
		if (fType instanceof IArrayType) {
			fType = null;
		}
	}

    @Override
	public IASTNode[] getDeclarations() {
        return fDeclarations;
    }

    @Override
	public IASTNode getDefinition() {
        return fDefinition;
    }

	@Override
	public IType getType() {
		if (fType != null) {
			return fType;
		}

		boolean allResolved = fAllResolved;
		fAllResolved = true;
		fType = VariableHelpers.createType(this, fDefinition, fDeclarations, allResolved);

		return fType;
	}

	@Override
	public String getName() {
		return new String(getNameCharArray());
	}

	@Override
	public char[] getNameCharArray() {
	    if (fDeclarations != null) {
	        return fDeclarations[0].getSimpleID();
	    }
	    return fDefinition.getSimpleID();
	}

	@Override
	public IScope getScope() {
		return CPPVisitor.getContainingScope(fDefinition != null ? fDefinition : fDeclarations[0]);
	}

    @Override
	public String[] getQualifiedName() {
        return CPPVisitor.getQualifiedName(this);
    }

    @Override
	public char[][] getQualifiedNameCharArray() {
        return CPPVisitor.getQualifiedNameCharArray(this);
    }

    @Override
	public boolean isGloballyQualified() throws DOMException {
        IScope scope = getScope();
        while (scope != null) {
            if (scope instanceof ICPPBlockScope)
                return false;
            scope = scope.getParent();
        }
        return true;
    }

	@Override
	public void addDefinition(IASTNode node) {
		addDeclaration(node);
	}

	public boolean hasStorageClass(int storage) {
	    IASTName name = (IASTName) getDefinition();
        IASTNode[] ns = getDeclarations();

        return VariableHelpers.hasStorageClass(name, ns, storage);
	}

    @Override
	public boolean isMutable() {
        // 7.1.1-8 the mutable specifier can only be applied to names of class data members.
        return false;
    }

    @Override
	public boolean isStatic() {
		return hasStorageClass(IASTDeclSpecifier.sc_static);
	}

    @Override
	public boolean isExtern() {
        return hasStorageClass(IASTDeclSpecifier.sc_extern);
    }

    @Override
	public boolean isExternC() {
	    return CPPVisitor.isExternC(getDefinition(), getDeclarations());
    }

    @Override
	public boolean isAuto() {
        return hasStorageClass(IASTDeclSpecifier.sc_auto);
    }

    @Override
	public boolean isRegister() {
        return hasStorageClass(IASTDeclSpecifier.sc_register);
    }

	@Override
	public ILinkage getLinkage() {
		return Linkage.CPP_LINKAGE;
	}

	@Override
	public IBinding getOwner() {
		IASTName node = fDefinition != null ? fDefinition : fDeclarations[0];
		return CPPVisitor.findNameOwner(node, !hasStorageClass(IASTDeclSpecifier.sc_extern));
	}

	@Override
	public IValue getInitialValue() {
		Set<CPPVariable> recursionProtectionSet = fInitialValueInProgress.get();
		if (!recursionProtectionSet.add(this)) {
			return IntegralValue.UNKNOWN;
		}
		try {
			final IValue initialValue = VariableHelpers.getInitialValue(fDefinition, fDeclarations, getType());
			final IType nestedType = SemanticUtil.getNestedType(getType(), TDEF | REF | CVTYPE);
			if(nestedType instanceof ICPPClassType || initialValue == IntegralValue.UNKNOWN) {
				ICPPEvaluation initEval = getInitializerEvaluation();
				if(initEval == null) {
					return null;
				}
				return IntegralValue.create(initEval);
			}
			return initialValue;
		} finally {
			recursionProtectionSet.remove(this);
		}
	}
	
	private IASTDeclarator findDeclarator() {
		IASTDeclarator declarator = null;
		if (fDefinition != null) {
			declarator = VariableHelpers.findDeclarator(fDefinition);
			if(declarator != null) {
				return declarator;
			}
		}
		if (fDeclarations != null) {
			for (IASTName decl : fDeclarations) {
				if (decl == null)
					break;
				declarator = VariableHelpers.findDeclarator(decl);
				if(declarator != null) {
					return declarator;
				}
			}
		}
		return null;
	}

	@Override
	public String toString() {
		return getName();
	}

	public ICPPEvaluation getInitializerEvaluation() {
		ICPPASTDeclarator declarator = (ICPPASTDeclarator)findDeclarator();
		if(declarator != null) {
			IASTInitializer initializer = declarator.getInitializer();
			if(hasImplicitlyCalledCtor(declarator)) {
				ICPPConstructor constructor = (ICPPConstructor)CPPSemantics.findImplicitlyCalledConstructor(declarator);
				ICPPEvaluation[] arguments = EvalConstructor.extractArguments(initializer, constructor);
				return new EvalConstructor(getType(), constructor, arguments, declarator);
			} else if(initializer instanceof IASTEqualsInitializer) {
				IASTEqualsInitializer equalsInitializer = (IASTEqualsInitializer)initializer;
				ICPPASTInitializerClause initClause = (ICPPASTInitializerClause)equalsInitializer.getInitializerClause();
				return initClause.getEvaluation();
			} else if(initializer instanceof ICPPASTInitializerList) {
				ICPPASTInitializerList initList = (ICPPASTInitializerList)initializer;
				return initList.getEvaluation();
			} else if(initializer instanceof ICPPASTConstructorInitializer) {
				ICPPASTConstructorInitializer ctorInitializer = (ICPPASTConstructorInitializer)initializer;
				ICPPASTInitializerClause initClause = (ICPPASTInitializerClause)ctorInitializer.getArguments()[0];
				return initClause.getEvaluation();
			} else if(initializer == null) {
				return null;
			}	
		}
		return EvalFixed.INCOMPLETE;
	}

	private static boolean hasImplicitlyCalledCtor(ICPPASTDeclarator declarator) {
		IBinding ctor = CPPSemantics.findImplicitlyCalledConstructor(declarator);
		return ctor != null && !EvalUtil.isCompilerGeneratedCtor(ctor);
	}
}
