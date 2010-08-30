/*************************************************************************
 * Copyright (c) 2005, 2010 IBM Corporation and others.
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

import org.eclipse.cdt.core.dom.ast.cpp.ICPPClassType;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPConstructor;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPTemplateParameterMap;

/**
 * Specialization of a constructor for a class-template or class-template specialization
 */
public class CPPConstructorSpecialization extends CPPMethodSpecialization
		implements ICPPConstructor {

	public CPPConstructorSpecialization(ICPPConstructor orig, ICPPClassType owner,
			ICPPTemplateParameterMap argMap) {
		super(orig, owner, argMap);
	}
}
