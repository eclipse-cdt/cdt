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
package org.eclipse.cdt.lsp.internal.core.tests;

import static org.junit.Assert.assertEquals;

import org.eclipse.cdt.lsp.LanguageProtocolExtension;
import org.eclipse.cdt.lsp.LanguageServerConfiguration;
import org.eclipse.cdt.lsp.internal.core.ContributedProtocolExtensions;
import org.junit.Test;

public class ContributedProtocolExtensionsTest {

	private final LanguageServerConfiguration s1 = new FakeServerConfiguration("s1");
	private final LanguageServerConfiguration s2 = new FakeServerConfiguration("s2");
	private final LanguageServerConfiguration s3 = new FakeServerConfiguration("s3");
	private final LanguageProtocolExtension p1s1 = new FakeProtocolExtension("s1");
	private final LanguageProtocolExtension p2s1 = new FakeProtocolExtension("s1");
	private final LanguageProtocolExtension p3s2 = new FakeProtocolExtension("s2");

	@Test
	public void positive() {
		ContributedProtocolExtensions extensions = new ContributedProtocolExtensions();
		assertEquals(0, extensions.all().size());
		assertEquals(0, extensions.applicable(s1).size());
		assertEquals(0, extensions.applicable(s2).size());
		assertEquals(0, extensions.applicable(s3).size());
		extensions.register(p1s1);
		extensions.register(p2s1);
		extensions.register(p3s2);
		assertEquals(3, extensions.all().size());
		assertEquals(2, extensions.applicable(s1).size());
		assertEquals(1, extensions.applicable(s2).size());
		assertEquals(0, extensions.applicable(s3).size());
		extensions.unregister(p1s1);
		extensions.unregister(p2s1);
		extensions.unregister(p3s2);
		assertEquals(0, extensions.all().size());
		assertEquals(0, extensions.applicable(s1).size());
		assertEquals(0, extensions.applicable(s2).size());
		assertEquals(0, extensions.applicable(s3).size());
	}

}
