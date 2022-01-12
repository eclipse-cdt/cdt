/*******************************************************************************
 * Copyright (c) 2020 ArSysOp and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Alexander Fedorov (ArSysOp) - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.lsp;

import org.eclipse.lsp4j.jsonrpc.services.ServiceEndpoints;

/**
 *
 * Declares the language protocol extension methods.
 *
 * @see ServiceEndpoints#getSupportedMethods(Class)
 *
 */
public interface LanguageProtocolExtension {

	/**
	 *
	 * @return the identifier of the target language server
	 * @see LanguageServerConfiguration#identifier()
	 */
	String targetIdentifier();

}
