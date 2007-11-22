/*******************************************************************************
 * Copyright (c) 2007 Wind River Systems, Inc. and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Markus Schorn - initial API and implementation
 *******************************************************************************/

package org.eclipse.cdt.internal.core.dom.parser.cpp;

import org.eclipse.cdt.core.dom.ast.DOMException;


/**
 * Interface for ast-internal implementations of variable bindings.
 * @since 5.0
 */
public interface ICPPInternalVariable extends ICPPInternalBinding {

    /**
     * Returns whether there is a static declaration for this variable.
     * @param checkHeaders if <code>false</code> declarations within header files are not 
     * considered.
     */
    public boolean isStatic(boolean checkHeaders) throws DOMException;
}
