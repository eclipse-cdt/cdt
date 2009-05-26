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

import org.eclipse.cdt.core.dom.upc.UPCLanguage;
import org.eclipse.cdt.core.lrparser.tests.LRSelectionParseTest;
import org.eclipse.cdt.core.model.ILanguage;

public class UPCSelectionParseTest extends LRSelectionParseTest {

	public UPCSelectionParseTest() { }
	public UPCSelectionParseTest(String name) { super(name); }

	
	@Override
	protected ILanguage getCLanguage() {
		return UPCLanguage.getDefault();
	}
}
