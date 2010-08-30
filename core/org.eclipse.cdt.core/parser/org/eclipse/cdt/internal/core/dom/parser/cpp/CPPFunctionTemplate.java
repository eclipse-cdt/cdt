/*******************************************************************************
 * Copyright (c) 2005, 2010 IBM Corporation and others.
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

import static org.eclipse.cdt.internal.core.dom.parser.cpp.semantics.SemanticUtil.TDEF;
import static org.eclipse.cdt.internal.core.dom.parser.cpp.semantics.SemanticUtil.getNestedType;

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
import org.eclipse.cdt.core.dom.ast.IProblemBinding;
import org.eclipse.cdt.core.dom.ast.IScope;
import org.eclipse.cdt.core.dom.ast.IType;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTDeclSpecifier;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTFunctionDeclarator;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPClassTemplatePartialSpecialization;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPFunctionTemplate;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPFunctionType;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPParameter;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPTemplateParameter;
import org.eclipse.cdt.internal.core.dom.parser.ASTInternal;
import org.eclipse.cdt.internal.core.dom.parser.ASTQueries;
import org.eclipse.cdt.internal.core.dom.parser.ProblemBinding;
import org.eclipse.cdt.internal.core.dom.parser.cpp.semantics.CPPVisitor;

/**
 * Implementation of function templates
 */
