/*******************************************************************************
 * Copyright (c) 2008, 2015 Wind River Systems, Inc. and others.
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
package org.eclipse.cdt.core.dom.parser.cpp;

/**
 * Configures the parser to accept POP C++,
 * see <a href=http://gridgroup.tic.hefr.ch/popc/index.php/Documentation>Documentation</a>
 * @since 5.1
 */
public class POPCPPParserExtensionConfiguration extends GPPParserExtensionConfiguration {
	private static POPCPPParserExtensionConfiguration sInstance = new POPCPPParserExtensionConfiguration();

	public static POPCPPParserExtensionConfiguration getInstance() {
		return sInstance;
	}

	@Override
	public boolean supportParameterInfoBlock() {
		return true;
	}
}
