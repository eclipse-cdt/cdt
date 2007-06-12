/*******************************************************************************
 * Copyright (c) 2006, 2007 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.core.parser.upc.tests;

import org.eclipse.cdt.core.dom.c99.BaseExtensibleLanguage;
import org.eclipse.cdt.core.dom.c99.C99Language;
import org.eclipse.cdt.core.parser.c99.tests.C99CompletionBasicTest;

public class UPCC99CompletionBasicTest extends C99CompletionBasicTest {

	public UPCC99CompletionBasicTest() { }

	protected BaseExtensibleLanguage getLanguage() {
		return C99Language.getDefault();
	}

}