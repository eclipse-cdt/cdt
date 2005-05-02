/*************************************************************************
 * Copyright (c) 2005 IBM Corporation and others.
 * All rights reserved.   This program and the accompanying materials
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors: 
 * IBM - Initial API and implementation
 */
/*
 * Created on Apr 29, 2005
 */
package org.eclipse.cdt.internal.core.dom.parser.cpp;

import org.eclipse.cdt.core.dom.ast.DOMException;
import org.eclipse.cdt.core.dom.ast.IASTName;
import org.eclipse.cdt.core.dom.ast.IBinding;
import org.eclipse.cdt.core.dom.ast.IType;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPDelegate;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPFunction;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPFunctionTemplate;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPScope;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPSpecialization;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPTemplateParameter;
import org.eclipse.cdt.core.parser.util.ObjectMap;

/**
 * @author aniefer
 *
 */
public class CPPFunctionTemplateSpecialization extends CPPFunctionSpecialization implements
		ICPPFunctionTemplate, ICPPFunction, ICPPInternalTemplate {

	private ObjectMap instances = null;
	
	public CPPFunctionTemplateSpecialization(IBinding specialized, ICPPScope scope, ObjectMap argumentMap) {
		super(specialized, scope, argumentMap);
	}

	public ICPPTemplateParameter[] getTemplateParameters() throws DOMException {
		ICPPFunctionTemplate template = (ICPPFunctionTemplate) getSpecializedBinding();
		return template.getTemplateParameters();
	}

	public ICPPDelegate createDelegate(IASTName name) {
		// TODO Auto-generated method stub
		return null;
	}

	public void addSpecialization(IType[] arguments, ICPPSpecialization specialization) {
		if( instances == null )
			instances = new ObjectMap(2);
		instances.put( arguments, specialization );
	}
	
	public ICPPSpecialization getInstance( IType [] arguments ) {
		if( instances == null )
			return null;
		
		int found = -1;
		for( int i = 0; i < instances.size(); i++ ){
			IType [] args = (IType[]) instances.keyAt( i );
			if( args.length == arguments.length ){
				int j = 0;
				for(; j < args.length; j++) {
					if( !( args[j].isSameType( arguments[j] ) ) )
						break;
				}
				if( j == args.length ){
					found = i;
					break;
				}
			}
		}
		if( found != -1 ){
			return (ICPPSpecialization) instances.getAt(found);
		}
		return null;
	}
	
	public IBinding instantiate(IType[] arguments) {
		return CPPTemplates.instantiateTemplate( this, arguments, argumentMap ); 
	}

	public ICPPSpecialization deferredInstance(IType[] arguments) {
		// TODO Auto-generated method stub
		return null;
	}
}
