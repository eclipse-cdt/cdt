/*******************************************************************************
 * Copyright (c) 2004, 2009 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Doug Schaefer (IBM) - Initial API and implementation
 *     Sergey Prigogin (Google)
 *     Markus Schorn (Wind River Systems)
 *******************************************************************************/
package org.eclipse.cdt.core.dom.ast;

import org.eclipse.cdt.core.dom.ast.cpp.ICPPTemplateTemplateParameter;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPTemplateTypeParameter;

/**
 * Interface for all c- and c++ types.
 * 
 * @noextend This interface is not intended to be extended by clients.
 * @noimplement This interface is not intended to be implemented by clients.
 */
public interface IType extends Cloneable {
	public static final IType[] EMPTY_TYPE_ARRAY = {};
	public static final ASTTypeMatcher TYPE_MATCHER = new ASTTypeMatcher();

    public Object clone();

    /**
     * Test whether this type is the same as the given one. A typedef is considered to be the same type as
     * it's target type.
     * See {@link ICPPTemplateTemplateParameter#isSameType(IType)} or {@link ICPPTemplateTypeParameter#isSameType(IType)}
     * for the semantics of comparing template parameters denoting types.
     */
    public boolean isSameType(IType type);
}
