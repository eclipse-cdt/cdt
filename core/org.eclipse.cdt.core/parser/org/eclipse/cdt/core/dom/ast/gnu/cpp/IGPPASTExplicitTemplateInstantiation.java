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
package org.eclipse.cdt.core.dom.ast.gnu.cpp;

import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTExplicitTemplateInstantiation;

/**
 * @author jcamelon
 */
public interface IGPPASTExplicitTemplateInstantiation extends
        ICPPASTExplicitTemplateInstantiation {

    public static final int ti_static = 1;
    public static final int ti_inline = 2;
    public static final int ti_extern = 3;
    
    public int getModifier();
    public void setModifier( int value );
}
