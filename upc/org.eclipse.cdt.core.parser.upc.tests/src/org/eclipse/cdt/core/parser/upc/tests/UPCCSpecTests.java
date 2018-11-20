/*******************************************************************************
 * Copyright (c) 2006, 2008 IBM Corporation and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.core.parser.upc.tests;

import org.eclipse.cdt.core.dom.upc.UPCLanguage;
import org.eclipse.cdt.core.lrparser.tests.LRCSpecTests;
import org.eclipse.cdt.core.model.ILanguage;

public class UPCCSpecTests extends LRCSpecTests {

	public UPCCSpecTests() {
	}

	public UPCCSpecTests(String name) {
		super(name);
	}

	@Override
	protected ILanguage getCLanguage() {
		return UPCLanguage.getDefault();
	}

}
