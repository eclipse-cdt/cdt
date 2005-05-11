/**********************************************************************
 * Copyright (c) 2005 IBM Corporation and others.
 * All rights reserved.   This program and the accompanying materials
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors: 
 * IBM - Initial API and implementation
 **********************************************************************/
/*
 * Created on Mar 29, 2005
 */
package org.eclipse.cdt.internal.core.dom.parser.cpp;

import org.eclipse.cdt.core.dom.ast.DOMException;
import org.eclipse.cdt.core.dom.ast.IASTName;
import org.eclipse.cdt.core.dom.ast.IASTParameterDeclaration;
import org.eclipse.cdt.core.dom.ast.IBinding;
import org.eclipse.cdt.core.dom.ast.IFunctionType;
import org.eclipse.cdt.core.dom.ast.IParameter;
import org.eclipse.cdt.core.dom.ast.IScope;
import org.eclipse.cdt.core.dom.ast.IType;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPDelegate;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPFunction;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPParameter;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPScope;
import org.eclipse.cdt.core.parser.util.ObjectMap;

/**
 * @author aniefer
 */
public class CPPFunctionInstance extends CPPInstance implements ICPPFunction, ICPPInternalFunction {
	private IFunctionType type = null;
	private IParameter [] parameters = null;
	/**
	 * @param scope
	 * @param orig
	 * @param argMap
	 * @param args
	 */
	public CPPFunctionInstance(ICPPScope scope, IBinding orig, ObjectMap argMap, IType[] args) {
		super(scope, orig, argMap, args);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.internal.core.dom.parser.cpp.ICPPInternalBinding#createDelegate(org.eclipse.cdt.core.dom.ast.IASTName)
	 */
	public ICPPDelegate createDelegate(IASTName name) {
		return new CPPFunction.CPPFunctionDelegate( name, this );
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.dom.ast.IFunction#getParameters()
	 */
	public IParameter[] getParameters() throws DOMException {
		if( parameters == null ){
			IParameter [] params = ((ICPPFunction)getTemplateDefinition()).getParameters();
			parameters = new IParameter[ params.length ];
			for (int i = 0; i < params.length; i++) {
				parameters[i] = new CPPParameterSpecialization( (ICPPParameter)params[i], null, getArgumentMap() );
			}
		}
		
		return parameters;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.dom.ast.IFunction#getFunctionScope()
	 */
	public IScope getFunctionScope() {
		// TODO Auto-generated method stub
		return null;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.dom.ast.IFunction#getType()
	 */
	public IFunctionType getType() throws DOMException {
		if( type == null ){
			type = (IFunctionType) CPPTemplates.instantiateType( ((ICPPFunction)getTemplateDefinition()).getType(), getArgumentMap() );
		}
		return type;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.dom.ast.IFunction#isStatic()
	 */
	public boolean isStatic() throws DOMException {
		return ((ICPPFunction)getTemplateDefinition()).isStatic();
	}

    /* (non-Javadoc)
     * @see org.eclipse.cdt.core.dom.ast.cpp.ICPPFunction#isMutable()
     */
    public boolean isMutable() throws DOMException {
        return ((ICPPFunction)getTemplateDefinition()).isMutable();
    }

    /* (non-Javadoc)
     * @see org.eclipse.cdt.core.dom.ast.cpp.ICPPFunction#isInline()
     */
    public boolean isInline() throws DOMException {
        return ((ICPPFunction)getTemplateDefinition()).isInline();
    }

    /* (non-Javadoc)
     * @see org.eclipse.cdt.core.dom.ast.IFunction#isExtern()
     */
    public boolean isExtern() throws DOMException {
        return ((ICPPFunction)getTemplateDefinition()).isExtern();
    }

    /* (non-Javadoc)
     * @see org.eclipse.cdt.core.dom.ast.IFunction#isAuto()
     */
    public boolean isAuto() throws DOMException {
        return ((ICPPFunction)getTemplateDefinition()).isAuto();
    }

    /* (non-Javadoc)
     * @see org.eclipse.cdt.core.dom.ast.IFunction#isRegister()
     */
    public boolean isRegister() throws DOMException {
        return ((ICPPFunction)getTemplateDefinition()).isRegister();
    }

    /* (non-Javadoc)
     * @see org.eclipse.cdt.core.dom.ast.IFunction#takesVarArgs()
     */
    public boolean takesVarArgs() throws DOMException {
        return ((ICPPFunction)getTemplateDefinition()).takesVarArgs();
    }

    /* (non-Javadoc)
     * @see org.eclipse.cdt.internal.core.dom.parser.cpp.ICPPInternalFunction#isStatic(boolean)
     */
    public boolean isStatic( boolean resolveAll ) {
        return ((ICPPInternalFunction)getTemplateDefinition()).isStatic( resolveAll );
    }

    /* (non-Javadoc)
     * @see org.eclipse.cdt.internal.core.dom.parser.cpp.ICPPInternalFunction#resolveParameter(org.eclipse.cdt.core.dom.ast.IASTParameterDeclaration)
     */
    public IBinding resolveParameter( IASTParameterDeclaration param ) {
        // TODO Auto-generated method stub
        return null;
    }
}
