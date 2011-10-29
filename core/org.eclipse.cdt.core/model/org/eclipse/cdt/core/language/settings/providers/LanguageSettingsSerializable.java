/*******************************************************************************
 * Copyright (c) 2009, 2011 Andrew Gvozdev and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Andrew Gvozdev - initial API and implementation
 *******************************************************************************/

package org.eclipse.cdt.core.language.settings.providers;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.eclipse.cdt.core.settings.model.ICConfigurationDescription;
import org.eclipse.cdt.core.settings.model.ICLanguageSettingEntry;
import org.eclipse.cdt.core.settings.model.ICSettingEntry;
import org.eclipse.cdt.core.settings.model.util.CDataUtil;
import org.eclipse.cdt.core.settings.model.util.LanguageSettingEntriesSerializer;
import org.eclipse.cdt.internal.core.XmlUtil;
import org.eclipse.cdt.internal.core.parser.util.WeakHashSet;
import org.eclipse.core.resources.IResource;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;


/**
 * This class is the base class for language settings providers able to serialize
 * into XML storage.
 *
 * TODO - more JavaDoc, info and hints about class hierarchy
 *
 */
public class LanguageSettingsSerializable extends LanguageSettingsBaseProvider {
	public static final String ELEM_PROVIDER = "provider"; //$NON-NLS-1$
	private static final String ATTR_ID = "id"; //$NON-NLS-1$

	private static final String ELEM_LANGUAGE_SCOPE = "language-scope"; //$NON-NLS-1$
	private static final String ELEM_LANGUAGE = "language"; //$NON-NLS-1$
	private static final String ELEM_RESOURCE = "resource"; //$NON-NLS-1$
	private static final String ATTR_PROJECT_PATH = "project-relative-path"; //$NON-NLS-1$

	private static final String ELEM_ENTRY = "entry"; //$NON-NLS-1$
	private static final String ATTR_KIND = "kind"; //$NON-NLS-1$
	private static final String ATTR_NAME = "name"; //$NON-NLS-1$
	private static final String ATTR_VALUE = "value"; //$NON-NLS-1$
	private static final String ATTR_CLASS = "class"; //$NON-NLS-1$
	private static final String ATTR_PARAMETER = "parameter"; //$NON-NLS-1$
	private static final String ELEM_FLAG = "flag"; //$NON-NLS-1$

	private static final String ATTR_STORE_ENTRIES = "store-entries"; //$NON-NLS-1$
	private static final String VALUE_WORKSPACE = "workspace"; //$NON-NLS-1$
	private static final String VALUE_PROJECT = "project"; //$NON-NLS-1$

	/**
	 * Pool of LSE lists implemented as WeakHashSet. That allows to gain memory savings
	 * at the expense of CPU time. WeakHashSet handles garbage collection when a list is not
	 * referenced anywhere else. See JavaDoc {@link java.lang.ref.WeakReference} about weak reference objects.
	 */
	private static WeakHashSet<List<ICLanguageSettingEntry>> listPool = new WeakHashSet<List<ICLanguageSettingEntry>>() {
		@Override
		public synchronized List<ICLanguageSettingEntry> add(List<ICLanguageSettingEntry> list) {
			return super.add(list);
		}

	};

	/** Tells if language settings entries are persisted with the project or in workspace area while serializing. */
	private boolean storeEntriesInProjectArea = false;

	/**
	 * Storage to keep settings entries. Note that it is not necessary to keep configuration in the maps
	 * as the configuration is always the one provider belongs to.
	 */
	private Map<String, // languageId
				Map<String, // resource project path
					List<ICLanguageSettingEntry>>> fStorage = new HashMap<String, Map<String, List<ICLanguageSettingEntry>>>();

	/**
	 * Default constructor. This constructor has to be always followed with setting id and name of the provider.
	 */
	public LanguageSettingsSerializable() {
		super();
	}

	/**
	 * Constructor.
	 *
	 * @param id - id of the provider.
	 * @param name - name of the provider. Note that this name may show up in UI.
	 */
	public LanguageSettingsSerializable(String id, String name) {
		super(id, name);
	}

