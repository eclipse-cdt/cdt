/*******************************************************************************
 * Copyright (c) 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
/*
 * Created on Dec 21, 2004
 */
package org.eclipse.cdt.core.dom.ast.cpp;

import org.eclipse.cdt.core.dom.ast.DOMException;

/**
 * @author aniefer
 */
public interface ICPPConstructor extends ICPPMethod {

	/**
	 * Whether or not this constructor was declared as explicit
	 * 
	 * @return
	 * @throws DOMException
	 */
	boolean isExplicit() throws DOMException;

}
