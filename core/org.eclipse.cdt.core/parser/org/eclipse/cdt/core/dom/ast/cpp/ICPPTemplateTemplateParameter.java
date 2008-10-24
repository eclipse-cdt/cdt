/*******************************************************************************
 * Copyright (c) 2005, 2008 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Andrew Niefer (IBM Corporation) - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.core.dom.ast.cpp;

import org.eclipse.cdt.core.dom.ast.DOMException;
import org.eclipse.cdt.core.dom.ast.IType;

/**
 * Template parameters of type template
 */
public interface ICPPTemplateTemplateParameter extends ICPPTemplateParameter, ICPPClassTemplate {

	public ICPPTemplateParameter[] getTemplateParameters() throws DOMException;
	
	/**
	 * Return the default value for this parameter, or <code>null</code>.
	 */
	public IType getDefault() throws DOMException;
}
