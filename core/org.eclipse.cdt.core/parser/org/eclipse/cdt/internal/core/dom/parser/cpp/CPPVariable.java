/*******************************************************************************
 * Copyright (c) 2004, 2014 IBM Corporation and others.
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

import java.util.HashSet;
import java.util.Set;

import org.eclipse.cdt.core.dom.ILinkage;
import org.eclipse.cdt.core.dom.ast.DOMException;
import org.eclipse.cdt.core.dom.ast.IASTDeclSpecifier;
import org.eclipse.cdt.core.dom.ast.IASTDeclaration;
import org.eclipse.cdt.core.dom.ast.IASTDeclarator;
import org.eclipse.cdt.core.dom.ast.IASTInitializer;
import org.eclipse.cdt.core.dom.ast.IASTName;
import org.eclipse.cdt.core.dom.ast.IASTNode;
import org.eclipse.cdt.core.dom.ast.IASTSimpleDeclaration;
import org.eclipse.cdt.core.dom.ast.IASTTranslationUnit;
import org.eclipse.cdt.core.dom.ast.IArrayType;
import org.eclipse.cdt.core.dom.ast.IBinding;
import org.eclipse.cdt.core.dom.ast.IScope;
import org.eclipse.cdt.core.dom.ast.IType;
import org.eclipse.cdt.core.dom.ast.IValue;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTQualifiedName;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPBlockScope;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPVariable;
import org.eclipse.cdt.core.parser.util.ArrayUtil;
import org.eclipse.cdt.internal.core.dom.Linkage;
import org.eclipse.cdt.internal.core.dom.parser.ASTNode;
import org.eclipse.cdt.internal.core.dom.parser.Value;
import org.eclipse.cdt.internal.core.dom.parser.cpp.semantics.CPPVisitor;
import org.eclipse.cdt.internal.core.dom.parser.cpp.semantics.SemanticUtil;
import org.eclipse.core.runtime.PlatformObject;

public class CPPVariable extends PlatformObject implements ICPPVariable, ICPPInternalBinding {
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
	
	private IASTDeclarator findDeclarator(IASTName name) {
	    IASTNode node = name.getParent();
	    if (node instanceof ICPPASTQualifiedName)
	        node = node.getParent();
	    
	    if (!(node instanceof IASTDeclarator))
	        return null;
	    
	    IASTDeclarator dtor = (IASTDeclarator) node;
	    while (dtor.getParent() instanceof IASTDeclarator)
	        dtor = (IASTDeclarator) dtor.getParent();
	    
	    return dtor;
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

		boolean doneWithDefinition = false;
		IArrayType firstCandidate= null;
		final int length = fDeclarations == null ? 0 : fDeclarations.length;
		for (int i = 0; i <= length; i++) {
			IASTName n;
			// Process the definition according to its relative position among the declarations.
			// See http://bugs.eclipse.org/434150
			if (fDefinition != null && !doneWithDefinition &&
					(i == length || ((ASTNode) fDefinition).getOffset() < ((ASTNode) fDeclarations[i]).getOffset())) {
				n = fDefinition;
				doneWithDefinition = true;
				--i;  // We still have to come back to the declaration at position i.
			} else if (i < length) {
				n = fDeclarations[i];
			} else {
				break;
			}
			if (n != null) {
				while (n.getParent() instanceof IASTName)
					n = (IASTName) n.getParent();

				IASTNode node = n.getParent();
				if (node instanceof IASTDeclarator) {
					IType t= CPPVisitor.createType((IASTDeclarator) node);
					if (t instanceof IArrayType && !((IArrayType) t).hasSize()) {
						if (firstCandidate == null) {
							firstCandidate= (IArrayType) t;
						}
					} else {
						return fType= t;
					}
				}
			}
		}
		fType= firstCandidate;
		if (!fAllResolved) {
			resolveAllDeclarations();
			return getType();
		}
		return fType;
	}

	private void resolveAllDeclarations() {
		if (fAllResolved)
			return;
		fAllResolved= true;
		final int length = fDeclarations == null ? 0 : fDeclarations.length;
		for (int i = -1; i < length; i++) {
			IASTName n = i == -1 ? fDefinition : fDeclarations[i];
			if (n != null) {
			    IASTTranslationUnit tu = n.getTranslationUnit();
		        if (tu != null) {
		            CPPVisitor.getDeclarations(tu, this);
		            return;
		        }
		    }
		}
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
        
        int i = -1;
        do {
            if (name != null) {
                IASTNode parent = name.getParent();
	            while (!(parent instanceof IASTDeclaration))
	                parent = parent.getParent();
	            
	            if (parent instanceof IASTSimpleDeclaration) {
	                IASTDeclSpecifier declSpec = ((IASTSimpleDeclaration) parent).getDeclSpecifier();
	                if (declSpec.getStorageClass() == storage) {
	                	return true;
	                }
	            }
            }
            if (ns != null && ++i < ns.length) {
                name = (IASTName) ns[i];
            } else {
                break;
            }
        } while (name != null);
        return false;
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
	    if (CPPVisitor.isExternC(getDefinition())) {
	    	return true;
	    }
        IASTNode[] ds= getDeclarations();
        if (ds != null) {
        	for (IASTNode element : ds) {
        		if (CPPVisitor.isExternC(element)) {
        			return true;
        		}
			}
        }
        return false;
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
			return Value.UNKNOWN;
		}
		try {
			if (fDefinition != null) {
				final IValue val= getInitialValue(fDefinition);
				if (val != null)
					return val;
			}
			if (fDeclarations != null) {
				for (IASTName decl : fDeclarations) {
					if (decl == null)
						break;
					final IValue val= getInitialValue(decl);
					if (val != null)
						return val;
				}
			}		
		} finally {
			recursionProtectionSet.remove(this);
		}
		return null;
	}
	
	private IValue getInitialValue(IASTName name) {
		IASTDeclarator dtor= findDeclarator(name);
		if (dtor != null) {
			IASTInitializer init= dtor.getInitializer();
			if (init != null) {
				return SemanticUtil.getValueOfInitializer(init, getType());
			}
		}
		return null;
	}

	@Override
	public String toString() {
		return getName();
	}
}
