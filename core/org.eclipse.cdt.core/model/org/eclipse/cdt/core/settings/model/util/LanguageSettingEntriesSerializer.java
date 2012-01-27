/*******************************************************************************
 * Copyright (c) 2007, 2010 Intel Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Intel Corporation - Initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.core.settings.model.util;

import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;

import org.eclipse.cdt.core.settings.model.ICExclusionPatternPathEntry;
import org.eclipse.cdt.core.settings.model.ICLanguageSettingEntry;
import org.eclipse.cdt.core.settings.model.ICLibraryFileEntry;
import org.eclipse.cdt.core.settings.model.ICSettingEntry;
import org.eclipse.cdt.core.settings.model.ICStorageElement;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;

public class LanguageSettingEntriesSerializer {
	public static final String ELEMENT_ENTRY = "entry"; //$NON-NLS-1$
	public static final String ATTRIBUTE_KIND = "kind"; //$NON-NLS-1$
	public static final String ATTRIBUTE_NAME = "name"; //$NON-NLS-1$
	public static final String ATTRIBUTE_VALUE = "value"; //$NON-NLS-1$
	public static final String ATTRIBUTE_FLAGS = "flags"; //$NON-NLS-1$
	public static final String ATTRIBUTE_EXCLUDING = "excluding"; //$NON-NLS-1$
	public static final String ATTRIBUTE_SOURCE_ATTACHMENT_PATH = "srcPath"; //$NON-NLS-1$
	public static final String ATTRIBUTE_SOURCE_ATTACHMENT_ROOT_PATH = "srcRootPath"; //$NON-NLS-1$
	public static final String ATTRIBUTE_SOURCE_ATTACHMENT_PREFIX_MAPPING = "srcPrefixMapping"; //$NON-NLS-1$

	//	public static final String ATTRIBUTE_FULL_PATH = "fullPath"; //$NON-NLS-1$
	//	public static final String ATTRIBUTE_LOCATION = "location"; //$NON-NLS-1$

	public static final String INCLUDE_PATH = "includePath"; //$NON-NLS-1$
	public static final String INCLUDE_FILE = "includeFile"; //$NON-NLS-1$
	public static final String MACRO = "macro"; //$NON-NLS-1$
	public static final String MACRO_FILE = "macroFile"; //$NON-NLS-1$
	public static final String LIBRARY_PATH = "libraryPath"; //$NON-NLS-1$
	public static final String LIBRARY_FILE = "libraryFile"; //$NON-NLS-1$
	public static final String SOURCE_PATH = "sourcePath"; //$NON-NLS-1$
	public static final String OUTPUT_PATH = "outputPath"; //$NON-NLS-1$

	public static final String BUILTIN = "BUILTIN"; //$NON-NLS-1$
	public static final String READONLY = "READONLY"; //$NON-NLS-1$
	public static final String LOCAL = "LOCAL"; //$NON-NLS-1$
	public static final String VALUE_WORKSPACE_PATH = "VALUE_WORKSPACE_PATH"; //$NON-NLS-1$
	public static final String RESOLVED = "RESOLVED"; //$NON-NLS-1$
	private static final String UNDEFINED = "UNDEFINED"; //$NON-NLS-1$
	private static final String FRAMEWORK = "FRAMEWORK"; //$NON-NLS-1$

	public static final String FLAGS_SEPARATOR = "|"; //$NON-NLS-1$

	public static ICSettingEntry[] loadEntries(ICStorageElement el) {
		return loadEntries(el, 0);
	}

	public static ICSettingEntry[] loadEntries(ICStorageElement el, int kindFilter) {
		List<ICSettingEntry> list = loadEntriesList(el, kindFilter);
		return list.toArray(new ICSettingEntry[list.size()]);
	}

	public static List<ICSettingEntry> loadEntriesList(ICStorageElement el) {
		return loadEntriesList(el, 0);
	}

	public static List<ICSettingEntry> loadEntriesList(ICStorageElement el, int kindFilter) {
		ICStorageElement children[] = el.getChildren();
		ICStorageElement child;
		List<ICSettingEntry> list = new ArrayList<ICSettingEntry>();
		ICSettingEntry entry;
		for (int i = 0; i < children.length; i++) {
			child = children[i];
			if (ELEMENT_ENTRY.equals(child.getName())) {
				entry = loadEntry(child);
				if (entry != null && (kindFilter == 0 || (kindFilter & entry.getKind()) != 0))
					list.add(entry);
			}
		}
		return list;
	}

	public static ICSettingEntry loadEntry(ICStorageElement el){
		int kind = stringToKind(el.getAttribute(ATTRIBUTE_KIND));
		if(kind == 0)
			return null;

		int flags = composeFlags(el.getAttribute(ATTRIBUTE_FLAGS));
		String name = el.getAttribute(ATTRIBUTE_NAME);
		String value = el.getAttribute(ATTRIBUTE_VALUE);
		IPath srcPath = loadPath(el, ATTRIBUTE_SOURCE_ATTACHMENT_PATH);
		IPath srcRootPath = loadPath(el, ATTRIBUTE_SOURCE_ATTACHMENT_ROOT_PATH);
		IPath[] exclusionPatterns = loadExclusions(el);
		IPath srcPrefixMapping = loadPath(el, ATTRIBUTE_SOURCE_ATTACHMENT_PREFIX_MAPPING);

		return CDataUtil.createEntry(kind, name, value, exclusionPatterns, flags, srcPath, srcRootPath, srcPrefixMapping);
	}

	private static IPath loadPath(ICStorageElement el, String attr) {
		String value = el.getAttribute(attr);
		if (value != null)
			return new Path(value);
		return null;
	}

	// private static void storePath(ICStorageElement el, String attr, IPath path){
	// if(path != null)
	// el.setAttribute(attr, path.toString());
	// }

	private static IPath[] loadExclusions(ICStorageElement el) {
		String attr = el.getAttribute(ATTRIBUTE_EXCLUDING);
		if (attr != null) {
			String[] strs = CDataUtil.stringToArray(attr, FLAGS_SEPARATOR);
			IPath[] paths = new IPath[strs.length];
			for (int i = 0; i < strs.length; i++) {
				paths[i] = new Path(strs[i]);
			}
			return paths;
		}
		return null;
	}

	private static void storeExclusions(ICStorageElement el, IPath[] paths) {
		if (paths == null || paths.length == 0)
			return;

		String[] strs = new String[paths.length];
		for (int i = 0; i < strs.length; i++) {
			strs[i] = paths[i].toString();
		}

		String attr = CDataUtil.arrayToString(strs, FLAGS_SEPARATOR);
		el.setAttribute(ATTRIBUTE_EXCLUDING, attr);
	}

	public static void serializeEntries(ICSettingEntry entries[], ICStorageElement element) {
		ICStorageElement child;
		if (entries != null) {
			for (int i = 0; i < entries.length; i++) {
				child = element.createChild(ELEMENT_ENTRY);
				serializeEntry(entries[i], child);
			}
		}
	}

	public static void serializeEntry(ICSettingEntry entry, ICStorageElement element) {
		String kind = kindToString(entry.getKind());
		String flags = composeFlagsString(entry.getFlags());
		String name = entry.getName();
		element.setAttribute(ATTRIBUTE_KIND, kind);
		element.setAttribute(ATTRIBUTE_FLAGS, flags);
		element.setAttribute(ATTRIBUTE_NAME, name);
		switch (entry.getKind()) {
		case ICSettingEntry.MACRO:
			String value = entry.getValue();
			element.setAttribute(ATTRIBUTE_VALUE, value);
			break;
		case ICSettingEntry.SOURCE_PATH:
		case ICSettingEntry.OUTPUT_PATH:
			IPath paths[] = ((ICExclusionPatternPathEntry) entry).getExclusionPatterns();
			storeExclusions(element, paths);
			break;
		case ICSettingEntry.LIBRARY_FILE:
			ICLibraryFileEntry libFile = (ICLibraryFileEntry) entry;
			IPath path = libFile.getSourceAttachmentPath();
			if (path != null)
				element.setAttribute(ATTRIBUTE_SOURCE_ATTACHMENT_PATH, path.toString());

			path = libFile.getSourceAttachmentRootPath();
			if (path != null)
				element.setAttribute(ATTRIBUTE_SOURCE_ATTACHMENT_ROOT_PATH, path.toString());

			path = libFile.getSourceAttachmentPrefixMapping();
			if (path != null)
				element.setAttribute(ATTRIBUTE_SOURCE_ATTACHMENT_PREFIX_MAPPING, path.toString());
		}
	}

	public static String kindToString(int kind) {
		switch (kind) {
		case ICSettingEntry.INCLUDE_PATH:
			return INCLUDE_PATH;
		case ICSettingEntry.INCLUDE_FILE:
			return INCLUDE_FILE;
		case ICSettingEntry.MACRO:
			return MACRO;
		case ICSettingEntry.MACRO_FILE:
			return MACRO_FILE;
		case ICSettingEntry.LIBRARY_PATH:
			return LIBRARY_PATH;
		case ICSettingEntry.LIBRARY_FILE:
			return LIBRARY_FILE;
		case ICSettingEntry.SOURCE_PATH:
			return SOURCE_PATH;
		case ICSettingEntry.OUTPUT_PATH:
			return OUTPUT_PATH;
		}
		throw new IllegalArgumentException();
	}

	public static int stringToKind(String kind) {
		if (INCLUDE_PATH.equals(kind))
			return ICSettingEntry.INCLUDE_PATH;
		if (INCLUDE_FILE.equals(kind))
			return ICSettingEntry.INCLUDE_FILE;
		if (MACRO.equals(kind))
			return ICSettingEntry.MACRO;
		if (MACRO_FILE.equals(kind))
			return ICSettingEntry.MACRO_FILE;
		if (LIBRARY_PATH.equals(kind))
			return ICSettingEntry.LIBRARY_PATH;
		if (LIBRARY_FILE.equals(kind))
			return ICSettingEntry.LIBRARY_FILE;
		if (SOURCE_PATH.equals(kind))
			return ICSettingEntry.SOURCE_PATH;
		if (OUTPUT_PATH.equals(kind))
			return ICSettingEntry.OUTPUT_PATH;
		return 0;
		// throw new UnsupportedOperationException();
	}

	public static String composeFlagsString(int flags) {
		StringBuffer buf = new StringBuffer();
		if ((flags & ICSettingEntry.BUILTIN) != 0) {
			buf.append(BUILTIN);
		}
		if ((flags & ICSettingEntry.READONLY) != 0) {
			if (buf.length() != 0)
				buf.append(FLAGS_SEPARATOR);

			buf.append(READONLY);
		}
		if ((flags & ICSettingEntry.LOCAL) != 0) {
			if (buf.length() != 0)
				buf.append(FLAGS_SEPARATOR);

			buf.append(LOCAL);
		}
		if ((flags & ICSettingEntry.VALUE_WORKSPACE_PATH) != 0) {
			if (buf.length() != 0)
				buf.append(FLAGS_SEPARATOR);

			buf.append(VALUE_WORKSPACE_PATH);
		}
		if ((flags & ICSettingEntry.RESOLVED) != 0) {
			if (buf.length() != 0)
				buf.append(FLAGS_SEPARATOR);

			buf.append(RESOLVED);
		}
		if ((flags & ICLanguageSettingEntry.UNDEFINED) != 0) {
			if (buf.length() != 0)
				buf.append(FLAGS_SEPARATOR);

			buf.append(UNDEFINED);
		}
		if ((flags & ICLanguageSettingEntry.FRAMEWORKS_MAC) != 0) {
			if (buf.length() != 0)
				buf.append(FLAGS_SEPARATOR);

			buf.append(FRAMEWORK);
		}
		return buf.toString();
	}

	/**
	 * @since 5.4
	 */
	public static int composeFlags(String flagsString) {
		if (flagsString == null || flagsString.length() == 0)
			return 0;

		StringTokenizer tokenizer = new StringTokenizer(flagsString, FLAGS_SEPARATOR);
		int flags = 0;
		String f;
		while (tokenizer.hasMoreElements()) {
			f = tokenizer.nextToken();
			if (BUILTIN.equals(f))
				flags |= ICSettingEntry.BUILTIN;
			if (READONLY.equals(f))
				flags |= ICSettingEntry.READONLY;
			if (LOCAL.equals(f))
				flags |= ICSettingEntry.LOCAL;
			if (VALUE_WORKSPACE_PATH.equals(f))
				flags |= ICSettingEntry.VALUE_WORKSPACE_PATH;
			if (RESOLVED.equals(f))
				flags |= ICSettingEntry.RESOLVED;
			if (UNDEFINED.equals(f))
				flags |= ICSettingEntry.UNDEFINED;
			if (FRAMEWORK.equals(f))
				flags |= ICSettingEntry.FRAMEWORKS_MAC;
		}

		return flags;
	}

}
