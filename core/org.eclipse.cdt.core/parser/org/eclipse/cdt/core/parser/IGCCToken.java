/*******************************************************************************
 * Copyright (c) 2002, 2009 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    John Camelon (IBM Rational Software) - Initial API and implementation
 *    Ed Swartz (Nokia)
 *******************************************************************************/

package org.eclipse.cdt.core.parser;

/**
 * @noextend This interface is not intended to be extended by clients.
 * @noimplement This interface is not intended to be implemented by clients.
 */
public interface IGCCToken extends IToken {
	
	public static final int t_typeof = FIRST_RESERVED_IGCCToken;
	public static final int t___alignof__ = FIRST_RESERVED_IGCCToken + 1;
	public static final int tMAX = FIRST_RESERVED_IGCCToken + 2;
	public static final int tMIN = FIRST_RESERVED_IGCCToken + 3;
	public static final int t__attribute__ = FIRST_RESERVED_IGCCToken + 4;
	public static final int t__declspec = FIRST_RESERVED_IGCCToken + 5;
	
}
