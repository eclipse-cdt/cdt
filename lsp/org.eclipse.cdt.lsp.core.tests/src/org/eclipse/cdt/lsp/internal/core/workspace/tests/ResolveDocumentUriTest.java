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
package org.eclipse.cdt.lsp.internal.core.workspace.tests;

import static org.junit.Assert.assertFalse;

import org.eclipse.cdt.lsp.internal.core.workspace.ResolveDocumentUri;
import org.eclipse.core.filebuffers.FileBuffers;
import org.eclipse.core.filebuffers.LocationKind;
import org.eclipse.core.runtime.Path;
import org.eclipse.jface.text.Document;
import org.junit.Test;

public class ResolveDocumentUriTest {

	private final ResolveDocumentUri uri;

	public ResolveDocumentUriTest() {
		uri = new ResolveDocumentUri();
	}

	@Test
	public void emptyDocument() {
		assertFalse(uri.apply(new Document()).isPresent());
	}

	@Test
	public void externalDocument() {
		Document document = new Document();
		FileBuffers.createTextFileBufferManager().createEmptyDocument(new Path("some.c"), LocationKind.LOCATION);
		assertFalse(uri.apply(document).isPresent());
	}

	@Test
	public void workspaceDocument() {
		Document document = new Document();
		FileBuffers.createTextFileBufferManager().createEmptyDocument(new Path("some.c"), LocationKind.LOCATION);
		//it's a pity! see https://bugs.eclipse.org/bugs/show_bug.cgi?id=566044
		assertFalse(uri.apply(document).isPresent());
	}

}
