/*******************************************************************************
 * Copyright (c) 2005 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * QNX - Initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.core.pdom;

import java.sql.Connection;

import org.eclipse.cdt.core.CCorePlugin;
import org.eclipse.core.runtime.QualifiedName;

public interface IPDOMDatabaseProvider {

	public static final QualifiedName dbNameKey
		= new QualifiedName(CCorePlugin.PLUGIN_ID, "pdomDBName");

	Connection getDatabase(String dbName, boolean create);
	
	void shutdownDatabase(String dbName);
	
}
