/*******************************************************************************
 * Copyright (c) 2010 Wind River Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Wind River Systems - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.dsf.ui.viewmodel.properties;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.eclipse.cdt.dsf.concurrent.DsfMultiStatus;
import org.eclipse.cdt.dsf.internal.ui.DsfUIPlugin;
import org.eclipse.core.runtime.IStatus;

/**
 * Status object for use with the IPropertiesUpdate.  This status class
 * allows setting a different status result for each property.  This allows
 * for better interpretation of status by the client of the update.
 * <p>
 * This status class derives from MultiStatus class such that the status 
 * objects for each property can be accessed through the standard 
 * {@link #getChildren()} method.  Also, multiple properties can reference 
 * the same status object, meaning that the number of properties returned
 * by {@link #getProperties()} may be greater than the status objects 
 * returned by <code>getChildren()</code>.
 * <p>
 * The properties status object does not have its own message, severity, 
 * error status or exception.  All these attributes are calculated from 
 * the child status objects.  If the status has more than one status child, 
 * the String returned by {@link #getMessage()} is: "Multiple errors reported".
 * 
 * @since 2.2
 */
public class PropertiesUpdateStatus extends DsfMultiStatus {

    final private Map<String,IStatus> fPropertiesStatus = new HashMap<String, IStatus>(1);
    private boolean fFirstStatusSet;
    
    public PropertiesUpdateStatus() {
        super(DsfUIPlugin.PLUGIN_ID, 0, "", null); //$NON-NLS-1$
    }
    
    /**
     * Returns set of properties that have an additional status specified.
     */
    public Set<String> getProperties() {
        return fPropertiesStatus.keySet();
    }
    
    /**
     * Returns an additional status for the given property in a property 
     * update.  Returned value may be <code>null</code> if no additional
     * status is given.
     */
    public IStatus getStatus(String property) {
        return fPropertiesStatus.get(property);
    }

    /**
     * Sets the given status for the given property.
     */
    public void setStatus(String property, IStatus status) {
        IStatus child = findEquivalentChild(status);
        if (child != null) {
            status = child;
        } else {
            add(status);
        }

        fPropertiesStatus.put(property, status);
    }

    /**
     * Sets the given status for the properties array.
     */
    public void setStatus(String[] properties, IStatus status) {
        IStatus child = findEquivalentChild(status);
        if (child != null) {
            status = child;
        } else {
            add(status);
        }

        for (String property : properties) {
            fPropertiesStatus.put(property, status);
        }
    }

    /**
     * Merges data in the new status into the base status data, and returns the 
     * resulting status. Only properties specified in the given set are merged.
     * <p>
     * The new status is considered to be more up to date than the base 
     * status and its data overrides the base status .  If the base status 
     * holds an error for a given property, which is found in the 
     * given set, and the new status does not, then the base error status is 
     * removed. 
     *
     * @param baseStatus Properties into which the new status properties will 
     * be merged. 
     * @param newStatus Properties status to merge. 
     * @param properties The properties to consider in the new status.
     * @return Resulting merged status object. 
     */
    public static PropertiesUpdateStatus mergePropertiesStatus(PropertiesUpdateStatus baseStatus, 
        PropertiesUpdateStatus newStatus, Set<String> properties) 
    {
        PropertiesUpdateStatus mergedStatus = new PropertiesUpdateStatus();
        mergedStatus.fPropertiesStatus.putAll(baseStatus.fPropertiesStatus);
        
        for (String property : properties) {
            IStatus propertyStatus = newStatus.getStatus(property);
            if (propertyStatus != null) {
                mergedStatus.fPropertiesStatus.put(property, propertyStatus);
            } else {
                mergedStatus.fPropertiesStatus.remove(property);
            }
        }
        Set<IStatus> children = new HashSet<IStatus>((baseStatus.getChildren().length + newStatus.getChildren().length) * 4/3);
        
        children.addAll(mergedStatus.fPropertiesStatus.values());
        for (IStatus child : children) {
            mergedStatus.add(child);
        }
        
        return mergedStatus;
    }
    
    /**
     * Adds the given status object as a child of this status.  If there's an
     * equivalent child status already, the new status is ignored.
     */
    @Override
    public void add(IStatus status) {
        if (findEquivalentChild(status) != null) {
            return;
        }
        
        super.add(status);

        boolean firstSet;
        synchronized(this) {
            firstSet = fFirstStatusSet;
            fFirstStatusSet = true;
        }
        
        if (!firstSet) {
            setMessage(status.getMessage());
        } else {
            setMessage(MessagesForProperties.PropertiesUpdateStatus_message);
        }        
    }
    
    /**
     * Finds a child status that is equivalent to the given status.
     */
    private IStatus findEquivalentChild(IStatus status) {
        if (getChildren().length != 0) {
            for (IStatus child : getChildren()) {
                if (areEquivalent(child, status)) {
                    return child;
                }
            }
        }
        return null;
    }    
    
    /**
     * Compares two status objects to determine if they are equivalent.
     */
    private boolean areEquivalent(IStatus s1, IStatus s2) {
        if ( (s1 == null && s2 != null) || (s1 != null && s2 == null) ) 
        {
            return false;
        }
        if (s1 == null) {
            return true;
        }
        if ( (s1.getSeverity() != s2.getSeverity()) ||
             !s1.getPlugin().equals(s2.getPlugin()) ||
             (s1.getCode() != s2.getCode()) )
        {
            return false;
        }
        if ( (s1.getException() == null && s1.getException() != null) ||
             (s1.getException() != null && s1.getException() == null) ||
             (s1.getException() != null && !s1.getException().equals(s2.getException())) )
        {
            return false;
        }
        return s1.getMessage().equals(s2.getMessage());
    };

    
    /**
     * Convenience method that returns and optionally creates a properties 
     * update status object for the given update.
     */
    public static PropertiesUpdateStatus getPropertiesStatus(IPropertiesUpdate update) {
        IStatus updateStatus = update.getStatus();
        if (updateStatus instanceof PropertiesUpdateStatus) {
            return (PropertiesUpdateStatus)updateStatus; 
        } else {
            PropertiesUpdateStatus propertiesStatus = new PropertiesUpdateStatus(); 
            if (!updateStatus.isOK()) {
                propertiesStatus.add(updateStatus);
            }
            return propertiesStatus;
        }
    }
}
