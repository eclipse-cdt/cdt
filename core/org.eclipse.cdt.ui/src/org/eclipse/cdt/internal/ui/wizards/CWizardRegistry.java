/*******************************************************************************
 * Copyright (c) 2004 QNX Software Systems and others. All rights reserved. This
 * program and the accompanying materials are made available under the terms of
 * the Common Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors: QNX Software Systems - initial API and implementation
 ******************************************************************************/
package org.eclipse.cdt.internal.ui.wizards;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.cdt.ui.CUIPlugin;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtensionPoint;
import org.eclipse.core.runtime.Platform;
import org.eclipse.jface.action.IAction;
import org.eclipse.ui.PlatformUI;

/**
 * Convenience class for drop-in C/C++ Wizard contributions.
 */
public class CWizardRegistry {

	private final static String TAG_WIZARD = "wizard"; //$NON-NLS-1$
	private final static String ATT_CATEGORY = "category";//$NON-NLS-1$
	private final static String ATT_PROJECT = "project";//$NON-NLS-1$
	private final static String TAG_PARAMETER = "parameter";//$NON-NLS-1$
	private final static String TAG_NAME = "name";//$NON-NLS-1$
	private final static String TAG_VALUE = "value";//$NON-NLS-1$
	private final static String ATT_CTYPE = "ctype";//$NON-NLS-1$
	private final static String ATT_CFILE = "cfile";//$NON-NLS-1$
	private final static String ATT_CFOLDER = "cfolder";//$NON-NLS-1$
	private final static String ATT_CPROJECT = "cproject";//$NON-NLS-1$
	private final static String ATT_CCPROJECT = "ccproject";//$NON-NLS-1$
    private final static String TAG_CLASS = "class"; //$NON-NLS-1$
    private final static String TAG_ID = "id"; //$NON-NLS-1$
    private final static String PL_NEW = "newWizards"; //$NON-NLS-1$

	
	
	
	/**
	 * Checks if wizard supports C projects.
	 * 
	 * @param element the wizard element
	 * 
	 * @return <code>true</code> if the given wizard element applies to a C Project
	 */
	public static boolean isCProjectWizard(IConfigurationElement element) {
	    String category = element.getAttribute(ATT_CATEGORY);
	    return (category != null && category.equals(CUIPlugin.CWIZARD_CATEGORY_ID));
	}
    
	/**
	 * Checks if wizard supports C++ project.
	 * 
	 * @param element the wizard element
	 * 
	 * @return <code>true</code> if the given wizard element applies to a C++ Project
	 */
	public static boolean isCCProjectWizard(IConfigurationElement element) {
	    String category = element.getAttribute(ATT_CATEGORY);
	    return (category != null && category.equals(CUIPlugin.CCWIZARD_CATEGORY_ID));
	}

	/**
	 * Returns IDs of all C/C++ project wizards contributed to the workbench.
	 * 
	 * @return an array of wizard ids
	 */
	public static String[] getProjectWizardIDs() {
		return getWizardIDs(getProjectWizardElements());
	}

	/**
	 * Returns extension data for all the C/C++ project wizards contributed to the workbench.
	 *     <wizard
	 *         name="My C Wizard"
	 *         icon="icons/cwiz.gif"
	 *         category="org.eclipse.cdt.ui.newCWizards"
	 *         id="xx.MyCWizard"
	 *         class="org.xx.MyCWizard"
	 *         project="true">
	 *         <description>
	 *             My C Wizard
	 *         </description>
	 *      </wizard>
	 *
	 * 
	 * @return an array of IConfigurationElement
	 */
	public static IConfigurationElement[] getProjectWizardElements() {
		List elemList = new ArrayList();
	    IConfigurationElement[] elements = getAllWizardElements();
	    for (int i = 0; i < elements.length; ++i) {
			IConfigurationElement element = elements[i];
			if (isProjectWizard(element)) {
			    elemList.add(element);
            }
	    }
		return (IConfigurationElement[]) elemList.toArray(new IConfigurationElement[elemList.size()]);
	}
	
    private static boolean isProjectWizard(IConfigurationElement element) {
	    String project = element.getAttribute(ATT_PROJECT);
	    if (project != null) {
	        return Boolean.valueOf(project).booleanValue();
	    }

	    IConfigurationElement[] classElements = element.getChildren(TAG_CLASS);
		if (classElements.length > 0) {
			for (int i = 0; i < classElements.length; i++) {
				IConfigurationElement[] paramElements = classElements[i].getChildren(TAG_PARAMETER);
				for (int k = 0; k < paramElements.length; k++) {
					IConfigurationElement curr = paramElements[k];
					String name = curr.getAttribute(TAG_NAME);
					if (name != null && (name.equals(ATT_CPROJECT) || name.equals(ATT_CCPROJECT))) {
					    String value = curr.getAttribute(TAG_VALUE);
					    if (value != null)
					        return Boolean.valueOf(value).booleanValue();
					}
				}
			}
			return false;
		}
		// fall back, if no <class> element found then assume it's a project wizard
		return true;
    }
    
