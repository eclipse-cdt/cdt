package org.eclipse.cdt.internal.corext.template;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */

import org.eclipse.cdt.internal.ui.CStatusConstants;
import org.eclipse.cdt.internal.ui.CUIException;
import org.eclipse.cdt.internal.ui.CUIStatus;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.apache.xml.serialize.OutputFormat;
import org.apache.xml.serialize.Serializer;
import org.apache.xml.serialize.SerializerFactory;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IStatus;

import org.w3c.dom.Attr;
import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.Text;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

/**
 * <code>TemplateSet</code> manages a collection of templates and makes them
 * persistent.
 */
public class TemplateSet {
	
	private static class TemplateComparator implements Comparator {
		public int compare(Object arg0, Object arg1) {
			if (arg0 == arg1)
				return 0;
			
			if (arg0 == null)
				return -1;
				
			Template template0= (Template) arg0;
			Template template1= (Template) arg1;
			
			return template0.getName().compareTo(template1.getName());
		}
	}

	private static final String TEMPLATE_TAG= "template"; //$NON-NLS-1$
	private static final String NAME_ATTRIBUTE= "name"; //$NON-NLS-1$
	private static final String DESCRIPTION_ATTRIBUTE= "description"; //$NON-NLS-1$
	private static final String CONTEXT_ATTRIBUTE= "context"; //$NON-NLS-1$
	private static final String ENABLED_ATTRIBUTE= "enabled"; //$NON-NLS-1$

	private List fTemplates= new ArrayList();
	private Comparator fTemplateComparator= new TemplateComparator();
	private Template[] fSortedTemplates= new Template[0];
	
	/**
	 * Convenience method for reading templates from a file.
	 * 
	 * @see addFromStream(InputStream)
	 */
	public void addFromFile(File file) throws CoreException {
		InputStream stream= null;

		try {
			stream= new FileInputStream(file);
			addFromStream(stream);

		} catch (IOException e) {
			throwReadException(e);

		} finally {
			try {
				if (stream != null)
					stream.close();
			} catch (IOException e) {}
		}		
	}

	/**
	 * Reads templates from a XML stream and adds them to the template set.
	 */	
	public void addFromStream(InputStream stream) throws CoreException {
		try {
			DocumentBuilderFactory factory= DocumentBuilderFactory.newInstance();
			DocumentBuilder parser= factory.newDocumentBuilder();		
			Document document= parser.parse(new InputSource(stream));
			NodeList elements= document.getElementsByTagName(TEMPLATE_TAG);
			
			int count= elements.getLength();
			for (int i= 0; i != count; i++) {
				Node node= elements.item(i);					
				NamedNodeMap attributes= node.getAttributes();

				if (attributes == null)
					continue;

				String name= getAttributeValue(attributes, NAME_ATTRIBUTE);
				String description= getAttributeValue(attributes, DESCRIPTION_ATTRIBUTE);
				String context= getAttributeValue(attributes, CONTEXT_ATTRIBUTE);
				Node enabledNode= attributes.getNamedItem(ENABLED_ATTRIBUTE);

				if (name == null || description == null || context == null)
					throw new SAXException(TemplateMessages.getString("TemplateSet.error.missingAttribute")); //$NON-NLS-1$

				boolean enabled= (enabledNode == null) || (enabledNode.getNodeValue().equals("true")); //$NON-NLS-1$

				StringBuffer buffer= new StringBuffer();
				NodeList children= node.getChildNodes();
				for (int j= 0; j != children.getLength(); j++) {
					String value= children.item(j).getNodeValue();
					if (value != null)
						buffer.append(value);
				}
				String pattern= buffer.toString().trim();

				Template template= new Template(name, description, context, pattern);	
				template.setEnabled(enabled);
				add(template);
			}
	
			sort();

		} catch (ParserConfigurationException e) {
			throwReadException(e);
		} catch (IOException e) {
			throwReadException(e);
		} catch (SAXException e) {
			throwReadException(e);
		}
	}
	
	private String getAttributeValue(NamedNodeMap attributes, String name) {
		Node node= attributes.getNamedItem(name);

		return node == null
			? null
			: node.getNodeValue();
	}

