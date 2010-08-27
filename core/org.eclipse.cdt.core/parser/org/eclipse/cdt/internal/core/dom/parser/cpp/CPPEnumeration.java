/*******************************************************************************
 * Copyright (c) 2004, 2010 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Andrew Niefer (IBM Corporation) - initial API and implementation
 *     Markus Schorn (Wind River Systems)
 *     Sergey Prigogin (Google)
 *******************************************************************************/
package org.eclipse.cdt.internal.core.dom.parser.cpp;

import org.eclipse.cdt.core.dom.ILinkage;
import org.eclipse.cdt.core.dom.ast.ASTVisitor;
import org.eclipse.cdt.core.dom.ast.DOMException;
import org.eclipse.cdt.core.dom.ast.IASTDeclSpecifier;
import org.eclipse.cdt.core.dom.ast.IASTDeclaration;
import org.eclipse.cdt.core.dom.ast.IASTDeclarator;
import org.eclipse.cdt.core.dom.ast.IASTEnumerationSpecifier;
import org.eclipse.cdt.core.dom.ast.IASTEnumerationSpecifier.IASTEnumerator;
import org.eclipse.cdt.core.dom.ast.IASTName;
import org.eclipse.cdt.core.dom.ast.IASTNode;
import org.eclipse.cdt.core.dom.ast.IASTSimpleDeclaration;
import org.eclipse.cdt.core.dom.ast.IASTTranslationUnit;
import org.eclipse.cdt.core.dom.ast.IBinding;
import org.eclipse.cdt.core.dom.ast.IEnumerator;
import org.eclipse.cdt.core.dom.ast.IScope;
import org.eclipse.cdt.core.dom.ast.IType;
import org.eclipse.cdt.core.dom.ast.ITypedef;
import org.eclipse.cdt.core.dom.ast.IValue;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTEnumerationSpecifier;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTQualifiedName;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTTemplateId;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPBlockScope;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPEnumeration;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPScope;
import org.eclipse.cdt.core.index.IIndex;
import org.eclipse.cdt.core.index.IIndexBinding;
import org.eclipse.cdt.core.parser.util.ArrayUtil;
import org.eclipse.cdt.core.parser.util.CharArrayUtils;
import org.eclipse.cdt.internal.core.dom.Linkage;
import org.eclipse.cdt.internal.core.dom.parser.cpp.semantics.CPPVisitor;
import org.eclipse.core.runtime.PlatformObject;

/**
 * Enumerations in C++
 */
public class CPPEnumeration extends PlatformObject implements ICPPEnumeration, ICPPInternalBinding {
	private static final IASTName NOT_INITIALIZED = CPPASTName.NOT_INITIALIZED;
	private static final IEnumerator[] EMPTY_ENUMERATORS = {};

	private final boolean fIsScoped;
	private final IType fFixedType;
	private IASTName fDefinition= NOT_INITIALIZED;
    private IASTName[] fDeclarations= IASTName.EMPTY_NAME_ARRAY;
	private Long fMaxValue;
	private Long fMinValue;

	private ICPPEnumeration fIndexBinding= null;
	private boolean fSearchedIndex= false;

    public CPPEnumeration(ICPPASTEnumerationSpecifier spec, IType fixedType) {
        final IASTName name = spec.getName();
        fIsScoped= spec.isScoped();
        fFixedType= fixedType;
        if (spec.isOpaque()) {
        	addDeclaration(name);
        } else {
        	addDefinition(name);
        }
		name.setBinding(this);
    }

    public IASTNode[] getDeclarations() {
    	fDeclarations= ArrayUtil.trim(fDeclarations);
    	return fDeclarations;
    }

    private class FindDefinitionAction extends ASTVisitor {
		private char[] nameArray = CPPEnumeration.this.getNameCharArray();
		public IASTName result = null;

		{
			shouldVisitNames          = true;
			shouldVisitDeclarations   = true;
			shouldVisitDeclSpecifiers = true;
			shouldVisitDeclarators    = true;
		}

		@Override
		public int visit(IASTName name) {
			if (name instanceof ICPPASTTemplateId || name instanceof ICPPASTQualifiedName)
				return PROCESS_SKIP;
			char[] c = name.getLookupKey();
			if (name.getParent() instanceof ICPPASTEnumerationSpecifier && CharArrayUtils.equals(c, nameArray)) {
				IBinding binding = name.resolveBinding();
				if (binding == CPPEnumeration.this) {
					result= name;
					return PROCESS_ABORT;
				}
			}
			return PROCESS_CONTINUE; 
		}

		@Override
		public int visit(IASTDeclaration declaration) { 
			if (declaration instanceof IASTSimpleDeclaration)
				return PROCESS_CONTINUE;
			return PROCESS_SKIP; 
		}
		@Override
		public int visit(IASTDeclSpecifier declSpec) {
			return (declSpec instanceof ICPPASTEnumerationSpecifier) ? PROCESS_CONTINUE : PROCESS_SKIP; 
		}
		@Override
		public int visit(IASTDeclarator declarator) {
			return PROCESS_SKIP;
		}
	}

