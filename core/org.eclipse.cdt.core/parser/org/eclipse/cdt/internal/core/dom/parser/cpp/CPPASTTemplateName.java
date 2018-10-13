/*******************************************************************************
 * Copyright (c) 2018
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *******************************************************************************/
package org.eclipse.cdt.internal.core.dom.parser.cpp;

import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTTemplateName;

/**
 * Represents a template name.
 */
public class CPPASTTemplateName extends CPPASTName implements ICPPASTTemplateName {
    public CPPASTTemplateName(char[] name) {
        super(name);
    }

    public CPPASTTemplateName() {
        super();
    }
}
