/*******************************************************************************
 * Copyright (c) 2007, 2009 Wind River Systems, Inc. and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Anton Leherbauer (Wind River Systems) - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.core.model;

/**
 * Represents a label in assembly code.
 *
 * @since 5.0
 * @noextend This interface is not intended to be extended by clients.
 * @noimplement This interface is not intended to be implemented by clients.
 */
public interface IAsmLabel extends ICElement, ISourceManipulation, ISourceReference {

	/**
	 * Test whether this label is declared global.
	 * A global label is available to the linker.
	 *
	 * @return  <code>true</code> if the label is global
	 */
	boolean isGlobal();

}
