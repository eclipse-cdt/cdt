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

import org.eclipse.cdt.core.dom.ast.IBinding;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPMethod;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPScope;
import org.eclipse.cdt.core.parser.util.ObjectMap;

/**
 * @author aniefer
 */
public class CPPMethodInstance extends CPPFunctionInstance implements
		ICPPMethod {

	/**
	 * @param scope
	 * @param orig
	 * @param argMap
	 */
	public CPPMethodInstance(ICPPScope scope, IBinding orig, ObjectMap argMap) {
		super(scope, orig, argMap);
		// TODO Auto-generated constructor stub
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.dom.ast.cpp.ICPPMember#getVisibility()
	 */
	public int getVisibility() {
		// TODO Auto-generated method stub
		return 0;
	}

}
