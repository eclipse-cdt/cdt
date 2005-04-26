/*************************************************************************
 * Copyright (c) 2005 IBM Corporation and others.
 * All rights reserved.   This program and the accompanying materials
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors: 
 * IBM - Initial API and implementation
 */
/*
 * Created on Apr 22, 2005
 */
package org.eclipse.cdt.core.dom.ast.cpp;

import org.eclipse.cdt.core.dom.ast.IFunctionType;

/**
 * @author aniefer
 *
 */
public interface ICPPFunctionType extends IFunctionType {

	/**
	 * returns true for a constant method
	 * @return
	 */
	public boolean isConst();
	
	/**
	 * returns true for a volatile method
	 * @return
	 */
	public boolean isVolatile();
}
