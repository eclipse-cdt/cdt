/*
 * Created on Mar 3, 2004
 *
 * Copyright (c) 2002,2003 QNX Software Systems Ltd.
 * 
 * Contributors: 
 * QNX Software Systems - Initial API and implementation
 ***********************************************************************/
package org.eclipse.cdt.make.internal.core.makefile;

import java.util.MissingResourceException;
import java.util.ResourceBundle;

public class MakefileMessages {

    private static final String BUNDLE_NAME = "org.eclipse.cdt.make.internal.core.makefile.MakeFileResources";//$NON-NLS-1$

    private static final ResourceBundle RESOURCE_BUNDLE = ResourceBundle
            .getBundle(BUNDLE_NAME);

    private MakefileMessages() {
    }

    public static String getString(String key) {
        try {
            return RESOURCE_BUNDLE.getString(key);
        } catch (MissingResourceException e) {
            return '!' + key + '!';
        }
    }
}