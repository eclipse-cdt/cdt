/*******************************************************************************
 * Copyright (c) 2000, 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.internal.ui.wizards;

import org.eclipse.cdt.internal.ui.ICHelpContextIds;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.ui.help.WorkbenchHelp;
import org.eclipse.ui.internal.IWorkbenchConstants;

/**     <wizard
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
 */
public class NewTypeDropDownAction extends AbstractWizardDropDownAction {

	private final static String ATT_CTYPE = "ctype";//$NON-NLS-1$
	private final static String TAG_PARAMETER = "parameter";//$NON-NLS-1$
	private final static String TAG_NAME = "name";//$NON-NLS-1$
	private final static String TAG_VALUE = "value";//$NON-NLS-1$
	
	public NewTypeDropDownAction() {
	    super();
		WorkbenchHelp.setHelp(this, ICHelpContextIds.OPEN_CLASS_WIZARD_ACTION);
	}

	/* (non-Javadoc)
     * @see org.eclipse.cdt.internal.ui.wizards.AbstractWizardDropDownAction#createWizardAction(org.eclipse.core.runtime.IConfigurationElement)
     */
    public AbstractOpenWizardAction createWizardAction(IConfigurationElement element) {
        if (isTypeWizard(element)) {
            return new OpenNewWizardAction(element);
        }
        return null;
    }

    /* (non-Javadoc)
     * @see org.eclipse.cdt.internal.ui.wizards.AbstractWizardDropDownAction#isValidWizard(org.eclipse.core.runtime.IConfigurationElement)
     */
    private static boolean isTypeWizard(IConfigurationElement element) {
		IConfigurationElement[] classElements = element.getChildren(IWorkbenchConstants.TAG_CLASS);
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
}
