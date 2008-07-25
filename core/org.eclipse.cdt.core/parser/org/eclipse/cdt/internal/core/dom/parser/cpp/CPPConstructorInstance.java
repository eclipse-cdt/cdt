/*******************************************************************************
 * Copyright (c) 2007, 2008 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    QNX - Initial API and implementation
 *    Markus Schorn (Wind River Systems)
 *******************************************************************************/
package org.eclipse.cdt.internal.core.dom.parser.cpp;

import org.eclipse.cdt.core.dom.ast.DOMException;
import org.eclipse.cdt.core.dom.ast.IType;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPClassType;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPConstructor;
import org.eclipse.cdt.core.parser.util.ObjectMap;

/**
 * @author Bryan Wilkinson
 *
 */
public class CPPConstructorInstance extends CPPMethodInstance implements
		ICPPConstructor {

	public CPPConstructorInstance(ICPPClassType owner, ICPPConstructor orig, ObjectMap argMap, IType[] args) {
		super(owner, orig, argMap, args);
	}
	
	public boolean isExplicit() throws DOMException {
		return ((ICPPConstructor)getTemplateDefinition()).isExplicit();
	}
}
