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
package org.eclipse.cdt.internal.core.parser2.cpp;

import org.eclipse.cdt.core.dom.ast.gnu.cpp.IGPPASTSimpleDeclSpecifier;

/**
 * @author jcamelon
 */
public class GPPASTSimpleDeclSpecifier extends CPPASTSimpleDeclSpecifier
        implements IGPPASTSimpleDeclSpecifier {

    private boolean longLong;

    /* (non-Javadoc)
     * @see org.eclipse.cdt.core.dom.ast.gnu.cpp.IGPPASTSimpleDeclSpecifier#isLongLong()
     */
    public boolean isLongLong() {
        return longLong;
    }

    /* (non-Javadoc)
     * @see org.eclipse.cdt.core.dom.ast.gnu.cpp.IGPPASTSimpleDeclSpecifier#setLongLong(boolean)
     */
    public void setLongLong(boolean value) {
        longLong = value;
    }

    /* (non-Javadoc)
     * @see org.eclipse.cdt.core.dom.ast.gnu.cpp.IGPPASTDeclSpecifier#isRestrict()
     */
    public boolean isRestrict() {
        // TODO Auto-generated method stub
        return false;
    }

    /* (non-Javadoc)
     * @see org.eclipse.cdt.core.dom.ast.gnu.cpp.IGPPASTDeclSpecifier#setRestrict(boolean)
     */
    public void setRestrict(boolean value) {
        // TODO Auto-generated method stub
        
    }

}
