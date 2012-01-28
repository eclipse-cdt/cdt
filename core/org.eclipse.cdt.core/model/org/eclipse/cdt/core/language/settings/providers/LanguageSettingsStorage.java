/*******************************************************************************
 * Copyright (c) 2011, 2012 Andrew Gvozdev and others.
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

import org.eclipse.cdt.core.settings.model.ICLanguageSettingEntry;
import org.eclipse.cdt.core.settings.model.ICSettingEntry;
import org.eclipse.cdt.internal.core.WeakHashSet;
import org.eclipse.cdt.internal.core.WeakHashSetSynchronized;

/**
 * The class representing the (in-memory) storage for language settings entries {@link ICLanguageSettingEntry}.
 *
 * @since 5.4
 */
public class LanguageSettingsStorage implements Cloneable {
	/** Storage to keep settings entries. */
	protected Map<String, // languageId
				Map<String, // resource project path
					List<ICLanguageSettingEntry>>> fStorage = new HashMap<String, Map<String, List<ICLanguageSettingEntry>>>();

	/**
	 * Pool of LSE lists implemented as WeakHashSet. That allows to gain memory savings
	 * at the expense of CPU time. WeakHashSet handles garbage collection when a list is not
	 * referenced anywhere else. See JavaDoc {@link java.lang.ref.WeakReference} about weak reference objects.
	 */
	private static WeakHashSet<List<ICLanguageSettingEntry>> listPool = new WeakHashSetSynchronized<List<ICLanguageSettingEntry>>();

	/**
	 * Returns the list of setting entries for the given resource and language.
	 * <br> Note that this list is <b>unmodifiable</b>.
	 *
	 * @param rcProjectPath - path to the resource relative to the project.
	 * @param languageId - language id.
	 *
	 * @return the list of setting entries or {@code null} if no settings defined.
	 */
	public List<ICLanguageSettingEntry> getSettingEntries(String rcProjectPath, String languageId) {
		List<ICLanguageSettingEntry> entries = null;
		Map<String, List<ICLanguageSettingEntry>> langMap = fStorage.get(languageId);
		if (langMap!=null) {
			entries = langMap.get(rcProjectPath);
		}
		return entries;
	}

	/**
	 * Some providers may collect entries in pretty much random order. For the intent of
	 * predictability, UI usability and efficient storage the entries are sorted by kinds
	 * and secondary by name for kinds where the secondary order is not significant.
	 *
	 * @param entries - list of entries to sort.
	 * @return - sorted entries.
	 */
	private List<ICLanguageSettingEntry> sortEntries(List<ICLanguageSettingEntry> entries) {
		List<ICLanguageSettingEntry> sortedEntries = new ArrayList<ICLanguageSettingEntry>(entries);
		Collections.sort(sortedEntries, new Comparator<ICLanguageSettingEntry>() {
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
	 * Sets language settings entries for the resource and language.
	 *
	 * @param rcProjectPath - path to the resource relative to the project.
	 * @param languageId - language id.
	 * @param entries - language settings entries to set.
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
				// reduct the empty maps in the tables
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
	 * @return {@code true} if the storage is empty or {@code false} otherwise.
	 */
	public boolean isEmpty() {
		return fStorage.isEmpty();
	}

	/**
	 * Clear all the entries for all resources and all languages.
	 */
	public void clear() {
		synchronized (fStorage) {
			fStorage.clear();
		}
	}

	/**
	 * Find and return the equal list of entries from the pool.
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
	 * Find and return the equal list of entries from the pool to conserve the memory.
	 *
	 * @param entries - list of entries to pool.
	 * @return returns the list of entries from the pool.
	 */
	public static List<ICLanguageSettingEntry> getPooledList(List<ICLanguageSettingEntry> entries) {
		return getPooledList(entries, true);
	}

	/**
	 * @return Returns the empty immutable list which is pooled. Use this call rather than creating
	 * new empty array to ensure that faster shallow operator '==' can be used instead of equals()
	 * which goes deep on HashMaps.
	 */
	public static List<ICLanguageSettingEntry> getPooledEmptyList() {
		List<ICLanguageSettingEntry> pooledEmptyList = Collections.emptyList();
		return listPool.add(pooledEmptyList);
	}

	/**
	 * Clone storage for the entries. Copies references for lists of entries as a whole.
	 * Note that that is OK as the lists kept in storage are unmodifiable and pooled.
	 */
	@Override
	public LanguageSettingsStorage clone() throws CloneNotSupportedException {
		LanguageSettingsStorage storageClone = (LanguageSettingsStorage) super.clone();
		storageClone.fStorage = new HashMap<String, Map<String, List<ICLanguageSettingEntry>>>();
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
					// don't need to clone entries, they are from the LSE lists pool
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
