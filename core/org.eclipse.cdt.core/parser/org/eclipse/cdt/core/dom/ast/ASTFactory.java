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
package org.eclipse.cdt.core.dom.ast;


/**
 * This is a factory class that will create a IASTTranslationUnit for a given
 * source file or string.
 * 
 * @author Doug Schaefer
 */
public class ASTFactory {

    public static IASTTranslationUnit parseString(StringBuffer buff) {
    	// TODO maybe we should plug in the parser here, no?
    	return null;
    }
    
}
