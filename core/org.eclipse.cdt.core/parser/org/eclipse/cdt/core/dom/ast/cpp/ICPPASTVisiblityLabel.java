/**********************************************************************
 * Copyright (c) 2004 IBM Corporation and others.
 * All rights reserved.   This program and the accompanying materials
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors: 
 * IBM - Initial API and implementation
 **********************************************************************/
package org.eclipse.cdt.core.dom.ast.cpp;

import org.eclipse.cdt.core.dom.ast.IASTDeclaration;

/**
 * @author jcamelon
 */
public interface ICPPASTVisiblityLabel extends IASTDeclaration {

    public static final int v_public = 1;
    public static final int v_protected = 2;
    public static final int v_private = 3;
    
    public int getVisibility();
    public void setVisibility( int visibility );
    
}
