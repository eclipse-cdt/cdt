/**********************************************************************
 * Created on Mar 25, 2003
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

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

/**
 * @author jcamelon
 *
 */
public class LinkageSpecification extends Declaration implements IScope {

	private List declarations = new LinkedList();
	private IScope ownerScope;
	private String languageLinkage; 
	
	LinkageSpecification( IScope owner, String linkage )
	{
		ownerScope = owner; 
		languageLinkage = linkage;
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
	 * @return IScope
	 */
	public IScope getOwnerScope() {
		return ownerScope;
	}

	/**
	 * @return String
	 */
	public String getLanguageLinkage() {
		return languageLinkage;
	}

}
