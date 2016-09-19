/*******************************************************************************
 * Copyright (c) 2004, 2012 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Doug Schaefer (IBM) - Initial API and implementation
 *     Sergey Prigogin (Google)
 *******************************************************************************/
package org.eclipse.cdt.core.dom.ast.cpp;

import org.eclipse.cdt.core.dom.ast.IField;

/**
 * @noextend This interface is not intended to be extended by clients.
 * @noimplement This interface is not intended to be implemented by clients.
 */
public interface ICPPField extends IField, ICPPMember, ICPPVariable {
    public static final ICPPField[] EMPTY_CPPFIELD_ARRAY = {};
    
    /**
     * Returns the position of this field within its class owner's declared fields, or -1 if the position
     * cannot be determined.
     *
	 * @since 6.2
	 */
    int getFieldPosition();
}
