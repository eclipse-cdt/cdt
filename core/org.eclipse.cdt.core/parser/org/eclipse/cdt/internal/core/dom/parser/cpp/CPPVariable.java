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
 *     Ed Swartz (Nokia)
 *******************************************************************************/
package org.eclipse.cdt.internal.core.dom.parser.cpp;

import org.eclipse.cdt.core.dom.ILinkage;
import org.eclipse.cdt.core.dom.ast.DOMException;
import org.eclipse.cdt.core.dom.ast.IASTDeclSpecifier;
import org.eclipse.cdt.core.dom.ast.IASTDeclaration;
import org.eclipse.cdt.core.dom.ast.IASTDeclarator;
import org.eclipse.cdt.core.dom.ast.IASTExpression;
import org.eclipse.cdt.core.dom.ast.IASTInitializer;
import org.eclipse.cdt.core.dom.ast.IASTInitializerExpression;
import org.eclipse.cdt.core.dom.ast.IASTName;
import org.eclipse.cdt.core.dom.ast.IASTNode;
import org.eclipse.cdt.core.dom.ast.IASTSimpleDeclaration;
import org.eclipse.cdt.core.dom.ast.IBasicType;
import org.eclipse.cdt.core.dom.ast.IBinding;
import org.eclipse.cdt.core.dom.ast.IPointerType;
import org.eclipse.cdt.core.dom.ast.IScope;
import org.eclipse.cdt.core.dom.ast.IType;
import org.eclipse.cdt.core.dom.ast.IValue;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTCompositeTypeSpecifier;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTConstructorInitializer;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTQualifiedName;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPBlockScope;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPVariable;
import org.eclipse.cdt.core.parser.util.ArrayUtil;
import org.eclipse.cdt.internal.core.dom.Linkage;
import org.eclipse.cdt.internal.core.dom.parser.ASTNode;
import org.eclipse.cdt.internal.core.dom.parser.IInternalVariable;
import org.eclipse.cdt.internal.core.dom.parser.ProblemBinding;
import org.eclipse.cdt.internal.core.dom.parser.Value;
import org.eclipse.cdt.internal.core.dom.parser.cpp.semantics.CPPVisitor;
import org.eclipse.cdt.internal.core.dom.parser.cpp.semantics.SemanticUtil;
import org.eclipse.core.runtime.PlatformObject;

/**
 * @author aniefer
 */
public class CPPVariable extends PlatformObject implements ICPPVariable, ICPPInternalBinding, IInternalVariable {
    public static class CPPVariableProblem extends ProblemBinding implements ICPPVariable{
        public CPPVariableProblem(IASTNode node, int id, char[] arg) {
            super(node, id, arg);
        }

        public IType getType() throws DOMException {
            throw new DOMException(this);
        }

        public boolean isStatic() throws DOMException {
            throw new DOMException(this);
        }
        public String[] getQualifiedName() throws DOMException {
            throw new DOMException(this);
        }
        public char[][] getQualifiedNameCharArray() throws DOMException {
            throw new DOMException(this);
        }
        public boolean isGloballyQualified() throws DOMException {
            throw new DOMException(this);
        }
        public boolean isMutable() throws DOMException {
            throw new DOMException(this);
        }
        public boolean isExtern() throws DOMException {
             throw new DOMException(this);
        }
        public boolean isExternC() throws DOMException {
            throw new DOMException(this);
        }
        public boolean isAuto() throws DOMException {
            throw new DOMException(this);
        }
        public boolean isRegister() throws DOMException {
            throw new DOMException(this);
        }
		public IValue getInitialValue() {
			return null;
		}
    }

	private IASTName declarations[] = null;
	private IASTName definition = null;
	private IType type = null;
	
	public CPPVariable(IASTName name) {
	    boolean isDef = isDefinition(name);
	    if (name instanceof ICPPASTQualifiedName) {
	        IASTName[] ns = ((ICPPASTQualifiedName)name).getNames();
	        name = ns[ns.length - 1];
	    }
	    
	    if (isDef)
	        definition = name;
	    else 
	        declarations = new IASTName[] { name };
	    
	    // built-in variables supply a null
	    if (name != null) {
	    	name.setBinding(this);
	    } else {
	    	assert this instanceof CPPBuiltinVariable;
	    }
	}
	
