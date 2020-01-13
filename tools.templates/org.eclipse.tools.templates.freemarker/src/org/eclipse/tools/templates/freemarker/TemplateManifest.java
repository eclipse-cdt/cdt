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

import java.util.List;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement
public class TemplateManifest {

	private List<FileTemplate> files;
	private List<SourceRoot> srcRoots;

	@XmlElement(name = "file")
	public List<FileTemplate> getFiles() {
		return files;
	}

	public void setFiles(List<FileTemplate> files) {
		this.files = files;
	}

	@XmlElement(name = "srcRoot")
	public List<SourceRoot> getSrcRoots() {
		return srcRoots;
	}

	public void setSrcRoots(List<SourceRoot> srcRoots) {
		this.srcRoots = srcRoots;
	}

}
