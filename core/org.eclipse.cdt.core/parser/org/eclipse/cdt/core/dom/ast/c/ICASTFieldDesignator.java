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

import org.eclipse.cdt.core.dom.ast.ASTNodeProperty;
import org.eclipse.cdt.core.dom.ast.IASTName;

/**
 * @author jcamelon
 */
public interface ICASTFieldDesignator extends ICASTDesignator {

    public static final ASTNodeProperty FIELD_NAME = new ASTNodeProperty( "Designator Field Name"); //$NON-NLS-1$
    
    public IASTName getName();
    public void setName( IASTName name );
}
