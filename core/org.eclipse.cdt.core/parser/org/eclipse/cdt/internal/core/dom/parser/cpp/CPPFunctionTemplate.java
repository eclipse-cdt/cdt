/*******************************************************************************
 * Copyright (c) 2005, 2008 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Andrew Niefer (IBM) - Initial API and implementation
 *    Markus Schorn (Wind River Systems)
 *******************************************************************************/
package org.eclipse.cdt.internal.core.dom.parser.cpp;

import org.eclipse.cdt.core.dom.ast.ASTTypeUtil;
import org.eclipse.cdt.core.dom.ast.DOMException;
import org.eclipse.cdt.core.dom.ast.IASTDeclSpecifier;
import org.eclipse.cdt.core.dom.ast.IASTDeclaration;
import org.eclipse.cdt.core.dom.ast.IASTDeclarator;
import org.eclipse.cdt.core.dom.ast.IASTFunctionDefinition;
import org.eclipse.cdt.core.dom.ast.IASTName;
import org.eclipse.cdt.core.dom.ast.IASTNode;
import org.eclipse.cdt.core.dom.ast.IASTParameterDeclaration;
import org.eclipse.cdt.core.dom.ast.IASTSimpleDeclaration;
import org.eclipse.cdt.core.dom.ast.IASTTypeId;
import org.eclipse.cdt.core.dom.ast.IBinding;
import org.eclipse.cdt.core.dom.ast.IFunctionType;
import org.eclipse.cdt.core.dom.ast.IParameter;
import org.eclipse.cdt.core.dom.ast.IProblemBinding;
import org.eclipse.cdt.core.dom.ast.IScope;
import org.eclipse.cdt.core.dom.ast.IType;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTDeclSpecifier;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTFunctionDeclarator;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPClassTemplatePartialSpecialization;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPFunction;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPFunctionTemplate;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPFunctionType;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPTemplateParameter;
import org.eclipse.cdt.internal.core.dom.parser.ProblemBinding;
import org.eclipse.cdt.internal.core.dom.parser.cpp.semantics.CPPVisitor;

/**
 * Implementation of function templates
 */
