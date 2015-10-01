/*******************************************************************************
 * Copyright (c) 2004, 2015 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     QNX Software Systems - Initial API and implementation
 *     Jonah Graham (Kichwa Coders) - Add support for gdb's "set substitute-path" (Bug 472765)
 *******************************************************************************/
package org.eclipse.cdt.debug.internal.core.sourcelookup; 

import java.util.ArrayList;
import java.util.List;

import org.eclipse.cdt.debug.core.sourcelookup.MappingSourceContainer;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.sourcelookup.ISourceContainer;
import org.eclipse.debug.core.sourcelookup.ISourceContainerType;
import org.eclipse.debug.core.sourcelookup.containers.AbstractSourceContainerTypeDelegate;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
 
/**
 * The mapping container type.
 */
public class MappingSourceContainerType extends AbstractSourceContainerTypeDelegate {
	private final static String ELEMENT_MAPPING = "mapping"; //$NON-NLS-1$
	private final static String ELEMENT_MAP_ENTRY = "mapEntry"; //$NON-NLS-1$
	private final static String ATTR_NAME = "name"; //$NON-NLS-1$
	private final static String ATTR_MEMENTO = "memento"; //$NON-NLS-1$

	@Override
	public ISourceContainer createSourceContainer(String memento) throws CoreException {
		Node node = parseDocument(memento);
		if (node.getNodeType() == Node.ELEMENT_NODE) {
			Element element = (Element) node;
			if (ELEMENT_MAPPING.equals(element.getNodeName())) {
				String name = element.getAttribute(ATTR_NAME);
				if (name == null) 
					name = ""; //$NON-NLS-1$
				List<MapEntrySourceContainer> entries = new ArrayList<MapEntrySourceContainer>();
				Node childNode = element.getFirstChild();
				while (childNode != null) {
					if (childNode.getNodeType() == Node.ELEMENT_NODE) {
						Element child = (Element)childNode;
						if (ELEMENT_MAP_ENTRY.equals(child.getNodeName())) {
							String childMemento = child.getAttribute(ATTR_MEMENTO);
							if (childMemento == null || childMemento.length() == 0) {
								abort(InternalSourceLookupMessages.MappingSourceContainerType_0, null);
							}
							ISourceContainerType type = DebugPlugin.getDefault().getLaunchManager().getSourceContainerType(getMapEntryTypeId());
							MapEntrySourceContainer entry = (MapEntrySourceContainer) type.createSourceContainer(childMemento);
							entries.add(entry);
						}
					}
					childNode = childNode.getNextSibling();
				}
				MappingSourceContainer container = newSourceContainer(name);
				for (MapEntrySourceContainer entry : entries) {
					container.addMapEntry(entry);
				}
				return container;
			}
			abort(InternalSourceLookupMessages.MappingSourceContainerType_1, null);
		}
		abort(InternalSourceLookupMessages.MappingSourceContainerType_2, null);
		return null;		
	}

	/**
	 * Return a new container for the given parameters.
	 * 
	 * May be overridden by other container type delegates which deserialize to
	 * alternate container types.
	 */
	protected MappingSourceContainer newSourceContainer(String name) {
		return new MappingSourceContainer(name);
	}

	/**
	 * Return the type id of the entry's source container type.
	 * 
	 * May be overridden by other container type delegates which deserialize to
	 * alternate entry container types.
	 */
	protected String getMapEntryTypeId() {
		return MapEntrySourceContainer.TYPE_ID;
	}

	@Override
	public String getMemento(ISourceContainer container) throws CoreException {
		Document document = newDocument();
		Element element = document.createElement(ELEMENT_MAPPING);
		element.setAttribute(ATTR_NAME, container.getName());
		ISourceContainer[] entries = ((MappingSourceContainer)container).getSourceContainers();
		for (int i = 0; i < entries.length; ++i) {
			Element child = document.createElement(ELEMENT_MAP_ENTRY);
			child.setAttribute(ATTR_MEMENTO, entries[i].getType().getMemento(entries[i]));
			element.appendChild(child);
		}
		document.appendChild(element);
		return serializeDocument(document);
	}
}
