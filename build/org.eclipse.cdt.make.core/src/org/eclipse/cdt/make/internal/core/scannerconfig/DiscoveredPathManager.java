/*******************************************************************************
 * Copyright (c) 2004 QNX Software Systems and others. All rights reserved. This
 * program and the accompanying materials are made available under the terms of
 * the Common Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors: QNX Software Systems - initial API and implementation
 ******************************************************************************/
package org.eclipse.cdt.make.internal.core.scannerconfig;

import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.eclipse.cdt.core.CCorePlugin;
import org.eclipse.cdt.core.model.CoreModel;
import org.eclipse.cdt.core.model.ICProject;
import org.eclipse.cdt.make.core.MakeCorePlugin;
import org.eclipse.cdt.make.core.scannerconfig.IDiscoveredPathManager;
import org.eclipse.cdt.make.core.scannerconfig.ScannerConfigUtil;
import org.eclipse.cdt.make.internal.core.MakeMessages;
import org.eclipse.cdt.make.internal.core.scannerconfig.util.SymbolEntry;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceChangeEvent;
import org.eclipse.core.resources.IResourceChangeListener;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.ISafeRunnable;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.Status;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.xml.sax.SAXException;

public class DiscoveredPathManager implements IDiscoveredPathManager, IResourceChangeListener {

	private static final String CDESCRIPTOR_ID = MakeCorePlugin.getUniqueIdentifier() + ".discoveredScannerInfo"; //$NON-NLS-1$
	public static final String INCLUDE_PATH = "includePath"; //$NON-NLS-1$
	public static final String PATH = "path"; //$NON-NLS-1$
	public static final String DEFINED_SYMBOL = "definedSymbol"; //$NON-NLS-1$
	public static final String SYMBOL = "symbol"; //$NON-NLS-1$
	public static final String REMOVED = "removed"; //$NON-NLS-1$

	private Map fDiscoveredMap = new HashMap();
	private List listeners = Collections.synchronizedList(new ArrayList());

	private static final int INFO_CHANGED = 1;
	private static final int INFO_REMOVED = 2;

	public DiscoveredPathManager() {
	}
	
	public void startup() {
		ResourcesPlugin.getWorkspace().addResourceChangeListener(this);
	}
	
