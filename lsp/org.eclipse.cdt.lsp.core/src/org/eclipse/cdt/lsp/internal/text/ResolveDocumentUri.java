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
package org.eclipse.cdt.lsp.internal.text;

import java.net.URI;
import java.util.Optional;
import java.util.function.Function;

import org.eclipse.jface.text.IDocument;
import org.eclipse.lsp4e.LSPEclipseUtils;

@SuppressWarnings("restriction")
public final class ResolveDocumentUri implements Function<IDocument, Optional<URI>> {

	@Override
	public Optional<URI> apply(IDocument document) {
		return Optional.ofNullable(document)//
				//FIXME rewrite involved static utilities and contribute the result back to LSP4E
				.flatMap(d -> Optional.ofNullable(LSPEclipseUtils.getFile(d)))
				.flatMap(f -> Optional.ofNullable(LSPEclipseUtils.toUri(f)));
	}

}
