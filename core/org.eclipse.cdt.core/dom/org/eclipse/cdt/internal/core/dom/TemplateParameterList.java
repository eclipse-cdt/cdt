/**********************************************************************
 * Created on Mar 29, 2003
 *
 * Copyright (c) 2002,2003 IBM/Rational Software Corporation and others.
 * All rights reserved.   This program and the accompanying materials
 * are made available under the terms of the Common Public License v0.5
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v05.html
 * 
 * Contributors: 
 * IBM Ltd. - Rational Software - Initial API and implementation
***********************************************************************/
package org.eclipse.cdt.internal.core.dom;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * @author jcamelon
 */
public class TemplateParameterList implements IScope {

	private List parameters = new ArrayList(); 

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.internal.core.dom.IScope#addDeclaration(org.eclipse.cdt.internal.core.dom.Declaration)
	 */
	public void addDeclaration(Declaration declaration) {
		parameters.add( declaration );
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.internal.core.dom.IScope#getDeclarations()
	 */
	public List getDeclarations() {
		return Collections.unmodifiableList( parameters );
	}
}
