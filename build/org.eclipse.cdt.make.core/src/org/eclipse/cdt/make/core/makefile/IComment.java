/*******************************************************************************
 * Copyright (c) 2000, 2006 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     QNX Software Systems - Initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.make.core.makefile;

/**
 * Comments start with '#' and until the end of the line.
 */
public interface IComment  extends IDirective {

	final public static char POUND = '#';

	final public static String POUND_STRING = "#"; //$NON-NLS-1$

}
