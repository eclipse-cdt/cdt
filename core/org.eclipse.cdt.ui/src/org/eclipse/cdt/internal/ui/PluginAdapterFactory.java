package org.eclipse.cdt.internal.ui;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */
 
import org.eclipse.cdt.core.resources.*;
import org.eclipse.cdt.internal.CCorePlugin;
import org.eclipse.core.runtime.IAdapterFactory;

//import org.eclipse.cdt.core.model.ICElement;
//import org.eclipse.cdt.core.model.CCore;

public class PluginAdapterFactory implements IAdapterFactory {

	private static Class[] PROPERTIES= new Class[] {
		ICPlugin.class
	};
	
	//private static CCore fgCElementFactory= CCore.getDefault();

	/**
	 * @see IAdapterFactory#getAdapterList
	 */	
	public Class[] getAdapterList() {
		return PROPERTIES;
	}
	
	/**
	 * @see IAdapterFactory#getAdapter
	 */
	public Object getAdapter(Object element, Class key) {
		if (ICPlugin.class.equals(key)) {
			if (element instanceof CCorePlugin) {
				return CPlugin.getDefault();
			}
		}
		return null;
	}	
}
