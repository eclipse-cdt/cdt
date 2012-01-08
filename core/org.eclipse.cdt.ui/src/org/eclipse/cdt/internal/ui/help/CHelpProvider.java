/*******************************************************************************
 * Copyright (c) 2007, 2008 Intel Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Intel Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.internal.ui.help;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtension;
import org.eclipse.core.runtime.IExtensionPoint;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Platform;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import org.eclipse.cdt.ui.ICHelpBook;
import org.eclipse.cdt.ui.ICHelpProvider;
import org.eclipse.cdt.ui.ICHelpResourceDescriptor;
import org.eclipse.cdt.ui.IFunctionSummary;
import org.eclipse.cdt.ui.text.ICHelpInvocationContext;

public class CHelpProvider implements ICHelpProvider {

	private static final String EXTENSION_POINT_ID = "org.eclipse.cdt.ui.HelpInfo"; //$NON-NLS-1$
	private static final String ELEMENT_NAME  = "helpInfo"; //$NON-NLS-1$
	private static final String ATTRIB_FILE   = "file"; //$NON-NLS-1$
	private static final String NODE_HEAD = "documentation"; //$NON-NLS-1$
	private static final String NODE_BOOK = "helpBook"; //$NON-NLS-1$

	private boolean Done = false;
	
	ICHelpBook[] hbs = null;
	
	@Override
	public ICHelpBook[] getCHelpBooks() {
		waitForDone();
		return hbs;
	}

	@Override
	public IFunctionSummary getFunctionInfo(
			ICHelpInvocationContext context,
			ICHelpBook[] helpBooks, 
			String name) {
		for (int i=0; i<helpBooks.length; i++) {
			if (helpBooks[i] instanceof CHelpBook) {
				IFunctionSummary fs = ((CHelpBook)helpBooks[i]).getFunctionInfo(context, name);
				if (fs != null) // if null, try with another book
					return fs;
			}
		}
		return null;
	}

	@Override
	public ICHelpResourceDescriptor[] getHelpResources(
			ICHelpInvocationContext context, ICHelpBook[] helpBooks, String name) {

		ArrayList<ICHelpResourceDescriptor> lst = new ArrayList<ICHelpResourceDescriptor>();
		for (ICHelpBook h : helpBooks) {
			if (h instanceof CHelpBook) {
				ICHelpResourceDescriptor hrd = 
					((CHelpBook)h).getHelpResources(context, name);
				if (hrd != null)
					lst.add(hrd);
			}
		}
		if (lst.size() > 0)
			return lst.toArray(new ICHelpResourceDescriptor[lst.size()]);
		return null;
	}

	@Override
	public IFunctionSummary[] getMatchingFunctions(
			ICHelpInvocationContext context, ICHelpBook[] helpBooks,
			String prefix) {
		ArrayList<IFunctionSummary> lst = new ArrayList<IFunctionSummary>();
		for (int i=0; i<helpBooks.length; i++) {
			if (helpBooks[i] instanceof CHelpBook) {
				List<IFunctionSummary> fs = ((CHelpBook)helpBooks[i]).getMatchingFunctions(context, prefix);
				if (fs != null) // if null, try with another book
					lst.addAll(fs);
			}
		}
		if (lst.size() > 0)
			return lst.toArray(new IFunctionSummary[lst.size()]);
		return null;
	}

	@Override
	public void initialize() {
//		(new Thread() {
//		public void run() {
			loadExtensions();
//		  }
//	    }).run();
	}

	private void waitForDone() {
		if (hbs != null)
			return;
		try {
			while (! Done ) Thread.sleep(10);
		} catch (InterruptedException e) {}
	}
	
	private void loadExtensions()
	{
		try {
			IExtensionPoint extensionPoint = Platform.getExtensionRegistry()
			.getExtensionPoint(EXTENSION_POINT_ID);
			if (extensionPoint != null) {
				IExtension[] extensions = extensionPoint.getExtensions();
				if (extensions != null) {
					ArrayList<ICHelpBook> chbl = new ArrayList<ICHelpBook>();
					for (IExtension ex: extensions)	{
						String pluginId = ex.getNamespaceIdentifier();			
						for (IConfigurationElement el : ex.getConfigurationElements()) {
							if (el.getName().equals(ELEMENT_NAME)) {
								loadFile(el, chbl, pluginId);
							}
						}
					}
					if (chbl.size() > 0) {
						hbs = chbl.toArray(new ICHelpBook[chbl.size()]);
					}
				}
			}
		} finally {
			Done = true;
		}
	}
	
	private void loadFile(IConfigurationElement el, ArrayList<ICHelpBook> chbl, String pluginId) {
		String fname = el.getAttribute(ATTRIB_FILE);
		if (fname == null || fname.trim().length() == 0) return;
		URL x = FileLocator.find(Platform.getBundle(pluginId), new Path(fname), null);
		if (x == null) return;
		try { x = FileLocator.toFileURL(x);
		} catch (IOException e) { return; }
		fname = x.getPath();
		if (fname == null || fname.trim().length() == 0) return;

		// format is not supported for now 
		// String format = el.getAttribute(ATTRIB_FORMAT);
		
		Document doc = null;
		try {
			InputStream stream = new FileInputStream(fname);
			BufferedReader reader = new BufferedReader(new InputStreamReader(stream));
			InputSource src = new InputSource(reader);
			DocumentBuilder builder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
			doc = builder.parse(src);
			Element e = doc.getDocumentElement();
			if(NODE_HEAD.equals(e.getNodeName())){
				NodeList list = e.getChildNodes();
				for(int j = 0; j < list.getLength(); j++){
					Node node = list.item(j);
					if(node.getNodeType() != Node.ELEMENT_NODE)	continue;
					if(NODE_BOOK.equals(node.getNodeName())){
						chbl.add(new CHelpBook((Element)node));
					}
				}
			}
		} catch (ParserConfigurationException e) {
		} catch (SAXException e) {
		} catch (IOException e) {
		}
	}	
}
