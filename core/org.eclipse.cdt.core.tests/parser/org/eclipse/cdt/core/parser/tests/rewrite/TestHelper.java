/*******************************************************************************
 * Copyright (c) 2008 Institute for Software, HSR Hochschule fuer Technik  
 * Rapperswil, University of applied sciences and others
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Eclipse Public License v1.0 
 * which accompanies this distribution, and is available at 
 * http://www.eclipse.org/legal/epl-v10.html  
 *  
 * Contributors: 
 * Institute for Software - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.core.parser.tests.rewrite;

public class TestHelper {

	public static String unifyNewLines(String code) {
		String replacement = System.getProperty("line.separator"); //$NON-NLS-1$
		return code.replaceAll("(\n)|(\r\n)", replacement).trim(); //$NON-NLS-1$
	}
}
