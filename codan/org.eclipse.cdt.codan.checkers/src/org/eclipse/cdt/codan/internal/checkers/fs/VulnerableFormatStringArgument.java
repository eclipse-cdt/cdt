/*******************************************************************************
 * Copyright (c) 2010, 2011 Meisam Fathi and others
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *    Meisam Fathi  - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.codan.internal.checkers.fs;

/**
 * @version 0.2 February 16, 2010
 * @author Meisam Fathi
 */
public class VulnerableFormatStringArgument {
	/**
	 * The index of the argument that is matched, starting at zero.
	 */
	private final int indexOfArgument;
	/**
	 * The string format argument that may contain the fault
	 */
	private final String argument;
	/**
	 * the size of the argument.
	 * <ul>
	 * <li><code>%15s ==> 15 </code>
	 * <li><code>%128s ==> 128 </code>
	 * <li><code>%s ==> infinity </code>
	 * </ul>
	 */
	private final int size;

	/**
	 * @param indexOfCurrentArgument
	 * @param group
	 */
	public VulnerableFormatStringArgument(final int indexOfArgument, final String rgument, final int size) {
		this.indexOfArgument = indexOfArgument;
		this.argument = rgument;
		this.size = size;
	}

	/**
	 * @return the indexOfArgument
	 */
	public int getArgumentIndex() {
		return this.indexOfArgument;
	}

	/**
	 * @return the argument
	 */
	public String getArgument() {
		return this.argument;
	}

	/**
	 * @return
	 */
	public int getArgumentSize() {
		return this.size;
	}
}