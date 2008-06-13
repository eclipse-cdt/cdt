/********************************************************************************
 * Copyright (c) 2008 IBM Corporation. All rights reserved.
 * This program and the accompanying materials are made available under the terms
 * of the Eclipse Public License v1.0 which accompanies this distribution, and is 
 * available at http://www.eclipse.org/legal/epl-v10.html
 * 
 * Initial Contributors:
 * The following IBM employees contributed to the Remote System Explorer
 * component that contains this file: David McKnight.
 * 
 * Contributors:
 * David McKnight    (IBM)     - [236505] Remote systems dialog not working
 ********************************************************************************/
package org.eclipse.rse.internal.ui;

import java.util.Hashtable;
import java.util.Iterator;
import java.util.Map;

import org.eclipse.swt.graphics.Image;

public class RSEImageMap {

    private static Map _imageTable = new Hashtable(100);
      
    public static Image get(Object key) {
    	if (_imageTable != null){
    		return (Image)_imageTable.get(key);
    	}
    	return null;
    }
    
    public static void put(Object key, Image image) {
    	if (_imageTable == null){
    		_imageTable = new Hashtable(100);
    	}
 	
    	_imageTable.put(key, image);
    }
    
    public static final void shutdown() {
        if (_imageTable != null) {
            for (Iterator i = _imageTable.values().iterator(); i.hasNext();) {
                ((Image) i.next()).dispose();
            }
            _imageTable = null;
        }
    }
}
