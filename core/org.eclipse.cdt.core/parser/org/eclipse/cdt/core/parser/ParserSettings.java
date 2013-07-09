/*******************************************************************************
 * Copyright (c) 2013 Institute for Software, HSR Hochschule fuer Technik
 * Rapperswil, University of applied sciences and others
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
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
