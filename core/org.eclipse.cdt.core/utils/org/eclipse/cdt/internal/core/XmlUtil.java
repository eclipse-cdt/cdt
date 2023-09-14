/*******************************************************************************
 * Copyright (c) 2009, 2013 Andrew Gvozdev (Quoin Inc.).
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Andrew Gvozdev (Quoin Inc.)
 *******************************************************************************/
package org.eclipse.cdt.internal.core;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.util.Arrays;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.eclipse.cdt.core.CCorePlugin;
import org.eclipse.cdt.core.resources.ResourcesUtil;
import org.eclipse.cdt.internal.core.model.Util;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * XML utilities.
 *
 */
public class XmlUtil {
	private static final String ENCODING_UTF_8 = "UTF-8"; //$NON-NLS-1$
	private static final String EOL_XML = "\n"; //$NON-NLS-1$
	private static final String DEFAULT_INDENT = "\t"; //$NON-NLS-1$
	private static String LINE_SEPARATOR = System.getProperty("line.separator"); //$NON-NLS-1$

	/**
	 * Convenience method to create new XML DOM Document.
	 *
	 * @return a new instance of a DOM {@link Document}.
	 * @throws ParserConfigurationException in case of a problem retrieving {@link DocumentBuilder}.
	 */
	public static Document newDocument() throws ParserConfigurationException {
		DocumentBuilder builder = XmlProcessorFactoryCdt.createDocumentBuilderWithErrorOnDOCTYPE();
		return builder.newDocument();
	}

	/**
	 * Convenience method to retrieve value of a node.
	 * @return node value or {@code null}
	 */
	public static String determineNodeValue(Node node) {
		return node != null ? node.getNodeValue() : null;
	}

	/**
	 * Convenience method to retrieve an attribute of an element.
	 * Note that calling element.getAttributes() once may be more efficient when pulling several attributes.
	 *
	 * @param element - element to retrieve the attribute from.
	 * @param attr - attribute to get value.
	 * @return attribute value or {@code null}
	 */
	public static String determineAttributeValue(Node element, String attr) {
		NamedNodeMap attributes = element.getAttributes();
		return attributes != null ? determineNodeValue(attributes.getNamedItem(attr)) : null;
	}

	/**
	 * The method creates an element with specified name and attributes and appends it to the parent element.
	 * This is a convenience method for often used sequence of calls.
	 *
	 * @param parent - the node where to append the new element.
	 * @param name - the name of the element type being created.
	 * @param attributes - string array of pairs attributes and their values.
	 *     Each attribute must have a value, so the array must have even number of elements.
	 * @return the newly created element.
	 *
	 * @throws ArrayIndexOutOfBoundsException in case of odd number of elements of the attribute array
	 *    (i.e. the last attribute is missing a value).
	 */
	public static Element appendElement(Node parent, String name, String[] attributes) {
		Document doc = parent instanceof Document ? (Document) parent : parent.getOwnerDocument();
		Element element = doc.createElement(name);
		if (attributes != null) {
			int attrLen = attributes.length;
			for (int i = 0; i < attrLen; i += 2) {
				String attrName = attributes[i];
				String attrValue = attributes[i + 1];
				element.setAttribute(attrName, attrValue);
			}
		}
		parent.appendChild(element);
		return element;
	}

	/**
	 * The method creates an element with specified name and appends it to the parent element.
	 * This is a shortcut for {@link #appendElement(Node, String, String[])} with no attributes specified.
	 *
	 * @param parent - the node where to append the new element.
	 * @param name - the name of the element type being created.
	 * @return the newly created element.
	 */
	public static Element appendElement(Node parent, String name) {
		return appendElement(parent, name, null);
	}

	/**
	 * As a workaround for {@code javax.xml.transform.Transformer} not being able
	 * to pretty print XML. This method prepares DOM {@code Document} for the transformer
	 * to be pretty printed, i.e. providing proper indentations for enclosed tags.
	 *
	 * Note, while this was originally a workaround, the user community of CDT
	 * has come to expect the format of the .cproject file and others to be
	 * unchanging, therefore CDT always uses this and does not attempt
	 * to format with the Transformer.
	 *
	 * @param doc - DOM document to be pretty printed
	 */
	public static void prettyFormat(Document doc) {
		prettyFormat(doc, DEFAULT_INDENT);
	}

