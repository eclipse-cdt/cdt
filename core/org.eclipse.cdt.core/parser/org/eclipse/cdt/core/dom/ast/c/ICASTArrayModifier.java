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

import org.eclipse.cdt.core.dom.ast.IASTArrayModifier;

/**
 * @author jcamelon
 */
public interface ICASTArrayModifier extends IASTArrayModifier {
    
    public boolean isConst();
    public boolean isStatic();
    public boolean isRestrict();
    public boolean isVolatile();
    
    public void setConst( boolean value );
    public void setVolatile( boolean value );
    public void setRestrict( boolean value );
    public void setStatic( boolean value );
    public boolean isVariableSized();
    public void setVariableSized( boolean value );
}
