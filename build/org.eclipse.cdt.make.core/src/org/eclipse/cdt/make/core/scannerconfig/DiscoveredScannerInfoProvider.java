/**********************************************************************
 * Copyright (c) 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors: 
 * IBM - Initial API and implementation
 **********************************************************************/
package org.eclipse.cdt.make.core.scannerconfig;

import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.FactoryConfigurationError;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.TransformerFactoryConfigurationError;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.eclipse.cdt.core.AbstractCExtension;
import org.eclipse.cdt.core.CCorePlugin;
import org.eclipse.cdt.core.ICDescriptor;
import org.eclipse.cdt.core.parser.IScannerInfo;
import org.eclipse.cdt.core.parser.IScannerInfoChangeListener;
import org.eclipse.cdt.core.parser.IScannerInfoProvider;
import org.eclipse.cdt.make.core.MakeCorePlugin;
import org.eclipse.cdt.make.core.MakeScannerInfo;
import org.eclipse.cdt.make.core.MakeScannerProvider;
import org.eclipse.cdt.make.internal.core.scannerconfig.util.ScannerConfigUtil;
import org.eclipse.cdt.make.internal.core.scannerconfig.util.SymbolEntry;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.QualifiedName;
import org.eclipse.core.runtime.Status;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.xml.sax.SAXException;

/**
 * Provider of both user specified and discovered scanner info
 * 
 * @author vhirsl
 */
public class DiscoveredScannerInfoProvider extends AbstractCExtension implements IScannerInfoProvider {
	
	// This is the id of the IScannerInfoProvider extension point entry
	public static final String INTERFACE_IDENTITY = MakeCorePlugin.getUniqueIdentifier() + ".DiscoveredScannerInfoProvider"; //$NON-NLS-1$

	// Name we will use to store build property with the project
	private static final QualifiedName scannerInfoProperty = new QualifiedName(MakeCorePlugin.getUniqueIdentifier(), "discoveredMakeBuildInfo"); //$NON-NLS-1$
	private static final String CDESCRIPTOR_ID = MakeCorePlugin.getUniqueIdentifier() + ".discoveredScannerInfo"; //$NON-NLS-1$

	public static final String INCLUDE_PATH = "includePath"; //$NON-NLS-1$
	public static final String PATH = "path"; //$NON-NLS-1$
	public static final String DEFINED_SYMBOL = "definedSymbol"; //$NON-NLS-1$
	public static final String SYMBOL = "symbol"; //$NON-NLS-1$
	public static final String REMOVED = "removed"; //$NON-NLS-1$
	
	private static final String ROOT_ELEM_NAME = "DiscoveredScannerInfo";	//$NON-NLS-1$

