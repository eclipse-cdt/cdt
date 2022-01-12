/*******************************************************************************
 * Copyright (c) 2005, 2009 Intel Corporation and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *    Intel Corporation - Initial API and implementation
 *    James Blackburn (Broadcom Corp.)
 *******************************************************************************/
package org.eclipse.cdt.utils.envvar;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
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
import org.eclipse.cdt.core.settings.model.ICStorageElement;
import org.eclipse.cdt.internal.core.settings.model.xml.XmlStorageElement;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.preferences.IEclipsePreferences;
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
 */
public abstract class StorableEnvironmentLoader {

	/**
	 * this interface represents the preference node and the preference name
	 * that are used for holding the environment data
	 * @noextend This interface is not intended to be extended by clients.
	 * @noimplement This interface is not intended to be implemented by clients.
	 */
	public interface ISerializeInfo {

		/**
		 * {@link IEclipsePreferences} root node in the Preference store
		 * @return the Preferences Node into which environment should be (de) serialized
		 */
		Preferences getNode();

		/**
		 * Name in the preference store
		 * @return the key in the preference node to use for loading preferences
		 */
		String getPrefName();
	}

	/**
	 * Creates the StorableEnvironment clone for a new configuration, say,
	 * based on an existing configuration
	 *
	 * @param context the configuration / workspace context the configuration is to be cloned for
	 * @param base the base environment to copy
	 * @return a StorableEnvironment clone of the configuration's environment
	 * @since 5.2
	 */
	public StorableEnvironment cloneEnvironmentWithContext(Object context, StorableEnvironment base,
			boolean isReadOnly) {
		PrefsStorableEnvironment env = new PrefsStorableEnvironment(base, getSerializeInfo(context), isReadOnly);
		return env;
	}

	/**
	 * this method should return the ISerializeInfo representing the information
	 * of where the variable should be stored and loaded
	 * If the given context is not supported this method should return null
	 */
	protected abstract ISerializeInfo getSerializeInfo(Object context);

	/**
	 * Loads the environment from the context's {@link ISerializeInfo}.
	 *
	 * NB the environment in the {@link ISerializeInfo} need not be available
	 * yet. The {@link ISerializeInfo} may be held by the {@link StorableEnvironment}
	 * to pick up any external changes in the environment.
	 *
	 * @param context
	 * @param readOnly
	 * @return StorableEnvironment
	 */
	protected StorableEnvironment loadEnvironment(Object context, boolean readOnly) {
		ISerializeInfo serializeInfo = getSerializeInfo(context);
		if (serializeInfo == null)
			return null;

		return new PrefsStorableEnvironment(serializeInfo, readOnly);
	}

	/*
	 * stores the given environment
	 */
	protected void storeEnvironment(StorableEnvironment env, Object context, boolean force, boolean flush)
			throws CoreException {
		if (!env.isDirty() && !force)
			return;

		ISerializeInfo serializeInfo = getSerializeInfo(context);
		if (serializeInfo == null)
			return;

		if (env instanceof PrefsStorableEnvironment) {
			((PrefsStorableEnvironment) env).serialize();
		} else {
			// Backwards compatibility
			ByteArrayOutputStream stream = storeEnvironmentToStream(env);
			if (stream == null)
				return;
			storeOutputStream(stream, serializeInfo.getNode(), serializeInfo.getPrefName(), flush);

			env.setDirty(false);
		}
	}

