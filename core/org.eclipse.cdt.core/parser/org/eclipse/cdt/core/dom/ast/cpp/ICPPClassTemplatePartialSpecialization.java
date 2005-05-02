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
 * Created on Mar 23, 2005
 */
package org.eclipse.cdt.core.dom.ast.cpp;

import org.eclipse.cdt.core.dom.ast.DOMException;
import org.eclipse.cdt.core.dom.ast.IType;

/**
 * @author aniefer
 */

/**
 * This interface represents a class template partial specialization.  A partial specialization is
 * a class template in its own right.
 * 
 * eg:
 * template <class T> class A {};     //the primary class template
 * template <class T> class A<T*> {}; //a partial specialization of the primary class template
 *
 * @author aniefer
 */
public interface ICPPClassTemplatePartialSpecialization extends ICPPClassTemplate {
	public static final ICPPClassTemplatePartialSpecialization[] EMPTY_PARTIAL_SPECIALIZATION_ARRAY = new ICPPClassTemplatePartialSpecialization[0];
	
	/**
	 * get the arguments to this specialization
	 * @return
	 */
	public IType [] getArguments() throws DOMException;
	
	
	/**
	 * get the ICPPTemplateDefinition which this is a specialization of
	 * @return
	 */
	public ICPPClassTemplate getPrimaryClassTemplate() throws DOMException;
}
