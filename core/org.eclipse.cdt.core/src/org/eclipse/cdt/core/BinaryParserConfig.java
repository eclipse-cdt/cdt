/*******************************************************************************
 * Copyright (c) 2000, 2004 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     QNX Software Systems - Initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.core;

/*
 * BinaryParserConfig 
 */
public class BinaryParserConfig {

	IBinaryParser parser;
	String id;

	public BinaryParserConfig(IBinaryParser parser, String id) {
		this.parser = parser;
		this.id = id;
	}

	public String getId() {
		return id;
	}

	public IBinaryParser getBinaryParser() {
		return parser;
	}
}
