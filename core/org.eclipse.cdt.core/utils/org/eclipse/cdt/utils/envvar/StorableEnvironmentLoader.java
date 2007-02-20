/*******************************************************************************
 * Copyright (c) 2005, 2007 Intel Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Intel Corporation - Initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.utils.envvar;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.eclipse.cdt.core.CCorePlugin;
import org.eclipse.cdt.core.settings.model.util.XmlStorageElement;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.osgi.service.prefs.BackingStoreException;
import org.osgi.service.prefs.Preferences;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

/**
 * This class implements the common functionality that allows
 * storing and loading environment variable settings from eclipse properties
 * 
 * @since 3.0
 *
 */
public abstract class StorableEnvironmentLoader {

	/**
	 * this interface represents the preference node and the preference name
	 * that are used for holding the environment data 
	 * 
	 */
	public interface ISerializeInfo{
		Preferences getNode();
		
		String getPrefName();
	}

	/**
	 * this method should return the ISerializeInfo representing the information
	 * of where the variable should be stored and loaded
	 * If the given context is not supported this method should return null
	 * 
	 * @param context
	 * @return
	 */
	protected abstract ISerializeInfo getSerializeInfo(Object context);

	/*
	 * loads the stored environment for the given context 
	 */
	protected StorableEnvironment loadEnvironment(Object context, boolean readOnly){
		ISerializeInfo serializeInfo = getSerializeInfo(context);
		if(serializeInfo == null)
			return null;
		
		InputStream stream = loadInputStream(serializeInfo.getNode(),serializeInfo.getPrefName());
		if(stream == null)
			return new StorableEnvironment(readOnly);
		return loadEnvironmentFromStream(stream, readOnly);
	}
	
	/*
	 * stores the given environment 
	 */
	protected void storeEnvironment(StorableEnvironment env, Object context, boolean force, boolean flush) throws CoreException{
		if(!env.isDirty() && !force)
			return;
		
		ISerializeInfo serializeInfo = getSerializeInfo(context);
		if(serializeInfo == null)
			return;
		
		ByteArrayOutputStream stream = storeEnvironmentToStream(env);
		if(stream == null)
			return;
		storeOutputStream(stream,serializeInfo.getNode(),serializeInfo.getPrefName(), flush);
		
		env.setDirty(false);
	}
	
	private StorableEnvironment loadEnvironmentFromStream(InputStream stream, boolean readOnly){
		try{
			DocumentBuilder parser = DocumentBuilderFactory.newInstance().newDocumentBuilder();
			InputSource inputSource = new InputSource(stream);
			Document document = parser.parse(inputSource);
			Element el = document.getDocumentElement();
			XmlStorageElement rootElement = new XmlStorageElement(el);
			
			if(!StorableEnvironment.ENVIRONMENT_ELEMENT_NAME.equals(rootElement.getName()))
				return null;
			
			return new StorableEnvironment(rootElement, readOnly);
		}
		catch(ParserConfigurationException e){
			
		}
		catch(SAXException e){
			
		}
		catch(IOException e){
			
		}
		
		return null;
	}
	
	private ByteArrayOutputStream storeEnvironmentToStream(StorableEnvironment env) throws CoreException{
		try{
			DocumentBuilderFactory factory= DocumentBuilderFactory.newInstance();
			DocumentBuilder builder= factory.newDocumentBuilder();		
			Document document= builder.newDocument();
			
			Element el = document.createElement(StorableEnvironment.ENVIRONMENT_ELEMENT_NAME);
			document.appendChild(el);
			XmlStorageElement rootElement = new XmlStorageElement(el);
			env.serialize(rootElement);
			
			Transformer transformer=TransformerFactory.newInstance().newTransformer();
			transformer.setOutputProperty(OutputKeys.METHOD, "xml"); //$NON-NLS-1$
			transformer.setOutputProperty(OutputKeys.ENCODING, "UTF-8"); //$NON-NLS-1$
			transformer.setOutputProperty(OutputKeys.INDENT, "yes"); //$NON-NLS-1$
			DOMSource source = new DOMSource(document);
			
			ByteArrayOutputStream stream = new ByteArrayOutputStream();
			StreamResult result = new StreamResult(stream);
	
			transformer.transform(source, result);
			return stream;
		}
		catch(ParserConfigurationException e){
			throw new CoreException(new Status(IStatus.ERROR, 
					CCorePlugin.PLUGIN_ID,
					-1,
					e.getMessage(),
					e));
		}
		catch(TransformerConfigurationException e){
			throw new CoreException(new Status(IStatus.ERROR, 
					CCorePlugin.PLUGIN_ID,
					-1,
					e.getMessage(),
					e));
		}
		catch(TransformerException e){
			throw new CoreException(new Status(IStatus.ERROR, 
					CCorePlugin.PLUGIN_ID,
					-1,
					e.getMessage(),
					e));
		}
	}
	
	private InputStream loadInputStream(Preferences node, String key){
		if(node == null || key == null)
			return null;
		
		String value = node.get(key,null);
		if(value == null || value.length() == 0)
			return null;
		
		byte[] bytes;
		try {
			bytes = value.getBytes("UTF-8"); //$NON-NLS-1$
		} catch (UnsupportedEncodingException e) {
			bytes = value.getBytes();
		}
		
		return new ByteArrayInputStream(bytes);
	}
	
	private void storeOutputStream(ByteArrayOutputStream stream, Preferences node, String key, boolean flush) throws CoreException{
		if(stream == null || node == null || key == null)
			throw new CoreException(new Status(IStatus.ERROR, 
					CCorePlugin.PLUGIN_ID,
					-1,
					//TODO:ManagedMakeMessages.getResourceString(
					"StorableEnvironmentLoader.storeOutputStream.wrong.arguments"
					//)
					, //$NON-NLS-1$
					null));
		byte[] bytes= stream.toByteArray();
		
		String val = null;
		try {
			val= new String(bytes, "UTF-8"); //$NON-NLS-1$
		} catch (UnsupportedEncodingException e) {
			val= new String(bytes);
		}
		
		node.put(key,val);
		
		if(flush){
			try{
				node.flush();
			}
			catch(BackingStoreException e){
				throw new CoreException(new Status(IStatus.ERROR, 
						CCorePlugin.PLUGIN_ID,
						-1,
						e.getMessage(),
						e));
			}
		}
	}
}