public class CPPFunctionTemplate extends CPPTemplateDefinition
		implements ICPPFunctionTemplate, ICPPFunction, ICPPInternalFunction {
	public static final class CPPFunctionTemplateProblem extends ProblemBinding
			implements ICPPFunctionTemplate, ICPPFunction {
		public CPPFunctionTemplateProblem(IASTNode node, int id, char[] arg) {
			super(node, id, arg);
		}
		public ICPPTemplateParameter[] getTemplateParameters() throws DOMException {
			throw new DOMException(this);
		}
		public ICPPClassTemplatePartialSpecialization[] getTemplateSpecializations() throws DOMException {
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
		public boolean isInline() throws DOMException {
			throw new DOMException(this);
		}
		public boolean isExternC() throws DOMException {
			throw new DOMException(this);
		}
		public IParameter[] getParameters() throws DOMException {
			throw new DOMException(this);
		}
		public IScope getFunctionScope() throws DOMException {
			throw new DOMException(this);
		}
		public ICPPFunctionType getType() throws DOMException {
			throw new DOMException(this);
		}
		public boolean isStatic() throws DOMException {
			throw new DOMException(this);
		}
		public boolean isExtern() throws DOMException {
			throw new DOMException(this);
		}
		public boolean isAuto() throws DOMException {
			throw new DOMException(this);
		}
		public boolean isRegister() throws DOMException {
			throw new DOMException(this);
		}
		public boolean takesVarArgs() throws DOMException {
			throw new DOMException(this);
		}
		public IType[] getExceptionSpecification() throws DOMException {
			throw new DOMException( this );
		}
	}
	
	protected ICPPFunctionType type = null;

	public CPPFunctionTemplate(IASTName name) {
		super(name);
	}

	@Override
	public void addDefinition(IASTNode node) {
		if (!(node instanceof IASTName))
			return;
		updateFunctionParameterBindings((IASTName) node);
		super.addDefinition(node);
	}

	@Override
	public void addDeclaration(IASTNode node) {
		if (!(node instanceof IASTName))
			return;
		updateFunctionParameterBindings((IASTName) node);
		super.addDeclaration(node);
	}

	private void updateFunctionParameterBindings(IASTName declName) {
		IASTName defName = definition != null ? definition : declarations[0];
		ICPPASTFunctionDeclarator orig = getDeclaratorByName(defName);
    	IASTParameterDeclaration[] ops = orig.getParameters();
    	IASTParameterDeclaration[] nps = getDeclaratorByName(declName).getParameters();
    	CPPParameter temp = null;
    	for(int i = 0; i < nps.length; i++) {
    		temp = (CPPParameter) CPPVisitor.findInnermostDeclarator(ops[i].getDeclarator()).getName().getBinding();
    		if (temp != null) {
    		    IASTName name = CPPVisitor.findInnermostDeclarator(nps[i].getDeclarator()).getName();
    			name.setBinding(temp);
    			temp.addDeclaration(name);
    		}
    	}
	}

	public IParameter[] getParameters() {
		IASTName name = getTemplateName();
		ICPPASTFunctionDeclarator fdecl= getDeclaratorByName(name);
		if (fdecl != null) {
			IASTParameterDeclaration[] params = fdecl.getParameters();
			int size = params.length;
			IParameter[] result = new IParameter[size];
			if (size > 0) {
				for(int i = 0; i < size; i++) {
					IASTParameterDeclaration p = params[i];
					final IASTName pname = CPPVisitor.findInnermostDeclarator(p.getDeclarator()).getName();
					final IBinding binding= pname.resolveBinding();
					if (binding instanceof IParameter) {
						result[i]= (IParameter) binding;
					} else {
						result[i] = new CPPParameter.CPPParameterProblem(p,
								IProblemBinding.SEMANTIC_INVALID_TYPE, pname.toCharArray());
					}
				}
			}
			return result;
		}
		return null;
	}

	public IScope getFunctionScope() {
		return null;
	}

	public ICPPFunctionType getType() {
		if (type == null) {
			IASTName name = getTemplateName();
			IASTNode parent = name.getParent();
			while(parent.getParent() instanceof IASTDeclarator)
				parent = parent.getParent();
			
			IType temp = CPPVisitor.createType((IASTDeclarator)parent);
			if (temp instanceof ICPPFunctionType)
				type = (ICPPFunctionType) temp;
		}
		return type;
	}

	public boolean hasStorageClass(int storage) {
	    IASTName name = (IASTName) getDefinition();
        IASTNode[] ns = getDeclarations();
        int i = -1;
        do {
            if (name != null) {
                IASTNode parent = name.getParent();
	            while(!(parent instanceof IASTDeclaration))
	                parent = parent.getParent();
	            
	            IASTDeclSpecifier declSpec = null;
	            if (parent instanceof IASTSimpleDeclaration) {
	                declSpec = ((IASTSimpleDeclaration)parent).getDeclSpecifier();
	            } else if (parent instanceof IASTFunctionDefinition) {
	                declSpec = ((IASTFunctionDefinition)parent).getDeclSpecifier();
	            }
	            if (declSpec != null && declSpec.getStorageClass() == storage) {
	            	return true;
	            }
            }
            if (ns != null && ++i < ns.length)
                name = (IASTName) ns[i];
            else
                break;
        } while (name != null);
        return false;
	}

	public IBinding resolveParameter(IASTParameterDeclaration param) {
	   	IASTName name = CPPVisitor.findInnermostDeclarator(param.getDeclarator()).getName();
    	IBinding binding = name.getBinding();
    	if (binding != null)
    		return binding;
		
    	ICPPASTFunctionDeclarator fdtor = (ICPPASTFunctionDeclarator) param.getParent();
    	IASTParameterDeclaration[] ps = fdtor.getParameters();
    	int i = 0;
    	for (; i < ps.length; i++) {
    		if (param == ps[i])
    			break;
    	}
    	
    	//create a new binding and set it for the corresponding parameter in all known defns and decls
    	binding = new CPPParameter(name);
    	IASTParameterDeclaration temp = null;
    	if (definition != null) {
    		ICPPASTFunctionDeclarator fdecl= getDeclaratorByName(definition);
    		if (fdecl != null) {
    			temp = fdecl.getParameters()[i];
    			IASTName n = CPPVisitor.findInnermostDeclarator(temp.getDeclarator()).getName();
    			if (n != name) {
    				n.setBinding(binding);
    				((CPPParameter)binding).addDeclaration(n);
    			}
    		}
    	}
    	if (declarations != null) {
    		for(int j = 0; j < declarations.length && declarations[j] != null; j++) {
        		ICPPASTFunctionDeclarator fdecl= getDeclaratorByName(declarations[j]);
        		if (fdecl != null) {
        			temp = fdecl.getParameters()[i];
        			IASTName n = CPPVisitor.findInnermostDeclarator(temp.getDeclarator()).getName();
        			if (n != name) {
        				n.setBinding(binding);
        				((CPPParameter)binding).addDeclaration(n);
        			}
        		}
    		}
    	}
    	return binding;	
    }

	public boolean isStatic() {
		return hasStorageClass(IASTDeclSpecifier.sc_static);
	}

    public boolean isMutable() {
        return hasStorageClass(ICPPASTDeclSpecifier.sc_mutable);
    }

    public boolean isInline() throws DOMException {
        IASTName name = (IASTName) getDefinition();
        IASTNode[] ns = getDeclarations();
        int i = -1;
        do {
            if (name != null) {
                IASTNode parent = name.getParent();
	            while(!(parent instanceof IASTDeclaration))
	                parent = parent.getParent();
	            
	            IASTDeclSpecifier declSpec = null;
	            if (parent instanceof IASTSimpleDeclaration) {
	                declSpec = ((IASTSimpleDeclaration)parent).getDeclSpecifier();
	            } else if (parent instanceof IASTFunctionDefinition) {
	                declSpec = ((IASTFunctionDefinition)parent).getDeclSpecifier();
	            }
	            
	            if (declSpec != null && declSpec.isInline())
                    return true;
            }
            if (ns != null && ++i < ns.length)
                name = (IASTName) ns[i];
            else
                break;
        } while(name != null);
        return false;
    }

    public boolean isExternC() throws DOMException {
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

    public boolean isExtern() {
        return hasStorageClass(IASTDeclSpecifier.sc_extern);
    }

    public boolean isAuto() {
        return hasStorageClass(IASTDeclSpecifier.sc_auto);
    }

    public boolean isRegister() {
        return hasStorageClass(IASTDeclSpecifier.sc_register);
    }

    public boolean takesVarArgs() {
    	ICPPASTFunctionDeclarator fdecl= getDeclaratorByName(getDefinition());
    	if (fdecl == null) {
    		IASTName[] ns = (IASTName[]) getDeclarations();
    		if (ns != null && ns.length > 0) {
    			for (int i = 0; i < ns.length && fdecl == null; i++) {
					IASTName name = ns[i];
					fdecl= getDeclaratorByName(name);
				}
    		}
    	}
    	if (fdecl != null) {
    		return fdecl.takesVarArgs();
    	}
        return false;
    }

	private ICPPASTFunctionDeclarator getDeclaratorByName(IASTNode node) {
		// skip qualified names and nested declarators.
    	while (node != null) {
    		node= node.getParent();	
    		if (node instanceof ICPPASTFunctionDeclarator) {
    			return ((ICPPASTFunctionDeclarator) node);
    		}
        }
    	return null;
	}

    public boolean isStatic(boolean resolveAll) {
    	return hasStorageClass(IASTDeclSpecifier.sc_static);
    }
    
	@Override
	public String toString() {
		StringBuilder result = new StringBuilder();
		result.append(getName());
		IFunctionType t = getType();
		result.append(t != null ? ASTTypeUtil.getParameterTypeString(t) : "()"); //$NON-NLS-1$
		return result.toString();
	}

	public IType[] getExceptionSpecification() throws DOMException {
    	ICPPASTFunctionDeclarator declarator = getDeclaratorByName(getDefinition());
		if (declarator != null) {
			IASTTypeId[] astTypeIds = declarator.getExceptionSpecification();
			if (astTypeIds.equals(ICPPASTFunctionDeclarator.NO_EXCEPTION_SPECIFICATION)) {
				return null;
			}
			if (astTypeIds.equals(IASTTypeId.EMPTY_TYPEID_ARRAY)) {
				return IType.EMPTY_TYPE_ARRAY;
			}
			
			IType[] typeIds = new IType[astTypeIds.length];
			for (int i=0; i<astTypeIds.length; ++i) {
				typeIds[i] = CPPVisitor.createType(astTypeIds[i]);
			}
			return typeIds;
		}
		return null;
	}
}
