/*******************************************************************************
 * Copyright (c) 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/

/*
 * Created on Sep 16, 2004
 */
package org.eclipse.cdt.core.parser;

/**
 * @author aniefer
 */
public interface IMacro {
    //For object-like macros these will be the same, 
    //for function-like macros, the signature includes the parameters
    public char[] getSignature();
    public char[] getName();
}
