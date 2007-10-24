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
	
	ICHelpBook[] hbs = null;
	
	public ICHelpBook[] getCHelpBooks() {
		return hbs;
	}

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

	public ICHelpResourceDescriptor[] getHelpResources(
			ICHelpInvocationContext context, ICHelpBook[] helpBooks, String name) {

		ArrayList lst = new ArrayList();
		for (int i=0; i<helpBooks.length; i++) {
			if (helpBooks[i] instanceof CHelpBook) {
				ICHelpResourceDescriptor hrd = 
					((CHelpBook)helpBooks[i]).getHelpResources(context, name);
				if (hrd != null)
					lst.add(hrd);
			}
		}
		if (lst.size() > 0)
			return (ICHelpResourceDescriptor[])lst.toArray(
					new ICHelpResourceDescriptor[lst.size()]);
		else
			return null;
	}

	public IFunctionSummary[] getMatchingFunctions(
			ICHelpInvocationContext context, ICHelpBook[] helpBooks,
			String prefix) {
		ArrayList lst = new ArrayList();
		for (int i=0; i<helpBooks.length; i++) {
			if (helpBooks[i] instanceof CHelpBook) {
				List fs = ((CHelpBook)helpBooks[i]).getMatchingFunctions(context, prefix);
				if (fs != null) // if null, try with another book
					lst.addAll(fs);
			}
		}
		if (lst.size() > 0)
			return (IFunctionSummary[])lst.toArray(new IFunctionSummary[lst.size()]);
		else	
			return null;
	}

	public void initialize() {
		loadExtensions();
		System.out.println();
	}

	private void loadExtensions()
	{
		IExtensionPoint extensionPoint = Platform.getExtensionRegistry()
				.getExtensionPoint(EXTENSION_POINT_ID);
		if (extensionPoint == null) return;
		IExtension[] extensions = extensionPoint.getExtensions();
		if (extensions == null) return;
		
		ArrayList chbl = new ArrayList();
		
		for (int i = 0; i < extensions.length; ++i)	{
			String pluginId = extensions[i].getNamespaceIdentifier();			
			IConfigurationElement[] elements = extensions[i].getConfigurationElements();
			for (int k = 0; k < elements.length; k++) {
				if (elements[k].getName().equals(ELEMENT_NAME)) {
					loadFile(elements[k], chbl, pluginId);
				}
			}
		}
		if (chbl.size() > 0) {
			hbs = (ICHelpBook[])chbl.toArray(new ICHelpBook[chbl.size()]);
		}
	}
	
	private void loadFile(IConfigurationElement el, ArrayList chbl, String pluginId) {
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
		} catch (ParserConfigurationException e) {
		} catch (SAXException e) {
		} catch (IOException e) {
		}

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
	}
	
}
