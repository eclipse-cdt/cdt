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
 * Created on Dec 10, 2004
 */
package org.eclipse.cdt.core.dom.ast.c;

import org.eclipse.cdt.core.dom.ast.IBasicType;

/**
 * @author aniefer
 */
public interface ICBasicType extends IBasicType {
	// Extra types in C
	public static final int t_Bool = ICASTSimpleDeclSpecifier.t_Bool;
	public static final int t_Complex = ICASTSimpleDeclSpecifier.t_Complex;
	public static final int t_Imaginary = ICASTSimpleDeclSpecifier.t_Imaginary;
	
	public boolean isLongLong();
}