    public static IAction[] getProjectWizardActions() {
	    return createActions(getProjectWizardElements());
    }
    
	/**
	 * Returns IDs of all C/C++ type wizards contributed to the workbench.
	 * 
	 * @return an array of wizard ids
	 */
	public static String[] getTypeWizardIDs() {
		return getWizardIDs(getTypeWizardElements());
	}
    
	/**
	 * Returns extension data for all the C/C++ type wizards contributed to the workbench.
	 *     <wizard
	 *         name="My C Wizard"
	 *         icon="icons/cwiz.gif"
	 *         category="org.eclipse.cdt.ui.newCWizards"
	 *         id="xx.MyCWizard">
	 *         <class class="org.xx.MyCWizard">
	 *             <parameter name="ctype" value="true" />
	 *         </class> 
	 *         <description>
	 *             My C Wizard
	 *         </description>
	 *      </wizard>
	 * 
	 * @return an array of IConfigurationElement
	 */
	public static IConfigurationElement[] getTypeWizardElements() {
		List elemList = new ArrayList();
	    IConfigurationElement[] elements = getAllWizardElements();
	    for (int i = 0; i < elements.length; ++i) {
			IConfigurationElement element = elements[i];
			if (isTypeWizard(element)) {
			    elemList.add(element);
            }
	    }
		return (IConfigurationElement[]) elemList.toArray(new IConfigurationElement[elemList.size()]);
	}
	
    private static boolean isTypeWizard(IConfigurationElement element) {
		IConfigurationElement[] classElements = element.getChildren(TAG_CLASS);
		if (classElements.length > 0) {
			for (int i = 0; i < classElements.length; i++) {
				IConfigurationElement[] paramElements = classElements[i].getChildren(TAG_PARAMETER);
				for (int k = 0; k < paramElements.length; k++) {
					IConfigurationElement curr = paramElements[k];
					String name = curr.getAttribute(TAG_NAME);
					if (name != null && name.equals(ATT_CTYPE)) {
					    String value = curr.getAttribute(TAG_VALUE);
					    if (value != null)
					        return Boolean.valueOf(value).booleanValue();
					}
				}
			}
		}
		return false;
    }
	
    public static IAction[] getTypeWizardActions() {
	    return createActions(getTypeWizardElements());
    }
    
	/**
	 * Returns IDs of all C/C++ file wizards contributed to the workbench.
	 * 
	 * @return an array of wizard ids
	 */
	public static String[] getFileWizardIDs() {
		return getWizardIDs(getFileWizardElements());
	}

	/**
	 * Returns extension data for all the C/C++ file wizards contributed to the workbench.
	 *     <wizard
	 *         name="My C File Wizard"
	 *         icon="icons/cwiz.gif"
	 *         category="org.eclipse.cdt.ui.newCWizards"
	 *         id="xx.MyCWizard">
	 *         <class class="org.xx.MyCFileWizard">
	 *             <parameter name="cfile" value="true" />
	 *         </class> 
	 *         <description>
	 *             My C File Wizard
	 *         </description>
	 *      </wizard>
	 * 
	 * @return an array of IConfigurationElement
	 */
	public static IConfigurationElement[] getFileWizardElements() {
		List elemList = new ArrayList();
	    IConfigurationElement[] elements = getAllWizardElements();
	    for (int i = 0; i < elements.length; ++i) {
			IConfigurationElement element = elements[i];
			if (isFileWizard(element)) {
			    elemList.add(element);
            }
	    }
		return (IConfigurationElement[]) elemList.toArray(new IConfigurationElement[elemList.size()]);
	}
	
    private static boolean isFileWizard(IConfigurationElement element) {
		IConfigurationElement[] classElements = element.getChildren(TAG_CLASS);
		if (classElements.length > 0) {
			for (int i = 0; i < classElements.length; i++) {
				IConfigurationElement[] paramElements = classElements[i].getChildren(TAG_PARAMETER);
				for (int k = 0; k < paramElements.length; k++) {
					IConfigurationElement curr = paramElements[k];
					String name = curr.getAttribute(TAG_NAME);
					if (name != null && name.equals(ATT_CFILE)) {
					    String value = curr.getAttribute(TAG_VALUE);
					    if (value != null)
					        return Boolean.valueOf(value).booleanValue();
					}
				}
			}
		}
		return false;
    }
    
    public static IAction[] getFolderWizardActions() {
	    return createActions(getFolderWizardElements());
    }

	/**
	 * Returns IDs of all C/C++ folder wizards contributed to the workbench.
	 * 
	 * @return an array of wizard ids
	 */
	public static String[] getFolderWizardIDs() {
		return getWizardIDs(getFolderWizardElements());
	}

