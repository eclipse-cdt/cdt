/*******************************************************************************
 * Copyright (c) 2006, 2008 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     QNX - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.internal.errorparsers;

import org.eclipse.cdt.core.errorparsers.ErrorPattern;

/**
 * @deprecated use org.eclipse.cdt.core.errorparsers.AbstractErrorParser
 * this class is moved to public package
 */
@Deprecated
public class AbstractErrorParser extends org.eclipse.cdt.core.errorparsers.AbstractErrorParser{
	protected AbstractErrorParser(ErrorPattern[] patterns) {
		super(patterns);
	}

}
