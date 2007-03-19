/*******************************************************************************
 * Copyright (c) 2005, 2007 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * IBM - Initial API and implementation
 * /
 *******************************************************************************/
/*
 * Created on May 2, 2005
 */
package org.eclipse.cdt.internal.core.dom.parser.cpp;

import org.eclipse.cdt.core.dom.ast.DOMException;
import org.eclipse.cdt.core.dom.ast.IBinding;
import org.eclipse.cdt.core.dom.ast.IProblemBinding;
import org.eclipse.cdt.core.dom.ast.IType;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPClassTemplate;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPClassTemplatePartialSpecialization;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPScope;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPSpecialization;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPTemplateDefinition;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPTemplateParameter;
import org.eclipse.cdt.core.parser.util.ArrayUtil;
import org.eclipse.cdt.core.parser.util.ObjectMap;

/**
 * @author aniefer
 *
 */
public class CPPClassTemplateSpecialization extends CPPClassSpecialization
		implements ICPPClassTemplate, ICPPInternalClassTemplate {

	private ObjectMap instances = null;
	private ICPPClassTemplatePartialSpecialization [] partialSpecializations = null;
	
	/**
	 * @param specialized
	 * @param scope
	 * @param argumentMap
	 */
	public CPPClassTemplateSpecialization(IBinding specialized,
			ICPPScope scope, ObjectMap argumentMap) {
		super(specialized, scope, argumentMap);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.dom.ast.cpp.ICPPClassTemplate#getPartialSpecializations()
	 */
	public ICPPClassTemplatePartialSpecialization[] getPartialSpecializations() {
		partialSpecializations = (ICPPClassTemplatePartialSpecialization[]) ArrayUtil.trim( ICPPClassTemplatePartialSpecialization.class, partialSpecializations );
		return partialSpecializations;
	}


	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.dom.ast.cpp.ICPPTemplateDefinition#getTemplateParameters()
	 */
	public ICPPTemplateParameter[] getTemplateParameters() throws DOMException {
		ICPPClassTemplate template = (ICPPClassTemplate) getSpecializedBinding();
		return template.getTemplateParameters();
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
		ICPPTemplateDefinition template = null;
		
		try {
			template = CPPTemplates.matchTemplatePartialSpecialization( (ICPPClassTemplate) this, arguments );
		} catch (DOMException e) {
			return e.getProblem();
		}
		
		if( template instanceof IProblemBinding )
			return template;
		if( template != null && template instanceof ICPPClassTemplatePartialSpecialization ){
			return ((CPPTemplateDefinition)template).instantiate( arguments );	
		}
		
		return CPPTemplates.instantiateTemplate( this, arguments, argumentMap );
	}

	public ICPPSpecialization deferredInstance(IType[] arguments) {
		// TODO Auto-generated method stub
		return null;
	}

	public void addPartialSpecialization( ICPPClassTemplatePartialSpecialization spec ){
		partialSpecializations = (ICPPClassTemplatePartialSpecialization[]) ArrayUtil.append( ICPPClassTemplatePartialSpecialization.class, partialSpecializations, spec );
	}
}