	/**
	 * As a workaround for {@code javax.xml.transform.Transformer} not being able
	 * to pretty print XML. This method prepares DOM {@code Document} for the transformer
	 * to be pretty printed, i.e. providing proper indentations for enclosed tags.
	 *
	 * Note, while this was originally a workaround, the user community of CDT
	 * has come to expect the format of the .cproject file and others to be
	 * unchanging, therefore CDT always uses this and does not attempt
	 * to format with the Transformer.
	 *
	 * @param doc - DOM document to be pretty printed
	 * @param indent - custom indentation as a string of white spaces
	 */
	public static void prettyFormat(Document doc, String indent) {
		doc.normalize();
		Element documentElement = doc.getDocumentElement();
		if (documentElement != null) {
			prettyFormat(documentElement, "", indent); //$NON-NLS-1$
		}
	}

	/**
	 * The method inserts end-of-line+indentation Text nodes where indentation is necessary.
	 *
	 * @param node - node to be pretty formatted
	 * @param indentLevel - initial indentation level of the node
	 * @param indent - additional indentation inside the node
	 */
	private static void prettyFormat(Node node, String indentLevel, String indent) {
		NodeList nodelist = node.getChildNodes();
		int iStart = 0;
		Node item = nodelist.item(0);
		if (item != null) {
			short type = item.getNodeType();
			if (type == Node.ELEMENT_NODE || type == Node.COMMENT_NODE) {
				Node newChild = node.getOwnerDocument().createTextNode(EOL_XML + indentLevel + indent);
				node.insertBefore(newChild, item);
				iStart = 1;
			}
		}
		for (int i = iStart; i < nodelist.getLength(); i++) {
			item = nodelist.item(i);
			if (item != null) {
				short type = item.getNodeType();
				if (type == Node.TEXT_NODE && item.getNodeValue().trim().length() == 0) {
					if (i + 1 < nodelist.getLength()) {
						item.setNodeValue(EOL_XML + indentLevel + indent);
					} else {
						item.setNodeValue(EOL_XML + indentLevel);
					}
				} else if (type == Node.ELEMENT_NODE) {
					prettyFormat(item, indentLevel + indent, indent);
					if (i + 1 < nodelist.getLength()) {
						Node nextItem = nodelist.item(i + 1);
						if (nextItem != null) {
							short nextType = nextItem.getNodeType();
							if (nextType == Node.ELEMENT_NODE || nextType == Node.COMMENT_NODE) {
								Node newChild = node.getOwnerDocument().createTextNode(EOL_XML + indentLevel + indent);
								node.insertBefore(newChild, nextItem);
								i++;
								continue;
							}
						}
					} else {
						Node newChild = node.getOwnerDocument().createTextNode(EOL_XML + indentLevel);
						node.appendChild(newChild);
						i++;
						continue;
					}
				}
			}
		}
	}

	/**
	 * Load XML from input stream to DOM Document.
	 *
	 * @param xmlStream - XML stream.
	 * @return new loaded DOM Document.
	 * @throws CoreException if something goes wrong.
	 */
	private static Document loadXml(InputStream xmlStream) throws CoreException {
		try {
			DocumentBuilder builder = XmlProcessorFactoryCdt.createDocumentBuilderWithErrorOnDOCTYPE();
			return builder.parse(xmlStream);
		} catch (Exception e) {
			throw new CoreException(CCorePlugin.createStatus(Messages.XmlUtil_InternalErrorLoading, e));
		}
	}

	/**
	 * Load XML from file to DOM Document.
	 *
	 * @param uriLocation - location of XML file.
	 * @return new loaded XML Document or {@code null} if file does not exist.
	 * @throws CoreException if something goes wrong.
	 */
	public static Document loadXml(URI uriLocation) throws CoreException {
		java.io.File xmlFile = new java.io.File(uriLocation);
		if (!xmlFile.exists()) {
			return null;
		}

		try {
			InputStream xmlStream = new FileInputStream(xmlFile);
			try {
				return loadXml(xmlStream);
			} finally {
				xmlStream.close();
			}
		} catch (Exception e) {
			throw new CoreException(CCorePlugin.createStatus(Messages.XmlUtil_InternalErrorLoading, e));
		}
	}

	/**
	 * Load XML from file to DOM Document.
	 *
	 * @param xmlFile - XML file
	 * @return new loaded XML Document.
	 * @throws CoreException if something goes wrong.
	 */
	public static Document loadXml(IFile xmlFile) throws CoreException {
		try {
			InputStream xmlStream = xmlFile.getContents();
			try {
				return loadXml(xmlStream);
			} finally {
				xmlStream.close();
			}
		} catch (Exception e) {
			throw new CoreException(CCorePlugin.createStatus(Messages.XmlUtil_InternalErrorLoading, e));
		}
	}

