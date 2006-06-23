/*******************************************************************************
 * Copyright (c) 2004, 2006 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/

/*
 * Created on Oct 5, 2004
 */
package org.eclipse.cdt.internal.core.parser.scanner2;

import org.eclipse.cdt.core.parser.util.CharArrayObjectMap;

/**
 * @author aniefer
 */
abstract public class DynamicFunctionStyleMacro extends FunctionStyleMacro {

    public DynamicFunctionStyleMacro( char[] name, char[][] arglist ) {
        super( name, "".toCharArray(), arglist ); //$NON-NLS-1$
    }

    public abstract char [] execute( CharArrayObjectMap argmap );
}
