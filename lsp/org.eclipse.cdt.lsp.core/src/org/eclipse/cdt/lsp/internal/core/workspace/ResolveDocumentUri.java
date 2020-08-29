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
package org.eclipse.cdt.lsp.internal.core.workspace;

import java.net.URI;
import java.util.Optional;
import java.util.function.Function;

import org.eclipse.jface.text.IDocument;
import org.eclipse.lsp4e.LSPEclipseUtils;

@SuppressWarnings("restriction")
public final class ResolveDocumentUri implements Function<IDocument, Optional<URI>> {

	private final ResolveDocumentFile file;

	public ResolveDocumentUri() {
		file = new ResolveDocumentFile();
	}

	@Override
	public Optional<URI> apply(IDocument document) {
		return Optional.ofNullable(document)//
				.flatMap(file)//
				//FIXME rewrite involved static utilities
				.flatMap(f -> Optional.ofNullable(LSPEclipseUtils.toUri(f)));
	}

}
