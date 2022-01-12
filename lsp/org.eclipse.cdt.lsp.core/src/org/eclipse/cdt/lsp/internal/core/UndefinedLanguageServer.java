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
package org.eclipse.cdt.lsp.internal.core;

import java.net.URI;

import org.eclipse.cdt.lsp.LanguageServerConfiguration;

final class UndefinedLanguageServer implements LanguageServerConfiguration {

	@Override
	public String identifier() {
		return ""; //$NON-NLS-1$
	}

	@Override
	public String label() {
		return "<undefined>";
	}

	@Override
	public Object options(Object defaults, URI uri) {
		return defaults;
	}

}
