/*******************************************************************************
 * Copyright (c) 2005, 2010 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Andrew Niefer (IBM) - Initial API and implementation
 *    Bryan Wilkinson (QNX)
 *    Markus Schorn (Wind River Systems)
 *******************************************************************************/
package org.eclipse.cdt.internal.core.dom.parser.cpp;

import org.eclipse.cdt.core.dom.ast.ASTTypeUtil;
import org.eclipse.cdt.core.dom.ast.DOMException;
import org.eclipse.cdt.core.dom.ast.IASTDeclSpecifier;
import org.eclipse.cdt.core.dom.ast.IASTDeclarator;
import org.eclipse.cdt.core.dom.ast.IASTFunctionDefinition;
import org.eclipse.cdt.core.dom.ast.IASTName;
import org.eclipse.cdt.core.dom.ast.IASTNode;
import org.eclipse.cdt.core.dom.ast.IASTParameterDeclaration;
import org.eclipse.cdt.core.dom.ast.IBinding;
import org.eclipse.cdt.core.dom.ast.IFunctionType;
import org.eclipse.cdt.core.dom.ast.IParameter;
import org.eclipse.cdt.core.dom.ast.IScope;
import org.eclipse.cdt.core.dom.ast.IType;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTFunctionDeclarator;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPFunction;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPFunctionType;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPParameter;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPTemplateParameterMap;
import org.eclipse.cdt.core.index.IIndexBinding;
import org.eclipse.cdt.internal.core.dom.parser.ASTInternal;
import org.eclipse.cdt.internal.core.dom.parser.ASTQueries;
import org.eclipse.cdt.internal.core.dom.parser.cpp.semantics.CPPVisitor;

/**
 * The specialization of a friend function in the context of a class specialization,
 * also used as base class for function instances.
 */
public class CPPFunctionSpecialization extends CPPSpecialization implements ICPPFunction, ICPPInternalFunction {
	private ICPPFunctionType type = null;
	private IParameter[] specializedParams = null;
	private IType[] specializedExceptionSpec = null;

	public CPPFunctionSpecialization(IBinding orig, IBinding owner, ICPPTemplateParameterMap argMap) {
		super(orig, owner, argMap);
	}
	
	private ICPPFunction getFunction() {
		return (ICPPFunction) getSpecializedBinding();
	}

	public IParameter[] getParameters() throws DOMException {
		if (specializedParams == null) {
			ICPPFunction function = (ICPPFunction) getSpecializedBinding();
			IParameter[] params = function.getParameters();
			specializedParams = new IParameter[params.length];
			for (int i = 0; i < params.length; i++) {
				specializedParams[i] = new CPPParameterSpecialization((ICPPParameter)params[i],
						this, getTemplateParameterMap());
			}
		}
		return specializedParams;
	}

	public IScope getFunctionScope() {
//		resolveAllDeclarations();
//	    if (definition != null) {
//			return definition.getFunctionScope();
//	    } 
//	        
//	    return declarations[0].getFunctionScope();
		return null;
	}

	public ICPPFunctionType getType() throws DOMException {
		if (type == null) {
			ICPPFunction function = (ICPPFunction) getSpecializedBinding();
			type = (ICPPFunctionType) specializeType(function.getType());
		}
		
		return type;
	}

	public boolean isMutable() {
		return false;
	}

	public boolean isInline() throws DOMException {
		if (getDefinition() != null) {
			IASTNode def = getDefinition();
			while (!(def instanceof IASTFunctionDefinition))
				def = def.getParent();
			return ((IASTFunctionDefinition)def).getDeclSpecifier().isInline();
		}
		return getFunction().isInline();
	}
	
	public boolean isExternC() throws DOMException {
		if (CPPVisitor.isExternC(getDefinition())) {
			return true;
		}
		return getFunction().isExternC();
	}

	public boolean isStatic() {
		return isStatic(true);
	}
	public boolean isStatic(boolean resolveAll) {
		//TODO resolveAll
		IBinding f = getSpecializedBinding();
		if (f instanceof ICPPInternalFunction)
			return ((ICPPInternalFunction)f).isStatic(resolveAll);
		if (f instanceof IIndexBinding && f instanceof ICPPFunction) {
			try {
				return ((ICPPFunction) f).isStatic();
			} catch(DOMException de) { /* cannot occur as we query the index */}
		}
		return CPPFunction.hasStorageClass(this, IASTDeclSpecifier.sc_static);
	}

	public boolean isExtern() throws DOMException {
		ICPPFunction f = (ICPPFunction) getSpecializedBinding();
		if (f != null)
			return f.isExtern();
		return CPPFunction.hasStorageClass(this, IASTDeclSpecifier.sc_extern);
	}

