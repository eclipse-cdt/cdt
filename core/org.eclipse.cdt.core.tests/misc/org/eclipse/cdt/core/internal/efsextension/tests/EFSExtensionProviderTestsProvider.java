/*******************************************************************************
 * Copyright (c) 2010 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.core.internal.efsextension.tests;

import java.net.URI;

import org.eclipse.cdt.core.EFSExtensionProvider;

/**
 * Test class that is used to make sure that extensions to the EFSExtensionProvider
 * extension point are picked up.  Overrrides the default behaviour for isVirtual()
 * to return true.
 * 
 * @author crecoskie
 *
 */
public class EFSExtensionProviderTestsProvider extends EFSExtensionProvider {

	@Override
	public boolean isVirtual(URI locationURI) {
		return true;
	}

	

}