	protected boolean isDefinition(IASTName name) {
	    IASTDeclarator dtor= findDeclarator(name);
	    if (dtor == null) {
	    	return false;
	    }
	    
	    IASTSimpleDeclaration simpleDecl = (IASTSimpleDeclaration) dtor.getParent();
	    IASTDeclSpecifier declSpec = simpleDecl.getDeclSpecifier();
	    
	    //(3.1-1) A declaration is a definition unless ...
	    //it contains the extern specifier or a linkage-spec and does not contain an initializer
	    if (dtor.getInitializer() == null && declSpec.getStorageClass() == IASTDeclSpecifier.sc_extern)
	        return false;
	    //or it declares a static data member in a class declaration
	    if (simpleDecl.getParent() instanceof ICPPASTCompositeTypeSpecifier && 
	    		declSpec.getStorageClass() == IASTDeclSpecifier.sc_static) {
	        return false;
	    }
	    
	    return true;
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
	
	public void addDeclaration(IASTNode node) {
		if (!(node instanceof IASTName))
			return;
		IASTName name = (IASTName) node;
	    if (isDefinition(name)) {
	        definition = name;
	    } else if (declarations == null) {
	        declarations = new IASTName[] { name };
	    } else {
	        //keep the lowest offset declaration in[0]
			if (declarations.length > 0 && ((ASTNode) node).getOffset() < ((ASTNode) declarations[0]).getOffset()) {
				declarations = (IASTName[]) ArrayUtil.prepend(IASTName.class, declarations, name);
			} else {
				declarations = (IASTName[]) ArrayUtil.append(IASTName.class, declarations, name);
			}
	    }
	}
	
	public void removeDeclaration(IASTNode node) {
		if (node == definition) {
			definition = null;
			return;
		}
		ArrayUtil.remove(declarations, node);
	}
	
    /* (non-Javadoc)
     * @see org.eclipse.cdt.internal.core.dom.parser.cpp.ICPPBinding#getDeclarations()
     */
    public IASTNode[] getDeclarations() {
        return declarations;
    }

    /* (non-Javadoc)
     * @see org.eclipse.cdt.internal.core.dom.parser.cpp.ICPPBinding#getDefinition()
     */
    public IASTNode getDefinition() {
        return definition;
    }

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.dom.ast.IVariable#getType()
	 */
	public IType getType() {
		if (type == null) {
			IASTName n = null;
			if (definition != null)
				n = definition;
			else if (declarations != null && declarations.length > 0)
				n = declarations[0];
			
			if (n != null) {
				while (n.getParent() instanceof IASTName)
					n = (IASTName) n.getParent();
				IASTNode node = n.getParent();
				if (node instanceof IASTDeclarator)
					type = CPPVisitor.createType((IASTDeclarator) node);
			}
		}
		return type;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.dom.ast.IBinding#getName()
	 */
	public String getName() {
		return new String(getNameCharArray());
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.dom.ast.IBinding#getNameCharArray()
	 */
	public char[] getNameCharArray() {
	    if (declarations != null) {
	        return declarations[0].getSimpleID();
	    } 
	    return definition.getSimpleID();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.dom.ast.IBinding#getScope()
	 */
	public IScope getScope() {
		return CPPVisitor.getContainingScope(definition != null ? definition : declarations[0]);
	}
	
    /* (non-Javadoc)
     * @see org.eclipse.cdt.core.dom.ast.IBinding#getFullyQualifiedName()
     */
    public String[] getQualifiedName() {
        return CPPVisitor.getQualifiedName(this);
    }

    /* (non-Javadoc)
     * @see org.eclipse.cdt.core.dom.ast.IBinding#getFullyQualifiedNameCharArray()
     */
    public char[][] getQualifiedNameCharArray() {
        return CPPVisitor.getQualifiedNameCharArray(this);
    }

    /* (non-Javadoc)
     * @see org.eclipse.cdt.core.dom.ast.cpp.ICPPBinding#isGloballyQualified()
     */
    public boolean isGloballyQualified() throws DOMException {
        IScope scope = getScope();
        while (scope != null) {
            if (scope instanceof ICPPBlockScope)
                return false;
            scope = scope.getParent();
        }
        return true;
    }

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.internal.core.dom.parser.cpp.ICPPInternalBinding#addDefinition(org.eclipse.cdt.core.dom.ast.IASTNode)
	 */
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
	                IASTDeclSpecifier declSpec = ((IASTSimpleDeclaration)parent).getDeclSpecifier();
	                if (declSpec.getStorageClass() == storage) {
	                	return true;
	                }
	            }
            }
            if (ns != null && ++i < ns.length)
                name = (IASTName) ns[i];
            else
                break;
        } while (name != null);
        return false;
	}
	
