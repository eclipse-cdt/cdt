/*******************************************************************************
 * Copyright (c) 2004, 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * IBM - Initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.make.internal.core.scannerconfig;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.HashMap;
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

import org.eclipse.cdt.make.core.MakeCorePlugin;
import org.eclipse.cdt.make.core.scannerconfig.IDiscoveredPathManager.IDiscoveredScannerInfoSerializable;
import org.eclipse.cdt.make.internal.core.MakeMessages;
import org.eclipse.cdt.make.internal.core.scannerconfig2.PerProjectSICollector;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceDelta;
import org.eclipse.core.resources.IResourceDeltaVisitor;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.QualifiedName;
import org.eclipse.core.runtime.Status;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.ProcessingInstruction;
import org.xml.sax.SAXException;

/**
 * Discovered scanner info persistance store
 * 
 * @author vhirsl
 */
public class DiscoveredScannerInfoStore {
	private static final QualifiedName dscFileNameProperty = new 
            QualifiedName(MakeCorePlugin.getUniqueIdentifier(), "discoveredScannerConfigFileName"); //$NON-NLS-1$
	private static final String CDESCRIPTOR_ID = MakeCorePlugin.getUniqueIdentifier() + ".discoveredScannerInfo"; //$NON-NLS-1$
    public static final String SCD_STORE_VERSION = "scdStore"; //$NON-NLS-1$
	public static final String SI_ELEM = "scannerInfo"; //$NON-NLS-1$
	public static final String COLLECTOR_ELEM = "collector"; //$NON-NLS-1$
	public static final String ID_ATTR = "id"; //$NON-NLS-1$

	private static DiscoveredScannerInfoStore instance;

	private Map fDocumentMap = new HashMap();

	public static DiscoveredScannerInfoStore getInstance() {
		if (instance == null) {
			instance = new DiscoveredScannerInfoStore();
		}
		return instance;
	}
	/**
	 * 
	 */
	private DiscoveredScannerInfoStore() {
	}

	public void loadDiscoveredScannerInfoFromState(IProject project, IDiscoveredScannerInfoSerializable serializable)
			throws CoreException {
		// Get the document
		Document document = getDocument(project);
		if (document != null) {
			NodeList rootList = document.getElementsByTagName(SI_ELEM);
			if (rootList.getLength() > 0) {
				Element rootElem = (Element) rootList.item(0);
		        // get the collector element
		        NodeList collectorList = rootElem.getElementsByTagName(COLLECTOR_ELEM);
		        if (collectorList.getLength() > 0) {
		        	// find the collector element
		        	for (int i = 0; i < collectorList.getLength(); ++i) {
		        		Element collectorElem = (Element) collectorList.item(i);
		        		String collectorId = collectorElem.getAttribute(ID_ATTR);
		        		if (serializable.getCollectorId().equals(collectorId)) {
		    		        serializable.deserialize(collectorElem);
		        			break;
		        		}
		        	}
		        }
			}
		}
	}