	/**
	 * Returns extension data for all the C/C++ folder wizards contributed to the workbench.
	 *     <wizard
	 *         name="My C Folder Wizard"
	 *         icon="icons/cwiz.gif"
	 *         category="org.eclipse.cdt.ui.newCWizards"
	 *         id="xx.MyCWizard">
	 *         <class class="org.xx.MyCFolderWizard">
	 *             <parameter name="cfolder" value="true" />
	 *         </class> 
	 *         <description>
	 *             My C Folder Wizard
	 *         </description>
	 *      </wizard>
	 * 
	 * @return an array of IConfigurationElement
	 */
	public static IConfigurationElement[] getFolderWizardElements() {
		List elemList = new ArrayList();
	    IConfigurationElement[] elements = getAllWizardElements();
	    for (int i = 0; i < elements.length; ++i) {
			IConfigurationElement element = elements[i];
			if (isFolderWizard(element)) {
			    elemList.add(element);
            }
	    }
		return (IConfigurationElement[]) elemList.toArray(new IConfigurationElement[elemList.size()]);
	}
	
    private static boolean isFolderWizard(IConfigurationElement element) {
		IConfigurationElement[] classElements = element.getChildren(TAG_CLASS);
		if (classElements.length > 0) {
			for (int i = 0; i < classElements.length; i++) {
				IConfigurationElement[] paramElements = classElements[i].getChildren(TAG_PARAMETER);
				for (int k = 0; k < paramElements.length; k++) {
					IConfigurationElement curr = paramElements[k];
					String name = curr.getAttribute(TAG_NAME);
					if (name != null && name.equals(ATT_CFOLDER)) {
					    String value = curr.getAttribute(TAG_VALUE);
					    if (value != null)
					        return Boolean.valueOf(value).booleanValue();
					}
				}
			}
		}
		return false;
    }
    
    public static IAction[] getFileWizardActions() {
	    return createActions(getFileWizardElements());
    }
    
	private static String[] getWizardIDs(IConfigurationElement[] elements) {
	    List idList = new ArrayList();

	    // add C wizards first
	    for (int i = 0; i < elements.length; ++i) {
			IConfigurationElement element= elements[i];
			if (isCProjectWizard(element)) {
	            String id = element.getAttribute(TAG_ID);
	            if (id != null && !idList.contains(id)) {
	            	idList.add(id);
	            }
			}
	    }
	    // now add C++ wizards
	    for (int i = 0; i < elements.length; ++i) {
			IConfigurationElement element= elements[i];
			if (isCCProjectWizard(element)) {
	            String id = element.getAttribute(TAG_ID);
	            if (id != null && !idList.contains(id)) {
	            	idList.add(id);
	            }
			}
	    }
	    
		return (String[]) idList.toArray(new String[idList.size()]);
	}
    
    private static IAction[] createActions(IConfigurationElement[] elements) {
	    List idList = new ArrayList();
	    List actionList = new ArrayList();

	    // add C wizards first
	    for (int i = 0; i < elements.length; ++i) {
			IConfigurationElement element = elements[i];
			if (isCProjectWizard(element)) {
	            String id = element.getAttribute(TAG_ID);
	            if (id != null && !idList.contains(id)) {
	            	idList.add(id);
	    	        IAction action = new OpenNewWizardAction(element);
	    	        if (action != null) {
	    	        	actionList.add(action);
	    	        }
	            }
			}
	    }
	    // now add C++ wizards
	    for (int i = 0; i < elements.length; ++i) {
			IConfigurationElement element = elements[i];
			if (isCCProjectWizard(element)) {
	            String id = element.getAttribute(TAG_ID);
	            if (id != null && !idList.contains(id)) {
	            	idList.add(id);
	    	        IAction action = new OpenNewWizardAction(element);
	    	        if (action != null) {
	    	        	actionList.add(action);
	    	        }
	            }
			}
	    }
	    
		return (IAction[]) actionList.toArray(new IAction[actionList.size()]);
    }

    /**
	 * Returns extension data for all the C/C++ wizards contributed to the workbench.
	 *     <wizard
	 *         name="My C Wizard"
	 *         icon="icons/cwiz.gif"
	 *         category="org.eclipse.cdt.ui.newCWizards"
	 *         id="xx.MyCWizard">
	 *         <description>
	 *             My C Wizard
	 *         </description>
	 *      </wizard>
	 * 
	 * @return an array of IConfigurationElement
	 */
	public static IConfigurationElement[] getAllWizardElements() {
		List elemList = new ArrayList();
		IExtensionPoint extensionPoint = Platform.getExtensionRegistry().getExtensionPoint(PlatformUI.PLUGIN_ID, PL_NEW);
		if (extensionPoint != null) {
			IConfigurationElement[] elements = extensionPoint.getConfigurationElements();
			for (int i = 0; i < elements.length; i++) {
				IConfigurationElement element= elements[i];
				if (element.getName().equals(TAG_WIZARD)) {
				    String category = element.getAttribute(ATT_CATEGORY);
				    if (category != null &&
				        (category.equals(CUIPlugin.CCWIZARD_CATEGORY_ID)
				           || category.equals(CUIPlugin.CWIZARD_CATEGORY_ID))) {
			            elemList.add(element);
				    }
				}
			}
		}
		return (IConfigurationElement[]) elemList.toArray(new IConfigurationElement[elemList.size()]);
	}
	
}