	/**
	 * Serialize XML Document into a file.<br/>
	 * Note: clients should synchronize access to this method.
	 *
	 * @param doc - DOM Document to serialize.
	 * @param uriLocation - URI of the file.
	 *
	 * @throws IOException in case of problems with file I/O
	 * @throws TransformerException in case of problems with XML output
	 */
	public static void serializeXml(Document doc, URI uriLocation)
			throws IOException, TransformerException, CoreException {
		serializeXmlInternal(doc, uriLocation, null);
	}

	/**
	 * Serialize XML Document into a file.<br/>
	 * Note: clients should synchronize access to this method.
	 *
	 * @param doc - DOM Document to serialize.
	 * @param uriLocation - URI of the file.
	 * @param lineSeparator - line separator.
	 * Note: This will serialize in pretty format
	 *
	 * @throws IOException in case of problems with file I/O
	 * @throws TransformerException in case of problems with XML output
	 */
	public static void serializeXml(Document doc, URI uriLocation, String lineSeparator)
			throws IOException, TransformerException, CoreException {
		serializeXmlInternal(doc, uriLocation, lineSeparator);
	}

	private static void serializeXmlInternal(Document doc, URI uriLocation, String lineSeparator)
			throws IOException, TransformerException, CoreException {
		java.io.File storeFile = new java.io.File(uriLocation);
		if (!storeFile.exists()) {
			storeFile.createNewFile();
		}

		String utfString = lineSeparator != null ? toString(doc, lineSeparator) : toString(doc);

		FileOutputStream output = getFileOutputStreamWorkaround(storeFile);
		output.write(utfString.getBytes(ENCODING_UTF_8));

		output.close();
		ResourcesUtil.refreshWorkspaceFiles(uriLocation);
	}

	/**
	 * Workaround for Java problem on Windows with releasing buffers for memory-mapped files.
	 *
	 * @see "http://stackoverflow.com/questions/3602783/file-access-synchronized-on-java-object"
	 * @see "http://bugs.sun.com/bugdatabase/view_bug.do?bug_id=6354433"
	 * @see "http://bugs.sun.com/bugdatabase/view_bug.do?bug_id=4715154"
	 * @see "http://bugs.sun.com/bugdatabase/view_bug.do?bug_id=4469299"
	 */
	private static FileOutputStream getFileOutputStreamWorkaround(java.io.File storeFile) throws FileNotFoundException {
		final int maxCount = 10;
		for (int i = 0; i <= maxCount; i++) {
			try {
				// there is no sleep on first round
				Thread.sleep(10 * i);
			} catch (InterruptedException e) {
				// restore interrupted status
				Thread.currentThread().interrupt();
			}
			try {
				return new FileOutputStream(storeFile);
			} catch (FileNotFoundException e) {
				// only apply workaround for the very specific exception
				if (i >= maxCount || !e.getMessage().contains(
						"The requested operation cannot be performed on a file with a user-mapped section open")) { //$NON-NLS-1$
					throw e;
				}
				//				CCorePlugin.log(new Status(IStatus.INFO, CCorePlugin.PLUGIN_ID, "Workaround for concurrent access to memory-mapped files applied, attempt " + (i + 1), e)); //$NON-NLS-1$
			}
		}

		// will never get here
		return null;
	}

	/**
	 * Serialize XML Document into a byte array.
	 * @param doc - DOM Document to serialize.
	 * @return XML as a byte array.
	 * @throws CoreException if something goes wrong.
	 */
	private static byte[] toByteArray(Document doc) throws CoreException {
		try {
			ByteArrayOutputStream stream = new ByteArrayOutputStream();
			Transformer transformer = XmlProcessorFactoryCdt.createTransformerFactoryWithErrorOnDOCTYPE()
					.newTransformer();
			transformer.setOutputProperty(OutputKeys.METHOD, "xml"); //$NON-NLS-1$
			transformer.setOutputProperty(OutputKeys.ENCODING, ENCODING_UTF_8);
			// Indentation is done with XmlUtil.prettyFormat(doc).
			transformer.setOutputProperty(OutputKeys.INDENT, "no"); //$NON-NLS-1$
			DOMSource source = new DOMSource(doc);
			StreamResult result = new StreamResult(stream);
			transformer.transform(source, result);

			return stream.toByteArray();
		} catch (Exception e) {
			throw new CoreException(CCorePlugin.createStatus(Messages.XmlUtil_InternalErrorSerializing, e));
		}
	}

