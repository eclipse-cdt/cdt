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
 * Interface for providing settings for the parser.
 * @since 5.6
 */
public interface IParserSettings {
	/**
	 * Returns the maximum number of trivial expressions in aggregate initializers. Exceeding numbers
	 * of trivial aggregate initializers should be skipped by the parser for performance reasons.
	 * A negative number indicates that the parser shall not skip any initializers.
	 */
	public int getMaximumTrivialExpressionsInAggregateInitializers();
}
