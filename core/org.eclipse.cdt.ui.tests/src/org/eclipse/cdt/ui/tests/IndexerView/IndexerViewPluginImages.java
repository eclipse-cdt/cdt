/**********************************************************************
 * Copyright (c) 2005 IBM Canada and others.
 * All rights reserved.   This program and the accompanying materials
 * are made available under the terms of the Common Public License v0.5
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v05.html
 * 
 * Contributors: 
 * IBM Rational Software - Initial API and implementation 
 **********************************************************************/
package org.eclipse.cdt.ui.tests.IndexerView;

import java.net.MalformedURLException;
import java.net.URL;

import org.eclipse.cdt.ui.CUIPlugin;
import org.eclipse.cdt.ui.testplugin.CTestPlugin;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.resource.ImageRegistry;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Display;

/**
 * @author dsteffle
 */
public class IndexerViewPluginImages {
    private static ImageRegistry imageRegistry = new ImageRegistry(CUIPlugin.getStandardDisplay());
    
    /**
     * Returns the standard display to be used. The method first checks, if
     * the thread calling this method has an associated display. If so, this
     * display is returned. Otherwise the method returns the default display.
     */
    public static Display getStandardDisplay() {
        Display display= Display.getCurrent();
        if (display == null) {
            display= Display.getDefault();
        }
        return display;     
    }   
    
    // Subdirectory (under the package containing this class) where 16 color images are
    private static URL fgIconBaseURL;

    static {
        try {
            fgIconBaseURL= new URL(CTestPlugin.getDefault().getBundle().getEntry("/"), "icons/" ); //$NON-NLS-1$ //$NON-NLS-2$
        } catch (MalformedURLException e) {}
    }   
    public static final String PLUGIN_ID = "org.eclipse.cdt.testplugin.CTestPlugin"; //$NON-NLS-1$
    private static final String NAME_PREFIX= PLUGIN_ID + '.';
    private static final int NAME_PREFIX_LENGTH= NAME_PREFIX.length();
    public static final String ICON_PREFIX= "indexer_view/"; //$NON-NLS-1$

