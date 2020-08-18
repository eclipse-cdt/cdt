/*******************************************************************************
 * Copyright (c) 2018 Manish Khurana , Nathan Ridge and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *******************************************************************************/

package org.eclipse.cdt.internal.clangd;

import java.net.URI;

import org.eclipse.cdt.lsp.LanguageServerConfiguration;

public class ClangdLanguageServer implements LanguageServerConfiguration {

	public static final String CLANGD_ID = "clangd"; //$NON-NLS-1$

	@Override
	public String identifier() {
		return ClangdLanguageServer.CLANGD_ID;
	}

	@Override
	public String label() {
		return "ClangD";
	}

	@Override
	public Object options(Object defaults, URI uri) {
		return defaults;
	}

}
