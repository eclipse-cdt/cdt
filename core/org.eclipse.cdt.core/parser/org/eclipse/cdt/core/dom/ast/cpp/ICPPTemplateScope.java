/*******************************************************************************
 * Copyright (c) 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * IBM - Initial API and implementation
 *******************************************************************************/
/*
 * Created on Mar 11, 2005
 */
package org.eclipse.cdt.core.dom.ast.cpp;

import org.eclipse.cdt.core.dom.ast.DOMException;


/**
 * @author aniefer
 */
public interface ICPPTemplateScope extends ICPPScope {

	/**
	 * get the template that this scope represents
	 * @return
	 */
	public ICPPTemplateDefinition getTemplateDefinition() throws DOMException;

}
