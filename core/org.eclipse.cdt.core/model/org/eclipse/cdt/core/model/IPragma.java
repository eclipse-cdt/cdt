/*******************************************************************************
 * Copyright (c) 2021 Kichwa Coders Canada Inc. and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *******************************************************************************/

package org.eclipse.cdt.core.model;

import java.util.Optional;

/**
 * Represents a pragma statement.
 *
 * @noextend This interface is not intended to be extended by clients.
 * @noimplement This interface is not intended to be implemented by clients.
 * @since 7.3
 */
public interface IPragma extends ICElement, ISourceManipulation, ISourceReference {

	public interface PragmaMarkInfo {
		/**
		 * Whether the pragma indicates a divider before it.
		 */
		public boolean isDividerBeforeMark();

		/**
		 * Whether the pragma indicates a divider after it.
		 */
		public boolean isDividerAfterMark();

		/**
		 * The display string of the mark.
		 */
		public String getMarkName();
	}

	/**
	 * Returns whether this uses the pragma operator syntax, e.g: <code>_Pragma("once")</code>
	 * @since 5.2
	 */
	public boolean isPragmaOperator();

	/**
	 * Returns the PragmaMarkInfo if the pragma represents a <code>#pragma mark</code> or similar pragma
	 * that should be interpreted as such.
	 * @return {@link Optional} of the {@link PragmaMarkInfo}
	 */
	Optional<PragmaMarkInfo> getPragmaMarkInfo();
}
