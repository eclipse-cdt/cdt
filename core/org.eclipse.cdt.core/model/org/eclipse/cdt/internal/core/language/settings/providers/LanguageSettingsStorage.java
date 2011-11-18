package org.eclipse.cdt.internal.core.language.settings.providers;

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

public class LanguageSettingsStorage {
	private static final String ELEM_LANGUAGE = "language"; //$NON-NLS-1$
	private static final String ATTR_LANGUAGE_ID = "id"; //$NON-NLS-1$
	private static final String ELEM_RESOURCE = "resource"; //$NON-NLS-1$
	private static final String ATTR_PROJECT_PATH = "project-relative-path"; //$NON-NLS-1$

	private static final String ELEM_ENTRY = "entry"; //$NON-NLS-1$
	private static final String ATTR_KIND = "kind"; //$NON-NLS-1$
	private static final String ATTR_NAME = "name"; //$NON-NLS-1$
	private static final String ATTR_VALUE = "value"; //$NON-NLS-1$
	private static final String ELEM_FLAG = "flag"; //$NON-NLS-1$

	/**
	 * Storage to keep settings entries. Note that it is not necessary to keep configuration in the maps
	 * as the configuration is always the one provider belongs to.
	 */
	private Map<String, // languageId
				Map<String, // resource project path
					List<ICLanguageSettingEntry>>> fStorage = new HashMap<String, Map<String, List<ICLanguageSettingEntry>>>();

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

	/**
	 * TODO
	 * <br> Note that this list is <b>unmodifiable</b>.
	 *
	 */
	public List<ICLanguageSettingEntry> getSettingEntries(ICConfigurationDescription cfgDescription, IResource rc, String languageId) {
		List<ICLanguageSettingEntry> entries = null;
		Map<String, List<ICLanguageSettingEntry>> langMap = fStorage.get(languageId);
		if (langMap!=null) {
			String rcProjectPath = rc!=null ? rc.getProjectRelativePath().toString() : null;
			entries = langMap.get(rcProjectPath);
		}
		return entries;
	}

	/**
	 * Some providers may collect entries in pretty much random order. For the purposes of
	 * predictability, UI usability and efficient storage the entries are sorted by kinds
	 * and secondary by name for kinds where the secondary order is not significant.
	 *
	 * @param entries - list of entries to sort.
	 * @return - sorted entries.
	 */
	private List<ICLanguageSettingEntry> sortEntries(List<ICLanguageSettingEntry> entries) {
		List<ICLanguageSettingEntry> sortedEntries = new ArrayList<ICLanguageSettingEntry>(entries);
		Collections.sort(sortedEntries, new Comparator<ICLanguageSettingEntry>(){
			/**
			 * This comparator sorts by kinds first and the macros are sorted additionally by name.
			 */
			@Override
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
	 *
	 */
	public void setSettingEntries(String rcProjectPath, String languageId, List<ICLanguageSettingEntry> entries) {
		synchronized (fStorage) {
			if (entries!=null) {
				Map<String, List<ICLanguageSettingEntry>> langMap = fStorage.get(languageId);
				if (langMap==null) {
					langMap = new HashMap<String, List<ICLanguageSettingEntry>>();
					fStorage.put(languageId, langMap);
				}
				List<ICLanguageSettingEntry> sortedEntries = getPooledList(sortEntries(entries), false);
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
	}

	/**
	 * @return {@code true} if the provider does not keep any settings yet or {@code false} if there are some.
	 */
	public boolean isEmpty() {
		return fStorage.isEmpty();
	}

	/**
	 * Clear all the entries for all configurations, all resources and all languages.
	 */
	public void clear() {
		synchronized (fStorage) {
			fStorage.clear();
		}
	}

	/**
	 * Serialize the provider entries under parent XML element.
	 * @param elementProvider - element where to serialize the entries.
	 */
	public void serializeEntries(Element elementProvider) {
		synchronized (fStorage) {
			for (Entry<String, Map<String, List<ICLanguageSettingEntry>>> entryLang : fStorage.entrySet()) {
				serializeLanguage(elementProvider, entryLang);
			}
		}
	}

	/**
	 * Serialize the provider entries for a given language list.
	 */
	private void serializeLanguage(Element parentElement, Entry<String, Map<String, List<ICLanguageSettingEntry>>> entryLang) {
		String langId = entryLang.getKey();
		if (langId!=null) {
			Element elementLanguage = XmlUtil.appendElement(parentElement, ELEM_LANGUAGE, new String[] {ATTR_LANGUAGE_ID, langId});
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
			setSettingEntries(null, null, settings);
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
	 * Load entries defined in language element.
	 */
	private void loadLanguageElement(Node parentNode, String cfgId) {
		String langId = XmlUtil.determineAttributeValue(parentNode, ATTR_LANGUAGE_ID);
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
			setSettingEntries(null, langId, settings);
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
			setSettingEntries(rcProjectPath, langId, settings);
		}
	}

	/**
	 * Returns the equal list of entries from the pool to conserve the memory.
	 *
	 * @param entries - list of entries to pool.
	 * @param copy - specify {@code true} to copy the list in order to prevent
	 *    back-door modification on the original list changes.
	 * @return returns the list of entries from the pool.
	 */
	private static List<ICLanguageSettingEntry> getPooledList(List<ICLanguageSettingEntry> entries, boolean copy) {
		if (entries == null)
			return null;

		List<ICLanguageSettingEntry> pooledList = listPool.get(entries);
		if (pooledList != null) {
			return pooledList;
		}

		if (entries.size() == 0) {
			return getPooledEmptyList();
		}

		if (copy) {
			entries = new ArrayList<ICLanguageSettingEntry>(entries);
		}
		pooledList = Collections.unmodifiableList(entries);
		return listPool.add(pooledList);
	}

	/**
	 * Returns the equal list of entries from the pool to conserve the memory.
	 *
	 * @param entries - list of entries to pool.
	 * @return returns the list of entries from the pool.
	 */
	public static List<ICLanguageSettingEntry> getPooledList(List<ICLanguageSettingEntry> entries) {
		return getPooledList(entries, true);
	}

	/**
	 * @return the empty immutable list which is pooled. Use this call rather than creating
	 * new empty array to ensure that operator '==' can be used instead of deep equals().
	 */
	public static List<ICLanguageSettingEntry> getPooledEmptyList() {
		List<ICLanguageSettingEntry> pooledEmptyList = Collections.emptyList();
		return listPool.add(pooledEmptyList);
	}

	/**
	 * Clone storage for the entries. Copies references for lists of entries as a whole.
	 * Note that is OK as the lists kept in storage are unmodifiable.
	 */
	public LanguageSettingsStorage cloneStorage() {
		LanguageSettingsStorage storageClone = new LanguageSettingsStorage();
		synchronized (fStorage) {
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
				storageClone.fStorage.put(langId, mapRcClone);
			}
		}
		return storageClone;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((fStorage == null) ? 0 : fStorage.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		LanguageSettingsStorage other = (LanguageSettingsStorage) obj;
		if (fStorage == null) {
			if (other.fStorage != null)
				return false;
		} else if (!fStorage.equals(other.fStorage))
			return false;
		return true;
	}

}
