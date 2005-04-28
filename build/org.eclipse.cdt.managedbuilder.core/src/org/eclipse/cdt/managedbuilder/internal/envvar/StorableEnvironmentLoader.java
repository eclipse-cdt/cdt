/**********************************************************************
 * Copyright (c) 2005 Intel Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors: 
 * Intel Corporation - Initial API and implementation
 **********************************************************************/
package org.eclipse.cdt.managedbuilder.internal.envvar;

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

import org.eclipse.cdt.managedbuilder.core.ManagedBuilderCorePlugin;
import org.eclipse.cdt.managedbuilder.internal.core.ManagedMakeMessages;
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
	protected interface ISerializeInfo{
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
	protected StorableEnvironment loadEnvironment(Object context){
		ISerializeInfo serializeInfo = getSerializeInfo(context);
		if(serializeInfo == null)
			return null;
		
		InputStream stream = loadInputStream(serializeInfo.getNode(),serializeInfo.getPrefName());
		if(stream == null)
			return new StorableEnvironment();
		return loadEnvironmentFromStream(stream);
	}
	
	/*
	 * stores the given environment 
	 */
	protected void storeEnvironment(StorableEnvironment env, Object context, boolean force) throws CoreException{
		if(!env.isDirty() && !force)
			return;
		
		ISerializeInfo serializeInfo = getSerializeInfo(context);
		if(serializeInfo == null)
			return;
		
		ByteArrayOutputStream stream = storeEnvironmentToStream(env);
		if(stream == null)
			return;
		storeOutputStream(stream,serializeInfo.getNode(),serializeInfo.getPrefName());
	}
	
	private StorableEnvironment loadEnvironmentFromStream(InputStream stream){
		try{
			DocumentBuilder parser = DocumentBuilderFactory.newInstance().newDocumentBuilder();
			InputSource inputSource = new InputSource(stream);
			Document document = parser.parse(inputSource);
			Element rootElement = document.getDocumentElement();
			
			if(!StorableEnvironment.ENVIRONMENT_ELEMENT_NAME.equals(rootElement.getNodeName()))
				return null;
			
			return new StorableEnvironment(rootElement);
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
			
			Element rootElement = document.createElement(StorableEnvironment.ENVIRONMENT_ELEMENT_NAME);
			document.appendChild(rootElement);
			env.serialize(document,rootElement);
			
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
					ManagedBuilderCorePlugin.getUniqueIdentifier(),
					-1,
					e.getMessage(),
					e));
		}
		catch(TransformerConfigurationException e){
			throw new CoreException(new Status(IStatus.ERROR, 
					ManagedBuilderCorePlugin.getUniqueIdentifier(),
					-1,
					e.getMessage(),
					e));
		}
		catch(TransformerException e){
			throw new CoreException(new Status(IStatus.ERROR, 
					ManagedBuilderCorePlugin.getUniqueIdentifier(),
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
	
	private void storeOutputStream(ByteArrayOutputStream stream, Preferences node, String key) throws CoreException{
		if(stream == null || node == null || key == null)
			throw new CoreException(new Status(IStatus.ERROR, 
					ManagedBuilderCorePlugin.getUniqueIdentifier(),
					-1,
					ManagedMakeMessages.getResourceString("StorableEnvironmentLoader.storeOutputStream.wrong.arguments"), //$NON-NLS-1$
					null));
		byte[] bytes= stream.toByteArray();
		
		String val = null;
		try {
			val= new String(bytes, "UTF-8"); //$NON-NLS-1$
		} catch (UnsupportedEncodingException e) {
			val= new String(bytes);
		}
		
		node.put(key,val);
		
		try{
			node.flush();
		}
		catch(BackingStoreException e){
			throw new CoreException(new Status(IStatus.ERROR, 
					ManagedBuilderCorePlugin.getUniqueIdentifier(),
					-1,
					e.getMessage(),
					e));
		}
	}
}
