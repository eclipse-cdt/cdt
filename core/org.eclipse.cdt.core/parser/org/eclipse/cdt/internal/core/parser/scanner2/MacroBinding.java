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
package org.eclipse.cdt.internal.core.parser.scanner2;

import org.eclipse.cdt.core.dom.ast.DOMException;
import org.eclipse.cdt.core.dom.ast.IMacroBinding;
import org.eclipse.cdt.core.dom.ast.IScope;

public class MacroBinding implements IMacroBinding {

    private final char[] name;
    private final IScope scope;
    private final IScannerPreprocessorLog.IMacroDefinition definition;

    public MacroBinding( char [] name, IScope scope, IScannerPreprocessorLog.IMacroDefinition definition )
    {
        this.name = name;
        this.scope = scope;
        this.definition = definition;
    }
    
    public String getName() {
        return new String( name );
    }

    public char[] getNameCharArray() {
        return name;
    }

    public IScope getScope() throws DOMException {
        return scope;
    }

    /**
     * @return Returns the definition.
     */
    public IScannerPreprocessorLog.IMacroDefinition getDefinition() {
        return definition;
    }
    

}
