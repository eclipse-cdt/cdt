/*******************************************************************************
 * Copyright (c) 2012 Google, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Alex Ruiz  - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.codan.core.externaltool;

import java.util.regex.Pattern;

/**
 * Separates the value of an <code>{@link ArgsSetting}</code> using an empty space as delimiter.
 *
 * @author alruiz@google.com (Alex Ruiz)
 */
public class SpaceDelimitedArgsSeparator implements IArgsSeparator {
	private static final Pattern EMPTY_SPACE_PATTERN = Pattern.compile("\\s+"); //$NON-NLS-1$

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String[] separateArgs(String args) {
		if (args == null || args.isEmpty()) {
			return NO_ARGS;
		}
		return EMPTY_SPACE_PATTERN.split(args);
	}
}
