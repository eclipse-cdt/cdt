/*******************************************************************************
 * Copyright (c) 2018 Nathan Ridge and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/

package org.eclipse.cdt.cquery;

/**
 * A class to contain constants that represent different roles
 * a symbol can have.
 * The constants are used as bit-flags to compose the value of
 * HighlightSymbol.role.
 */
public final class SymbolRole {
	public static final int Declaration = 1 << 0;
	public static final int Definition = 1 << 1;
	public static final int Reference = 1 << 2;
	public static final int Read = 1 << 3;
	public static final int Write = 1 << 4;
	public static final int Call = 1 << 5;
	public static final int Dynamic = 1 << 6;
	public static final int Address = 1 << 7;
	public static final int Implicit = 1 << 8;
}