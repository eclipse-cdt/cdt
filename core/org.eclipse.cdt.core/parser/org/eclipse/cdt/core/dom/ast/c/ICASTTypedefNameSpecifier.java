/**********************************************************************
 * Copyright (c) 2002-2004 IBM Canada and others.
 * All rights reserved.   This program and the accompanying materials
 * are made available under the terms of the Common Public License v0.5
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v05.html
 * 
 * Contributors: 
 * IBM Rational Software - Initial API and implementation */
package org.eclipse.cdt.core.dom.ast.c;

import org.eclipse.cdt.core.dom.ast.IASTNamedTypeSpecifier;

/**
 * This interface is just as an IASTNamedTypeSpecifier, except that it also
 * includes the abiliy to use the restrict modifier.
 * 
 * @author jcamelon
 */
public interface ICASTTypedefNameSpecifier extends IASTNamedTypeSpecifier,
		ICASTDeclSpecifier {

}