public class CPPFunctionTemplate extends CPPTemplateDefinition
		implements ICPPFunctionTemplate, ICPPInternalFunction {
	public static final class CPPFunctionTemplateProblem extends ProblemBinding
			implements ICPPFunctionTemplate {
		public CPPFunctionTemplateProblem(IASTNode node, int id, char[] arg) {
			super(node, id, arg);
		}
		public ICPPTemplateParameter[] getTemplateParameters() throws DOMException {
			throw new DOMException(this);
		}
		public ICPPClassTemplatePartialSpecialization[] getTemplateSpecializations() throws DOMException {
			throw new DOMException(this);		
		}
		public ICPPParameter[] getParameters() throws DOMException {
			throw new DOMException(this);
		}
		public IScope getFunctionScope() throws DOMException {
			throw new DOMException(this);
		}
		@Override
		public ICPPFunctionType getType() throws DOMException {
			throw new DOMException(this);
		}
		public int getRequiredArgumentCount() throws DOMException {
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
		IASTDeclarator fdecl= getDeclaratorByName(node);
		if (fdecl instanceof ICPPASTFunctionDeclarator) {
			updateFunctionParameterBindings((ICPPASTFunctionDeclarator) fdecl);
			super.addDefinition(node);
		}
	}
	
	@Override
	public void addDeclaration(IASTNode node) {
		if (!(node instanceof IASTName))
			return;
		IASTDeclarator fdecl= getDeclaratorByName(node);
		if (fdecl == null)
			return;

		if (fdecl instanceof ICPPASTFunctionDeclarator)
			updateFunctionParameterBindings((ICPPASTFunctionDeclarator) fdecl);
		
		super.addDeclaration(node);
	}

	private ICPPASTFunctionDeclarator getFirstFunctionDtor() {
		IASTDeclarator dtor= getDeclaratorByName(getDefinition());
        if (dtor instanceof ICPPASTFunctionDeclarator) 
        	return (ICPPASTFunctionDeclarator) dtor;

        IASTNode[] decls = getDeclarations();
        if (decls != null) {
        	for (IASTNode decl : decls) {
        		dtor= getDeclaratorByName(decl);
        		if (dtor instanceof ICPPASTFunctionDeclarator) 
        			return (ICPPASTFunctionDeclarator) dtor;
        	}
        }
        return null;
	}

	public ICPPParameter[] getParameters() {
		ICPPASTFunctionDeclarator fdecl= getFirstFunctionDtor();
		if (fdecl != null) {
			IASTParameterDeclaration[] params = fdecl.getParameters();
			int size = params.length;
			if (size == 0) {
				return ICPPParameter.EMPTY_CPPPARAMETER_ARRAY;
			}
			ICPPParameter[] result = new ICPPParameter[size];
			for (int i = 0; i < size; i++) {
				IASTParameterDeclaration p = params[i];
				final IASTName pname = ASTQueries.findInnermostDeclarator(p.getDeclarator()).getName();
				final IBinding binding= pname.resolveBinding();
				if (binding instanceof ICPPParameter) {
					result[i]= (ICPPParameter) binding;
				} else {
					result[i] = new CPPParameter.CPPParameterProblem(p,
							IProblemBinding.SEMANTIC_INVALID_TYPE, pname.toCharArray());
				}
			}
			return result;
		}
		return CPPBuiltinParameter.createParameterList(getType());
	}

	public int getRequiredArgumentCount() throws DOMException {
		return CPPFunction.getRequiredArgumentCount(getParameters());
	}

	public boolean hasParameterPack() {
		ICPPParameter[] pars= getParameters();
		return pars.length > 0 && pars[pars.length-1].isParameterPack();
	}

	public IScope getFunctionScope() {
		return null;
	}

	public ICPPFunctionType getType() {
		if (type == null) {
			IASTName name = getTemplateName();
			IASTNode parent = name.getParent();
			while (parent.getParent() instanceof IASTDeclarator)
				parent = parent.getParent();

			IType temp = getNestedType(CPPVisitor.createType((IASTDeclarator)parent), TDEF);
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
	            while (parent != null && !(parent instanceof IASTDeclaration))
	                parent = parent.getParent();
	            
	            IASTDeclSpecifier declSpec = getDeclSpecifier((IASTDeclaration) parent);
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

	protected ICPPASTDeclSpecifier getDeclSpecifier(IASTDeclaration decl) {
		if (decl instanceof IASTSimpleDeclaration) {
		    return (ICPPASTDeclSpecifier) ((IASTSimpleDeclaration)decl).getDeclSpecifier();
		} 
		if (decl instanceof IASTFunctionDefinition) {
		    return (ICPPASTDeclSpecifier) ((IASTFunctionDefinition)decl).getDeclSpecifier();
		}
		return null;
	}

    public IBinding resolveParameter(CPPParameter param) {
		int pos= param.getParameterPosition();
		
    	final IASTNode[] decls= getDeclarations();
		int tdeclLen= decls == null ? 0 : decls.length;
    	for (int i= -1; i < tdeclLen; i++) {
    		IASTDeclarator tdecl;
    		if (i == -1) {
    			tdecl= getDeclaratorByName(getDefinition());
    			if (tdecl == null)
    				continue;
    		} else if (decls != null){
    			tdecl= getDeclaratorByName(decls[i]);
    			if (tdecl == null)
    				break;
    		} else {
    			break;
    		}
    		
    		if (tdecl instanceof ICPPASTFunctionDeclarator) {
    			IASTParameterDeclaration[] params = ((ICPPASTFunctionDeclarator) tdecl).getParameters();
    			if (pos < params.length) {
    				final IASTName oName = getParamName(params[pos]);
    				return oName.resolvePreBinding();
    			}
    		}
    	}
    	return param;
    }
    
    protected void updateFunctionParameterBindings(ICPPASTFunctionDeclarator fdtor) {
		IASTParameterDeclaration[] updateParams = fdtor.getParameters();

    	int k= 0;
    	final IASTNode[] decls= getDeclarations();
    	int tdeclLen= decls == null ? 0 : decls.length;
    	for (int i= -1; i < tdeclLen && k < updateParams.length; i++) {
    		IASTDeclarator tdecl;
    		if (i == -1) {
    			tdecl= getDeclaratorByName(getDefinition());
    			if (tdecl == null)
    				continue;
    		} else if (decls != null) {
    			tdecl= getDeclaratorByName(decls[i]);
    			if (tdecl == null)
    				break;
    		} else {
    			break;
    		}
    		
    		if (tdecl instanceof ICPPASTFunctionDeclarator) {
    			IASTParameterDeclaration[] params = ((ICPPASTFunctionDeclarator) tdecl).getParameters();
    			int end= Math.min(params.length, updateParams.length);
    			for (; k < end; k++) {
    				final IASTName oName = getParamName(params[k]);
    				IBinding b= oName.resolvePreBinding();
    				IASTName n = getParamName(updateParams[k]);
    				n.setBinding(b);
    				ASTInternal.addDeclaration(b, n);
    			}
    		}
    	}
    }

	private IASTName getParamName(final IASTParameterDeclaration paramDecl) {
		return ASTQueries.findInnermostDeclarator(paramDecl.getDeclarator()).getName();
	}

	public boolean isStatic() {
		return hasStorageClass(IASTDeclSpecifier.sc_static);
	}

    public boolean isMutable() {
        return hasStorageClass(IASTDeclSpecifier.sc_mutable);
    }

    public boolean isInline() {
        IASTName name = (IASTName) getDefinition();
        IASTNode[] ns = getDeclarations();
        int i = -1;
        do {
            if (name != null) {
                IASTNode parent = name.getParent();
				while (parent != null && !(parent instanceof IASTDeclaration))
					parent = parent.getParent();
	            
	            IASTDeclSpecifier declSpec = getDeclSpecifier((IASTDeclaration) parent);
	            
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

    public boolean isExtern() {
        return hasStorageClass(IASTDeclSpecifier.sc_extern);
    }

	public boolean isDeleted() {
		return CPPFunction.isDeletedDefinition(getDefinition());
	}

    public boolean isAuto() {
        return hasStorageClass(IASTDeclSpecifier.sc_auto);
    }

    public boolean isRegister() {
        return hasStorageClass(IASTDeclSpecifier.sc_register);
    }

    public boolean takesVarArgs() {
    	ICPPASTFunctionDeclarator fdecl= getFirstFunctionDtor();
    	if (fdecl != null) {
    		return fdecl.takesVarArgs();
    	}
        return false;
    }

	private IASTDeclarator getDeclaratorByName(IASTNode node) {
		// skip qualified names and nested declarators.
    	while (node != null) {
    		node= node.getParent();	
    		if (node instanceof IASTDeclarator) {
    			return ASTQueries.findTypeRelevantDeclarator((IASTDeclarator) node);
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

	public IType[] getExceptionSpecification() {
    	ICPPASTFunctionDeclarator declarator = getFirstFunctionDtor();
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