	/**
	 * Constructor which allows to instantiate provider defined via XML markup.
	 *
	 * @param elementProvider
	 */
	public LanguageSettingsSerializable(Element elementProvider) {
		super();
		load(elementProvider);
	}

	@Override
	public void configureProvider(String id, String name, List<String> languages, List<ICLanguageSettingEntry> entries, String customParameter) {
		// do not pass entries to super, keep them in local storage
		super.configureProvider(id, name, languages, null, customParameter);

		fStorage.clear();

		if (entries!=null) {
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
	 * Set the language scope of the provider.
	 *
	 * @param languages - the list of languages this provider provides for.
	 *    If {@code null}, the provider provides for any language.
	 *
	 * @see #getLanguageScope()
	 */
	public void setLanguageScope(List <String> languages) {
		if (languages==null)
			this.languageScope = null;
		else
			this.languageScope = new ArrayList<String>(languages);
	}

	/**
	 * Set custom parameter for the provider.
	 * Subclasses are free to define how their behavior depends on custom parameter.
	 *
	 * @param customParameter
	 */
	public void setCustomParameter(String customParameter) {
		this.customParameter = customParameter;
	}

	/**
	 * Tells if language settings entries are persisted with the project (under .settings folder)
	 * or in workspace area. Persistence in the project area lets the entries migrate with the
	 * project.
	 *
	 * @return {@code true} if LSE persisted with the project or {@code false} if in the workspace.
	 */
	public boolean isStoringEntriesInProjectArea() {
		return storeEntriesInProjectArea;
	}

	/**
	 * Setter to define where language settings are persisted.
	 * @param storeEntriesWithProject - {@code true} if with the project,
	 *    {@code false} if in workspace area.
	 */
	public void setStoringEntriesInProjectArea(boolean storeEntriesWithProject) {
		this.storeEntriesInProjectArea = storeEntriesWithProject;
	}

	/**
	 * Clear all the entries for all configurations, all resources and all languages.
	 */
	public void clear() {
		fStorage.clear();
	}

	/**
	 * Internal convenience method to set language settings entries.
	 */
	private void setSettingEntriesInternal(String rcProjectPath, String languageId, List<ICLanguageSettingEntry> entries) {
		if (entries!=null) {
			Map<String, List<ICLanguageSettingEntry>> langMap = fStorage.get(languageId);
			if (langMap==null) {
				langMap = new HashMap<String, List<ICLanguageSettingEntry>>();
				fStorage.put(languageId, langMap);
			}
			List<ICLanguageSettingEntry> sortedEntries = listPool.add(Collections.unmodifiableList(sortEntries(entries)));
			langMap.put(rcProjectPath, sortedEntries);
		} else {
			// do not keep nulls in the tables
			Map<String, List<ICLanguageSettingEntry>> langMap = fStorage.get(languageId);
			if (langMap!=null) {
				langMap.remove(rcProjectPath);
				if (langMap.size()==0) {
					fStorage.remove(languageId);
				}
			}
		}
	}

	/**
	 * Some providers may collect entries in pretty much random order. For the purposes of
	 * predictability, UI usability and efficient storage the entries are sorted by kinds
	 * and secondary by name for kinds where the secondary order is not significant.
	 *
	 * @param entries - list of entries to sort.
	 * @return - sorted entries.
	 */
	protected List<ICLanguageSettingEntry> sortEntries(List<ICLanguageSettingEntry> entries) {
		List<ICLanguageSettingEntry> sortedEntries = new ArrayList<ICLanguageSettingEntry>(entries);
		Collections.sort(sortedEntries, new Comparator<ICLanguageSettingEntry>(){
			/**
			 * This comparator sorts by kinds first and the macros are sorted additionally by name.
			 */
			public int compare(ICLanguageSettingEntry entry0, ICLanguageSettingEntry entry1) {
				int kind0 = entry0.getKind();
				int kind1 = entry1.getKind();
				if (kind0==ICSettingEntry.MACRO && kind1==ICSettingEntry.MACRO) {
					return entry0.getName().compareTo(entry1.getName());
				}

				return kind0 - kind1;
			}});

		return sortedEntries;
	}

	/**
	 * Sets language settings entries for the provider.
	 * Note that the entries are not persisted at that point. To persist use TODO
	 *
	 * @param cfgDescription - configuration description.
	 * @param rc - resource such as file or folder.
	 * @param languageId - language id. If {@code null}, then entries are considered to be defined for
	 *    the language scope. See {@link #getLanguageScope()}
	 * @param entries - language settings entries to set.
	 */
	public void setSettingEntries(ICConfigurationDescription cfgDescription, IResource rc, String languageId, List<ICLanguageSettingEntry> entries) {
		String rcProjectPath = rc!=null ? rc.getProjectRelativePath().toString() : null;
		setSettingEntriesInternal(rcProjectPath, languageId, entries);
	}

	/**
	 * {@inheritDoc}
	 * <br> Note that this list is <b>unmodifiable</b>. To modify the list copy it, change and use
	 * {@link #setSettingEntries(ICConfigurationDescription, IResource, String, List)}.
	 *
	 */
	@Override
	public List<ICLanguageSettingEntry> getSettingEntries(ICConfigurationDescription cfgDescription, IResource rc, String languageId) {
		Map<String, List<ICLanguageSettingEntry>> langMap = fStorage.get(languageId);
		if (langMap!=null) {
			String rcProjectPath = rc!=null ? rc.getProjectRelativePath().toString() : null;
			List<ICLanguageSettingEntry> entries = langMap.get(rcProjectPath);
			if (entries!=null)
				return entries;
		}

		if (languageId!=null && (languageScope==null || languageScope.contains(languageId))) {
			List<ICLanguageSettingEntry> entries = getSettingEntries(cfgDescription, rc, null);
			return entries;
		}

		return null;
	}

	/**
	 * Serialize the provider under parent XML element.
	 * This is convenience method not intended to be overridden on purpose.
	 * 
	 * @param parentElement - element where to serialize.
	 * @return - newly created <provider> element. That element will already be
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
	 * @return - newly created <provider> element. That element will already be
	 *    attached to the parent element.
	 */
	public Element serializeAttributes(Element parentElement) {
		Element elementProvider = XmlUtil.appendElement(parentElement, ELEM_PROVIDER, new String[] {
				ATTR_ID, getId(),
				ATTR_NAME, getName(),
				ATTR_CLASS, getClass().getCanonicalName(),
				ATTR_PARAMETER, getCustomParameter(),
				ATTR_STORE_ENTRIES, isStoringEntriesInProjectArea() ? VALUE_PROJECT : VALUE_WORKSPACE,
			});

		if (languageScope!=null) {
			for (String langId : languageScope) {
				XmlUtil.appendElement(elementProvider, ELEM_LANGUAGE_SCOPE, new String[] {ATTR_ID, langId});
			}
		}
		return elementProvider;
	}

	/**
	 * Serialize the provider entries under parent XML element.
	 * @param elementProvider - element where to serialize the entries.
	 */
	public void serializeEntries(Element elementProvider) {
		for (Entry<String, Map<String, List<ICLanguageSettingEntry>>> entryLang : fStorage.entrySet()) {
			serializeLanguage(elementProvider, entryLang);
		}
	}

	/**
	 * Serialize the provider entries for a given language list.
	 */
	private void serializeLanguage(Element parentElement, Entry<String, Map<String, List<ICLanguageSettingEntry>>> entryLang) {
		String langId = entryLang.getKey();
		if (langId!=null) {
			Element elementLanguage = XmlUtil.appendElement(parentElement, ELEM_LANGUAGE, new String[] {ATTR_ID, langId});
			parentElement = elementLanguage;
		}
		for (Entry<String, List<ICLanguageSettingEntry>> entryRc : entryLang.getValue().entrySet()) {
			serializeResource(parentElement, entryRc);
		}
	}

	/**
	 * Serialize the provider entries for a given resource list.
	 */
	private void serializeResource(Element parentElement, Entry<String, List<ICLanguageSettingEntry>> entryRc) {
		String rcProjectPath = entryRc.getKey();
		if (rcProjectPath!=null) {
			Element elementRc = XmlUtil.appendElement(parentElement, ELEM_RESOURCE, new String[] {ATTR_PROJECT_PATH, rcProjectPath});
			parentElement = elementRc;
		}
		serializeSettingEntries(parentElement, entryRc.getValue());
	}

	/**
	 * Serialize given settings entries.
	 */
	private void serializeSettingEntries(Element parentElement, List<ICLanguageSettingEntry> settingEntries) {
		for (ICLanguageSettingEntry entry : settingEntries) {
			Element elementSettingEntry = XmlUtil.appendElement(parentElement, ELEM_ENTRY, new String[] {
					ATTR_KIND, LanguageSettingEntriesSerializer.kindToString(entry.getKind()),
					ATTR_NAME, entry.getName(),
				});
			switch(entry.getKind()) {
			case ICSettingEntry.MACRO:
				elementSettingEntry.setAttribute(ATTR_VALUE, entry.getValue());
				break;
//			case ICLanguageSettingEntry.LIBRARY_FILE:
//				// TODO: sourceAttachment fields may need to be covered
//				break;
			}
			int flags = entry.getFlags();
			if (flags!=0) {
				// Element elementFlag =
				XmlUtil.appendElement(elementSettingEntry, ELEM_FLAG, new String[] {
						ATTR_VALUE, LanguageSettingEntriesSerializer.composeFlagsString(entry.getFlags())
					});
			}
		}
	}

	/**
	 * Load a setting entry from XML element.
	 */
	private ICLanguageSettingEntry loadSettingEntry(Node parentElement) {
		String settingKind = XmlUtil.determineAttributeValue(parentElement, ATTR_KIND);
		String settingName = XmlUtil.determineAttributeValue(parentElement, ATTR_NAME);

		NodeList flagNodes = parentElement.getChildNodes();
		int flags = 0;
		for (int i=0;i<flagNodes.getLength();i++) {
			Node flagNode = flagNodes.item(i);
			if(flagNode.getNodeType() != Node.ELEMENT_NODE || !ELEM_FLAG.equals(flagNode.getNodeName()))
				continue;

			String settingFlags = XmlUtil.determineAttributeValue(flagNode, ATTR_VALUE);
			int bitFlag = LanguageSettingEntriesSerializer.composeFlags(settingFlags);
			flags |= bitFlag;

		}

		String settingValue = null;
		int kind = LanguageSettingEntriesSerializer.stringToKind(settingKind);
		if (kind == ICSettingEntry.MACRO)
			settingValue = XmlUtil.determineAttributeValue(parentElement, ATTR_VALUE);
		ICLanguageSettingEntry entry = (ICLanguageSettingEntry) CDataUtil.createEntry(kind, settingName, settingValue, null, flags);
		return entry;
	}


	/**
	 * Load provider from XML provider element.
	 * @param providerNode - XML element <provider> to load provider from.
	 */
	public void load(Element providerNode) {
		fStorage.clear();
		languageScope = null;

		// provider/configuration/language/resource/entry
		if (providerNode!=null) {
			loadAttributes(providerNode);
			loadEntries(providerNode);
		}
	}

	/**
	 * Load attributes from XML provider element.
	 * @param providerNode - XML element <provider> to load attributes from.
	 */
	public void loadAttributes(Element providerNode) {
		String providerId = XmlUtil.determineAttributeValue(providerNode, ATTR_ID);
		String providerName = XmlUtil.determineAttributeValue(providerNode, ATTR_NAME);
		String providerParameter = XmlUtil.determineAttributeValue(providerNode, ATTR_PARAMETER);
		String providerStoreEntries = XmlUtil.determineAttributeValue(providerNode, ATTR_STORE_ENTRIES);

		this.setId(providerId);
		this.setName(providerName);
		this.setCustomParameter(providerParameter);
		this.setStoringEntriesInProjectArea(VALUE_PROJECT.equals(providerStoreEntries));

		NodeList nodes = providerNode.getChildNodes();
		for (int i=0;i<nodes.getLength();i++) {
			Node elementNode = nodes.item(i);
			if(elementNode.getNodeType() != Node.ELEMENT_NODE)
				continue;

			if (ELEM_LANGUAGE_SCOPE.equals(elementNode.getNodeName())) {
				loadLanguageScopeElement(elementNode);
			}
		}

	}

	/**
	 * Load provider entries from XML provider element.
	 * @param providerNode - parent XML element <provider> where entries are defined.
	 */
	public void loadEntries(Element providerNode) {
		List<ICLanguageSettingEntry> settings = new ArrayList<ICLanguageSettingEntry>();
		NodeList nodes = providerNode.getChildNodes();
		for (int i=0;i<nodes.getLength();i++) {
			Node elementNode = nodes.item(i);
			if(elementNode.getNodeType() != Node.ELEMENT_NODE)
				continue;

			if (ELEM_LANGUAGE.equals(elementNode.getNodeName())) {
				loadLanguageElement(elementNode, null);
			} else if (ELEM_RESOURCE.equals(elementNode.getNodeName())) {
				loadResourceElement(elementNode, null, null);
			} else if (ELEM_ENTRY.equals(elementNode.getNodeName())) {
				ICLanguageSettingEntry entry = loadSettingEntry(elementNode);
				if (entry!=null) {
					settings.add(entry);
				}
			}
		}
		// set settings
		if (settings.size()>0) {
			setSettingEntriesInternal(null, null, settings);
		}
	}

	/**
	 * Determine and set language scope from given XML node.
	 */
	private void loadLanguageScopeElement(Node parentNode) {
		if (languageScope==null) {
			languageScope = new ArrayList<String>();
		}
		String id = XmlUtil.determineAttributeValue(parentNode, ATTR_ID);
		languageScope.add(id);

	}

	/**
	 * Load entries defined in language element.
	 */
	private void loadLanguageElement(Node parentNode, String cfgId) {
		String langId = XmlUtil.determineAttributeValue(parentNode, ATTR_ID);
		if (langId.length()==0) {
			langId=null;
		}

		List<ICLanguageSettingEntry> settings = new ArrayList<ICLanguageSettingEntry>();
		NodeList nodes = parentNode.getChildNodes();
		for (int i=0;i<nodes.getLength();i++) {
			Node elementNode = nodes.item(i);
			if(elementNode.getNodeType() != Node.ELEMENT_NODE)
				continue;

			if (ELEM_RESOURCE.equals(elementNode.getNodeName())) {
				loadResourceElement(elementNode, cfgId, langId);
			} else if (ELEM_ENTRY.equals(elementNode.getNodeName())) {
				ICLanguageSettingEntry entry = loadSettingEntry(elementNode);
				if (entry!=null) {
					settings.add(entry);
				}
			}
		}
		// set settings
		if (settings.size()>0) {
			setSettingEntriesInternal(null, langId, settings);
		}
	}

	/**
	 * Load entries defined in resource element.
	 */
	private void loadResourceElement(Node parentNode, String cfgId, String langId) {
		String rcProjectPath = XmlUtil.determineAttributeValue(parentNode, ATTR_PROJECT_PATH);

		List<ICLanguageSettingEntry> settings = new ArrayList<ICLanguageSettingEntry>();
		NodeList nodes = parentNode.getChildNodes();
		for (int i=0;i<nodes.getLength();i++) {
			Node elementNode = nodes.item(i);
			if(elementNode.getNodeType() != Node.ELEMENT_NODE)
				continue;

			if (ELEM_ENTRY.equals(elementNode.getNodeName())) {
				ICLanguageSettingEntry entry = loadSettingEntry(elementNode);
				if (entry!=null) {
					settings.add(entry);
				}
			}
		}

		// set settings
		if (settings.size()>0) {
			setSettingEntriesInternal(rcProjectPath, langId, settings);
		}
	}

	/**
	 * Clone storage for the entries. Copies references for lists of entries as a whole.
	 * Note that is OK as the lists kept in storage are unmodifiable.
	 */
	private Map<String, Map<String, List<ICLanguageSettingEntry>>> cloneStorage() {
		Map<String, // languageId
			Map<String, // resource
				List<ICLanguageSettingEntry>>> storageClone = new HashMap<String, Map<String, List<ICLanguageSettingEntry>>>();
		Set<Entry<String, Map<String, List<ICLanguageSettingEntry>>>> entrySetLang = fStorage.entrySet();
		for (Entry<String, Map<String, List<ICLanguageSettingEntry>>> entryLang : entrySetLang) {
			String langId = entryLang.getKey();
			Map<String, List<ICLanguageSettingEntry>> mapRc = entryLang.getValue();
			Map<String, List<ICLanguageSettingEntry>> mapRcClone = new HashMap<String, List<ICLanguageSettingEntry>>();
			Set<Entry<String, List<ICLanguageSettingEntry>>> entrySetRc = mapRc.entrySet();
			for (Entry<String, List<ICLanguageSettingEntry>> entryRc : entrySetRc) {
				String rcProjectPath = entryRc.getKey();
				List<ICLanguageSettingEntry> lsEntries = entryRc.getValue();
				// don't need to clone entries, they are from the LSE pool
				mapRcClone.put(rcProjectPath, lsEntries);
			}
			storageClone.put(langId, mapRcClone);
		}
		return storageClone;
	}

	/**
	 * See {@link #cloneShallow()}. This method is extracted
	 * to avoid expressing {@link #clone()} via {@link #cloneShallow()}.
	 */
	private LanguageSettingsSerializable cloneShallowInternal() throws CloneNotSupportedException {
		LanguageSettingsSerializable clone = (LanguageSettingsSerializable)super.clone();
		if (languageScope!=null)
			clone.languageScope = new ArrayList<String>(languageScope);

		clone.fStorage = new HashMap<String, Map<String, List<ICLanguageSettingEntry>>>();
		return clone;
	}

	/**
	 * Shallow clone of the provider. "Shallow" is defined here as the exact copy except that
	 * the copy will have zero language settings entries.
	 *
	 * @return shallow copy of the provider.
	 * @throws CloneNotSupportedException in case {@link #clone()} throws the exception.
	 */
	protected LanguageSettingsSerializable cloneShallow() throws CloneNotSupportedException {
		return cloneShallowInternal();
	}

	@Override
	protected LanguageSettingsSerializable clone() throws CloneNotSupportedException {
		LanguageSettingsSerializable clone = cloneShallowInternal();
		clone.fStorage = cloneStorage();
		return clone;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((getId() == null) ? 0 : getId().hashCode());
		result = prime * result + ((getName() == null) ? 0 : getName().hashCode());
		result = prime * result + ((languageScope == null) ? 0 : languageScope.hashCode());
		result = prime * result + ((customParameter == null) ? 0 : customParameter.hashCode());
		result = prime * result + (storeEntriesInProjectArea ? 0 : 1);
		result = prime * result + ((fStorage == null) ? 0 : fStorage.hashCode());
		result = prime * result + getClass().hashCode();
		return result;
	}

	/**
	 * @return {@code true} if the objects are equal, {@code false } otherwise.
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		LanguageSettingsSerializable other = (LanguageSettingsSerializable) obj;

		String id = getId();
		String otherId = other.getId();
		if (id == null) {
			if (otherId != null)
				return false;
		} else if (!id.equals(otherId))
			return false;

		String name = getName();
		String otherName = other.getName();
		if (name == null) {
			if (otherName != null)
				return false;
		} else if (!name.equals(otherName))
			return false;

		if (languageScope == null) {
			if (other.languageScope != null)
				return false;
		} else if (!languageScope.equals(other.languageScope))
			return false;

		if (customParameter == null) {
			if (other.customParameter != null)
				return false;
		} else if (!customParameter.equals(other.customParameter))
			return false;

		if (storeEntriesInProjectArea!=other.storeEntriesInProjectArea)
			return false;

		if (fStorage == null) {
			if (other.fStorage != null)
				return false;
		} else if (!fStorage.equals(other.fStorage))
			return false;
		return true;
	}
}
