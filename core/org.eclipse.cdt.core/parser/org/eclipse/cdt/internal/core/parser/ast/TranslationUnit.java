/**********************************************************************
 * Copyright (c) 2002,2003 Rational Software Corporation and others.
 * All rights reserved.   This program and the accompanying materials
 * are made available under the terms of the Common Public License v0.5
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v05.html
 * 
 * Contributors: 
 * IBM Rational Software - Initial API and implementation
***********************************************************************/
package org.eclipse.cdt.internal.core.parser.ast;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.eclipse.cdt.core.parser.ast.IDeclaration;
import org.eclipse.cdt.core.parser.ast.IScope;

/**
 * @author jcamelon
 *
 */
public class TranslationUnit implements IScope {

	private ArrayList declarations = new ArrayList(); 
	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.parser.ast.IScope#getDeclarations()
	 */
	public List getDeclarations() {
		return Collections.unmodifiableList( declarations );
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.parser.ast.IScope#addDeclaration(org.eclipse.cdt.core.parser.ast.IDeclaration)
	 */
	public void addDeclaration(IDeclaration declaration) {
		declarations.add( declaration );
	}

}
