/*
 * (c) Copyright QNX Software System Ltd. 2002.
 * All Rights Reserved.
 */
package org.eclipse.cdt.internal.core;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.apache.xerces.dom.DocumentImpl;
import org.apache.xml.serialize.Method;
import org.apache.xml.serialize.OutputFormat;
import org.apache.xml.serialize.Serializer;
import org.apache.xml.serialize.SerializerFactory;
import org.eclipse.cdt.core.CCorePlugin;
import org.eclipse.cdt.core.ICProjectDescriptor;
import org.eclipse.cdt.core.ICProjectOwner;
import org.eclipse.cdt.core.ICProjectOwnerInfo;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IProjectDescription;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.Status;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

public class CProjectDescriptor implements ICProjectDescriptor {
	/** constants */
	private static final String[] EMPTY_STRING_ARRAY = new String[0];
	private ICProjectOwnerInfo fOwner;
	private IProject fProject;
	private String fPlatform = "*";
	
	private static final String PROJECT_DESCRIPTION = "cdtproject";
	private static final String PROJECT_PLATFORM = "platform";

	private CProjectDescriptor(IProject project, String id) throws CoreException {
		fProject = project;
		fOwner = new CProjectOwner(id);
		IPath projectLocation = project.getDescription().getLocation();

		final boolean isDefaultLocation = projectLocation == null;
		if (isDefaultLocation) {
			projectLocation = getProjectDefaultLocation(project);
		}
		IPath descriptionPath = projectLocation.append(ICProjectDescriptor.DESCRIPTION_FILE_NAME);

		if (descriptionPath.toFile().exists()) {
			IStatus status = new Status(IStatus.WARNING, CCorePlugin.getDefault().PLUGIN_ID, CCorePlugin.STATUS_CDTPROJECT_EXISTS, "CDTProject already exisits", (Throwable)null);
			throw new CoreException(status);
		}
	}
		
	private CProjectDescriptor(IProject project) throws CoreException {
		fProject = project;
		FileInputStream file = null;
		IPath projectLocation = project.getDescription().getLocation();

		final boolean isDefaultLocation = projectLocation == null;
		if (isDefaultLocation) {
			projectLocation = getProjectDefaultLocation(project);
		}
		IPath descriptionPath = projectLocation.append(ICProjectDescriptor.DESCRIPTION_FILE_NAME);

		if (!descriptionPath.toFile().exists()) {
			IStatus status = new Status(IStatus.ERROR, CCorePlugin.getDefault().PLUGIN_ID, -1, "CDTProject file not found", (Throwable)null);
			throw new CoreException(status);
		}
		try {
			file = new FileInputStream(projectLocation.toFile());
			DocumentBuilder parser = DocumentBuilderFactory.newInstance().newDocumentBuilder();
			Document document = parser.parse(file);
			Node node = document.getFirstChild();
			if (node.getNodeName().equals(PROJECT_DESCRIPTION))
				fOwner = readProjectDescription(node);
		}
		catch (IOException e) {
		}
		catch (SAXException e) {
		}
		catch (ParserConfigurationException e) {
		}
		finally {
			if (file != null) {
				try {
					file.close();
				}
				catch (IOException e) {
				}
			}
		}
	}

	protected IPath getProjectDefaultLocation(IProject project) {
		return Platform.getLocation().append(project.getFullPath());
	}
	
	public ICProjectOwnerInfo getProjectOwner() {
		return fOwner;
	}
	
	public String getPlatform() {
		return fPlatform;
	}
	
	protected String getString(Node target, String tagName) {
		Node node = searchNode(target, tagName);
		return node != null ? (node.getFirstChild() == null ? null : node.getFirstChild().getNodeValue()) : null;
	}

	protected String[] getStrings(Node target) {
		if (target == null)
			return null;
		NodeList list = target.getChildNodes();
		if (list.getLength() == 0)
			return EMPTY_STRING_ARRAY;
		List result = new ArrayList(list.getLength());
		for (int i = 0; i < list.getLength(); i++) {
			Node node = list.item(i);
			if (node.getNodeType() == Node.ELEMENT_NODE)
				result.add((String) read(node.getChildNodes().item(0)));
		}
		return (String[]) result.toArray(new String[result.size()]);
	}

