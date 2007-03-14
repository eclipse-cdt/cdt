/*******************************************************************************
 * Copyright (c) 2000, 2007 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     Anton Leherbauer (Wind River Systems)
 *******************************************************************************/
package org.eclipse.cdt.internal.formatter;

/**
 * Unchecked exception wrapping invalid input checked exception which may occur
 * when scanning original formatted source. 
 * 
 * @since 4.0
 */
public class AbortFormatting extends RuntimeException {

	private static final long serialVersionUID= -5796507276311428526L;
	Throwable nestedException;
	
	public AbortFormatting(String message) {
		super(message);
	}
	public AbortFormatting(Throwable cause) {
		super(cause);
	}
}