    public static final String IMG_REF= NAME_PREFIX + "ref_obj.gif"; //$NON-NLS-1$
    public static final String IMG_TYPE_REF= NAME_PREFIX + "typedecl_ref_obj.gif"; //$NON-NLS-1$
    public static final String IMG_TYPE_DECL= NAME_PREFIX + "typedecl_obj.gif"; //$NON-NLS-1$
    public static final String IMG_FUNCTION_REF= NAME_PREFIX + "function_ref_obj.gif"; //$NON-NLS-1$
    public static final String IMG_FUNCTION_DECL= NAME_PREFIX + "function_obj.gif"; //$NON-NLS-1$
    public static final String IMG_CONSTRUCTOR_REF= NAME_PREFIX + "constructor_ref_obj.gif"; //$NON-NLS-1$
    public static final String IMG_CONSTRUCTOR_DECL= NAME_PREFIX + "constructor_obj.gif"; //$NON-NLS-1$
    public static final String IMG_NAMESPACE_REF= NAME_PREFIX + "namespace_ref_obj.gif"; //$NON-NLS-1$
    public static final String IMG_NAMESPACE_DECL= NAME_PREFIX + "namespace_obj.gif"; //$NON-NLS-1$
    public static final String IMG_FIELD_REF= NAME_PREFIX + "field_ref_obj.gif"; //$NON-NLS-1$
    public static final String IMG_FIELD_DECL= NAME_PREFIX + "field_obj.gif"; //$NON-NLS-1$
    public static final String IMG_ENUMTOR_REF= NAME_PREFIX + "enumerator_ref_obj.gif"; //$NON-NLS-1$
    public static final String IMG_ENUMTOR_DECL= NAME_PREFIX + "enumerator_obj.gif"; //$NON-NLS-1$
    public static final String IMG_METHOD_REF= NAME_PREFIX + "method_public_ref_obj.gif"; //$NON-NLS-1$
    public static final String IMG_METHOD_DECL= NAME_PREFIX + "method_public_obj.gif"; //$NON-NLS-1$
    public static final String IMG_MACRO_DECL= NAME_PREFIX + "macroDecl_obj.gif"; //$NON-NLS-1$
    public static final String IMG_INCLUDE_REF= NAME_PREFIX + "include_obj.gif"; //$NON-NLS-1$
    public static final String IMG_SUPER_REF= NAME_PREFIX + "super_co.gif"; //$NON-NLS-1$
    public static final String IMG_VARIABLE= NAME_PREFIX + "variable_obj.gif"; //$NON-NLS-1$
    public static final String IMG_CLASS= NAME_PREFIX + "class_obj.gif"; //$NON-NLS-1$
    public static final String IMG_ENUM= NAME_PREFIX + "enum_obj.gif"; //$NON-NLS-1$
    public static final String IMG_BACK= NAME_PREFIX + "ngback.gif"; //$NON-NLS-1$
    public static final String IMG_NEXT= NAME_PREFIX + "ngnext.gif"; //$NON-NLS-1$
    public static final String IMG_STRUCT= NAME_PREFIX + "struct_obj.gif"; //$NON-NLS-1$
    public static final String IMG_TYPEDEF= NAME_PREFIX + "typedef_obj.gif"; //$NON-NLS-1$
    public static final String IMG_UNION= NAME_PREFIX + "union_obj.gif"; //$NON-NLS-1$
    public static final String IMG_DERIVED= NAME_PREFIX + "derived.gif"; //$NON-NLS-1$
    public static final String IMG_FRIEND= NAME_PREFIX + "friend.gif"; //$NON-NLS-1$
    public static final String IMG_FWD_CLASS= NAME_PREFIX + "fwd_class.gif"; //$NON-NLS-1$
    public static final String IMG_FWD_STRUCT= NAME_PREFIX + "fwd_struct.gif"; //$NON-NLS-1$
    public static final String IMG_FWD_UNION= NAME_PREFIX + "fwd_union.gif"; //$NON-NLS-1$
    public static final String IMG_WARNING= NAME_PREFIX + "warning_icon.gif"; //$NON-NLS-1$
    public static final String IMG_FILTER_BUTTON= NAME_PREFIX + "filterbutton.gif"; //$NON-NLS-1$
    public static final String IMG_STATS= NAME_PREFIX + "stats.gif"; //$NON-NLS-1$
    public static final String IMG_GROUPED_ALL= NAME_PREFIX + "grouped_all.gif"; //$NON-NLS-1$
    public static final String IMG_GROUPED_DECL= NAME_PREFIX + "grouped_decl.gif"; //$NON-NLS-1$
    public static final String IMG_GROUPED_REF= NAME_PREFIX + "grouped_ref.gif"; //$NON-NLS-1$
    public static final String IMG_GROUPED_TYPE= NAME_PREFIX + "grouped_type.gif"; //$NON-NLS-1$
    
