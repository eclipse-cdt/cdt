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


/**
 * @author jcamelon
 *
 */
public class TemplateParameter extends Declaration implements ITemplateParameterListOwner {

	private final int kind; 
	
	public final static int k_class = 2;
	public final static int k_typename = 3;
	public final static int k_template = 4;
	
	
	public TemplateParameter( int kind )
	{
		this.kind = kind; 
	}
	
	private Name name = null; 
	private Name typeId = null;  


	/**
	 * @return int
	 */
	public int getKind() {
		return kind;
	}

	/**
	 * @return Name
	 */
	public Name getName() {
		return name;
	}

	/**
	 * @return Name
	 */
	public Name getTypeId() {
		return typeId;
	}

	/**
	 * Sets the name.
	 * @param name The name to set
	 */
	public void setName(Name name) {
		this.name = name;
	}

	/**
	 * Sets the typeId.
	 * @param typeId The typeId to set
	 */
	public void setTypeId(Name typeId) {
		this.typeId = typeId;
	}

	TemplateParameterList list;

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.internal.core.dom.ITemplateParameterListOwner#getTemplateParms()
	 */
	public TemplateParameterList getTemplateParms() {
		return list;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.internal.core.dom.ITemplateParameterListOwner#setTemplateParms(org.eclipse.cdt.internal.core.dom.TemplateParameterList)
	 */
	public void setTemplateParms(TemplateParameterList list) {
		this.list = list;
	}

}
