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
/*
 * Created on Mar 23, 2005
 */
package org.eclipse.cdt.core.dom.ast.cpp;

import org.eclipse.cdt.core.dom.ast.DOMException;
import org.eclipse.cdt.core.dom.ast.IType;

/**
 * @author aniefer
 */
public interface ICPPTemplateSpecialization extends ICPPTemplateDefinition {
	public static final ICPPTemplateSpecialization[] EMPTY_TEMPLATE_SPECIALIZATION_ARRAY = new ICPPTemplateSpecialization[0];
	/**
	 * get the arguments to this specialization
	 * @return
	 */
	public IType [] getArguments() throws DOMException;
	
	/**
	 * is this a partial specialization? if not, this will be an explicit specialization
	 * @return
	 */
	public boolean isPartialSpecialization() throws DOMException;
	
	/**
	 * get the ICPPTemplateDefinition which this is a specialization of
	 * @return
	 */
	public ICPPTemplateDefinition getPrimaryTemplateDefinition() throws DOMException;
}
