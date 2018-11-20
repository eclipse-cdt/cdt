/*******************************************************************************
 * Copyright (c) 2013 Institute for Software, HSR Hochschule fuer Technik
 * Rapperswil, University of applied sciences and others
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Thomas Corbat (IFS) - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.core.parser;

/**
 * Implementation for the {@link IParserSettings} interface. Allows to configure
 * the parser with additional settings.
 * @since 5.6
 */
public class ParserSettings implements IParserSettings {
	private int maximumTrivialExpressionsInAggregateInitializers = -1;

	@Override
	public int getMaximumTrivialExpressionsInAggregateInitializers() {
		return maximumTrivialExpressionsInAggregateInitializers;
	}

	/**
	 * Sets the maximum number of trivial expressions in aggregate initializers.
	 * @param value The new maximum number of trivial expressions in aggregate initializers.
	 */
	public void setMaximumTrivialExpressionsInAggregateInitializers(int value) {
		maximumTrivialExpressionsInAggregateInitializers = value;
	}
}
