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
 * Created on Mar 28, 2005
 */
package org.eclipse.cdt.core.dom.ast.cpp;

import org.eclipse.cdt.core.dom.ast.IType;
import org.eclipse.cdt.core.parser.util.ObjectMap;

/**
 * @author aniefer
 */

/**
 * This interface represents an instantiation of a class or function template.
 * An instantiated template is a specialization of that template.
 * 
 * An instance of a class template will also implement ICPPClassType and similarily
 * a function template instance will also implement ICPPFunction (or even ICPPMethod 
 * or ICPPConstructor as appropriate)
 * 
 * @author aniefer
 */
public interface ICPPTemplateInstance extends ICPPSpecialization {
	
	/**
	 * get the template that this was instantiated from
	 * @return
	 */
	public ICPPTemplateDefinition getTemplateDefinition();
	
	/**
	 * get the types of the arguments the template was instantiated with.
	 * @return
	 */
	public IType [] getArguments();
	
	/**
	 * return a map which maps from template parameter to the corresponding
	 * template argument 
	 * @return
	 */
	public ObjectMap getArgumentMap();
}
