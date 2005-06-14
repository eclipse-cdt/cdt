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
    public static final String NAME_PREFIX= PLUGIN_ID + '.';
    private static final int NAME_PREFIX_LENGTH= NAME_PREFIX.length();
    public static final String ICON_PREFIX= "indexer_view/"; //$NON-NLS-1$

    public static final String [] icon_images = {
    	"macroDecl_obj.gif",
    	"function_obj.gif",
    	"namespace_obj.gif",     
    	"function_ref_obj.gif",
    	"namespace_ref_obj.gif",    
    	"field_obj.gif",
    	"enumerator_obj.gif",
    	"method_public_obj.gif",
    	"field_ref_obj.gif",
    	"enumerator_ref_obj.gif",
    	"method_public_ref_obj.gif",
    	"typedecl_ref_obj.gif",
    	"typedef_obj.gif",
    	"class_obj.gif",      
    	"variable_obj.gif",
    	"struct_obj.gif",
    	"enum_obj.gif",
    	"union_obj.gif",
    	"derived.gif",
    	"friend.gif",
    	"include_obj.gif"
    };
      
    public static final String IMG_REF= NAME_PREFIX + "ref_obj.gif"; //$NON-NLS-1$
    public static final String IMG_TYPE_DECL= NAME_PREFIX + "typedecl_obj.gif"; //$NON-NLS-1$
    public static final String IMG_SUPER_REF= NAME_PREFIX + "super_co.gif"; //$NON-NLS-1$
    public static final String IMG_BACK= NAME_PREFIX + "ngback.gif"; //$NON-NLS-1$
    public static final String IMG_NEXT= NAME_PREFIX + "ngnext.gif"; //$NON-NLS-1$
    public static final String IMG_WARNING= NAME_PREFIX + "warning_icon.gif"; //$NON-NLS-1$
    public static final String IMG_FILTER_BUTTON= NAME_PREFIX + "filterbutton.gif"; //$NON-NLS-1$
    public static final String IMG_STATS= NAME_PREFIX + "stats.gif"; //$NON-NLS-1$
    public static final String IMG_GROUPED_ALL= NAME_PREFIX + "grouped_all.gif"; //$NON-NLS-1$
    public static final String IMG_GROUPED_DECL= NAME_PREFIX + "grouped_decl.gif"; //$NON-NLS-1$
    public static final String IMG_GROUPED_REF= NAME_PREFIX + "grouped_ref.gif"; //$NON-NLS-1$
    public static final String IMG_GROUPED_TYPE= NAME_PREFIX + "grouped_type.gif"; //$NON-NLS-1$
    public static final String IMG_SEARCH_LOCATION= NAME_PREFIX + "search_decl_obj.gif"; //$NON-NLS-1$
    public static final String IMG_SORT= NAME_PREFIX + "alphab_sort.gif"; //$NON-NLS-1$
    public static final String IMG_SORTED= NAME_PREFIX + "alphab_sorted.gif"; //$NON-NLS-1$
    public static final String IMG_FULL_NAME_DISPLAYED= NAME_PREFIX + "full_name_displayed.gif"; //$NON-NLS-1$
    public static final String IMG_DISPLAY_FULL_NAME= NAME_PREFIX + "display_full_name.gif"; //$NON-NLS-1$
    
    public static final ImageDescriptor DESC_REF= createManaged(ICON_PREFIX, IMG_REF);
    static {
    	for (int i = 0 ; i < icon_images.length; i++) {
    		createManaged(ICON_PREFIX, NAME_PREFIX + icon_images[i]);
    	}
    	createManaged(ICON_PREFIX, IMG_TYPE_DECL);
      	createManaged(ICON_PREFIX, IMG_SUPER_REF);
      	createManaged(ICON_PREFIX, IMG_WARNING);
      	createManaged(ICON_PREFIX, IMG_GROUPED_ALL);
      	createManaged(ICON_PREFIX, IMG_GROUPED_DECL);
      	createManaged(ICON_PREFIX, IMG_GROUPED_REF);
     	createManaged(ICON_PREFIX, IMG_GROUPED_TYPE);
   }

    public static final ImageDescriptor DESC_BACK= createManaged(ICON_PREFIX, IMG_BACK);
    public static final ImageDescriptor DESC_NEXT= createManaged(ICON_PREFIX, IMG_NEXT);
    public static final ImageDescriptor DESC_FILTER_BUTTON= createManaged(ICON_PREFIX, IMG_FILTER_BUTTON);
    public static final ImageDescriptor DESC_STATS= createManaged(ICON_PREFIX, IMG_STATS);
    public static final ImageDescriptor DESC_SEARCH_LOCATION= createManaged(ICON_PREFIX, IMG_SEARCH_LOCATION);
    public static final ImageDescriptor DESC_SORT= createManaged(ICON_PREFIX, IMG_SORT);
    public static final ImageDescriptor DESC_SORTED= createManaged(ICON_PREFIX, IMG_SORTED);
    public static final ImageDescriptor DESC_FULL_NAME_DISPLAYED= createManaged(ICON_PREFIX, IMG_FULL_NAME_DISPLAYED);
    public static final ImageDescriptor DESC_DISPLAY_FULL_NAME= createManaged(ICON_PREFIX, IMG_DISPLAY_FULL_NAME);
    
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
    public static Image get(int key) {
        return imageRegistry.get(NAME_PREFIX + icon_images[key]);
    }
    

}