	/**
	 * @param env String representing the encoded environment
	 * @return ICStorageElement tree from the passed in InputStream
	 *                          or null on failure
	 */
	static ICStorageElement environmentStorageFromString(String env) {
		if (env == null)
			return null;
		try {
			DocumentBuilder parser = DocumentBuilderFactory.newInstance().newDocumentBuilder();
			InputSource inputSource = new InputSource(new ByteArrayInputStream(env.getBytes()));
			Document document = parser.parse(inputSource);
			Element el = document.getDocumentElement();
			XmlStorageElement rootElement = new XmlStorageElement(el);

			if (!StorableEnvironment.ENVIRONMENT_ELEMENT_NAME.equals(rootElement.getName()))
				return null;

			return rootElement;
		} catch (ParserConfigurationException e) {
			CCorePlugin.log(e);
		} catch (SAXException e) {
			CCorePlugin.log(e);
		} catch (IOException e) {
			CCorePlugin.log(e);
		}
		return null;
	}

	private ByteArrayOutputStream storeEnvironmentToStream(StorableEnvironment env) throws CoreException {
		try {
			DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
			DocumentBuilder builder = factory.newDocumentBuilder();
			Document document = builder.newDocument();

			Element el = document.createElement(StorableEnvironment.ENVIRONMENT_ELEMENT_NAME);
			document.appendChild(el);
			XmlStorageElement rootElement = new XmlStorageElement(el);
			env.serialize(rootElement);

			Transformer transformer = TransformerFactory.newInstance().newTransformer();
			transformer.setOutputProperty(OutputKeys.METHOD, "xml"); //$NON-NLS-1$
			transformer.setOutputProperty(OutputKeys.ENCODING, "UTF-8"); //$NON-NLS-1$
			transformer.setOutputProperty(OutputKeys.INDENT, "yes"); //$NON-NLS-1$
			DOMSource source = new DOMSource(document);

			ByteArrayOutputStream stream = new ByteArrayOutputStream();
			StreamResult result = new StreamResult(stream);

			transformer.transform(source, result);
			return stream;
		} catch (ParserConfigurationException e) {
			throw new CoreException(new Status(IStatus.ERROR, CCorePlugin.PLUGIN_ID, -1, e.getMessage(), e));
		} catch (TransformerConfigurationException e) {
			throw new CoreException(new Status(IStatus.ERROR, CCorePlugin.PLUGIN_ID, -1, e.getMessage(), e));
		} catch (TransformerException e) {
			throw new CoreException(new Status(IStatus.ERROR, CCorePlugin.PLUGIN_ID, -1, e.getMessage(), e));
		}
	}

	/**
	 * Preferences can be encoded as a single long ICStorageElement String
	 * @return String value stored in the node or null if no such value exists.
	 */
	static String loadPreferenceNode(ISerializeInfo serializeInfo) {
		if (serializeInfo == null)
			return null;
		return loadPreferenceNode(serializeInfo.getNode(), serializeInfo.getPrefName());
	}

	/**
	 * Returns the value stored in a Preferences node
	 * @param node Preferences node
	 * @param key
	 * @return String value stored in the node or null if no such value exists.
	 */
	static String loadPreferenceNode(Preferences node, String key) {
		if (node == null || key == null)
			return null;

		String value = node.get(key, null);
		if (value == null || value.length() == 0)
			return null;

		return value;
	}

	private void storeOutputStream(ByteArrayOutputStream stream, Preferences node, String key, boolean flush)
			throws CoreException {
		if (stream == null || node == null || key == null)
			throw new CoreException(new Status(IStatus.ERROR, CCorePlugin.PLUGIN_ID, -1,
					//TODO:ManagedMakeMessages.getResourceString(
					"StorableEnvironmentLoader.storeOutputStream.wrong.arguments" //$NON-NLS-1$
					//)
					, null));
		byte[] bytes = stream.toByteArray();

		String val = null;
		try {
			val = new String(bytes, "UTF-8"); //$NON-NLS-1$
		} catch (UnsupportedEncodingException e) {
			val = new String(bytes);
		}

		node.put(key, val);

		if (flush) {
			try {
				node.flush();
			} catch (BackingStoreException e) {
				throw new CoreException(new Status(IStatus.ERROR, CCorePlugin.PLUGIN_ID, -1, e.getMessage(), e));
			}
		}
	}
}
