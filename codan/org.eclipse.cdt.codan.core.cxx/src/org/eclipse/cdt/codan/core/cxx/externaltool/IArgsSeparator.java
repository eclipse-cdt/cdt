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
package org.eclipse.cdt.codan.core.cxx.externaltool;

/**
 * Parses a given {@code String} containing the arguments to pass to an external tool and separates
 * them into individual values.
 *
 * @author alruiz@google.com (Alex Ruiz)
 *
 * @since 2.1
 */
public interface IArgsSeparator {
	/**
	 * Parses a given {@code String} containing the arguments to pass to an external tool and
	 * separates them into individual values.
	 * @param args contains the arguments to pass to the external tool executable.
	 * @return the separated argument values.
	 */
	String[] separateArgs(String args);
}
