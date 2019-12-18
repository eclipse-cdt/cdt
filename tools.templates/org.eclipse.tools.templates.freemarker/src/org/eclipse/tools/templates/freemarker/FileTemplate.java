/*******************************************************************************
 * Copyright (c) 2016 QNX Software Systems and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *******************************************************************************/
package org.eclipse.tools.templates.freemarker;

import javax.xml.bind.annotation.XmlAttribute;

public class FileTemplate {
	private String src;
	private String dest;
	private boolean open;
	private boolean show;
	private boolean copy;

	@XmlAttribute(name = "src")
	public String getSrc() {
		return src;
	}

	public void setSrc(String src) {
		this.src = src;
	}

	@XmlAttribute(name = "dest")
	public String getDest() {
		return dest;
	}

	public void setDest(String dest) {
		this.dest = dest;
	}

	@XmlAttribute(name = "open")
	public boolean isOpen() {
		return open;
	}

	public void setOpen(boolean open) {
		this.open = open;
	}

	@XmlAttribute(name = "show")
	public boolean isShow() {
		return show;
	}

	public void setShow(boolean show) {
		this.show = show;
	}

	@XmlAttribute(name = "copy")
	public boolean isCopy() {
		return copy;
	}

	public void setCopy(boolean copy) {
		this.copy = copy;
	}

}
