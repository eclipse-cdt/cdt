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
package org.eclipse.cdt.internal.core.dom.parser.cpp;

import org.eclipse.cdt.core.dom.ast.IBinding;
import org.eclipse.cdt.core.dom.ast.IScope;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPInstance;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPScope;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPTemplateDefinition;
import org.eclipse.cdt.core.parser.util.ObjectMap;

/**
 * @author aniefer
 */
public class CPPInstance implements ICPPInstance {
	private IBinding binding;
	private ObjectMap argMap;
	private ICPPScope scope;

	public CPPInstance( ICPPScope scope, IBinding orig, ObjectMap argMap ){
		this.binding = orig;
		this.argMap = argMap;
		this.scope = scope;
	}
	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.dom.ast.cpp.ICPPInstance#getOriginalBinding()
	 */
	public IBinding getOriginalBinding() {
		return binding;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.dom.ast.cpp.ICPPInstance#getTemplate()
	 */
	public ICPPTemplateDefinition getTemplate() {
		// TODO Auto-generated method stub
		return null;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.dom.ast.IBinding#getName()
	 */
	public String getName() {
		return binding.getName();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.dom.ast.IBinding#getNameCharArray()
	 */
	public char[] getNameCharArray() {
		return binding.getNameCharArray();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.dom.ast.IBinding#getScope()
	 */
	public IScope getScope() {
		return scope;
	}
	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.dom.ast.cpp.ICPPInstance#getArgumentMap()
	 */
	public ObjectMap getArgumentMap() {
		return argMap;
	}


}
