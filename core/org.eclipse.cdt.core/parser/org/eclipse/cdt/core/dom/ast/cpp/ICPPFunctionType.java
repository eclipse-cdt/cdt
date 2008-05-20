/*******************************************************************************
 * Copyright (c) 2005-2008 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM - Initial API and implementation
 * 	   Sergey Prigogin (Google)
 *******************************************************************************/
/*
 * Created on Apr 22, 2005
 */
package org.eclipse.cdt.core.dom.ast.cpp;

import org.eclipse.cdt.core.dom.ast.IFunctionType;
import org.eclipse.cdt.core.dom.ast.IPointerType;

/**
 * @author aniefer
 */
public interface ICPPFunctionType extends IFunctionType {

	/**
	 * Returns type of implicit <code>this</code>. parameter, or null, if the function
	 * is not a class method or a static method.
	 */
	public IPointerType getThisType();
	
	/**
	 * Returns <code>true</code> for a constant method
	 */
	public boolean isConst();
	
	/**
	 * Returns <code>true</code> for a volatile method
	 */
	public boolean isVolatile();
}
