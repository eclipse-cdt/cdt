/*******************************************************************************
 * Copyright (c) 2010, 2016 Google, Inc and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 * 	  Sergey Prigogin (Google) - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.debug.internal.core.sourcelookup;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.Path;
import org.eclipse.debug.core.sourcelookup.ISourceContainer;
import org.eclipse.debug.core.sourcelookup.containers.AbstractSourceContainerTypeDelegate;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

/**
 * See {@link CompilationDirectorySourceContainer}.
 */
public class CompilationDirectorySourceContainerType extends AbstractSourceContainerTypeDelegate {
	@Override
	public String getMemento(ISourceContainer container) throws CoreException {
		CompilationDirectorySourceContainer folder = (CompilationDirectorySourceContainer) container;
		Document document = newDocument();
		Element element = document.createElement("directory"); //$NON-NLS-1$
		element.setAttribute("path", folder.getDirectory().getAbsolutePath()); //$NON-NLS-1$
		String nest = "false"; //$NON-NLS-1$
		if (folder.isComposite()) {
			nest = "true"; //$NON-NLS-1$
		}
		element.setAttribute("nest", nest); //$NON-NLS-1$
		document.appendChild(element);
		return serializeDocument(document);
	}

	@Override
	public ISourceContainer createSourceContainer(String memento) throws CoreException {
		Node node = parseDocument(memento);
		if (node.getNodeType() == Node.ELEMENT_NODE) {
			Element element = (Element) node;
			if ("directory".equals(element.getNodeName())) { //$NON-NLS-1$
				String path = element.getAttribute("path"); //$NON-NLS-1$
				if (path == null || path.isEmpty()) {
					abort(InternalSourceLookupMessages.CompilationDirectorySourceContainerType_0, null);
				}
				String nest = element.getAttribute("nest"); //$NON-NLS-1$
				boolean nested = Boolean.parseBoolean(nest);
				return new CompilationDirectorySourceContainer(new Path(path), nested);
			}
			abort(InternalSourceLookupMessages.CompilationDirectorySourceContainerType_1, null);
		}
		abort(InternalSourceLookupMessages.CompilationDirectorySourceContainerType_2, null);
		return null;
	}
}
