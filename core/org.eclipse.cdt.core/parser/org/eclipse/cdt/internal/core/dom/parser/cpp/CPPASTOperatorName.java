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
package org.eclipse.cdt.internal.core.dom.parser.cpp;

import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTOperatorName;

/**
 * The imlemented ICPPASTOperatorName.
 * 
 * @author dsteffle
 */
public class CPPASTOperatorName extends CPPASTName implements ICPPASTOperatorName {
	public CPPASTOperatorName() {
		super();
	}
	
	public CPPASTOperatorName(char[] name) {
		super(name);
	}
}
