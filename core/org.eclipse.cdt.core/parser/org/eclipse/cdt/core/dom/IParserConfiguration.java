/*******************************************************************************
 * Copyright (c) 2004, 2013 IBM Corporation and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     IBM - Initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.core.dom;

import org.eclipse.cdt.core.parser.IScannerInfo;

/**
 * @deprecated Used only by other deprecated interfaces
 * @noreference This interface is not intended to be referenced by clients.
 */
@Deprecated
public interface IParserConfiguration {
	/**
	 * @return IScannerInfo representing the build information required to parse.
	 */
	public IScannerInfo getScannerInfo();

	/**
	 * @return String representing dialect name for the language
	 */
	public String getParserDialect();
}