	public boolean isAuto() throws DOMException {
		ICPPFunction f = (ICPPFunction) getSpecializedBinding();
		if (f != null)
			return f.isAuto();
		return CPPFunction.hasStorageClass(this, IASTDeclSpecifier.sc_auto);
	}

	public boolean isRegister() throws DOMException {
		ICPPFunction f = (ICPPFunction) getSpecializedBinding();
		if (f != null)
			return f.isRegister();
		return CPPFunction.hasStorageClass(this, IASTDeclSpecifier.sc_register);
	}

	public boolean takesVarArgs() throws DOMException {
		ICPPFunction f = (ICPPFunction) getSpecializedBinding();
		if (f != null)
			return f.takesVarArgs();
		
		ICPPASTFunctionDeclarator dtor = (ICPPASTFunctionDeclarator) getDefinition();
        if (dtor != null) {
            return dtor.takesVarArgs();
        }
        ICPPASTFunctionDeclarator[] ds = (ICPPASTFunctionDeclarator[]) getDeclarations();
        if (ds != null && ds.length > 0) {
            return ds[0].takesVarArgs();
        }
        return false;
	}

    /* (non-Javadoc)
     * @see org.eclipse.cdt.internal.core.dom.parser.cpp.ICPPInternalFunction#resolveParameter(org.eclipse.cdt.core.dom.ast.IASTParameterDeclaration)
     */
    public IBinding resolveParameter(IASTParameterDeclaration param) {
        IASTDeclarator dtor = param.getDeclarator();
        while (dtor.getNestedDeclarator() != null)
            dtor = dtor.getNestedDeclarator();
        IASTName name = dtor.getName();
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
    	
        try {
            IParameter[] params = getParameters();
            if (i < params.length) {
        	    final IParameter myParam = params[i];
				name.setBinding(myParam);
        	    ASTInternal.addDeclaration(myParam, name);
        	    return myParam;
        	}

        } catch (DOMException e) {
            return e.getProblem();
        }
        return null;
    }
    
    @Override
	public void addDefinition(IASTNode node) {
        IASTNode n = node;
		while (n instanceof IASTName)
			n = n.getParent();
		if (!(n instanceof ICPPASTFunctionDeclarator))
			return;
	    updateParameterBindings((ICPPASTFunctionDeclarator) n);
        super.addDefinition(n);
	}

	@Override
	public void addDeclaration(IASTNode node) {
	    IASTNode n = node;
		while (n instanceof IASTName)
			n = n.getParent();
		if (!(n instanceof ICPPASTFunctionDeclarator))
			return;
	    updateParameterBindings((ICPPASTFunctionDeclarator) n);
        super.addDeclaration(n);
	}

    protected void updateParameterBindings(ICPPASTFunctionDeclarator fdtor) {
        IParameter[] params = null;
        try {
            params = getParameters();
        } catch (DOMException e) {
            return;
        }
        IASTParameterDeclaration[] nps = fdtor.getParameters();
        
    	// The lengths can be different, e.g.: f(void) and f().
    	final int end= Math.min(params.length, nps.length);
    	for (int i = 0; i < end; i++) {
    		final IParameter param = params[i];
			if (param != null) {
    		    IASTDeclarator dtor = nps[i].getDeclarator();
    		    dtor= ASTQueries.findInnermostDeclarator(dtor);
    		    IASTName name = dtor.getName();
    			name.setBinding(param);
    			ASTInternal.addDeclaration(param, name);
    		}
    	}
    }

	@Override
	public String toString() {
		StringBuilder result = new StringBuilder();
		result.append(getName());
		IFunctionType t = null;
		try {
			t = getType();
		} catch (DOMException e) {
		}
		result.append(t != null ? ASTTypeUtil.getParameterTypeString(t) : "()"); //$NON-NLS-1$
		ICPPTemplateParameterMap tpmap= getTemplateParameterMap();
		if (tpmap != null) {
			result.append(" "); //$NON-NLS-1$
			result.append(tpmap.toString());
		}
		return result.toString();
	}

	public IType[] getExceptionSpecification() throws DOMException {
		if (specializedExceptionSpec == null) {
			ICPPFunction function = (ICPPFunction) getSpecializedBinding();
			IType[] types = function.getExceptionSpecification();
			if (types != null) {
				IType[] specializedTypeList = new IType[types.length];
				for (int i=0; i<types.length; ++i) 
					specializedTypeList[i] = specializeType(types[i]);
			}
		}
		return specializedExceptionSpec;
	}
}
