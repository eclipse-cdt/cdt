/*******************************************************************************
 * Copyright (c) 2001, 2004 IBM - Rational Software and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v0.5 
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v05.html
 * 
 * Contributors:
 *     IBM - Rational Software - initial implementation
 ******************************************************************************/

package org.eclipse.cdt.internal.core.parser.scanner;

import org.eclipse.cdt.core.parser.CodeReader;

public class ScannerContextTop extends ScannerContextInclusion
{
	ScannerContextTop(CodeReader code) {
		super(code, null, 0);
	}
	
	public int getKind() {
		return ScannerContextInclusion.ContextKind.TOP;
	}
}
