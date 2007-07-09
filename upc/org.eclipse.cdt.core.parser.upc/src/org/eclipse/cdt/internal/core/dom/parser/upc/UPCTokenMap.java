/*******************************************************************************
* Copyright (c) 2006, 2007 IBM Corporation and others.
* All rights reserved. This program and the accompanying materials
* are made available under the terms of the Eclipse Public License v1.0
* which accompanies this distribution, and is available at
* http://www.eclipse.org/legal/epl-v10.html
*
* Contributors:
*     IBM Corporation - initial API and implementation
*********************************************************************************/
package org.eclipse.cdt.internal.core.dom.parser.upc;

import org.eclipse.cdt.core.dom.parser.c99.TokenMap;

public class UPCTokenMap extends TokenMap {

	public UPCTokenMap() {
		super(UPCParsersym.orderedTerminalSymbols);
	}

	public int getCompletionTokenKind() {
		return UPCParsersym.TK_Completion;
	}

	public int getEOFTokenKind() {
		return UPCParsersym.TK_EOF_TOKEN;
	}

	public int getEndOfCompletionTokenKind() {
		return UPCParsersym.TK_EndOfCompletion;
	}

	public int getIntegerTokenKind() {
		return UPCParsersym.TK_integer;
	}

	public int getInvalidTokenKind() {
		return UPCParsersym.TK_Invalid;
	}

	public int getStringLitTokenKind() {
		return UPCParsersym.TK_stringlit;
	}

	public int getIdentifierTokenKind() {
		return UPCParsersym.TK_identifier;
	}

}