	/**
	 * <b>Do not use outside of CDT.</b> This method is a workaround for {@link javax.xml.transform.Transformer}
	 * not being able to specify the line separator. This method replaces a string generated by
	 * {@link javax.xml.transform.Transformer} which contains the system line.separator with the line separators
	 * from an existing file or the preferences if it's a new file.
	 *
	 * @param string - the string to be replaced
	 * @param lineSeparator - line separator to be used in the string
	 *
	 * @noreference This method is not intended to be referenced by clients.
	 *    This is an internal method which ideally should be made private.
	 */
	public static String replaceLineSeparatorInternal(String string, String lineSeparator) {
		string = string.replace(LINE_SEPARATOR, lineSeparator);
		return string;
	}

	/**
	 * <b>Do not use outside of CDT.</b>
	 *
	 * This method is used to workaround changing implementations of {@link javax.xml.transform.Transformer}
	 * to maintain the same file content for CDT users. See {@link #prettyFormat(Document)} and Bug 565628
	 *
	 * This method inserts a newline between the <?... xml ... ?> and first tag in the document
	 *
	 * @noreference This method is not intended to be referenced by clients.
	 *    This is an internal method which ideally should be made private.
	 */
	public static String insertNewlineAfterXMLVersionTag(String string, String lineSeparator) {
		if (string == null) {
			return null;
		}

		int indexOf = string.indexOf("?><"); //$NON-NLS-1$
		if (indexOf < 0 || string.length() < indexOf + 2) {
			return string;
		}

		return string.substring(0, indexOf + 2) + lineSeparator + string.substring(indexOf + 2);
	}

	/**
	 * Serialize XML Document into a workspace file.<br/>
	 * Note: clients should synchronize access to this method.
	 *
	 * @param doc - DOM Document to serialize.
	 * @param file - file where to write the XML.
	 * @throws CoreException if something goes wrong.
	 */
	public static void serializeXml(Document doc, IFile file) throws CoreException {
		try {
			String lineSeparator = Util.getLineSeparator(file);
			String utfString = toString(doc, lineSeparator);
			byte[] newContents = utfString.getBytes(ENCODING_UTF_8);
			InputStream input = new ByteArrayInputStream(newContents);

			if (file.exists()) {
				byte[] existingContents = readFile(file);
				if (!Arrays.equals(existingContents, newContents)) {
					file.setContents(input, IResource.FORCE, null);
				}
			} else {
				file.create(input, IResource.FORCE, null);
			}
		} catch (UnsupportedEncodingException e) {
		}
	}

	/**
	 * Read whole file, returning null on any error.
	 */
	private static byte[] readFile(IFile file) {
		try (InputStream is = file.getContents(true)) {
			ByteArrayOutputStream buffer = new ByteArrayOutputStream();

			int nRead;
			byte[] data = new byte[4096];

			while ((nRead = is.read(data)) != -1) {
				buffer.write(data, 0, nRead);
			}

			buffer.flush();

			return buffer.toByteArray();
		} catch (IOException | CoreException e) {
			return null;
		}
	}

	/**
	 * Serialize XML Document into a string.
	 * Note: This will return a non-pretty formatted string
	 *
	 * @param doc - DOM Document to serialize.
	 * @return XML as a String.
	 * @throws CoreException if something goes wrong.
	 */
	public static String toString(Document doc) throws CoreException {
		try {
			return new String(toByteArray(doc), ENCODING_UTF_8);
		} catch (UnsupportedEncodingException e) {
			throw new CoreException(CCorePlugin.createStatus(Messages.XmlUtil_InternalErrorSerializing, e));
		}
	}

	/**
	 * Serialize XML Document into a pretty formatted string.
	 * Note: This will return a pretty formatted string
	 *
	 * @param doc - DOM Document to serialize.
	 * @param lineSeparator - line separator
	 * @return XML as a pretty formatted String.
	 * @throws CoreException if something goes wrong.
	 */
	public static String toString(Document doc, String lineSeparator) throws CoreException {
		XmlUtil.prettyFormat(doc);

		String utfString = toString(doc);
		utfString = XmlUtil.replaceLineSeparatorInternal(utfString, lineSeparator);
		utfString = XmlUtil.insertNewlineAfterXMLVersionTag(utfString, lineSeparator);

		return utfString;
	}
}
