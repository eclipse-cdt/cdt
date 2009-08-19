/*******************************************************************************
 * Copyright (c) 2009, 2009 Andrew Gvozdev (Quoin Inc.).
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Andrew Gvozdev (Quoin Inc.)
 *******************************************************************************/
package org.eclipse.cdt.internal.core;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * XML utilities.
 *
 */
public class XmlUtil {
	private static final String EOL_XML = "\n"; //$NON-NLS-1$
	private static final String DEFAULT_IDENT = "\t"; //$NON-NLS-1$

	/**
	 * As a workaround for {@code javax.xml.transform.Transformer} not being able
	 * to pretty print XML. This method prepares DOM {@code Document} for the transformer
	 * to be pretty printed, i.e. providing proper indentations for enclosed tags.
	 *
	 * @param doc - DOM document to be pretty printed
	 */
	public static void prettyFormat(Document doc) {
		prettyFormat(doc, DEFAULT_IDENT);
	}

	/**
	 * As a workaround for {@code javax.xml.transform.Transformer} not being able
	 * to pretty print XML. This method prepares DOM {@code Document} for the transformer
	 * to be pretty printed, i.e. providing proper indentations for enclosed tags.
	 *
	 * @param doc - DOM document to be pretty printed
	 * @param ident - custom indentation as a string of white spaces
	 */
	public static void prettyFormat(Document doc, String ident) {
		doc.normalize();
		prettyFormat(doc.getDocumentElement(), "", ident); //$NON-NLS-1$
	}

	/**
	 * The method inserts end-of-line+indentation Text nodes where indentation is necessary.
	 * 
	 * @param node - node to be pretty formatted
	 * @param identLevel - initial indentation level of the node
	 * @param ident - additional indentation inside the node
	 */
	private static void prettyFormat(Node node, String identLevel, String ident) {
		NodeList nodelist = node.getChildNodes();
		int iStart=0;
		Node item = nodelist.item(0);
		if (item!=null) {
			short type = item.getNodeType();
			if (type==Node.ELEMENT_NODE || type==Node.COMMENT_NODE) {
				Node newChild = node.getOwnerDocument().createTextNode(EOL_XML + identLevel + ident);
				node.insertBefore(newChild, item);
				iStart=1;
			}
		}
		for (int i=iStart;i<nodelist.getLength();i++) {
			item = nodelist.item(i);
			if (item!=null) {
				short type = item.getNodeType();
				if (type==Node.TEXT_NODE && item.getNodeValue().trim().length()==0) {
					if (i+1<nodelist.getLength()) {
						item.setNodeValue(EOL_XML + identLevel + ident);
					} else {
						item.setNodeValue(EOL_XML + identLevel);
					}
				} else if (type==Node.ELEMENT_NODE) {
					prettyFormat(item, identLevel + ident, ident);
					if (i+1<nodelist.getLength()) {
						Node nextItem = nodelist.item(i+1);
						if (nextItem!=null) {
							short nextType = nextItem.getNodeType();
							if (nextType==Node.ELEMENT_NODE || nextType==Node.COMMENT_NODE) {
								Node newChild = node.getOwnerDocument().createTextNode(EOL_XML + identLevel + ident);
								node.insertBefore(newChild, nextItem);
								i++;
								continue;
							}
						}
					} else {
						Node newChild = node.getOwnerDocument().createTextNode(EOL_XML + identLevel);
						node.appendChild(newChild);
						i++;
						continue;
					}
				}
			}
		}

	}
}


