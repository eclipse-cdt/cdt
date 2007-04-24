/*******************************************************************************
 * Copyright (c) 2007 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * QNX - Initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.internal.core.dom.parser.cpp;

import org.eclipse.cdt.core.dom.ast.DOMException;
import org.eclipse.cdt.core.dom.ast.IBinding;
import org.eclipse.cdt.core.dom.ast.IType;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPConstructor;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPScope;
import org.eclipse.cdt.core.parser.util.ObjectMap;

/**
 * @author Bryan Wilkinson
 *
 */
public class CPPConstructorInstance extends CPPMethodInstance implements
		ICPPConstructor {

	/**
	 * @param scope
	 * @param orig
	 * @param argMap
	 * @param args
	 */
	public CPPConstructorInstance(ICPPScope scope, IBinding orig, ObjectMap argMap, IType[] args) {
		super(scope, orig, argMap, args);
	}
	
	public boolean isExplicit() throws DOMException {
		return ((ICPPConstructor)getTemplateDefinition()).isExplicit();
	}
}