	// Singleton
	private static DiscoveredScannerInfoProvider instance;
	public static DiscoveredScannerInfoProvider getDefault() {
		if (instance == null) {
			instance = new DiscoveredScannerInfoProvider();
		}
		return instance;
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.parser.IScannerInfoProvider#getScannerInformation(org.eclipse.core.resources.IResource)
	 */
	public IScannerInfo getScannerInformation(IResource resource) {
		IScannerInfo info = null;
		try {
			info = getDiscoveredScannerInfo(resource.getProject(), true);
		} catch (CoreException e) {
		}
		return info;
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.parser.IScannerInfoProvider#subscribe(org.eclipse.core.resources.IResource, org.eclipse.cdt.core.parser.IScannerInfoChangeListener)
	 */
	public void subscribe(IResource resource, IScannerInfoChangeListener listener) {
		MakeScannerProvider.getDefault().subscribe(resource, listener);
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.parser.IScannerInfoProvider#unsubscribe(org.eclipse.core.resources.IResource, org.eclipse.cdt.core.parser.IScannerInfoChangeListener)
	 */
	public void unsubscribe(IResource resource, IScannerInfoChangeListener listener) {
		MakeScannerProvider.getDefault().unsubscribe(resource, listener);
	}

	public DiscoveredScannerInfo getDiscoveredScannerInfo(IProject project, boolean cacheInfo) throws CoreException {
		DiscoveredScannerInfo scannerInfo = null;
		// See if there's already one associated with the resource for this
		// session
		scannerInfo = (DiscoveredScannerInfo)project.getSessionProperty(scannerInfoProperty);

		// Try to load one for the project
		if (scannerInfo == null) {
			scannerInfo = loadScannerInfo(project);
		}
		else {
			// TODO VMIR temporary
			// get the separately stored MakeScannerInfo in case someone accessed it
			// not through DiscoveredScannerInfoProvider
			MakeScannerInfo makeScannerInfo = MakeScannerProvider.getDefault().getMakeScannerInfo(project, cacheInfo);
			scannerInfo.setUserScannerInfo(makeScannerInfo);
		}		

		// There is nothing persisted for the session, or saved in a file so
		// create a build info object
		if (scannerInfo != null && cacheInfo == true) {
			project.setSessionProperty(scannerInfoProperty, scannerInfo);
		}
		return scannerInfo;
	}

	/*
	 * Loads the build file and parses the nodes for build information. The
	 * information is then associated with the resource for the duration of the
	 * session.
	 */
	private DiscoveredScannerInfo loadScannerInfo(IProject project) throws CoreException {
		LinkedHashMap includes = new LinkedHashMap();
		LinkedHashMap symbols = new LinkedHashMap();
//		loadDiscoveredScannerInfoFromCDescriptor(project, includes, symbols);
		loadDiscoveredScannerInfoFromState(project, includes, symbols);
		MakeScannerInfo userInfo = MakeScannerProvider.getDefault().loadScannerInfo(project);
		DiscoveredScannerInfo info = new DiscoveredScannerInfo(project);
		info.setUserScannerInfo(userInfo);
		info.setDiscoveredIncludePaths(includes);
		info.setDiscoveredSymbolDefinitions(symbols);
		return info;
	}

	/**
	 * Loads discovered scanner configuration from .cdtproject file
	 * @param project
	 * @param includes
	 * @param symbols
	 * @throws CoreException
	 */
	private void loadDiscoveredScannerInfoFromCDescriptor(IProject project, LinkedHashMap includes, LinkedHashMap symbols) throws CoreException {
		ICDescriptor descriptor = CCorePlugin.getDefault().getCProjectDescription(project);
		Node child = descriptor.getProjectData(CDESCRIPTOR_ID).getFirstChild();
		loadDiscoveredScannerInfo(includes, symbols, child);
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
				includes.put(((Element)child).getAttribute(PATH), 
							 Boolean.valueOf(((Element)child).getAttribute(REMOVED)));
			} else if (child.getNodeName().equals(DEFINED_SYMBOL)) {
				// Add the symbol to the symbol list
				String symbol = ((Element)child).getAttribute(SYMBOL);
				String removed = ((Element)child).getAttribute(REMOVED);
				boolean bRemoved = (removed != null && removed.equals("true"));	// $NON-NLS-1$
				ScannerConfigUtil.scAddSymbolString2SymbolEntryMap(symbols, symbol, !bRemoved);
			}
			child = child.getNextSibling();
		}
	}

	/**
	 * The build model manager for standard builds only caches the build
	 * information for a resource on a per-session basis. This method allows
	 * clients of the build model manager to programmatically remove the
	 * association between the resource and the information while the reource
	 * is still open or in the workspace. The Eclipse core will take care of
	 * removing it if a resource is closed or deleted.
	 * 
	 * @param resource
	 */
	public static void removeScannerInfo(IResource resource) {
		try {
			resource.getProject().setSessionProperty(scannerInfoProperty, null);
		} catch (CoreException e) {
		}
	}

	/**
	 * Persists build-specific information in the build file. Build information
	 * for standard make projects consists of preprocessor symbols and includes
	 * paths. Other project-related information is stored in the persistent
	 * properties of the project.
	 * 
	 * @param scannerInfo
	 */
	static void updateScannerInfo(DiscoveredScannerInfo scannerInfo) throws CoreException {
		IProject project = scannerInfo.getProject();

		// See if there's already one associated with the resource for this
		// session
		if (project.getSessionProperty(scannerInfoProperty) != null) {
			project.setSessionProperty(scannerInfoProperty, scannerInfo);
		}

//		saveDiscoveredScannerInfoToCDescriptor(scannerInfo, project);
		saveDiscoveredScannerInfoToState(scannerInfo, project);
		
		MakeScannerProvider.updateScannerInfo(scannerInfo.getUserScannerInfo());
// listeners are notified by MakeScannerProvider.updateScannerInfo
//		notifyInfoListeners(project, scannerInfo);
	}

	/**
	 * Save discovered scanner configuration to .cdtproject file
	 * @param scannerInfo
	 * @param project
	 * @throws CoreException
	 */
	private static void saveDiscoveredScannerInfoToCDescriptor(DiscoveredScannerInfo scannerInfo, IProject project) throws CoreException {
		ICDescriptor descriptor = CCorePlugin.getDefault().getCProjectDescription(project);

		Element rootElement = descriptor.getProjectData(CDESCRIPTOR_ID);

		// Clear out all current children
		// Note: Probably would be a better idea to merge in the data
		Node child = rootElement.getFirstChild();
		while (child != null) {
			rootElement.removeChild(child);
			child = rootElement.getFirstChild();
		}
		Document doc = rootElement.getOwnerDocument();

		saveDiscoveredScannerInfo(scannerInfo, rootElement, doc);
	}

	/**
	 * @param scannerInfo
	 * @param rootElement
	 * @param doc
	 */
	private static void saveDiscoveredScannerInfo(DiscoveredScannerInfo scannerInfo, Element rootElement, Document doc) {
		// Save the build info
		if (scannerInfo != null) {
			// Serialize the include paths
			Map discoveredIncludes = scannerInfo.getDiscoveredIncludePaths();
			Iterator iter = discoveredIncludes.keySet().iterator();
			while (iter.hasNext()) {
				Element pathElement = doc.createElement(INCLUDE_PATH);
				String include = (String) iter.next();
				pathElement.setAttribute(PATH, include);
				Boolean removed = (Boolean) discoveredIncludes.get(include);
				if (removed != null && removed.booleanValue() == true) {
					pathElement.setAttribute(REMOVED, "true");	//$NON-NLS-1$
				} 
				rootElement.appendChild(pathElement);
			}
			// Now do the same for the symbols
			Map discoveredSymbols = scannerInfo.getDiscoveredSymbolDefinitions();
			iter = discoveredSymbols.keySet().iterator();
			while (iter.hasNext()) {
				String symbol = (String) iter.next();
				SymbolEntry se = (SymbolEntry) discoveredSymbols.get(symbol);
				for (Iterator i = se.getActiveRaw().iterator(); i.hasNext(); ) {
					String value = (String) i.next();
					Element symbolElement = doc.createElement(DEFINED_SYMBOL);
					symbolElement.setAttribute(SYMBOL, value);
					rootElement.appendChild(symbolElement);
				}
				for (Iterator i = se.getRemovedRaw().iterator(); i.hasNext(); ) {
					String value = (String) i.next();
					Element symbolElement = doc.createElement(DEFINED_SYMBOL);
					symbolElement.setAttribute(SYMBOL, value);
					symbolElement.setAttribute(REMOVED, "true");	//$NON-NLS-1$
					rootElement.appendChild(symbolElement);
				}
			}
// descriptor is saved by MakeScannerProvider.updateScannerInfo
//			descriptor.saveProjectData();
		}
	}

	private void loadDiscoveredScannerInfoFromState(IProject project, LinkedHashMap includes, LinkedHashMap symbols) throws CoreException {
		// Save the document
		IPath path = MakeCorePlugin.getWorkingDirectory();
		path = path.append(project.getName() + ".sc");
		if (path.toFile().exists()) {
			try {
				FileInputStream file = new FileInputStream(path.toFile());
				DocumentBuilder parser = DocumentBuilderFactory.newInstance().newDocumentBuilder();
				Document document = parser.parse(file);
				Node rootElement = document.getFirstChild();
				if (rootElement.getNodeName().equals("scannerInfo")) {
					Node child = rootElement.getFirstChild();
					loadDiscoveredScannerInfo(includes, symbols, child);
				}
			} catch (IOException e) {
				throw new CoreException(new Status(IStatus.ERROR,
						MakeCorePlugin.getUniqueIdentifier(), -1,
						MakeCorePlugin.getResourceString("GCCScannerConfigUtil.Error_Message"), e));	//$NON-NLS-1$
			} catch (ParserConfigurationException e) {
				MakeCorePlugin.log(e);
			} catch (FactoryConfigurationError e) {
				MakeCorePlugin.log(e);
			} catch (SAXException e) {
				MakeCorePlugin.log(e);
			}
		}
	}
	
	private static void saveDiscoveredScannerInfoToState(DiscoveredScannerInfo scannerInfo, IProject project) throws CoreException {
		// Create document
		try {
			DocumentBuilder builder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
			Document doc = builder.newDocument();
			Element rootElement = doc.createElement("scannerInfo");
			rootElement.setAttribute("id", CDESCRIPTOR_ID);
			doc.appendChild(rootElement);

			saveDiscoveredScannerInfo(scannerInfo, rootElement, doc);
		
			// Transform the document to something we can save in a file
			ByteArrayOutputStream stream = new ByteArrayOutputStream();
			Transformer transformer = TransformerFactory.newInstance().newTransformer();
			transformer.setOutputProperty(OutputKeys.METHOD, "xml");	//$NON-NLS-1$
			transformer.setOutputProperty(OutputKeys.ENCODING, "UTF-8");	//$NON-NLS-1$
			transformer.setOutputProperty(OutputKeys.INDENT, "yes");	//$NON-NLS-1$
			DOMSource source = new DOMSource(doc);
			StreamResult result = new StreamResult(stream);
			transformer.transform(source, result);
			
			// Save the document
			IPath path = MakeCorePlugin.getWorkingDirectory();
			path = path.append(project.getName() + ".sc");
			try {
				FileOutputStream file = new FileOutputStream(path.toFile());
				file.write(stream.toByteArray());
				file.close();
			} catch (IOException e) {
				throw new CoreException(new Status(IStatus.ERROR,
						MakeCorePlugin.getUniqueIdentifier(), -1,
						MakeCorePlugin.getResourceString("GCCScannerConfigUtil.Error_Message"), e));	//$NON-NLS-1$
			}
			
			// Close the streams
			stream.close();
		} catch (ParserConfigurationException e) {
			MakeCorePlugin.log(e);
		} catch (FactoryConfigurationError e) {
			MakeCorePlugin.log(e);
		} catch (TransformerConfigurationException e) {
			MakeCorePlugin.log(e);
		} catch (TransformerFactoryConfigurationError e) {
			MakeCorePlugin.log(e);
		} catch (TransformerException e) {
			MakeCorePlugin.log(e);
		} catch (IOException e) {
			MakeCorePlugin.log(e);
		} catch (CoreException e) {
			// Save to IFile failed
			MakeCorePlugin.log(e.getStatus());
		}
		
	}
}
