/*******************************************************************************
 * Copyright (c) 2013 QNX Software Systems and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *******************************************************************************/
package org.eclipse.cdt.internal.core.pdom;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.eclipse.cdt.core.CCorePlugin;
import org.eclipse.cdt.core.dom.ast.IASTTranslationUnit;
import org.eclipse.cdt.core.index.IPDOMASTProcessor;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.Platform;

/**
 * An internal manager for the org.eclipse.cdt.core.PDOMASTProcessor extension-point.
 *
 * @see #getProcessors(IASTTranslationUnit)
 */
public class PDOMASTProcessorManager {

	private static final String EXTENSION_POINT = "PDOMASTProcessor"; //$NON-NLS-1$

	private static final List<PDOMASTProcessorDesc> processors = new ArrayList<>();
	static {
		// Load the extensions
		IConfigurationElement[] elements = Platform.getExtensionRegistry()
				.getConfigurationElementsFor(CCorePlugin.PLUGIN_ID, EXTENSION_POINT);
		for (IConfigurationElement element : elements)
			processors.add(new PDOMASTProcessorDesc(element));
	}

	private PDOMASTProcessorManager() {
	}

	/**
	 * Returns a list of the contributed PDOMASTProcessors that should be used with the
	 * specific AST.  Does not return null.
	 */
	public static List<IPDOMASTProcessor> getProcessors(IASTTranslationUnit ast) {
		List<IPDOMASTProcessor> list = null;

		for (PDOMASTProcessorDesc desc : processors) {
			IPDOMASTProcessor processor = desc.getProcessorFor(ast);
			if (processor != null) {
				if (list == null)
					list = new ArrayList<>();
				list.add(processor);
			}
		}

		return list == null ? Collections.<IPDOMASTProcessor>emptyList() : list;
	}
}
