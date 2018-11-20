/*******************************************************************************
 *  Copyright (c) 2006, 2010 IBM Corporation and others.
 *
 *  This program and the accompanying materials
 *  are made available under the terms of the Eclipse Public License 2.0
 *  which accompanies this distribution, and is available at
 *  https://www.eclipse.org/legal/epl-2.0/
 *
 *  SPDX-License-Identifier: EPL-2.0
 *
 *  Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.core.parser.upc.tests;

import org.eclipse.cdt.core.dom.upc.UPCLanguage;
import org.eclipse.cdt.core.lrparser.tests.LRCompleteParser2Tests;
import org.eclipse.cdt.core.model.ILanguage;

public class UPCCompleteParser2Tests extends LRCompleteParser2Tests {

	@Override
	public void testGNUASMExtension() throws Exception {
	}

	@Override
	public void testBug39551B() throws Exception {
	}

	@Override
	public void testBug39676_tough() throws Exception {
	}

	@Override
	public void testBug102376() throws Exception {
	}

	@Override
	protected ILanguage getCLanguage() {
		return UPCLanguage.getDefault();
	}

}