	private Document getDocument(IProject project) throws CoreException {
		// Get the document
		Document document = (Document) fDocumentMap.get(project);
		if (document == null) {
		    try {
		        DocumentBuilder builder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
		        IPath path = getDiscoveredScannerConfigStore(project);
		        if (path.toFile().exists()) {
		            // read form file
		            FileInputStream file = new FileInputStream(path.toFile());
		            document = builder.parse(file);
		            Node rootElem = document.getFirstChild();
		            if (rootElem.getNodeType() != Node.PROCESSING_INSTRUCTION_NODE) {
		                // no version info; upgrade
		                upgradeDocument(document, project);
		            }
		        }
		        else {
		            // create new document
		            document = builder.newDocument();
		            ProcessingInstruction pi = document.createProcessingInstruction(SCD_STORE_VERSION, "version=\"2\""); //$NON-NLS-1$
		            document.appendChild(pi);
		            Element rootElement = document.createElement(SI_ELEM); //$NON-NLS-1$
		            rootElement.setAttribute(ID_ATTR, CDESCRIPTOR_ID); //$NON-NLS-1$
		            document.appendChild(rootElement);
		        }
		        fDocumentMap.put(project, document);
		    }
		    catch (IOException e) {
		        MakeCorePlugin.log(e);
		        throw new CoreException(new Status(IStatus.ERROR, MakeCorePlugin.getUniqueIdentifier(), -1,
		                MakeMessages.getString("DiscoveredPathManager.File_Error_Message"), e)); //$NON-NLS-1$
		    }
		    catch (ParserConfigurationException e) {
		        MakeCorePlugin.log(e);
		        throw new CoreException(new Status(IStatus.ERROR, MakeCorePlugin.getUniqueIdentifier(), -1,
		                MakeMessages.getString("DiscoveredPathManager.File_Error_Message"), e)); //$NON-NLS-1$
		    }
		    catch (SAXException e) {
		        MakeCorePlugin.log(e);
		        throw new CoreException(new Status(IStatus.ERROR, MakeCorePlugin.getUniqueIdentifier(), -1,
		                MakeMessages.getString("DiscoveredPathManager.File_Error_Message"), e)); //$NON-NLS-1$
		    }
		}
		return document;
	}

	/**
	* @param document
	* @param project 
	*/
	private void upgradeDocument(Document document, IProject project) {
		Element rootElem = (Element) document.getElementsByTagName(SI_ELEM).item(0); //$NON-NLS-1$
		ProcessingInstruction pi = document.createProcessingInstruction(SCD_STORE_VERSION, "version=\"2.0\""); //$NON-NLS-1$
		document.insertBefore(pi, rootElem);
		
		Element collectorElem = document.createElement(COLLECTOR_ELEM);
		collectorElem.setAttribute(ID_ATTR, PerProjectSICollector.COLLECTOR_ID);
		for (Node child = rootElem.getFirstChild(); child != null; child = rootElem.getFirstChild()) {
			collectorElem.appendChild(rootElem.removeChild(child));
		}
		rootElem.appendChild(collectorElem);
	}

	/**
	* @param scannerInfo
	* @param rootElement
	* @param doc
	*/
	private void saveDiscoveredScannerInfo(IDiscoveredScannerInfoSerializable serializable, Document doc) {
		NodeList rootList = doc.getElementsByTagName(SI_ELEM);
		if (rootList.getLength() > 0) {
			Element rootElem = (Element) rootList.item(0);
			// get the collector element
	        Element collectorElem = null;
	        NodeList collectorList = rootElem.getElementsByTagName(COLLECTOR_ELEM);
	        if (collectorList.getLength() > 0) {
	        	// find per file collector element and remove children
	        	for (int i = 0; i < collectorList.getLength(); ++i) {
	        		Element cElem = (Element) collectorList.item(i);
	        		String collectorId = cElem.getAttribute(ID_ATTR);
	        		if (serializable.getCollectorId().equals(collectorId)) {
	        			for (Node child = cElem.getFirstChild(); child != null; 
	        					child = cElem.getFirstChild()) {
	        				cElem.removeChild(child);
	        			}
	        			collectorElem = cElem;
	        			break;
	        		}
	        	}
	        }
	        if (collectorElem == null) {
	        	// create per profile element
	        	collectorElem = doc.createElement(COLLECTOR_ELEM);
	        	collectorElem.setAttribute(ID_ATTR, serializable.getCollectorId());
	        	rootElem.appendChild(collectorElem);
	        }
	        
			// Save the discovered scanner info
			serializable.serialize(collectorElem);
		}
	}
	
