/*******************************************************************************
 * Copyright (c) 2009, 2014 Andrew Gvozdev and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Andrew Gvozdev - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.core.language.settings.providers;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.eclipse.cdt.core.CCorePlugin;
import org.eclipse.cdt.core.CommandLauncherManager;
import org.eclipse.cdt.core.settings.model.ICConfigurationDescription;
import org.eclipse.cdt.core.settings.model.ICLanguageSettingEntry;
import org.eclipse.cdt.internal.core.XmlUtil;
import org.eclipse.cdt.internal.core.language.settings.providers.LanguageSettingsProvidersSerializer;
import org.eclipse.cdt.internal.core.language.settings.providers.LanguageSettingsSerializableStorage;
import org.eclipse.cdt.internal.core.settings.model.CConfigurationSpecSettings;
import org.eclipse.cdt.internal.core.settings.model.IInternalCCfgInfo;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * This class is the base class for language settings providers able to serialize
 * into XML storage.
 * Although this class has setter methods, by design its instances are not editable in UI
 * nor instances can be assigned to a configuration (cannot be global or non-shared).
 * Implement {@link ILanguageSettingsEditableProvider} interface for that. There is a generic
 * implementation of this interface available to be used, see {@link LanguageSettingsGenericProvider}.
 *
 * For more on the suggested way of extending this class see the description of
 * {@link ILanguageSettingsProvider}.
 *
 * @since 5.4
 */
