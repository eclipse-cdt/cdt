/*******************************************************************************
 * Copyright (c) 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
/*
 * Created on Apr 13, 2005
 */
package org.eclipse.cdt.core.dom.ast.cpp;

import org.eclipse.cdt.core.dom.ast.DOMException;
import org.eclipse.cdt.core.dom.ast.IType;

/**
 * @author aniefer
 */
public interface ICPPTemplateTemplateParameter extends ICPPTemplateParameter, ICPPClassTemplate {

	public ICPPTemplateParameter[] getTemplateParameters() throws DOMException;
	
	/**
	 * The default type for this parameter. May be null
	 * 
	 * @return
	 */
	public IType getDefault() throws DOMException;
}
