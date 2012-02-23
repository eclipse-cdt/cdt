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

/**
 * Parses and separates the value of an <code>{@link ArgsSetting}</code> into an array of
 * {@code String}s.
 *
 * @author alruiz@google.com (Alex Ruiz)
 */
public interface IArgsSeparator {
	/**
	 * Indicates that there are no arguments to pass to the external tool executable.
	 */
	String[] NO_ARGS = new String[0];

	/**
	 * Parses and separates the given value.
	 * @param args contains the arguments to pass to the external tool executable.
	 * @return the separated argument values.
	 */
	String[] separateArgs(String args);
}