public class LanguageSettingsSerializableProvider extends LanguageSettingsBaseProvider
		implements ILanguageSettingsBroadcastingProvider {
	protected static final String ATTR_ID = LanguageSettingsProvidersSerializer.ATTR_ID;
	protected static final String ATTR_NAME = LanguageSettingsProvidersSerializer.ATTR_NAME;
	protected static final String ATTR_CLASS = LanguageSettingsProvidersSerializer.ATTR_CLASS;
	protected static final String ELEM_PROVIDER = LanguageSettingsProvidersSerializer.ELEM_PROVIDER;
	protected static final String ELEM_LANGUAGE_SCOPE = LanguageSettingsProvidersSerializer.ELEM_LANGUAGE_SCOPE;

	private LanguageSettingsSerializableStorage fStorage = new LanguageSettingsSerializableStorage();

	/**
	 * Default constructor. This constructor has to be always followed with setting id and name of the provider.
	 * This constructor is necessary to instantiate the class via the extension point in plugin.xml.
	 */
	public LanguageSettingsSerializableProvider() {
		super();
	}

	/**
	 * Constructor.
	 *
	 * @param id - id of the provider.
	 * @param name - name of the provider. Note that this name shows up in UI.
	 */
	public LanguageSettingsSerializableProvider(String id, String name) {
		super(id, name);
	}

	/**
	 * Constructor which allows to instantiate provider defined via XML markup.
	 *
	 * @param elementProvider
	 */
	public LanguageSettingsSerializableProvider(Element elementProvider) {
		super();
		load(elementProvider);
	}

	@Override
	public void configureProvider(String id, String name, List<String> languages, List<ICLanguageSettingEntry> entries,
			Map<String, String> properties) {
		// do not pass entries to super, keep them in local storage
		super.configureProvider(id, name, languages, null, properties);

		fStorage.clear();

		if (entries != null) {
			// note that these entries are intended to be retrieved by LanguageSettingsManager.getSettingEntriesUpResourceTree()
			// when the whole resource hierarchy has been traversed up
			setSettingEntries(null, null, null, entries);
		}
	}

	/**
	 * @return {@code true} if the provider does not keep any settings yet or {@code false} if there are some.
	 */
	public boolean isEmpty() {
		return fStorage.isEmpty();
	}

	/**
	 * Sets the language scope of the provider.
	 *
	 * @param languages - the list of languages this provider provides for.
	 *    If {@code null}, the provider provides for any language.
	 *
	 * @see #getLanguageScope()
	 */
	public void setLanguageScope(List<String> languages) {
		if (languages == null) {
			this.languageScope = null;
		} else {
			this.languageScope = new ArrayList<>(languages);
		}
	}

	/**
	 * Clears all the entries for all configurations, all resources and all languages.
	 */
	public void clear() {
		fStorage.clear();
	}

	/**
	 * Sets language settings entries for the provider.
	 * Note that the entries are not persisted at that point. Use this method to set
	 * the entries for all resources one by one and after all done persist in one shot
	 * using {@link #serializeLanguageSettings(ICConfigurationDescription)}.
	 * See for example {@code AbstractBuildCommandParser} and {@code AbstractBuiltinSpecsDetector}
	 * in build plugins.
	 *
	 * @param cfgDescription - configuration description.
	 * @param rc - resource such as file or folder. If {@code null} the entries are
	 *    considered to be being defined as default entries for resources.
	 * @param languageId - language id. If {@code null}, then entries are considered
	 *    to be defined for the language scope. See {@link #getLanguageScope()}
	 * @param entries - language settings entries to set.
	 */
	public void setSettingEntries(ICConfigurationDescription cfgDescription, IResource rc, String languageId,
			List<? extends ICLanguageSettingEntry> entries) {
		String rcProjectPath = rc != null ? rc.getProjectRelativePath().toString() : null;
		fStorage.setSettingEntries(rcProjectPath, languageId, entries);
		if (cfgDescription != null) {
			CommandLauncherManager.getInstance()
					.setLanguageSettingEntries(cfgDescription.getProjectDescription().getProject(), entries);
		}
	}

	/**
	 * {@inheritDoc}
	 * <br>
	 * Note that this list is <b>unmodifiable</b>. To modify the list copy it, change and use
	 * {@link #setSettingEntries(ICConfigurationDescription, IResource, String, List)}.
	 * <br><br>
	 * Note also that <b>you can compare these lists with simple equality operator ==</b>,
	 * as the lists themselves are backed by WeakHashSet<List<ICLanguageSettingEntry>> where
	 * identical copies (deep comparison is used) are replaced with the same one instance.
	 */
	@Override
	public List<ICLanguageSettingEntry> getSettingEntries(ICConfigurationDescription cfgDescription, IResource rc,
			String languageId) {
		String rcProjectPath = rc != null ? rc.getProjectRelativePath().toString() : null;
		List<ICLanguageSettingEntry> entries = fStorage.getSettingEntries(rcProjectPath, languageId);
		if (entries == null) {
			if (languageId != null && (languageScope == null || languageScope.contains(languageId))) {
				entries = fStorage.getSettingEntries(rcProjectPath, null);
			}
		}

		if (cfgDescription != null) {
			entries = CommandLauncherManager.getInstance()
					.getLanguageSettingEntries(cfgDescription.getProjectDescription().getProject(), entries);
		}

		return entries;
	}

	/**
	 * Serialize the provider under parent XML element.
	 * This is convenience method not intended to be overridden on purpose.
	 * Override {@link #serializeAttributes(Element)} or
	 * {@link #serializeEntries(Element)} instead.
	 *
	 * @param parentElement - element where to serialize.
	 * @return - newly created "provider" element. That element will already be
	 *    attached to the parent element.
	 */
	final public Element serialize(Element parentElement) {
		/*
		<provider id="provider.id" ...>
			<language-scope id="lang.id"/>
			<language id="lang.id">
				<resource project-relative-path="/">
					<entry flags="" kind="includePath" name="path"/>
				</resource>
			</language>
		</provider>
		 */
		Element elementProvider = serializeAttributes(parentElement);
		serializeEntries(elementProvider);
		return elementProvider;
	}

	/**
	 * Serialize the provider attributes under parent XML element. That is
	 * equivalent to serializing everything (including language scope) except entries.
	 *
	 * @param parentElement - element where to serialize.
	 * @return - newly created "provider" element. That element will already be
	 *    attached to the parent element.
	 */
	public Element serializeAttributes(Element parentElement) {
		// Keeps pairs: key, value. See JavaDoc XmlUtil.appendElement(Node, String, String[]).
		List<String> attributes = new ArrayList<>();

		attributes.add(ATTR_ID);
		attributes.add(getId());
		attributes.add(ATTR_NAME);
		attributes.add(getName());
		attributes.add(ATTR_CLASS);
		attributes.add(getClass().getCanonicalName());
		for (Entry<String, String> entry : properties.entrySet()) {
			attributes.add(entry.getKey());
			attributes.add(entry.getValue());
		}

		Element elementProvider = XmlUtil.appendElement(parentElement, ELEM_PROVIDER,
				attributes.toArray(new String[0]));

		if (languageScope != null) {
			for (String langId : languageScope) {
				XmlUtil.appendElement(elementProvider, ELEM_LANGUAGE_SCOPE, new String[] { ATTR_ID, langId });
			}
		}
		return elementProvider;
	}

	/**
	 * Serialize the provider entries under parent XML element.
	 * @param elementProvider - element where to serialize the entries.
	 */
	public void serializeEntries(Element elementProvider) {
		fStorage.serializeEntries(elementProvider);
	}

	/**
	 * Convenience method to persist language settings entries for the project or
	 * workspace as often-used operation.
	 * Note that configuration description is passed as an argument but the
	 * current implementation saves all configurations.
	 *
	 * @param cfgDescription - configuration description.
	 *    If not {@code null}, all providers of the project are serialized.
	 *    If {@code null}, global workspace providers are serialized.
	 *
	 * @return - status of operation.
	 */
	public IStatus serializeLanguageSettings(ICConfigurationDescription cfgDescription) {
		IStatus status = Status.OK_STATUS;
		try {
			if (cfgDescription != null) {
				LanguageSettingsManager.serializeLanguageSettings(cfgDescription.getProjectDescription());
			} else {
				LanguageSettingsManager.serializeLanguageSettingsWorkspace();
			}
		} catch (CoreException e) {
			status = new Status(IStatus.ERROR, CCorePlugin.PLUGIN_ID, IStatus.ERROR,
					"Error serializing language settings", e); //$NON-NLS-1$
			CCorePlugin.log(status);
		}
		return status;
	}

	/**
	 * Convenience method to persist language settings entries in background for the project or
	 * workspace as often-used operation.
	 * Note that configuration description is passed as an argument but the
	 * current implementation saves all configurations.
	 *
	 * @param cfgDescription - configuration description.
	 *    If not {@code null}, all providers of the project are serialized.
	 *    If {@code null}, global workspace providers are serialized.
	 */
	public void serializeLanguageSettingsInBackground(ICConfigurationDescription cfgDescription) {
		if (cfgDescription != null) {
			if (isLanguageSettingsProviderStoreChanged(cfgDescription)) {
				LanguageSettingsManager.serializeLanguageSettingsInBackground(cfgDescription.getProjectDescription());
			}
		} else {
			LanguageSettingsManager.serializeLanguageSettingsWorkspaceInBackground();
		}
	}

	/**
	 * Compare provider store with cached persistent store used to calculate delta.
	 */
	private boolean isLanguageSettingsProviderStoreChanged(ICConfigurationDescription cfgDescription) {
		if (cfgDescription instanceof IInternalCCfgInfo) {
			try {
				CConfigurationSpecSettings ss = ((IInternalCCfgInfo) cfgDescription).getSpecSettings();
				if (ss != null) {
					return ss.isLanguageSettingsProviderStoreChanged(this);
				}
			} catch (CoreException e) {
				CCorePlugin.log(e);
			}
		}

		// If something went wrong assuming it might have changed
		return true;
	}

	/**
	 * Load provider from XML provider element.
	 * This is convenience method not intended to be overridden on purpose.
	 * Override {@link #loadAttributes(Element)} or
	 * {@link #loadEntries(Element)} instead.
	 *
	 * @param providerNode - XML element "provider" to load provider from.
	 */
	final public void load(Element providerNode) {
		fStorage.clear();
		languageScope = null;

		// provider/configuration/language/resource/entry
		if (providerNode != null) {
			loadAttributes(providerNode);
			loadEntries(providerNode);
		}
	}

	/**
	 * Determine and set language scope from given XML node.
	 */
	private void loadLanguageScopeElement(Node parentNode) {
		if (languageScope == null) {
			languageScope = new ArrayList<>();
		}
		String id = XmlUtil.determineAttributeValue(parentNode, ATTR_ID);
		languageScope.add(id);

	}

	/**
	 * Load attributes from XML provider element.
	 * @param providerNode - XML element "provider" to load attributes from.
	 */
	public void loadAttributes(Element providerNode) {
		String providerId = XmlUtil.determineAttributeValue(providerNode, ATTR_ID);
		String providerName = XmlUtil.determineAttributeValue(providerNode, ATTR_NAME);

		properties.clear();
		NamedNodeMap attrs = providerNode.getAttributes();
		for (int i = 0; i < attrs.getLength(); i++) {
			Node attr = attrs.item(i);
			if (attr.getNodeType() == Node.ATTRIBUTE_NODE) {
				String key = attr.getNodeName();
				if (!key.equals(ATTR_ID) && !key.equals(ATTR_NAME) && !key.equals(ATTR_CLASS)) {
					String value = attr.getNodeValue();
					properties.put(key, value);
				}
			}
		}

		this.setId(providerId);
		this.setName(providerName);

		NodeList nodes = providerNode.getChildNodes();
		for (int i = 0; i < nodes.getLength(); i++) {
			Node elementNode = nodes.item(i);
			if (elementNode.getNodeType() != Node.ELEMENT_NODE)
				continue;

			if (ELEM_LANGUAGE_SCOPE.equals(elementNode.getNodeName())) {
				loadLanguageScopeElement(elementNode);
			}
		}

	}

	/**
	 * Load provider entries from XML provider element.
	 * @param providerNode - parent XML element "provider" where entries are defined.
	 */
	public void loadEntries(Element providerNode) {
		fStorage.loadEntries(providerNode);
	}

	/**
	 * Set a custom property of the provider.
	 * <br><br>
	 * A note of caution - do not use default values for a provider which are different
	 * from empty or {@code null} value. When providers are checked for equality
	 * (during internal operations in core) the missing properties are evaluated as
	 * empty ones.
	 *
	 * @see LanguageSettingsBaseProvider#getProperty(String)
	 *
	 * @param key - name of the property.
	 * @param value - value of the property.
	 *    If value is {@code null} the property is removed from the list.
	 */
	public void setProperty(String key, String value) {
		properties.put(key, value);
	}

	/**
	 * Set a custom boolean property of the provider.
	 * <br>Please, note that default value is always {@code false}.
	 * @see LanguageSettingsBaseProvider#getProperty(String)
	 *
	 * @param key - name of the property.
	 * @param value - {@code boolean} value of the property.
	 */
	public void setPropertyBool(String key, boolean value) {
		properties.put(key, Boolean.toString(value));
	}

	/**
	 * See {@link #cloneShallow()}. This method is extracted to avoid expressing
	 * {@link #clone()} via {@link #cloneShallow()}. Do not inline to "optimize"!
	 */
	private LanguageSettingsSerializableProvider cloneShallowInternal() throws CloneNotSupportedException {
		LanguageSettingsSerializableProvider clone = (LanguageSettingsSerializableProvider) super.clone();
		if (languageScope != null)
			clone.languageScope = new ArrayList<>(languageScope);
		clone.properties = new HashMap<>(properties);

		clone.fStorage = new LanguageSettingsSerializableStorage();
		return clone;
	}

	/**
	 * Shallow clone of the provider. "Shallow" is defined here as the exact copy except that
	 * the copy will have zero language settings entries.
	 *
	 * @return shallow copy of the provider.
	 * @throws CloneNotSupportedException in case {@link #clone()} throws the exception.
	 */
	protected LanguageSettingsSerializableProvider cloneShallow() throws CloneNotSupportedException {
		return cloneShallowInternal();
	}

	@Override
	protected LanguageSettingsSerializableProvider clone() throws CloneNotSupportedException {
		LanguageSettingsSerializableProvider clone = cloneShallowInternal();
		clone.fStorage = fStorage.clone();
		return clone;
	}

	@Override
	public LanguageSettingsStorage copyStorage() {
		try {
			return fStorage.clone();
		} catch (CloneNotSupportedException e) {
			CCorePlugin.log(e);
		}
		return null;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result + ((fStorage == null) ? 0 : fStorage.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (!super.equals(obj))
			return false;
		if (getClass() != obj.getClass())
			return false;
		LanguageSettingsSerializableProvider other = (LanguageSettingsSerializableProvider) obj;

		if (fStorage == null) {
			if (other.fStorage != null)
				return false;
		} else if (!fStorage.equals(other.fStorage))
			return false;
		return true;
	}
}
