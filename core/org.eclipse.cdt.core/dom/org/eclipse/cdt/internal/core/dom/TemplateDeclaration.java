/**********************************************************************
 * Created on Mar 30, 2003
 *
 * Copyright (c) 2002,2003 Rational Software Corporation and others.
 * All rights reserved.   This program and the accompanying materials
 * are made available under the terms of the Common Public License v0.5
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v05.html
 * 
 * Contributors: 
 * Rational Software - Initial API and implementation
***********************************************************************/
package org.eclipse.cdt.internal.core.dom;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * @author jcamelon
 *
 */
public class TemplateDeclaration extends Declaration implements IScope, TemplateParameter.ITemplateParameterList {

	private final boolean exported; 
	private IScope ownerScope;  
	private List declarations = new ArrayList(); 

	public TemplateDeclaration( IScope ownerScope, boolean exported )
	{
		this.ownerScope = ownerScope;
		this.exported = exported;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.internal.core.dom.IScope#addDeclaration(org.eclipse.cdt.internal.core.dom.Declaration)
	 */
	public void addDeclaration(Declaration declaration) {
		declarations.add( declaration );
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.internal.core.dom.IScope#getDeclarations()
	 */
	public List getDeclarations() {
		return Collections.unmodifiableList( declarations );
	}

	/**
	 * @return boolean
	 */
	public boolean isExported() {
		return exported;
	}

	/**
	 * @return IScope
	 */
	public IScope getOwnerScope() {
		return ownerScope;
	}


	private List templateParameters = new ArrayList(); 
	/* (non-Javadoc)
	 * @see org.eclipse.cdt.internal.core.dom.TemplateParameter.ITemplateParameterList#getTemplateParameters()
	 */
	public List getTemplateParameters() {
		return Collections.unmodifiableList(templateParameters);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.internal.core.dom.TemplateParameter.ITemplateParameterList#addTemplateParameter(org.eclipse.cdt.internal.core.dom.TemplateParameter)
	 */
	public void addTemplateParameter(TemplateParameter parm) {
		templateParameters.add( parm );
	}

}
