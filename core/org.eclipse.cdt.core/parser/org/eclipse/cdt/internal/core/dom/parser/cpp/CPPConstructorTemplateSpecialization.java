/*************************************************************************
 * Copyright (c) 2005, 2010 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    IBM Corporation - initial API and implementation
 *    Markus Schorn (Wind River Systems)
 *******************************************************************************/
package org.eclipse.cdt.internal.core.dom.parser.cpp;

import org.eclipse.cdt.core.dom.ast.cpp.ICPPClassType;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPConstructor;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPTemplateParameterMap;

/**
 * Specialization of a constructor template
 */
public class CPPConstructorTemplateSpecialization extends CPPMethodTemplateSpecialization 
		implements ICPPConstructor {
	
	public CPPConstructorTemplateSpecialization(ICPPConstructor original,
			ICPPClassType owner, ICPPTemplateParameterMap tpmap) {
		super(original, owner, tpmap);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.dom.ast.cpp.ICPPConstructor#isExplicit()
	 */
	public boolean isExplicit() {
		return ((ICPPConstructor)getSpecializedBinding()).isExplicit();
	}
}
