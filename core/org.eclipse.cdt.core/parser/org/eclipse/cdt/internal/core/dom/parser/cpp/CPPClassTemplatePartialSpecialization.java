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
 * Created on Apr 5, 2005
 */
package org.eclipse.cdt.internal.core.dom.parser.cpp;

import org.eclipse.cdt.core.dom.ast.DOMException;
import org.eclipse.cdt.core.dom.ast.IBinding;
import org.eclipse.cdt.core.dom.ast.IType;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTTemplateId;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPClassTemplate;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPClassTemplatePartialSpecialization;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPScope;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPSpecialization;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPTemplateInstance;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPTemplateParameter;
import org.eclipse.cdt.core.parser.util.ObjectMap;

/**
 * @author aniefer
 */
public class CPPClassTemplatePartialSpecialization extends CPPClassTemplate implements
		ICPPClassTemplatePartialSpecialization, ICPPSpecialization {

	private IType [] arguments;
	/**
	 * @param name
	 */
	public CPPClassTemplatePartialSpecialization(ICPPASTTemplateId name) {
		super(name);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.dom.ast.cpp.ICPPTemplateSpecialization#getArguments()
	 */
	public IType[] getArguments() {
		if( arguments == null ){
			ICPPASTTemplateId id = (ICPPASTTemplateId) getTemplateName();
			arguments = CPPTemplates.createTypeArray( id.getTemplateArguments() );
		}
		return arguments;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.dom.ast.cpp.ICPPClassTemplatePartialSpecialization#getPrimaryClassTemplate()
	 */
	public ICPPClassTemplate getPrimaryClassTemplate() {
		ICPPASTTemplateId id = (ICPPASTTemplateId) getTemplateName();
		return (ICPPClassTemplate) id.getTemplateName().resolveBinding();
	}

	public IBinding instantiate( IType [] args ){
		ICPPSpecialization instance = getInstance( args );
		if( instance != null ){
			return instance;
		}
		
		IType [] specArgs = getArguments();
		if( specArgs.length != arguments.length ){
			return null;
		}
		
		ObjectMap argMap = new ObjectMap( specArgs.length );
		int numSpecArgs = specArgs.length;
		for( int i = 0; i < numSpecArgs; i++ ){
			IType spec = specArgs[i];
			IType arg = args[i];
			
			//If the argument is a template parameter, we can't instantiate yet, defer for later
			if( CPPTemplates.typeContainsTemplateParameter( arg ) ){
				return deferredInstance( args );
			}
			try {
				if( !CPPTemplates.deduceTemplateArgument( argMap,  spec, arg ) )
					return null;
			} catch (DOMException e) {
				return null;
			}
		}
		
		ICPPTemplateParameter [] params = getTemplateParameters();
		int numParams = params.length;
		for( int i = 0; i < numParams; i++ ){
			if( params[i] instanceof IType && !argMap.containsKey( params[i] ) )
				return null;
		}
		
		instance = (ICPPTemplateInstance) CPPTemplates.createInstance( (ICPPScope) getScope(), this, argMap, args );
		addSpecialization( args, instance );
		
		return instance;
	}

	public IBinding getSpecializedBinding() {
		return getPrimaryClassTemplate();
	}
}
