/*******************************************************************************
 * Copyright (c) 2007, 2010 Wind River Systems and others.
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
import org.eclipse.core.runtime.IStatus;
import org.eclipse.debug.internal.ui.viewers.model.provisional.ILabelUpdate;

/**
 * Class used by the PropertiesBasedLabelProvider to generate store 
 * label attributes related to a single column.  Each column info is 
 * configured with an array of attributes (there are currently four
 * types of attributes: text, image, font, and color), which are 
 * evaluated in order to generate the label.  
 * <p/>
 * Clients are not intended to extend this class.
 * 
 * @see PropertiesBasedLabelProvider
 * 
 * @since 1.0
 */
@ThreadSafe
public class LabelColumnInfo  {
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
     * Creates the column info object with given array of attributes.
     * @param attributeInfos Attributes for the label.
     */
    public LabelColumnInfo(LabelAttribute[] attributes)
    {
        fLabelAttributes = attributes;

        List<String> names = new LinkedList<String>();
        for (LabelAttribute attr : attributes) {
            for (String name : attr.getPropertyNames()) {
                names.add(name);
            }
        }

        fPropertyNames = names.toArray(new String[names.size()]);
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
     * Returns the list of configured label attributes for this column.
     * 
     * @since 2.1
     */
    protected void setLabelAttributes(LabelAttribute[] attributes) { 
    	fLabelAttributes = attributes;

        List<String> names = new LinkedList<String>();
        for (LabelAttribute attr : attributes) {
            for (String name : attr.getPropertyNames()) {
                names.add(name);
            }
        }

        fPropertyNames = names.toArray(new String[names.size()]);
    }    

    /**
     * Inserts an attribute in front of all the other existing attributes.
     * 
     * @since 2.1
     */
    public void insertAttribute(LabelAttribute attribute) {
    	LabelAttribute[] newAttributeList = new LabelAttribute[fLabelAttributes.length+1];
    	
    	for ( int idx = 0 ; idx < fLabelAttributes.length; idx ++ ) {
    		newAttributeList[ idx + 1 ] = fLabelAttributes[ idx ];
    	}
    	
    	newAttributeList[ 0 ] = attribute;
    	
    	setLabelAttributes( newAttributeList );
    }
    
    /**
     * Updates the label parameters for this column based on the provided
     * properties.  The label information is written to the givne label
     * update under the given column index.   
     * 
     * @param update Update to write to.
     * @param columnIndex Column to write label information under.
     * @param status Result of the properties update
     * @param properties Map of properties to use to generate the label.
     * 
     * @since 2.0
     */
    public void updateColumn(ILabelUpdate update, int columnIndex, IStatus status, Map<String,Object> properties) {
        boolean textSet = false;
        boolean imageSet = false;
        boolean fontSet = false;
        boolean foregroundSet = false;
        boolean backgroundSet = false;
        
        LabelAttribute[] labelAttributes = getLabelAttributes();
        for (LabelAttribute info : labelAttributes) {
            
            if (!(info instanceof LabelText && textSet) &&
                !(info instanceof LabelImage && imageSet) &&
                !(info instanceof LabelFont && fontSet) &&
                !(info instanceof LabelForeground && foregroundSet) &&
                !(info instanceof LabelBackground && backgroundSet) &&
                info.isEnabled(status, properties))
            {
                info.updateAttribute(update, columnIndex, status, properties);
                textSet = textSet || info instanceof LabelText;
                imageSet = imageSet || info instanceof LabelImage;
                fontSet = fontSet || info instanceof LabelFont;
                foregroundSet = foregroundSet || info instanceof LabelForeground;
                backgroundSet = backgroundSet || info instanceof LabelBackground;
            }
        }
    }
}