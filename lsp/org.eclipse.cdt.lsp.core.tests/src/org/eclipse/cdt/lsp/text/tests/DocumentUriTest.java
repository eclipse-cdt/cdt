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
package org.eclipse.cdt.lsp.text.tests;

import static org.junit.Assert.assertTrue;

import org.eclipse.cdt.lsp.internal.text.DocumentUri;
import org.eclipse.core.filebuffers.FileBuffers;
import org.eclipse.core.filebuffers.LocationKind;
import org.eclipse.core.runtime.Path;
import org.eclipse.jface.text.Document;
import org.junit.Test;

public class DocumentUriTest {

	private final DocumentUri uri;

	public DocumentUriTest() {
		uri = new DocumentUri();
	}

	@Test
	public void emptyDocument() {
		assertTrue(uri.apply(new Document()).isEmpty());
	}

	@Test
	public void externalDocument() {
		Document document = new Document();
		FileBuffers.createTextFileBufferManager().createEmptyDocument(new Path("some.c"), LocationKind.LOCATION);
		assertTrue(uri.apply(document).isEmpty());
	}

	@Test
	public void workspaceDocument() {
		Document document = new Document();
		FileBuffers.createTextFileBufferManager().createEmptyDocument(new Path("some.c"), LocationKind.LOCATION);
		//it's a pity! see https://bugs.eclipse.org/bugs/show_bug.cgi?id=566044
		assertTrue(uri.apply(document).isEmpty());
	}

}
