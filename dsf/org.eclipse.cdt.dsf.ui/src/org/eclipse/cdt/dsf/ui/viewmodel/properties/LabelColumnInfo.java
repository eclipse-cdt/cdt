/*******************************************************************************
 * Copyright (c) 2007 Wind River Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Wind River Systems - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.dsf.ui.viewmodel.properties;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.eclipse.cdt.dsf.concurrent.ThreadSafe;
import org.eclipse.core.runtime.ListenerList;
import org.eclipse.debug.internal.ui.viewers.model.provisional.ILabelUpdate;

/**
 * Class used by the PropertyBasedLabelProvider to generate store 
 * label attributes related to a single column.  Each column info is 
 * configured with an array of attributes (there are currently four
 * types of attributes: text, image, font, and color), which are 
 * evaluated in order to generate the label.  
 * <p/>
 * Clients are not intended to extend this class.
 * 
 * @see PropertyBasedLabelProvider
 * 
 * @since 1.0
 */
@SuppressWarnings("restriction")
@ThreadSafe
public class LabelColumnInfo implements ILabelAttributeChangedListener {
    
    private static final LabelAttribute[] EMPTY_ATTRIBUTES_ARRAY = new LabelAttribute[0]; 
    
    /** 
     * Calculated list of property names that need to be retrieved to 
     * generate the label for this column.
     */
    private String[] fPropertyNames;
    
    /**
     * Array of label attribute objects.   
     */
    private LabelAttribute[] fLabelAttributes;
    
    /**
     * Listeners for when column attributes are modified.
     */
    private ListenerList fListeners = new ListenerList();
        
    /**
     * Creates the column info object with given array of attributes.
     * @param attributeInfos Attributes for the label.
     */
    public LabelColumnInfo(LabelAttribute[] attributes)
    {
        fLabelAttributes = attributes;

        List<String> names = new LinkedList<String>();
        for (LabelAttribute attr : attributes) {
            attr.addChangedListener(this);
            for (String name : attr.getPropertyNames()) {
                names.add(name);
            }
        }

        fPropertyNames = names.toArray(new String[names.size()]);
    }

    /**
     * Disposes this column info object and the attribute objects 
     * within it.
     */
    public void dispose() {
        for (LabelAttribute attr : fLabelAttributes) {
            attr.dispose();
            attr.removeChangedListener(this);
        } 
        fLabelAttributes = EMPTY_ATTRIBUTES_ARRAY;
        fPropertyNames = null;
    }
        
    /**
     * Returns the property names that need to be retrieved in order
     * to generate the label for this column.
     */
    public String[] getPropertyNames() { return fPropertyNames; }
    
    /**
     * Returns the list of configured label attributes for this column.
     */
    public LabelAttribute[] getLabelAttributes() { return fLabelAttributes; }

    /**
     * Registers the given listener for changes in the attributes of this 
     * column.  A change in the attributes of a label should cause
     * a view to repaint.
     * @param listener Listener to register.
     */
    public void addChangedListener(ILabelAttributeChangedListener listener) {
        fListeners.add(listener);
    }
    
    /**
     * Unregisters the given listener.
     * @param listener Listener to unregister.
     */
    public void removeChangedListener(ILabelAttributeChangedListener listener) {
        fListeners.remove(listener);
    }

    /** 
     * Listener method called by the attribute objects.
     * @see ILabelAttributeChangedListener
     */
    public void attributesChanged() {
        Object[] listeners = fListeners.getListeners();
        for (Object listener : listeners) {
            ((ILabelAttributeChangedListener)listener).attributesChanged();
        }
    }
    
    /**
     * Updates the label parameters for this column based on the provided
     * properties.  The label information is written to the givne label
     * update under the given column index.   
     * @param update Update to write to.
     * @param columnIndex Column to write label information under.
     * @param properties Map of properties to use to generate the label.
     */
    public void updateColumn(ILabelUpdate update, int columnIndex, Map<String,Object> properties) {
        boolean textSet = false;
        boolean imageSet = false;
        boolean fontSet = false;
        boolean colorSet = false;
        
        LabelAttribute[] labelAttributes = getLabelAttributes();
        for (LabelAttribute info : labelAttributes) {
            
            if (!(info instanceof LabelText && textSet) &&
                !(info instanceof LabelImage && imageSet) &&
                !(info instanceof LabelFont && fontSet) &&
                !(info instanceof LabelColor && colorSet) &&
                info.isEnabled(properties))
            {
                info.updateAttribute(update, columnIndex, properties);
                
                textSet = textSet || info instanceof LabelText;
                imageSet = imageSet || info instanceof LabelImage;
                fontSet = fontSet || info instanceof LabelFont;
                colorSet = colorSet || info instanceof LabelColor;
            }
        }
    }
}