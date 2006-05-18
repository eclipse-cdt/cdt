/*******************************************************************************
 * Copyright (c) 2004, 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial implementation
 *******************************************************************************/
package org.eclipse.cdt.internal.core.parser.scanner2;

import org.eclipse.cdt.core.parser.IMacro;
import org.eclipse.cdt.internal.core.parser.scanner2.IScannerPreprocessorLog.IMacroDefinition;

/**
 * @author Doug Schaefer
 */
public class ObjectStyleMacro implements IMacro {

	public char[] name;
	protected char[] expansion;
    public IMacroDefinition attachment;
	
	public ObjectStyleMacro(char[] name, char[] expansion) {
		this.name = name;
		this.expansion = expansion;
	}

    public char[] getSignature() {
        return name;
    }
    
    public char[] getName(){
        return name;
    }
    
    public String toString() {
        return new String( name );
    }
    
    public char[] getExpansion() {
		return expansion;
	}
    
}
