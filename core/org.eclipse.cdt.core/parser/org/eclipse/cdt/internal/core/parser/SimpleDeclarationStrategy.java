/*******************************************************************************
 * Copyright (c) 2002, 2006 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * IBM Rational Software - Initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.internal.core.parser;

import org.eclipse.cdt.core.parser.Enum;

/**
 * @author jcamelon
 *
 */
public class SimpleDeclarationStrategy extends Enum
{
    public static final SimpleDeclarationStrategy TRY_CONSTRUCTOR = new SimpleDeclarationStrategy( 1 );	
	public static final SimpleDeclarationStrategy TRY_FUNCTION = new SimpleDeclarationStrategy( 2 );
	public static final SimpleDeclarationStrategy TRY_VARIABLE = new SimpleDeclarationStrategy( 3 );
	
    /**
     * @param enumValue
     */
    public SimpleDeclarationStrategy(int enumValue)
    {
        super(enumValue);
    }
}
