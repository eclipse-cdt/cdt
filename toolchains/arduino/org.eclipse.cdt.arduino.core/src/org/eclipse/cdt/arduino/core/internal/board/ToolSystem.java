/*******************************************************************************
 * Copyright (c) 2015 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package org.eclipse.cdt.arduino.core.internal.board;

public class ToolSystem {

	private String host;
	private String archiveFileName;
	private String url;
	private String checksum;
	private String size;

	public String getHost() {
		return host;
	}

	public String getArchiveFileName() {
		return archiveFileName;
	}

	public String getUrl() {
		return url;
	}

	public String getChecksum() {
		return checksum;
	}

	public String getSize() {
		return size;
	}

}