    public static final ImageDescriptor DESC_REF= createManaged(ICON_PREFIX, IMG_REF);
    public static final ImageDescriptor DESC_TYPE_REF= createManaged(ICON_PREFIX, IMG_TYPE_REF);
    public static final ImageDescriptor DESC_TYPE_DECL= createManaged(ICON_PREFIX, IMG_TYPE_DECL);
    public static final ImageDescriptor DESC_FUNCTION_REF= createManaged(ICON_PREFIX, IMG_FUNCTION_REF);
    public static final ImageDescriptor DESC_FUNCTION_DECL= createManaged(ICON_PREFIX, IMG_FUNCTION_DECL);
    public static final ImageDescriptor DESC_CONSTRUCTOR_REF= createManaged(ICON_PREFIX, IMG_CONSTRUCTOR_REF);
    public static final ImageDescriptor DESC_CONSTRUCTOR_DECL= createManaged(ICON_PREFIX, IMG_CONSTRUCTOR_DECL);
    public static final ImageDescriptor DESC_NAMESPACE_REF= createManaged(ICON_PREFIX, IMG_NAMESPACE_REF);
    public static final ImageDescriptor DESC_NAMESPACE_DECL= createManaged(ICON_PREFIX, IMG_NAMESPACE_DECL);
    public static final ImageDescriptor DESC_FIELD_REF= createManaged(ICON_PREFIX, IMG_FIELD_REF);
    public static final ImageDescriptor DESC_FIELD_DECL= createManaged(ICON_PREFIX, IMG_FIELD_DECL);
    public static final ImageDescriptor DESC_ENUMTOR_REF= createManaged(ICON_PREFIX, IMG_ENUMTOR_REF);
    public static final ImageDescriptor DESC_ENUMTOR_DECL= createManaged(ICON_PREFIX, IMG_ENUMTOR_DECL);
    public static final ImageDescriptor DESC_METHOD_REF= createManaged(ICON_PREFIX, IMG_METHOD_REF);
    public static final ImageDescriptor DESC_METHOD_DECL= createManaged(ICON_PREFIX, IMG_METHOD_DECL);
    public static final ImageDescriptor DESC_MACRO_DECL= createManaged(ICON_PREFIX, IMG_MACRO_DECL);
    public static final ImageDescriptor DESC_INCLUDE_REF= createManaged(ICON_PREFIX, IMG_INCLUDE_REF);
    public static final ImageDescriptor DESC_SUPER_REF= createManaged(ICON_PREFIX, IMG_SUPER_REF);
    public static final ImageDescriptor DESC_VARIABLE= createManaged(ICON_PREFIX, IMG_VARIABLE);
    public static final ImageDescriptor DESC_CLASS= createManaged(ICON_PREFIX, IMG_CLASS);
    public static final ImageDescriptor DESC_ENUM= createManaged(ICON_PREFIX, IMG_ENUM);
    public static final ImageDescriptor DESC_BACK= createManaged(ICON_PREFIX, IMG_BACK);
    public static final ImageDescriptor DESC_NEXT= createManaged(ICON_PREFIX, IMG_NEXT);
    public static final ImageDescriptor DESC_STRUCT= createManaged(ICON_PREFIX, IMG_STRUCT);
    public static final ImageDescriptor DESC_TYPEDEF= createManaged(ICON_PREFIX, IMG_TYPEDEF);
    public static final ImageDescriptor DESC_UNION= createManaged(ICON_PREFIX, IMG_UNION);
    public static final ImageDescriptor DESC_DERIVED= createManaged(ICON_PREFIX, IMG_DERIVED);
    public static final ImageDescriptor DESC_FRIEND= createManaged(ICON_PREFIX, IMG_FRIEND);
    public static final ImageDescriptor DESC_FWD_CLASS= createManaged(ICON_PREFIX, IMG_FWD_CLASS);
    public static final ImageDescriptor DESC_FWD_STRUCT= createManaged(ICON_PREFIX, IMG_FWD_STRUCT);
    public static final ImageDescriptor DESC_FWD_UNION= createManaged(ICON_PREFIX, IMG_FWD_UNION);
    public static final ImageDescriptor DESC_WARNING= createManaged(ICON_PREFIX, IMG_WARNING);
    public static final ImageDescriptor DESC_FILTER_BUTTON= createManaged(ICON_PREFIX, IMG_FILTER_BUTTON);
    public static final ImageDescriptor DESC_STATS= createManaged(ICON_PREFIX, IMG_STATS);
    public static final ImageDescriptor DESC_GROUPED_ALL= createManaged(ICON_PREFIX, IMG_GROUPED_ALL);
    public static final ImageDescriptor DESC_GROUPED_DECL= createManaged(ICON_PREFIX, IMG_GROUPED_DECL);
    public static final ImageDescriptor DESC_GROUPED_REF= createManaged(ICON_PREFIX, IMG_GROUPED_REF);
    public static final ImageDescriptor DESC_GROUPED_TYPE= createManaged(ICON_PREFIX, IMG_GROUPED_TYPE);
    
    private static ImageDescriptor createManaged(String prefix, String name) {
        return createManaged(imageRegistry, prefix, name);
    }
    
    private static ImageDescriptor createManaged(ImageRegistry registry, String prefix, String name) {
        ImageDescriptor result= ImageDescriptor.createFromURL(makeIconFileURL(prefix, name.substring(NAME_PREFIX_LENGTH)));
        registry.put(name, result);
        return result;
    }
    
    private static URL makeIconFileURL(String prefix, String name) {
        StringBuffer buffer= new StringBuffer(prefix);
        buffer.append(name);
        try {
            return new URL(fgIconBaseURL, buffer.toString());
        } catch (MalformedURLException e) {
            CUIPlugin.getDefault().log(e);
            return null;
        }
    }
    
    public static Image get(String key) {
        return imageRegistry.get(key);
    }
    

}
