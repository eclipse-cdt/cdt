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

import java.util.ArrayList;
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

	public class Property {

		private String fKey;
		private Object fValue;

		/**
		 * Constructor for Property.
		 */
		public Property( String key, Object value ) {
			fKey = key;
			fValue = value;
		}

		public String getKey() {
			return fKey;
		}

		public Object getValue() {
			return fValue;
		}
	}

	private ArrayList fProperties;

	static CModuleProperties create( ICModule module ) {
		return new CModuleProperties( module );
	}

	/** 
	 * Constructor for CModuleProperties. 
	 */
	private CModuleProperties( ICModule module ) {
		fProperties = new ArrayList( 10 );
		fProperties.add( new Property( TYPE, new Integer( module.getType() ) ) );
		fProperties.add( new Property( CPU, module.getCPU() ) );
		fProperties.add( new Property( BASE_ADDRESS, module.getBaseAddress() ) );
		fProperties.add( new Property( SIZE, new Long( module.getSize() ) ) );
		fProperties.add( new Property( SYMBOLS_LOADED, new Boolean( module.areSymbolsLoaded() ) ) );
		fProperties.add( new Property( SYMBOLS_FILE, module.getSymbolsFileName() ) );
	}

	public Property[] getProperties() {
		return (Property[])fProperties.toArray( new Property[fProperties.size()] );
	}

	public void dispose() {
		fProperties.clear();
	}
}