	/**
	 * Convenience method for saving to a file.
	 * 
	 * @see saveToStream(OutputStream)
	 */
	public void saveToFile(File file) throws CoreException {
		OutputStream stream= null;

		try {
			stream= new FileOutputStream(file);
			saveToStream(stream);

		} catch (IOException e) {
			throwWriteException(e);

		} finally {
			try {
				if (stream != null)
					stream.close();
			} catch (IOException e) {}
		}
	}
		
	/**
	 * Saves the template set as XML.
	 */
	public void saveToStream(OutputStream stream) throws CoreException {
		try {
			DocumentBuilderFactory factory= DocumentBuilderFactory.newInstance();
			DocumentBuilder builder= factory.newDocumentBuilder();		
			Document document= builder.newDocument();

			Node root= document.createElement("templates"); // $NON-NLS-1$ //$NON-NLS-1$
			document.appendChild(root);
			
			for (int i= 0; i != fTemplates.size(); i++) {
				Template template= (Template) fTemplates.get(i);
				
				Node node= document.createElement("template"); // $NON-NLS-1$ //$NON-NLS-1$
				root.appendChild(node);
				
				NamedNodeMap attributes= node.getAttributes();
				
				Attr name= document.createAttribute(NAME_ATTRIBUTE);
				name.setValue(template.getName());
				attributes.setNamedItem(name);
	
				Attr description= document.createAttribute(DESCRIPTION_ATTRIBUTE);
				description.setValue(template.getDescription());
				attributes.setNamedItem(description);
	
				Attr context= document.createAttribute(CONTEXT_ATTRIBUTE);
				context.setValue(template.getContextTypeName());
				attributes.setNamedItem(context);			

				Attr enabled= document.createAttribute(ENABLED_ATTRIBUTE);
				enabled.setValue(template.isEnabled() ? "true" : "false"); //$NON-NLS-1$ //$NON-NLS-2$
				attributes.setNamedItem(enabled);
				
				Text pattern= document.createTextNode(template.getPattern());
				node.appendChild(pattern);			
			}		
			
			OutputFormat format = new OutputFormat();
			format.setPreserveSpace(true);
			Serializer serializer = SerializerFactory.getSerializerFactory("xml").makeSerializer(stream, format); //$NON-NLS-1$
			serializer.asDOMSerializer().serialize(document);

		} catch (ParserConfigurationException e) {
			throwWriteException(e);
		} catch (IOException e) {
			throwWriteException(e);
		}		
	}

	private static void throwReadException(Throwable t) throws CoreException {
		IStatus status= new CUIStatus(CStatusConstants.TEMPLATE_IO_EXCEPTION,
			TemplateMessages.getString("TemplateSet.error.read"), t); //$NON-NLS-1$
		throw new CUIException(status);
	}
	
	private static void throwWriteException(Throwable t) throws CoreException {
		IStatus status= new CUIStatus(CStatusConstants.TEMPLATE_IO_EXCEPTION,
			TemplateMessages.getString("TemplateSet.error.write"), t); //$NON-NLS-1$
		throw new CUIException(status);
	}

	/**
	 * Adds a template to the set.
	 */
	public void add(Template template) {
		if (exists(template))
			return; // ignore duplicate
		
		fTemplates.add(template);
		sort();
	}

	private boolean exists(Template template) {
		for (Iterator iterator = fTemplates.iterator(); iterator.hasNext();) {
			Template anotherTemplate = (Template) iterator.next();

			if (template.equals(anotherTemplate))
				return true;
		}
		
		return false;
	}
	
	/**
	 * Removes a template to the set.
	 */	
	public void remove(Template template) {
		fTemplates.remove(template);
		sort();
	}

	/**
	 * Empties the set.
	 */		
	public void clear() {
		fTemplates.clear();
		sort();
	}
	
	/**
	 * Returns all templates.
	 */
	public Template[] getTemplates() {
		return (Template[]) fTemplates.toArray(new Template[fTemplates.size()]);
	}
	
	private void sort() {
		fSortedTemplates= (Template[]) fTemplates.toArray(new Template[fTemplates.size()]);
		Arrays.sort(fSortedTemplates, fTemplateComparator);
	}
	
}

