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
 * Indicates how <code>{@link org.eclipse.cdt.core.IErrorParser}</code>s are used.
 *
 * @since 5.4
 */
public class ErrorParserContext {
	public static final int BUILD = 1;
	public static final int CODAN = 1 << 1;
	public static final int BUILD_AND_CODAN = BUILD | CODAN;
	
	public static int getValue(String text) {
		if (text == null || text.isEmpty() || text.equals("build")) { //$NON-NLS-1$
			return BUILD;
		}
		if (text.equals("codan")) { //$NON-NLS-1$
			return CODAN;
		}
		if (text.equals("build,codan")) { //$NON-NLS-1$
			return BUILD_AND_CODAN;
		}
		throw new IllegalArgumentException("Unknown context value: " + text); //$NON-NLS-1$
	}
}
