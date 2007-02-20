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

import org.eclipse.cdt.make.core.scannerconfig.InfoContext;
import org.eclipse.cdt.make.core.scannerconfig.IDiscoveredPathManager.IDiscoveredScannerInfoSerializable;
import org.eclipse.cdt.make.internal.core.scannerconfig2.PerProjectSICollector;
import org.eclipse.cdt.managedbuilder.core.IConfiguration;
import org.eclipse.cdt.managedbuilder.core.IInputType;
import org.eclipse.cdt.managedbuilder.core.ITool;
import org.eclipse.cdt.managedbuilder.core.ManagedBuilderCorePlugin;
import org.eclipse.cdt.newmake.internal.core.MakeMessages;
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
            QualifiedName(ManagedBuilderCorePlugin.getUniqueIdentifier(), "discoveredScannerConfigFileName"); //$NON-NLS-1$
	private static final String CDESCRIPTOR_ID = ManagedBuilderCorePlugin.getUniqueIdentifier() + ".discoveredScannerInfo"; //$NON-NLS-1$
    public static final String SCD_STORE_VERSION = "scdStore"; //$NON-NLS-1$
	public static final String SI_ELEM = "scannerInfo"; //$NON-NLS-1$
	public static final String COLLECTOR_ELEM = "collector"; //$NON-NLS-1$
	public static final String CFG_ELEM = "configuration"; //$NON-NLS-1$
	public static final String TOOL_ELEM = "tool"; //$NON-NLS-1$
	public static final String INTYPE_ELEM = "inputType"; //$NON-NLS-1$
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

	public void loadDiscoveredScannerInfoFromState(InfoContext context, IDiscoveredScannerInfoSerializable serializable) throws CoreException{
		loadDiscoveredScannerInfoFromState(context.getConfiguration().getOwner().getProject(), context, serializable);
	}
	
	public void loadDiscoveredScannerInfoFromState(IProject project, IDiscoveredScannerInfoSerializable serializable) throws CoreException{
		loadDiscoveredScannerInfoFromState(project, null, serializable);
	}

	public void loadDiscoveredScannerInfoFromState(IProject project, InfoContext context, IDiscoveredScannerInfoSerializable serializable)
			throws CoreException {
		// Get the document
		Document document = getDocument(project);
		if (document != null) {
			NodeList rootList = document.getElementsByTagName(SI_ELEM);
			if (rootList.getLength() > 0) {
				Element rootElem = (Element) rootList.item(0);
				
		        if(context != null && context.getConfiguration() != null){
					IConfiguration cfg = context.getConfiguration();
					ITool tool = context.getTool();
					IInputType inType = context.getInputType();

					Element cfgElem = findChild(rootElem, CFG_ELEM, ID_ATTR, cfg.getId());
		        	Element toolElem = null;
		        	Element inTypeEl = null;
		        	
		        	
		        	if(cfgElem != null){
		        		if(tool != null){
		        		toolElem = findChild(cfgElem, TOOL_ELEM, ID_ATTR, tool.getId());
			        		if(toolElem != null){
			        			if(inType != null){
			        				inTypeEl = findChild(toolElem, INTYPE_ELEM, ID_ATTR, inType.getId());
			        			}
			        		}
		        		}
		        	}
		        	
		        	if(inType != null){
		        		rootElem = inTypeEl;
		        	} else if(tool != null){
		        		rootElem = toolElem;
		        	} else if(cfg != null){
		        		rootElem = cfgElem;
		        	}
		        }
		        
				
				// get the collector element
		        if(rootElem != null){
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
		    	ManagedBuilderCorePlugin.log(e);
		        throw new CoreException(new Status(IStatus.ERROR, ManagedBuilderCorePlugin.getUniqueIdentifier(), -1,
		                MakeMessages.getString("DiscoveredPathManager.File_Error_Message"), e)); //$NON-NLS-1$
		    }
		    catch (ParserConfigurationException e) {
		    	ManagedBuilderCorePlugin.log(e);
		        throw new CoreException(new Status(IStatus.ERROR, ManagedBuilderCorePlugin.getUniqueIdentifier(), -1,
		                MakeMessages.getString("DiscoveredPathManager.File_Error_Message"), e)); //$NON-NLS-1$
		    }
		    catch (SAXException e) {
		    	ManagedBuilderCorePlugin.log(e);
		        throw new CoreException(new Status(IStatus.ERROR, ManagedBuilderCorePlugin.getUniqueIdentifier(), -1,
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

	private Element findChild(Element parentElem, String name, String attr, String attrValue){
        Element cfgElem = null;
        NodeList cfgList = parentElem.getElementsByTagName(name);
        if (cfgList.getLength() > 0) {
        	// find per file collector element and remove children
        	for (int i = 0; i < cfgList.getLength(); ++i) {
        		Element cElem = (Element) cfgList.item(i);
        		String value = cElem.getAttribute(attr);
        		if (value.equals(attrValue)) {
        			cfgElem = cElem;
        			break;
        		}
        	}
        }
        
        return cfgElem;
	}
	
	private void clearChildren(Element cElem){
		for (Node child = cElem.getFirstChild(); child != null; 
		child = cElem.getFirstChild()) {
				cElem.removeChild(child);
		}
	}

	

	/**
	* @param scannerInfo
	* @param rootElement
	* @param doc
	*/
	private void saveDiscoveredScannerInfo(IDiscoveredScannerInfoSerializable serializable, Document doc, InfoContext context) {
		NodeList rootList = doc.getElementsByTagName(SI_ELEM);
		if (rootList.getLength() > 0) {
			Element rootElem = (Element) rootList.item(0);
			if(context != null){
				IConfiguration cfg = context.getConfiguration();
				ITool tool = context.getTool();
				IInputType inType = context.getInputType();
				
				// get the collector element
				if(cfg != null){
			        Element cfgElem = findChild(rootElem, CFG_ELEM, ID_ATTR, cfg.getId());
			        Element toolElem = null;
			        Element inTypeElem = null;
			        if(cfgElem != null){
			        	if(tool != null){
				        	toolElem = findChild(cfgElem, TOOL_ELEM, ID_ATTR, tool.getId());
				        	if(toolElem != null){
				        		if(inType != null){
				        			inTypeElem = findChild(toolElem, INTYPE_ELEM, ID_ATTR, inType.getId());
				        		}
				        	}
			        	}
			        }
			        if(cfgElem == null){
			        	cfgElem = doc.createElement(CFG_ELEM);
			        	cfgElem.setAttribute(ID_ATTR, cfg.getId());
			        	rootElem.appendChild(cfgElem);
			        }
			        if(tool != null){
				        if(toolElem == null){
				        	toolElem = doc.createElement(TOOL_ELEM);
				        	toolElem.setAttribute(ID_ATTR, tool.getId());
				        	cfgElem.appendChild(toolElem);
				        }
				        if(inType != null){
					        if(inTypeElem == null){
					        	inTypeElem = doc.createElement(INTYPE_ELEM);
					        	inTypeElem.setAttribute(ID_ATTR, inType.getId());
					        	toolElem.appendChild(inTypeElem);
					        }
				        }
			        }
			        
			        if(inTypeElem != null){
			        	rootElem = inTypeElem;
			        } else if(toolElem != null){
			        	rootElem = toolElem;
			        } else {
			        	rootElem = cfgElem;
			        }
				}
			}
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

	public void saveDiscoveredScannerInfoToState(InfoContext context, IDiscoveredScannerInfoSerializable serializable) throws CoreException {
		saveDiscoveredScannerInfoToState(context.getConfiguration().getOwner().getProject(), context, serializable);
	}

	public void saveDiscoveredScannerInfoToState(IProject project, IDiscoveredScannerInfoSerializable serializable) throws CoreException {
		saveDiscoveredScannerInfoToState(project, null, serializable);
	}

	public void saveDiscoveredScannerInfoToState(IProject project, InfoContext context, IDiscoveredScannerInfoSerializable serializable) throws CoreException {
		Document document = getDocument(project);
		// Create document
		try {
			saveDiscoveredScannerInfo(serializable, document, context);
		
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
				throw new CoreException(new Status(IStatus.ERROR, ManagedBuilderCorePlugin.getUniqueIdentifier(), -1,
						MakeMessages.getString("DiscoveredPathManager.File_Error_Message"), e)); //$NON-NLS-1$
			}
		
			// Close the streams
			stream.close();
		} catch (TransformerException e) {
			ManagedBuilderCorePlugin.log(e);
			throw new CoreException(new Status(IStatus.ERROR, ManagedBuilderCorePlugin.getUniqueIdentifier(), -1,
					MakeMessages.getString("DiscoveredPathManager.File_Error_Message"), e)); //$NON-NLS-1$
		} catch (IOException e) {
			ManagedBuilderCorePlugin.log(e);
			throw new CoreException(new Status(IStatus.ERROR, ManagedBuilderCorePlugin.getUniqueIdentifier(), -1,
					MakeMessages.getString("DiscoveredPathManager.File_Error_Message"), e)); //$NON-NLS-1$
		}
	}

    public IPath getDiscoveredScannerConfigStore(IProject project) {
        String fileName = project.getName() + ".sc"; //$NON-NLS-1$
        String storedFileName = null;
        try {
            storedFileName = project.getPersistentProperty(dscFileNameProperty);
        } catch (CoreException e) {
        	ManagedBuilderCorePlugin.log(e.getStatus());
        }
        if (storedFileName != null && !storedFileName.equals(fileName)) {
            // try to move 2.x file name format to 3.x file name format
            movePluginStateFile(storedFileName, fileName);
        }
        try {
            project.setPersistentProperty(dscFileNameProperty, fileName);
        } catch (CoreException e) {
        	ManagedBuilderCorePlugin.log(e.getStatus());
        }

        return ManagedBuilderCorePlugin.getWorkingDirectory().append(fileName);
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
        	ManagedBuilderCorePlugin.log(e);
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
        IPath path = ManagedBuilderCorePlugin.getWorkingDirectory().append(scFileName);
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
        	ManagedBuilderCorePlugin.log(e);
        }
    }

    /**
     * @param oldFileName
     * @param newFileName
     */
    private void movePluginStateFile(String oldFileName, String newFileName) {
        IPath oldPath = ManagedBuilderCorePlugin.getWorkingDirectory().append(oldFileName);
        IPath newPath = ManagedBuilderCorePlugin.getWorkingDirectory().append(newFileName);
        File oldFile = oldPath.toFile();
        File newFile = newPath.toFile();
        if (oldFile.exists()) {
            oldFile.renameTo(newFile);
        }
    }

}
