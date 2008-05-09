/*******************************************************************************
 * Copyright (c) 2005, 2008 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     Markus Schorn (Wind River Systems)
 *******************************************************************************/
package org.eclipse.cdt.internal.core.dom.parser.cpp;

import org.eclipse.cdt.core.dom.ast.DOMException;
import org.eclipse.cdt.core.dom.ast.IASTParameterDeclaration;
import org.eclipse.cdt.core.dom.ast.IBinding;
import org.eclipse.cdt.core.dom.ast.IFunction;
import org.eclipse.cdt.core.dom.ast.IFunctionType;
import org.eclipse.cdt.core.dom.ast.IParameter;
import org.eclipse.cdt.core.dom.ast.IScope;
import org.eclipse.cdt.core.dom.ast.IType;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPDeferredTemplateInstance;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPFunction;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPFunctionTemplate;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPParameter;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPTemplateDefinition;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPTemplateParameter;
import org.eclipse.cdt.core.parser.util.ObjectMap;
import org.eclipse.cdt.internal.core.dom.parser.ASTInternal;
import org.eclipse.cdt.internal.core.dom.parser.cpp.semantics.CPPTemplates;
import org.eclipse.cdt.internal.core.dom.parser.cpp.semantics.CPPVisitor;

/**
 * @author aniefer
 */
public class CPPDeferredFunctionInstance extends CPPInstance implements	ICPPFunction, ICPPInternalFunction, ICPPDeferredTemplateInstance {
	private IParameter [] parameters;
	private IType[] arguments;
	private IFunctionType functionType;


	public CPPDeferredFunctionInstance( ICPPFunctionTemplate template, IType[] arguments ) {
		super( null, template, null, arguments );
		this.arguments = arguments;
		this.argumentMap = createArgumentMap( arguments );
	}

	private ObjectMap createArgumentMap( IType [] args ){
		ICPPTemplateDefinition template = getTemplateDefinition();
		ICPPTemplateParameter [] params;
		try {
			params = template.getTemplateParameters();
		} catch (DOMException e) {
			return null;
		}
		ObjectMap map = new ObjectMap( params.length );
		for( int i = 0; i < params.length; i++ ){
			if( i < args.length )
				map.put( params[i], args[i] );
		}
		return map;
	}
	

	@Override
	public IType[] getArguments() {
		return arguments;
	}
	
	public IParameter[] getParameters() throws DOMException {
		if( getArgumentMap() == null )
			return ((ICPPFunction)getTemplateDefinition()).getParameters();
		if( parameters == null ){
			IParameter [] params = ((ICPPFunction)getTemplateDefinition()).getParameters();
			parameters = new IParameter[ params.length ];
			for (int i = 0; i < params.length; i++) {
				parameters[i] = new CPPParameterSpecialization( (ICPPParameter)params[i], null, getArgumentMap() );
			}
		}
		
		return parameters;

	}


	public IScope getFunctionScope() {
		// TODO Auto-generated method stub
		return null;
	}


	public IFunctionType getType() throws DOMException {
		if( functionType == null ){
            IFunctionType ft = ((ICPPFunction)getTemplateDefinition()).getType(); 
            IType returnType = ft.getReturnType();
			returnType = CPPTemplates.instantiateType( returnType, getArgumentMap(), null);
			functionType = CPPVisitor.createImplicitFunctionType( returnType, getParameters() );
        }
        
        return functionType;
	}


	public boolean isStatic() throws DOMException {
		return ((ICPPFunction)getTemplateDefinition()).isStatic();
	}

	public boolean isMutable() throws DOMException {
		return ((ICPPFunction)getTemplateDefinition()).isMutable();
	}

	public boolean isInline() throws DOMException {
		return ((ICPPFunction)getTemplateDefinition()).isInline();	
	}

	public boolean isExternC() throws DOMException {
		return ((ICPPFunction)getTemplateDefinition()).isExternC();	
	}

	public boolean isExtern() throws DOMException {
		return ((ICPPFunction)getTemplateDefinition()).isExtern();
	}

	public boolean isAuto() throws DOMException {
		return ((ICPPFunction)getTemplateDefinition()).isAuto();
	}

	public boolean isRegister() throws DOMException {
		return ((ICPPFunction)getTemplateDefinition()).isRegister();
	}

	public boolean takesVarArgs() throws DOMException {
		return ((ICPPFunction)getTemplateDefinition()).takesVarArgs();
	}

    public boolean isStatic( boolean resolveAll) {
    	try {
			return ASTInternal.isStatic((IFunction) getTemplateDefinition(), resolveAll);
		} catch (DOMException e) {
			return false;
		}
    }

    public IBinding resolveParameter( IASTParameterDeclaration param ) {
        // TODO Auto-generated method stub
        return null;
    }
}