	public void shutdown() {
		ResourcesPlugin.getWorkspace().removeResourceChangeListener(this);
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.core.resources.IResourceChangeListener#resourceChanged(org.eclipse.core.resources.IResourceChangeEvent)
	 */
	public void resourceChanged(IResourceChangeEvent event) {
		if (event.getSource() instanceof IWorkspace) {
			IResource resource = event.getResource();

			switch (event.getType()) {
				case IResourceChangeEvent.PRE_DELETE :
				case IResourceChangeEvent.PRE_CLOSE :
					if (resource.getType() == IResource.PROJECT) {
						fDiscoveredMap.remove(resource);
					}
					break;
			}
		}
	}

	public IDiscoveredPathInfo getDiscoveredInfo(IProject project) throws CoreException {
		DiscoveredPathInfo info = (DiscoveredPathInfo)fDiscoveredMap.get(project);
		if (info == null) {
			info = loadPathInfo(project);
			fDiscoveredMap.put(project, info);
		}
		return info;
	}

	private DiscoveredPathInfo loadPathInfo(IProject project) throws CoreException {
		LinkedHashMap includes = new LinkedHashMap();
		LinkedHashMap symbols = new LinkedHashMap();
		loadDiscoveredScannerInfoFromState(project, includes, symbols);
		DiscoveredPathInfo info = new DiscoveredPathInfo(project);
		info.setIncludeMap(includes);
		info.setSymbolMap(symbols);
		return info;
	}

	public void removeDiscoveredInfo(IProject project) {
		ScannerConfigUtil.getDiscoveredScannerConfigStore(project, true);
		DiscoveredPathInfo info = (DiscoveredPathInfo)fDiscoveredMap.remove(project);
		fireUpdate(INFO_REMOVED, info);
	}

	public void updateDiscoveredInfo(IDiscoveredPathInfo info) throws CoreException {
		if (fDiscoveredMap.get(info.getProject()) != null) {
			saveDiscoveredScannerInfoToState((DiscoveredPathInfo)info);
			fireUpdate(INFO_CHANGED, info);
			ICProject cProject = CoreModel.getDefault().create(info.getProject());
			if (cProject != null) {
				CoreModel.getDefault().setPathEntryContainer(new ICProject[]{cProject},
						new DiscoveredPathContainer(info.getProject()), null);
			}
		}
	}

	private void loadDiscoveredScannerInfoFromState(IProject project, LinkedHashMap includes, LinkedHashMap symbols)
			throws CoreException {
		// Save the document
		IPath path = ScannerConfigUtil.getDiscoveredScannerConfigStore(project, false);
		if (path.toFile().exists()) {
			try {
				FileInputStream file = new FileInputStream(path.toFile());
				DocumentBuilder parser = DocumentBuilderFactory.newInstance().newDocumentBuilder();
				Document document = parser.parse(file);
				Node rootElement = document.getFirstChild();
				if (rootElement.getNodeName().equals("scannerInfo")) { //$NON-NLS-1$
					Node child = rootElement.getFirstChild();
					loadDiscoveredScannerInfo(includes, symbols, child);
				}
			} catch (IOException e) {
				throw new CoreException(new Status(IStatus.ERROR, MakeCorePlugin.getUniqueIdentifier(), -1,
						MakeMessages.getString("DiscoveredPathManager.File_Error_Message"), e)); //$NON-NLS-1$
			} catch (ParserConfigurationException e) {
				MakeCorePlugin.log(e);
				throw new CoreException(new Status(IStatus.ERROR, MakeCorePlugin.getUniqueIdentifier(), -1,
						MakeMessages.getString("DiscoveredPathManager.File_Error_Message"), e)); //$NON-NLS-1$
			} catch (SAXException e) {
				MakeCorePlugin.log(e);
				throw new CoreException(new Status(IStatus.ERROR, MakeCorePlugin.getUniqueIdentifier(), -1,
						MakeMessages.getString("DiscoveredPathManager.File_Error_Message"), e)); //$NON-NLS-1$
			}
		}
	}

	/**
	 * @param includes
	 * @param symbols
	 * @param child
	 */
	private void loadDiscoveredScannerInfo(LinkedHashMap includes, LinkedHashMap symbols, Node child) {
		while (child != null) {
			if (child.getNodeName().equals(INCLUDE_PATH)) {
				// Add the path to the property list
				includes.put( ((Element)child).getAttribute(PATH), Boolean.valueOf( ((Element)child).getAttribute(REMOVED)));
			} else if (child.getNodeName().equals(DEFINED_SYMBOL)) {
				// Add the symbol to the symbol list
				String symbol = ((Element)child).getAttribute(SYMBOL);
				String removed = ((Element)child).getAttribute(REMOVED);
				boolean bRemoved = (removed != null && removed.equals("true")); //$NON-NLS-1$
				ScannerConfigUtil.scAddSymbolString2SymbolEntryMap(symbols, symbol, !bRemoved);
			}
			child = child.getNextSibling();
		}
	}

	/**
	 * @param scannerInfo
	 * @param rootElement
	 * @param doc
	 */
	private static void saveDiscoveredScannerInfo(DiscoveredPathInfo info, Element rootElement, Document doc) {
		// Save the build info
		if (info != null) {
			// Serialize the include paths
			Map discoveredIncludes = info.getIncludeMap();
			Iterator iter = discoveredIncludes.keySet().iterator();
			while (iter.hasNext()) {
				Element pathElement = doc.createElement(INCLUDE_PATH);
				String include = (String)iter.next();
				pathElement.setAttribute(PATH, include);
				Boolean removed = (Boolean)discoveredIncludes.get(include);
				if (removed != null && removed.booleanValue() == true) {
					pathElement.setAttribute(REMOVED, "true"); //$NON-NLS-1$
				}
				rootElement.appendChild(pathElement);
			}
			// Now do the same for the symbols
			Map discoveredSymbols = info.getSymbolMap();
			iter = discoveredSymbols.keySet().iterator();
			while (iter.hasNext()) {
				String symbol = (String)iter.next();
				SymbolEntry se = (SymbolEntry)discoveredSymbols.get(symbol);
				for (Iterator i = se.getActiveRaw().iterator(); i.hasNext();) {
					String value = (String)i.next();
					Element symbolElement = doc.createElement(DEFINED_SYMBOL);
					symbolElement.setAttribute(SYMBOL, value);
					rootElement.appendChild(symbolElement);
				}
				for (Iterator i = se.getRemovedRaw().iterator(); i.hasNext();) {
					String value = (String)i.next();
					Element symbolElement = doc.createElement(DEFINED_SYMBOL);
					symbolElement.setAttribute(SYMBOL, value);
					symbolElement.setAttribute(REMOVED, "true"); //$NON-NLS-1$
					rootElement.appendChild(symbolElement);
				}
			}
		}
	}

	private static void saveDiscoveredScannerInfoToState(DiscoveredPathInfo info) throws CoreException {
		// Create document
		try {
			DocumentBuilder builder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
			Document doc = builder.newDocument();
			Element rootElement = doc.createElement("scannerInfo"); //$NON-NLS-1$
			rootElement.setAttribute("id", CDESCRIPTOR_ID); //$NON-NLS-1$
			doc.appendChild(rootElement);

			saveDiscoveredScannerInfo(info, rootElement, doc);

			// Transform the document to something we can save in a file
			ByteArrayOutputStream stream = new ByteArrayOutputStream();
			Transformer transformer = TransformerFactory.newInstance().newTransformer();
			transformer.setOutputProperty(OutputKeys.METHOD, "xml"); //$NON-NLS-1$
			transformer.setOutputProperty(OutputKeys.ENCODING, "UTF-8"); //$NON-NLS-1$
			transformer.setOutputProperty(OutputKeys.INDENT, "yes"); //$NON-NLS-1$
			DOMSource source = new DOMSource(doc);
			StreamResult result = new StreamResult(stream);
			transformer.transform(source, result);

			// Save the document
			try {
				IPath path = ScannerConfigUtil.getDiscoveredScannerConfigStore(info.getProject(), false);
				FileOutputStream file = new FileOutputStream(path.toFile());
				file.write(stream.toByteArray());
				file.close();
			} catch (IOException e) {
				throw new CoreException(new Status(IStatus.ERROR, MakeCorePlugin.getUniqueIdentifier(), -1,
						MakeMessages.getString("DiscoveredPathManager.File_Error_Message"), e)); //$NON-NLS-1$
			}

			// Close the streams
			stream.close();
		} catch (TransformerException e) {
			MakeCorePlugin.log(e);
			throw new CoreException(new Status(IStatus.ERROR, MakeCorePlugin.getUniqueIdentifier(), -1,
					MakeMessages.getString("DiscoveredPathManager.File_Error_Message"), e)); //$NON-NLS-1$
		} catch (IOException e) {
			MakeCorePlugin.log(e);
			throw new CoreException(new Status(IStatus.ERROR, MakeCorePlugin.getUniqueIdentifier(), -1,
					MakeMessages.getString("DiscoveredPathManager.File_Error_Message"), e)); //$NON-NLS-1$
		} catch (ParserConfigurationException e) {
			MakeCorePlugin.log(e);
			throw new CoreException(new Status(IStatus.ERROR, MakeCorePlugin.getUniqueIdentifier(), -1,
					MakeMessages.getString("DiscoveredPathManager.File_Error_Message"), e)); //$NON-NLS-1$
		}
	}

	private void fireUpdate(final int type, final IDiscoveredPathInfo info) {
		Object[] list = listeners.toArray();
		for (int i = 0; i < list.length; i++) {
			final IDiscoveredInfoListener listener = (IDiscoveredInfoListener)list[i];
			if (listener != null) {
				Platform.run(new ISafeRunnable() {

					public void handleException(Throwable exception) {
						IStatus status = new Status(IStatus.ERROR, CCorePlugin.PLUGIN_ID, -1,
								CCorePlugin.getResourceString("CDescriptorManager.exception.listenerError"), exception); //$NON-NLS-1$
						CCorePlugin.log(status);
					}

					public void run() throws Exception {
						switch (type) {
							case INFO_CHANGED :
								listener.infoChanged(info);
								break;
							case INFO_REMOVED :
								listener.infoRemoved(info.getProject());
								break;
						}
					}
				});
			}
		}
	}

	public void addDiscoveredInfoListener(IDiscoveredInfoListener listener) {
		listeners.add(listener);
	}

	public void removeDiscoveredInfoListener(IDiscoveredInfoListener listener) {
		listeners.remove(listener);
	}

}