/**********************************************************************
 * Copyright (c) 2004 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 *
 * Contributors:
 * QNX Software Systems - Initial API and implementation
 ***********************************************************************/ 
package org.eclipse.cdt.debug.internal.ui.propertypages; 

import java.util.HashMap;
import org.eclipse.cdt.debug.core.model.ICModule;
 
/**
 * A module's properties store. 
 */
public class CModuleProperties {

	final static public String TYPE = "type";  //$NON-NLS-1$
	final static public String CPU = "cpu";  //$NON-NLS-1$
	final static public String BASE_ADDRESS = "baseAddress";  //$NON-NLS-1$
	final static public String SIZE = "size";  //$NON-NLS-1$
	final static public String SYMBOLS_LOADED = "symbolsLoaded";  //$NON-NLS-1$
	final static public String SYMBOLS_FILE = "symbolsFile";  //$NON-NLS-1$

	private HashMap fMap;

	static CModuleProperties create( ICModule module ) {
		CModuleProperties p = new CModuleProperties();
		p.setProperty( TYPE, new Integer( module.getType() ) );
		p.setProperty( CPU, module.getCPU() );
		p.setProperty( BASE_ADDRESS, module.getBaseAddress() );
		p.setProperty( SIZE, new Long( module.getSize() ) );
		p.setProperty( SYMBOLS_LOADED, new Boolean( module.areSymbolsLoaded() ) );
		p.setProperty( SYMBOLS_FILE, module.getSymbolsFileName() );
		return p;
	}

	/** 
	 * Constructor for CModuleProperties. 
	 */
	private CModuleProperties() {
		fMap = new HashMap( 5 );
	}

	private void setProperty( String key, Object value ) {
		fMap.put( key, value );
	}

	public Object[] getProperties() {
		return fMap.entrySet().toArray();
	}
}
