/*******************************************************************************
 * Copyright (c) 2017 Ericsson and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/

package org.eclipse.lsp4e.cpp.language;

import java.io.IOException;

import org.eclipse.core.expressions.PropertyTester;
import org.eclipse.core.resources.IProject;
import org.eclipse.lsp4e.LanguageServersRegistry;
import org.eclipse.lsp4e.LanguageServersRegistry.LanguageServerDefinition;
import org.eclipse.lsp4e.LanguageServiceAccessor;
import org.eclipse.lsp4e.ProjectSpecificLanguageServerWrapper;

/**
 * Tests whether of not a project is running the C/C++ server.
 */
public class LanguageServerPropertyTester extends PropertyTester {

	private static final String KEY_HAS_SERVER = "hasServer"; //$NON-NLS-1$

	@Override
	public boolean test(Object receiver, String property, Object[] args, Object expectedValue) {
		//FIXME; needs https://git.eclipse.org/r/#/c/101835/
//		if (KEY_HAS_SERVER.equals(property)) {
//			if (receiver instanceof IProject) {
//				LanguageServerDefinition definition = LanguageServersRegistry.getInstance().getDefinition(CPPLanguageServer.ID);
//				try {
//					ProjectSpecificLanguageServerWrapper lsWrapperForConnection = LanguageServiceAccessor.getLSWrapperForConnection((IProject) receiver, definition, false);
//					return lsWrapperForConnection != null;
//				} catch (IOException e) {
//					return false;
//				}
//			}
//		}
		return false;
	}

}