    public IASTName getDefinition() {
    	if (fDefinition == NOT_INITIALIZED) {
    		FindDefinitionAction action = new FindDefinitionAction();
    		IASTNode node = CPPVisitor.getContainingBlockItem(getADeclaration()).getParent();
    		node.accept(action);
    		fDefinition = action.result;
    	}
        return fDefinition;
    }

    public String getName() {
        return new String(getNameCharArray());
    }

    public char[] getNameCharArray() {
    	return getADeclaration().getSimpleID();
    }

	private IASTName getADeclaration() {
    	if (fDefinition != null && fDefinition != NOT_INITIALIZED)
    		return fDefinition;
    	return fDeclarations[0];
	}

	public IScope getScope() {
        return CPPVisitor.getContainingScope(getADeclaration());
    }

    @Override
	public Object clone() {
    	throw new IllegalArgumentException("Enums must not be cloned"); //$NON-NLS-1$
    }

    public String[] getQualifiedName() {
        return CPPVisitor.getQualifiedName(this);
    }

    public char[][] getQualifiedNameCharArray() {
        return CPPVisitor.getQualifiedNameCharArray(this);
    }

    public boolean isGloballyQualified() throws DOMException {
        IScope scope = getScope();
        while (scope != null) {
            if (scope instanceof ICPPBlockScope)
                return false;
            scope = scope.getParent();
        }
        return true;
    }

	public void addDefinition(IASTNode node) {
		assert fDefinition == null || fDefinition == NOT_INITIALIZED;
		fDefinition= (IASTName) node;
	}

	public void addDeclaration(IASTNode node) {
		assert node instanceof IASTName;
		if (fDeclarations == null) {
			fDeclarations= new IASTName[] {(IASTName) node};
		} else {
			fDeclarations= ArrayUtil.append(fDeclarations, (IASTName) node);
		}
	}
	
    public boolean isSameType(IType type) {
        if (type == this)
            return true;
        if (type instanceof ITypedef || type instanceof IIndexBinding)
            return type.isSameType(this);
        return false;
    }
    
	public ILinkage getLinkage() {
		return Linkage.CPP_LINKAGE;
	}

	public IBinding getOwner() {
		return CPPVisitor.findDeclarationOwner(getADeclaration(), true);
	}

	@Override
	public String toString() {
		return getName();
	}
	
	public long getMinValue() {
		if (fMinValue != null)
			return fMinValue.longValue();

		long minValue = Long.MAX_VALUE;
		IEnumerator[] enumerators = getEnumerators();
		for (IEnumerator enumerator : enumerators) {
			IValue value = enumerator.getValue();
			if (value != null) {
				Long val = value.numericalValue();
				if (val != null) {
					long v = val.longValue();
					if (v < minValue) {
						minValue = v;
					}
				}
			}
		}
		fMinValue= minValue;
		return minValue;
	}

	public long getMaxValue() {
		if (fMaxValue != null)
			return fMaxValue.longValue();

		long maxValue = Long.MIN_VALUE;
		IEnumerator[] enumerators = getEnumerators();
		for (IEnumerator enumerator : enumerators) {
			IValue value = enumerator.getValue();
			if (value != null) {
				Long val = value.numericalValue();
				if (val != null) {
					long v = val.longValue();
					if (v > maxValue) {
						maxValue = v;
					}
				}
			}
		}
		fMaxValue= maxValue;
		return maxValue;
	}

	public boolean isScoped() {
		return fIsScoped;
	}

	public IType getFixedType() {
		return fFixedType;
	}

    public IEnumerator[] getEnumerators() {
    	final IASTName definition = getDefinition();
		if (definition == null) {
			ICPPEnumeration typeInIndex= getIndexBinding();
			if (typeInIndex != null) {
				try {
					return typeInIndex.getEnumerators();
				} catch (DOMException e) {
				}
			}
    		return EMPTY_ENUMERATORS;
    	}

		IASTEnumerator[] enums = ((IASTEnumerationSpecifier) definition.getParent()).getEnumerators();
        IEnumerator[] bindings = new IEnumerator[enums.length];
        for (int i = 0; i < enums.length; i++) {
            bindings[i] = (IEnumerator) enums[i].getName().resolveBinding();
        }
        return bindings;
    }

	private ICPPEnumeration getIndexBinding() {
		if (!fSearchedIndex) {
			final IASTTranslationUnit translationUnit = getADeclaration().getTranslationUnit();
			IIndex index= translationUnit.getIndex();
			if (index != null) {
				fIndexBinding= (ICPPEnumeration) index.adaptBinding(this);
			}
		}
		return fIndexBinding;
	}

	public ICPPScope asScope() {
		IASTName def = getDefinition();
		if (def == null) {
			ICPPEnumeration indexBinding= getIndexBinding();
			if (indexBinding != null) {
				return indexBinding.asScope();
			}
			def= getADeclaration();
		}
		return ((ICPPASTEnumerationSpecifier) def.getParent()).getScope();
	}
}
