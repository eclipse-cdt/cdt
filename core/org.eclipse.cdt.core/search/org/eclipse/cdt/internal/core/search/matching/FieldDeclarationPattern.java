/*******************************************************************************
 * Copyright (c) 2003 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v0.5 
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v05.html
 * 
 * Contributors:
 *     IBM Corp. - Rational Software - initial implementation
 ******************************************************************************/
/*
 * Created on Jul 11, 2003
 */
package org.eclipse.cdt.internal.core.search.matching;


/**
 * @author aniefer
 *
 * To change the template for this generated type comment go to
 * Window>Preferences>Java>Code Generation>Code and Comments
 */
public class FieldDeclarationPattern extends VariableDeclarationPattern {

	/**
	 * @param name
	 * @param cs
	 * @param matchMode
	 * @param limitTo
	 * @param caseSensitive
	 */
	public FieldDeclarationPattern(char[] name, char[][] cs, int matchMode, LimitTo limitTo, boolean caseSensitive) {
		super( name, matchMode, limitTo, caseSensitive );
		// TODO Auto-generated constructor stub
	}

}
