/*******************************************************************************
 * Copyright (c) 2004, 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * IBM - Initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.internal.core.dom.parser.cpp;

import org.eclipse.cdt.core.dom.ast.gnu.cpp.IGPPASTPointer;

/**
 * @author jcamelon
 */
public class GPPASTPointer extends CPPASTPointer implements IGPPASTPointer {

    private boolean isRestrict;

    /* (non-Javadoc)
     * @see org.eclipse.cdt.core.dom.ast.gnu.cpp.IGPPASTPointer#isRestrict()
     */
    public boolean isRestrict() {
        return isRestrict;
    }

    /* (non-Javadoc)
     * @see org.eclipse.cdt.core.dom.ast.gnu.cpp.IGPPASTPointer#setRestrict(boolean)
     */
    public void setRestrict(boolean value) {
        isRestrict = value;
    }

}
