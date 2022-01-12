/*******************************************************************************
 * Copyright (c) 2009, 2010 Wind River Systems, Inc. and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *    Markus Schorn - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.core.parser;

/**
 * Interface for tokens of kind {@link IToken#tINACTIVE_CODE_START}, {@link IToken#tINACTIVE_CODE_SEPARATOR} and
 * {@link IToken#tINACTIVE_CODE_END}.
 * @since 5.1
 * @noextend This interface is not intended to be extended by clients.
 * @noimplement This interface is not intended to be implemented by clients.
 */
public interface IInactiveCodeToken extends IToken {

	/**
	 * @return {@code 0} for the start-token of the outermost branch (indicates that code in the
	 *         translation-unit outside of any branches precedes this token). <br>
	 *         A value greater than 0 indicating how deep the code preceding this token is nested
	 *         within code branches, otherwise.
	 */
	int getOldNesting();

	/**
	 * @return {@code 0} for the end-token of the outermost branch (indicates that code in the
	 *         translation-unit outside of any branches will follow). <br>
	 *         A value greater than 0 indicating how deep the code following this token is nested
	 *         within code branches, otherwise.
	 */
	int getNewNesting();
}
