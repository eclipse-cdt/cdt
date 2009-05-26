/*******************************************************************************
 *  Copyright (c) 2006, 2009 IBM Corporation and others.
 *  All rights reserved. This program and the accompanying materials
 *  are made available under the terms of the Eclipse Public License v1.0
 *  which accompanies this distribution, and is available at
 *  http://www.eclipse.org/legal/epl-v10.html
 * 
 *  Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.core.parser.upc.tests;

import org.eclipse.cdt.core.dom.lrparser.c99.C99Language;
import org.eclipse.cdt.core.lrparser.tests.LRCompletionBasicTest;
import org.eclipse.cdt.core.model.ILanguage;

public class UPCCompletionBasicTest extends LRCompletionBasicTest {

	public UPCCompletionBasicTest() { }

	@Override
	protected ILanguage getCLanguage() {
		return C99Language.getDefault();
	}

}