/*******************************************************************************
 * Copyright (c) 2016 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
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
