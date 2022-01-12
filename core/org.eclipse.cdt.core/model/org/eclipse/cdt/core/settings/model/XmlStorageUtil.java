/*******************************************************************************
 * Copyright (c) 2008, 2010 Broadcom Corp. and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     James Blackburn (Broadcom Corp.) - Initial API and implementation
 *******************************************************************************/

package org.eclipse.cdt.core.settings.model;

import org.eclipse.cdt.internal.core.settings.model.xml.XmlStorageElement;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * This class serves as a bridge from Xml Document trees to
 * ICStorageElement trees.
 *
 * This allows importing of old style Xml trees into ICStorageElement
 * based project descriptions
 *
 * @noextend This class is not intended to be subclassed by clients.
 * @since 5.1
 */
public class XmlStorageUtil {

	/**
	 * Return an ICStorageElement tree based around the specified
	 * document
	 *
	 * N.B. the tree is backed by the passed in document
	 * so care should be taken to ensure that the tree is only
	 * subsequently through the ICStorageElement interface
	 *
	 * The ICStorageElement tree is based on the first Element
	 * found in the Document
	 * @param doc
	 * @return ICStorageElement tree or null if impossible
	 */
	public static ICStorageElement createCStorageTree(Document doc) {
		NodeList list = doc.getChildNodes();
		for (int i = 0; i < list.getLength(); i++) {
			if (list.item(i).getNodeType() == Node.ELEMENT_NODE) {
				return createCStorageTree((Element) list.item(i));
			}
		}
		return null;
	}

	/**
	 * Return an ICStorageElement tree based around the specified
	 * Element.
	 *
	 * NB the returned ICStorageElement is backed by the passed in
	 * Element which should only be modified via the ICStorageElement
	 * interface subsequent to this conversion.
	 *
	 * @param el input XML element
	 * @return ICStorageElement tree
	 */
	public static ICStorageElement createCStorageTree(Element el) {
		return new XmlStorageElement(el);
	}

}
