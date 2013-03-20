/*
 * Copyright (c) 2013 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Andrew Eidsness - Initial implementation
 */

package org.eclipse.cdt.internal.core.dom.ast;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.eclipse.cdt.core.CCorePlugin;
import org.eclipse.cdt.core.dom.ast.IASTChildProvider;
import org.eclipse.cdt.core.dom.ast.IASTExpression;
import org.eclipse.cdt.core.dom.ast.IASTNode;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.Platform;

/**
 * Manages the org.eclipse.cdt.core.astChildProvider extension point.  Extensions are
 * loaded and the contributed class is returned when applicable.
 */
public class ASTChildProviderManager {
	private static ASTChildProviderManager INSTANCE;
	private static final String ExtensionPoint = "astChildProvider"; //$NON-NLS-1$

	private Set<ASTChildProviderDescriptor> astChildProviders;

	public static ASTChildProviderManager getInstance() {
		if (INSTANCE == null)
			INSTANCE = new ASTChildProviderManager();
		return INSTANCE;
	}

	private ASTChildProviderManager() {
		astChildProviders = loadExtensions();
	}

	private static Set<ASTChildProviderDescriptor> loadExtensions() {
		Set<ASTChildProviderDescriptor> astChildProviders = new HashSet<ASTChildProviderDescriptor>();

		// load the extensions
		IConfigurationElement[] elements = Platform.getExtensionRegistry()
				.getConfigurationElementsFor(CCorePlugin.PLUGIN_ID, ExtensionPoint);
		for (IConfigurationElement element : elements)
			astChildProviders.add(new ASTChildProviderDescriptor(element));

		return astChildProviders;
	}

	/** Provide an opportunity for all enabled participants to return extra matches for the given values. */
	public Collection<IASTNode> getChildren(IASTExpression expr) {
		List<IASTNode> children = null;
		for (ASTChildProviderDescriptor desc : astChildProviders) {
			IASTChildProvider astChildProvider = desc.getASTChildProviderFor(expr);
			if (astChildProvider != null) {
				Collection<IASTNode> nodeChildren = astChildProvider.getChildren(expr);
				if (nodeChildren != null && nodeChildren != IASTChildProvider.NONE
						&& !nodeChildren.isEmpty()) {
					if (children == null)
						children = new ArrayList<IASTNode>(nodeChildren);
					else
						children.addAll(nodeChildren);
				}
			}
		}

		return children == null ? IASTChildProvider.NONE : children;
	}
}
