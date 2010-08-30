/*******************************************************************************
 * Copyright (c) 2004, 2010 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Andrew Niefer (IBM Corporation) - initial API and implementation
 *     Markus Schorn (Wind River Systems)
 *******************************************************************************/
package org.eclipse.cdt.internal.core.dom.parser.cpp;

import org.eclipse.cdt.core.dom.ast.IASTNode;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTFunctionDeclarator;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPClassType;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPConstructor;

public class CPPConstructor extends CPPMethod implements ICPPConstructor {

    static public class CPPConstructorProblem extends CPPMethod.CPPMethodProblem implements ICPPConstructor {
        public CPPConstructorProblem(ICPPClassType owner, IASTNode node, int id, char[] arg) {
            super(owner, node, id, arg);
        }
    }

	public CPPConstructor(ICPPASTFunctionDeclarator declarator) {
		super(declarator);
	}

}