    /* (non-Javadoc)
     * @see org.eclipse.cdt.core.dom.ast.cpp.ICPPVariable#isMutable()
     */
    public boolean isMutable() {
        //7.1.1-8 the mutable specifier can only be applied to names of class data members
        return false;
    }

    public boolean isStatic() {
		return hasStorageClass(IASTDeclSpecifier.sc_static);
	}

	/* (non-Javadoc)
     * @see org.eclipse.cdt.core.dom.ast.IVariable#isExtern()
     */
    public boolean isExtern() {
        return hasStorageClass(IASTDeclSpecifier.sc_extern);
    }

	/* (non-Javadoc)
     * @see org.eclipse.cdt.core.dom.ast.IVariable#isExtern()
     */
    public boolean isExternC() {
	    if (CPPVisitor.isExternC(getDefinition())) {
	    	return true;
	    }
        IASTNode[] ds= getDeclarations();
        if (ds != null) {
        	for (int i = 0; i < ds.length; i++) {
        		if (CPPVisitor.isExternC(ds[i])) {
        			return true;
        		}
			}
        }
        return false;
    }

    /* (non-Javadoc)
     * @see org.eclipse.cdt.core.dom.ast.IVariable#isAuto()
     */
    public boolean isAuto() {
        return hasStorageClass(IASTDeclSpecifier.sc_auto);
    }

    /* (non-Javadoc)
     * @see org.eclipse.cdt.core.dom.ast.IVariable#isRegister()
     */
    public boolean isRegister() {
        return hasStorageClass(IASTDeclSpecifier.sc_register);
    }
    
	public ILinkage getLinkage() {
		return Linkage.CPP_LINKAGE;
	}
	
	public IBinding getOwner() throws DOMException {
		IASTName node = definition != null ? definition : declarations[0];
		return CPPVisitor.findNameOwner(node, !hasStorageClass(IASTDeclSpecifier.sc_extern)); 
	}
	
	@Override
	public String toString() {
		return getName();
	}

	public IValue getInitialValue() {
		return getInitialValue(Value.MAX_RECURSION_DEPTH);
	}
	
	public IValue getInitialValue(int maxDepth) {
		if (definition != null) {
			final IValue val= getInitialValue(definition, maxDepth);
			if (val != null)
				return val;
		}
		if (declarations != null) {
			for (IASTName decl : declarations) {
				if (decl == null)
					break;
				final IValue val= getInitialValue(decl, maxDepth);
				if (val != null)
					return val;
			}
		}		
		return null;
	}
	
	private IValue getInitialValue(IASTName name, int maxDepth) {
		IASTDeclarator dtor= findDeclarator(name);
		if (dtor != null) {
			IASTInitializer init= dtor.getInitializer();
			if (init instanceof IASTInitializerExpression) {
				IASTExpression expr= ((IASTInitializerExpression) init).getExpression();
				if (expr != null)
					return Value.create(expr, maxDepth);
			} else if (init instanceof ICPPASTConstructorInitializer) {
				IType type= SemanticUtil.getUltimateTypeUptoPointers(getType());
				if (type instanceof IPointerType || type instanceof IBasicType) {
					IASTExpression expr= ((ICPPASTConstructorInitializer) init).getExpression();
					if (expr != null)
						return Value.create(expr, maxDepth);
				}
			}
			if (init != null)
				return Value.UNKNOWN;
		}
		return null;
	}
}
