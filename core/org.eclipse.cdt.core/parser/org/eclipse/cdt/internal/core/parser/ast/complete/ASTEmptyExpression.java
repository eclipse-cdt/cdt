/**********************************************************************
 * Copyright (c) 2002,2003 Rational Software Corporation and others.
 * All rights reserved.   This program and the accompanying materials
 * are made available under the terms of the Common Public License v0.5
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v05.html
 * 
 * Contributors: 
 * IBM Rational Software - Initial API and implementation */

package org.eclipse.cdt.internal.core.parser.ast.complete;

import java.util.List;

/**
 * @author jcamelon
 *
 */
public class ASTEmptyExpression extends ASTExpression {
	/**
	 * @param kind
	 * @param references
	 */
	public ASTEmptyExpression(Kind kind, List references) {
		super( kind, references );
	}
}
