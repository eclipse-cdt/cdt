/*******************************************************************************
 * Copyright (c) 2015 QNX Software Systems and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *******************************************************************************/
package org.eclipse.cdt.arduino.core.internal;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import org.eclipse.core.runtime.Platform;

public class HierarchicalProperties {

	private String value;
	private Map<String, HierarchicalProperties> children;
	private String platName;

	public HierarchicalProperties() {
		switch (Platform.getOS()) {
		case Platform.OS_WIN32:
			platName = "windows"; //$NON-NLS-1$
			break;
		case Platform.OS_MACOSX:
			platName = "macosx"; //$NON-NLS-1$
			break;
		case Platform.OS_LINUX:
			platName = "linux"; //$NON-NLS-1$
			break;
		}
	}

	public HierarchicalProperties(LinkedProperties properties) {
		this();
		for (Object keyObj : properties.orderedKeys()) {
			String key = (String) keyObj;
			String value = (String) properties.get(key);
			putProperty(key, value);
		}
	}

	public String getProperty(String qualifiedKey) {
		if (children == null) {
			return null;
		}

		int i = qualifiedKey.indexOf('.');
		if (i < 0) {
			HierarchicalProperties child = children.get(qualifiedKey);
			return child != null ? child.getValue() : null;
		} else {
			String key = qualifiedKey.substring(0, i);
			HierarchicalProperties child = children.get(key);
			if (child != null) {
				String childKey = qualifiedKey.substring(i + 1);
				return child.getProperty(childKey);
			} else {
				return null;
			}
		}
	}

	public void putProperty(String qualifiedKey, String value) {
		if (children == null) {
			children = new LinkedHashMap<>();
		}

		int i = qualifiedKey.indexOf('.');
		if (i < 0) {
			HierarchicalProperties child = children.get(qualifiedKey);
			if (child == null) {
				child = new HierarchicalProperties();
				children.put(qualifiedKey, child);
			}
			child.setValue(value);
		} else {
			String key = qualifiedKey.substring(0, i);
			HierarchicalProperties child = children.get(key);
			if (child == null) {
				child = new HierarchicalProperties();
				children.put(key, child);
			}
			String childKey = qualifiedKey.substring(i + 1);
			child.putProperty(childKey, value);
		}
	}

	public String getValue() {
		// Try a platform child
		if (platName != null && hasChild(platName)) {
			HierarchicalProperties child = getChild(platName);

			// return the child's value if
			// - it has a property
			// - it has no more children. In that case the value could even be null (specifically overridden)
			if ((null != child.getValue()) || (!child.hasChildren())) {
				return child.getValue();
			}
		}
		return value;
	}

	public void setValue(String value) {
		this.value = value;
	}

	public Map<String, HierarchicalProperties> getChildren() {
		return children;
	}

	private boolean hasChildren() {
		return (children != null && children.size() > 0);
	}

	private boolean hasChild(String key) {
		return (children != null && children.containsKey(key));
	}

	public HierarchicalProperties getChild(String key) {
		return children != null ? children.get(key) : null;
	}

	public void putChild(String key, HierarchicalProperties node) {
		if (children == null) {
			children = new LinkedHashMap<>();
		}
		children.put(key, node);
	}

	public List<HierarchicalProperties> listChildren() {
		int size = 0;
		for (Map.Entry<String, HierarchicalProperties> entry : children.entrySet()) {
			try {
				int i = Integer.parseInt(entry.getKey());
				if (i + 1 > size) {
					size = i + 1;
				}
			} catch (NumberFormatException e) {
				// ignore
			}
		}

		ArrayList<HierarchicalProperties> list = new ArrayList<>(size);
		for (Map.Entry<String, HierarchicalProperties> entry : children.entrySet()) {
			try {
				int i = Integer.parseInt(entry.getKey());
				list.set(i, entry.getValue());
			} catch (NumberFormatException e) {
				// ignore
			}
		}
		return list;
	}

	public void setChildren(List<HierarchicalProperties> list) {
		children.clear();
		for (int i = 0; i < list.size(); i++) {
			HierarchicalProperties node = list.get(i);
			if (node != null) {
				children.put(Integer.toString(i), node);
			}
		}
	}

	public Properties flatten() {
		Properties properties = new Properties();
		flatten(null, this, properties);
		return properties;
	}

	private static void flatten(String prefix, HierarchicalProperties tree, Properties props) {
		if (tree.getValue() != null && prefix != null) {
			props.put(prefix, tree.getValue());
		}

		if (tree.getChildren() != null) {
			for (Map.Entry<String, HierarchicalProperties> entry : tree.getChildren().entrySet()) {
				String childPrefix = entry.getKey();
				if (prefix != null) {
					childPrefix = prefix + "." + childPrefix; //$NON-NLS-1$
				}
				flatten(childPrefix, entry.getValue(), props);
			}
		}
	}
}
