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

import org.eclipse.cdt.internal.core.parser.Token;

/**
 * @author jcamelon
 *
 */
public class TemplateDeclaration extends Declaration implements IScope, IAccessable, ITemplateParameterListOwner {

	private final boolean exported;
	private AccessSpecifier visibility = null; 
	private Token firstToken, lastToken; 
	private List declarations = new ArrayList(); 
	private TemplateParameterList templateParms = null;                

	public TemplateDeclaration( IScope ownerScope, Token exported )
	{
		super( ownerScope );
		this.firstToken = exported; 
		this.exported = exported.getType() == Token.t_export ? true : false;
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
	 * @return
	 */
	public TemplateParameterList getTemplateParms() {
		return templateParms;
	}

	/**
	 * @param list
	 */
	public void setTemplateParms(TemplateParameterList list) {
		templateParms = list;
	}

	/**
	 * @return
	 */
	public Token getFirstToken() {
		return firstToken;
	}

	/**
	 * @return
	 */
	public Token getLastToken() {
		return lastToken;
	}

	/**
	 * @param token
	 */
	public void setFirstToken(Token token) {
		firstToken = token;
		setStartingOffset( getFirstToken().getOffset() );
	}

	/**
	 * @param token
	 */
	public void setLastToken(Token token) {
		lastToken = token;
		setTotalLength( getLastToken().getOffset() + getLastToken().getLength() - getStartingOffset() ); 
	}

	/**
	 * @return
	 */
	public int getVisibility() {
		if( visibility == null ) return AccessSpecifier.v_unknown;
		return visibility.getAccess();
	}

	/**
	 * @param specifier
	 */
	public void setVisibility(int visibility) {
		if( this.visibility == null ) this.visibility = new AccessSpecifier(visibility);
		else this.visibility.setAccess(visibility);
	}



}
