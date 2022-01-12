/*******************************************************************************
 * Copyright (c) 2009, 2015 Red Hat Inc.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Red Hat Inc. - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.internal.autotools.ui.properties;

import java.util.ArrayList;
import java.util.List;

public class ToolListElement {

	private List<ToolListElement> children;
	private ToolListElement parent;
	private String name;
	private int type;

	public ToolListElement(String name, int type) {
		this.name = name;
		this.type = type;
		this.children = new ArrayList<>();
	}

	public void setParent(ToolListElement p) {
		parent = p;
	}

	public ToolListElement getParent() {
		return parent;
	}

	public String getName() {
		return name;
	}

	public int getType() {
		return type;
	}

	public void addChild(ToolListElement e) {
		children.add(e);
		e.setParent(this);
	}

	public boolean hasChildren() {
		return children.size() > 0;
	}

	public Object[] getChildren() {
		return children.toArray();
	}

}
