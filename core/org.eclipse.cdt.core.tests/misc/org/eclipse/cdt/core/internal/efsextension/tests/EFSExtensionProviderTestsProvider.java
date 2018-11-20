/*******************************************************************************
 * Copyright (c) 2010 IBM Corporation and others.
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