	public void saveDiscoveredScannerInfoToState(IProject project, IDiscoveredScannerInfoSerializable serializable) throws CoreException {
		Document document = getDocument(project);
		// Create document
		try {
			saveDiscoveredScannerInfo(serializable, document);
		
			// Transform the document to something we can save in a file
			ByteArrayOutputStream stream = new ByteArrayOutputStream();
			Transformer transformer = TransformerFactory.newInstance().newTransformer();
			transformer.setOutputProperty(OutputKeys.METHOD, "xml"); //$NON-NLS-1$
			transformer.setOutputProperty(OutputKeys.ENCODING, "UTF-8"); //$NON-NLS-1$
			transformer.setOutputProperty(OutputKeys.INDENT, "yes"); //$NON-NLS-1$
			DOMSource source = new DOMSource(document);
			StreamResult result = new StreamResult(stream);
			transformer.transform(source, result);
		
			// Save the document
			try {
				IPath path = getDiscoveredScannerConfigStore(project);
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
		}
	}

    public IPath getDiscoveredScannerConfigStore(IProject project) {
        String fileName = project.getName() + ".sc"; //$NON-NLS-1$
        String storedFileName = null;
        try {
            storedFileName = project.getPersistentProperty(dscFileNameProperty);
        } catch (CoreException e) {
            MakeCorePlugin.log(e.getStatus());
        }
        if (storedFileName != null && !storedFileName.equals(fileName)) {
            // try to move 2.x file name format to 3.x file name format
            movePluginStateFile(storedFileName, fileName);
        }
        try {
            project.setPersistentProperty(dscFileNameProperty, fileName);
        } catch (CoreException e) {
            MakeCorePlugin.log(e.getStatus());
        }

        return MakeCorePlugin.getWorkingDirectory().append(fileName);
    }

    /**
     * @param delta
     */
    public void updateScannerConfigStore(IResourceDelta delta) {
        try {
            delta.accept(new IResourceDeltaVisitor() {

                public boolean visit(IResourceDelta delta) throws CoreException {
                    IResource resource = delta.getResource();
                    if (resource instanceof IProject) {
                        IProject project = (IProject) resource;
                        int kind = delta.getKind();
                        switch (kind) {
                        case IResourceDelta.REMOVED:
                            if ((delta.getFlags() & IResourceDelta.MOVED_TO) != 0) {
                                // project renamed
                                IPath newPath = delta.getMovedToPath();
                                IProject newProject = delta.getResource().getWorkspace().
                                        getRoot().getProject(newPath.toString());
                                scProjectRenamed(project, newProject);
                            }
                            else {
                                // project deleted
                                scProjectDeleted(project);
                            }
                            // remove from cache
                            fDocumentMap.put(project, null);
                        }
                        return false;
                    }
                    return true;
                }

            });
        }
        catch (CoreException e) {
            MakeCorePlugin.log(e);
        }
    }

    private void scProjectDeleted(IProject project) {
        String scFileName = project.getName() + ".sc"; //$NON-NLS-1$
        deletePluginStateFile(scFileName);
    }

    /**
     * @param scFileName
     */
    private void deletePluginStateFile(String scFileName) {
        IPath path = MakeCorePlugin.getWorkingDirectory().append(scFileName);
        File file = path.toFile();
        if (file.exists()) {
            file.delete();
        }
    }

    private void scProjectRenamed(IProject project, IProject newProject) {
        String scOldFileName = project.getName() + ".sc"; //$NON-NLS-1$
        String scNewFileName = newProject.getName() + ".sc"; //$NON-NLS-1$
        movePluginStateFile(scOldFileName, scNewFileName);
        try {
            newProject.setPersistentProperty(dscFileNameProperty, scNewFileName);
        }
        catch (CoreException e) {
            MakeCorePlugin.log(e);
        }
    }

    /**
     * @param oldFileName
     * @param newFileName
     */
    private void movePluginStateFile(String oldFileName, String newFileName) {
        IPath oldPath = MakeCorePlugin.getWorkingDirectory().append(oldFileName);
        IPath newPath = MakeCorePlugin.getWorkingDirectory().append(newFileName);
        File oldFile = oldPath.toFile();
        File newFile = newPath.toFile();
        if (oldFile.exists()) {
            oldFile.renameTo(newFile);
        }
    }

}
