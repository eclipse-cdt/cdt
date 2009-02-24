/*******************************************************************************
 * Copyright (c) 2005, 2009 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    IBM - Initial API and implementation
 *    Markus Schorn (Wind River Systems)
 *******************************************************************************/
package org.eclipse.cdt.internal.core.dom.parser.cpp;

import org.eclipse.cdt.core.dom.ast.IASTName;
import org.eclipse.cdt.core.dom.ast.IASTParameterDeclaration;
import org.eclipse.cdt.core.dom.ast.IBinding;
import org.eclipse.cdt.core.dom.ast.IParameter;
import org.eclipse.cdt.core.dom.ast.IScope;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTFunctionDeclarator;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPFunctionType;
import org.eclipse.cdt.internal.core.dom.parser.ASTInternal;
import org.eclipse.cdt.internal.core.dom.parser.ASTQueries;

/**
 * The CPPImplicitFunction is used to represent implicit functions that exist on the translation
 * unit but are not actually part of the physical AST created by CDT.
 * 
 * An example is GCC built-in functions.
 */
public class CPPImplicitFunction extends CPPFunction {

	private IParameter[] parms=null;
	private IScope scope=null;
    private ICPPFunctionType functionType=null;
	private boolean takesVarArgs=false;
	private char[] name=null;
	
	public CPPImplicitFunction(char[] name, IScope scope, ICPPFunctionType type, IParameter[] parms, boolean takesVarArgs) {
        super( null );
        this.name=name;
		this.scope=scope;
		this.functionType= type;
		this.parms=parms;
		this.takesVarArgs=takesVarArgs;
	}

    @Override
	public IParameter [] getParameters() {
        return parms;
    }
    
    @Override
	public ICPPFunctionType getType() {
    	return functionType;
    }
    
    @Override
	public String getName() {
        return String.valueOf( name );
    }

    /* (non-Javadoc)
     * @see org.eclipse.cdt.core.dom.ast.IBinding#getNameCharArray()
     */
    @Override
	public char[] getNameCharArray() {
        return name;
    }
    /* (non-Javadoc)
     * @see org.eclipse.cdt.core.dom.ast.IBinding#getScope()
     */
    @Override
	public IScope getScope() {
        return scope;
    }
    
    /* (non-Javadoc)
     * @see org.eclipse.cdt.core.dom.ast.IFunction#getFunctionScope()
     */
    @Override
	public IScope getFunctionScope() {
        return null;
    }
    
    @Override
	public IBinding resolveParameter(IASTParameterDeclaration param) {
		IASTName aName = ASTQueries.findInnermostDeclarator(param.getDeclarator()).getName();
		IParameter binding = (IParameter) aName.getBinding();
		if (binding != null)
			return binding;

		// get the index in the parameter list
		ICPPASTFunctionDeclarator fdtor = (ICPPASTFunctionDeclarator) param.getParent();
		IASTParameterDeclaration[] ps = fdtor.getParameters();
		int i = 0;
		for (; i < ps.length; i++) {
			if (param == ps[i])
				break;
		}

		// set the binding for the corresponding parameter in all known defns and decls
		binding = parms[i];
		IASTParameterDeclaration temp = null;
		if (definition != null) {
			temp = definition.getParameters()[i];
			IASTName n = ASTQueries.findInnermostDeclarator(temp.getDeclarator()).getName();
			n.setBinding(binding);
			ASTInternal.addDeclaration(binding, n);
		}
		if (declarations != null) {
			for (int j = 0; j < declarations.length && declarations[j] != null; j++) {
				temp = declarations[j].getParameters()[i];
				IASTName n = ASTQueries.findInnermostDeclarator(temp.getDeclarator()).getName();
				n.setBinding(binding);
				ASTInternal.addDeclaration(binding, n);
			}
		}
		return binding;
    }
   
    @Override
	protected void updateParameterBindings(ICPPASTFunctionDeclarator fdtor) {
		if (parms != null) {
			IASTParameterDeclaration[] nps = fdtor.getParameters();
			if (nps.length != parms.length)
				return;

			for (int i = 0; i < nps.length; i++) {
				IASTName aName = ASTQueries.findInnermostDeclarator(nps[i].getDeclarator()).getName();
				final IParameter param = parms[i];
				aName.setBinding(param);
				ASTInternal.addDeclaration(param, aName);
			}
		}
	}

    @Override
	public boolean takesVarArgs() {
        return takesVarArgs;
    }
    
    @Override
	public IBinding getOwner() {
    	return null;
    }
}
