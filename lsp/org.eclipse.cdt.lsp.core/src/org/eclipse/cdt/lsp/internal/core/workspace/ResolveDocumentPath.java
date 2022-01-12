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

import java.util.Optional;
import java.util.function.Function;

import org.eclipse.core.filebuffers.ITextFileBuffer;
import org.eclipse.core.runtime.IPath;
import org.eclipse.jface.text.IDocument;

public final class ResolveDocumentPath implements Function<IDocument, Optional<IPath>> {

	private final ResolveDocumentBuffer buffer;

	public ResolveDocumentPath() {
		buffer = new ResolveDocumentBuffer();
	}

	@Override
	public Optional<IPath> apply(IDocument document) {
		return Optional.ofNullable(document).flatMap(buffer).map(ITextFileBuffer::getLocation);
	}

}
