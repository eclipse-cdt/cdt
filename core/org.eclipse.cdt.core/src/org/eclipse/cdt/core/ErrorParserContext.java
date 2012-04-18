/*******************************************************************************
 * Copyright (c) 2012 Google, Inc and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Alex Ruiz (Google) - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.core;

/**
 * Indicates the context in which <code>{@link org.eclipse.cdt.core.IErrorParser}</code>s can be 
 * used.
 *
 * @since 5.4
 */
public class ErrorParserContext {
	public static final int BUILD = 1 << 0;
	public static final int CODAN = 1 << 1;
	
	public static int getValue(String text) {
		if ("build".equals(text)) { //$NON-NLS-1$
			return BUILD;
		}
		if ("codan".equals(text)) { //$NON-NLS-1$
			return CODAN;
		}
		throw new IllegalArgumentException("Unknown context value: " + text); //$NON-NLS-1$
	}
}