	protected Object read(Node node) {
		if (node == null)
			return null;
		switch (node.getNodeType()) {
			case Node.ELEMENT_NODE :
/*
				if (node.getNodeName().equals(BUILDER))
					return readBuildSpec(node);
				if (node.getNodeName().equals(DEBUGGER)
					return readProjectDescription(node);
*/					
			case Node.TEXT_NODE :
				String value = node.getNodeValue();
				return value == null ? null : value.trim();
			default :
				return node.toString();
		}
	}

	private ICProjectOwnerInfo readProjectDescription(Node node) {
		ICProjectOwnerInfo owner = null;
		NamedNodeMap attrib = node.getAttributes();
		try {
			owner = new CProjectOwner(attrib.getNamedItem("id").getNodeValue());
		}
		catch (CoreException e) {
			return null;
		}
		fPlatform = getString(node, PROJECT_PLATFORM);
		return owner;
	}


	protected Node searchNode(Node target, String tagName) {
		NodeList list = target.getChildNodes();
		for (int i = 0; i < list.getLength(); i++) {
			if (list.item(i).getNodeName().equals(tagName))
				return list.item(i);
		}
		return null;
	}
	
	public IProject getProject() {
		return fProject;
	}
	
	public void saveInfo() throws CoreException {
		String xml;
		try {
			xml = getAsXML();
		}
		catch (IOException e) {
			IStatus s= new Status(IStatus.ERROR, CCorePlugin.PLUGIN_ID, -1, e.getMessage(), e);
			throw new CoreException(s);
		}

		IFile rscFile = getProject().getFile(ICProjectDescriptor.DESCRIPTION_FILE_NAME);
		InputStream inputStream = new ByteArrayInputStream(xml.getBytes());
		// update the resource content
		if (rscFile.exists()) {
			rscFile.setContents(inputStream, IResource.FORCE, null);
		} else {
			rscFile.create(inputStream, IResource.FORCE, null);
		}
	}

	protected String serializeDocument(Document doc) throws IOException {
		ByteArrayOutputStream s= new ByteArrayOutputStream();
		OutputFormat format = new OutputFormat();
		format.setIndenting(true);
		format.setLineSeparator(System.getProperty("line.separator"));  //$NON-NLS-1$
		
		Serializer serializer =
			SerializerFactory.getSerializerFactory(Method.XML).makeSerializer(
				new OutputStreamWriter(s, "UTF8"), //$NON-NLS-1$
				format);
		serializer.asDOMSerializer().serialize(doc);
		return s.toString("UTF8"); //$NON-NLS-1$		
	}

	protected String getAsXML() throws IOException {
		Element element;
		Document doc = new DocumentImpl();
		Element configRootElement = doc.createElement(PROJECT_DESCRIPTION);
		doc.appendChild(configRootElement);
		configRootElement.setAttribute("id", fOwner.getID()); //$NON-NLS-1$
		element= createBuilderElement(doc);
		if ( element != null )
			configRootElement.appendChild(element);
		return serializeDocument(doc);
	}	

		
	protected Element createBuilderElement(Document doc) {
/*
		if ( builderID != null ) {
			Element element = doc.createElement("cdtBuilder");
			element.setAttribute("id", builderID);
			return element;
		}
*/
		return null;
	}

	public static synchronized ICProjectDescriptor getDescription(IProject project) throws CoreException {
		return new CProjectDescriptor(project);
	}

	public static synchronized void configure(IProject project, String id) throws CoreException {
		CProjectDescriptor cproject;
		try {
			cproject = new CProjectDescriptor(project, id);
		}
		catch (CoreException e) { // if .cdtproject already exists will use that
			IStatus status = e.getStatus();
			if ( status.getCode() == CCorePlugin.STATUS_CDTPROJECT_EXISTS )
				return;
			else
				throw e;
		}
		CProjectOwner cowner = new CProjectOwner(id);
		cowner.configure(project, cproject);
		cproject.saveInfo();
	}

}
