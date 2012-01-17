/*******************************************************************************
 * Copyright (c) 2003, 2010 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     Sergey Prigogin (Google)
 *******************************************************************************/
package org.eclipse.cdt.debug.internal.core.sourcelookup;

import org.eclipse.cdt.debug.core.sourcelookup.CProjectSourceContainer;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.debug.core.sourcelookup.ISourceContainer;
import org.eclipse.debug.core.sourcelookup.containers.AbstractSourceContainerTypeDelegate;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

/**
 * The type for creating/restoring a project source container.
 */
public class CProjectSourceContainerType extends AbstractSourceContainerTypeDelegate {

	/* (non-Javadoc)
	 * @see org.eclipse.debug.internal.core.sourcelookup.ISourceContainerType#getMemento(org.eclipse.debug.internal.core.sourcelookup.ISourceContainer)
	 */
	@Override
	public String getMemento(ISourceContainer container) throws CoreException {
		CProjectSourceContainer sourceContainer = (CProjectSourceContainer) container;
		Document document = newDocument();
		Element element = document.createElement("project"); //$NON-NLS-1$
		IProject project = sourceContainer.getProject();
		if (project != null) {
			element.setAttribute("name", project.getName()); //$NON-NLS-1$
		}
		element.setAttribute("referencedProjects", String.valueOf(sourceContainer.isSearchReferencedProjects())); //$NON-NLS-1$
		document.appendChild(element);
		return serializeDocument(document);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.internal.core.sourcelookup.ISourceContainerType#createSourceContainer(java.lang.String)
	 */
	@Override
	public ISourceContainer createSourceContainer(String memento) throws CoreException {
		Node node = parseDocument(memento);
		if (node.getNodeType() == Node.ELEMENT_NODE) {
			Element element = (Element)node;
			if ("project".equals(element.getNodeName())) { //$NON-NLS-1$
				String string = element.getAttribute("name"); //$NON-NLS-1$
				IProject project = null;
				if (string != null && string.length() > 0) {
					project = ResourcesPlugin.getWorkspace().getRoot().getProject(string);
				}
				String nest = element.getAttribute("referencedProjects"); //$NON-NLS-1$
				boolean ref = "true".equals(nest); //$NON-NLS-1$
				return new CProjectSourceContainer(project, ref);
			} 
			abort(InternalSourceLookupMessages.CProjectSourceContainerType_1, null); 
		}
		abort(InternalSourceLookupMessages.CProjectSourceContainerType_2, null); 
		return null;
	}
}
