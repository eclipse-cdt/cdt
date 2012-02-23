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
 * Default implementation of <code>{@link IArgsSeparator}</code> that uses an empty space as
 * the delimiter to separate the arguments to pass to an external tool.
 *
 * @author alruiz@google.com (Alex Ruiz)
 *
 * @since 2.1
 */
public class SpaceDelimitedArgsSeparator implements IArgsSeparator {
	private static final String[] NO_ARGS = new String[0];
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
