/*******************************************************************************
 * Copyright (c) 2000 - 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.internal.core.parser.ast.complete;

import java.util.Collections;

import org.eclipse.cdt.core.parser.ast.IASTExpression;

/**
 * @author jcamelon
 */
public class ASTLiteralIntegerExpression extends ASTExpression implements IASTExpression {
	private final boolean isHex;
	private final long literal;
	/**
	 * @param kind
	 * @param literal
	 * @param isHex
	 */
	public ASTLiteralIntegerExpression(Kind kind, long literal, boolean isHex) {
		super( kind, Collections.EMPTY_LIST );
		this.literal = literal;
		this.isHex = isHex;
	}
	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.parser.ast.IASTExpression#getLiteralString()
	 */
	public String getLiteralString() {
		if( isHex )
			return Long.toHexString( literal );
		return Long.toString( literal );
	}
}
