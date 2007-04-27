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

import org.eclipse.cdt.core.parser.c99.tests.C99Tests;
import org.eclipse.cdt.core.dom.c99.C99Language;
import org.eclipse.cdt.core.dom.upc.UPCLanguage;
/**
 * Run the C99 tests against the UPC parser
 *
 */
public class UPCC99Tests extends C99Tests {

	public UPCC99Tests(String name) {
		super(name);
	}

	protected C99Language getC99Language() {
		return new UPCLanguage();
	}
	
}
