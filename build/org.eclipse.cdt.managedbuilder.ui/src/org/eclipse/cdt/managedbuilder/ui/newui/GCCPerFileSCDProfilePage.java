/*******************************************************************************
 * Copyright (c) 2004, 2007 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * IBM - Initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.managedbuilder.ui.newui;

import org.eclipse.swt.widgets.Composite;

/**
 * SCD per project profile property/preference page
 * 
 * @author vhirsl
 */
public class GCCPerFileSCDProfilePage extends AbstractDiscoveryPage {
    private static final String providerId = "makefileGenerator";  //$NON-NLS-1$

    public void createSpecific(Composite parent) {}
    /* (non-Javadoc)
     * @see org.eclipse.cdt.make.ui.dialogs.AbstractDiscoveryPage#isValid()
     */
    protected boolean isValid() { return true; }

    public void initializeValues() {
        bopEnabledButton.setSelection(getContainer().getBuildInfo().isBuildOutputParserEnabled());
        setBopOpenFileText(getContainer().getBuildInfo().getBuildOutputFilePath());
    }

    protected void handlebopEnabledButtonPress() {
        getContainer().getBuildInfo().setBuildOutputParserEnabled(bopEnabledButton.getSelection());
        getContainer().getBuildInfo().setProviderOutputParserEnabled(providerId, bopEnabledButton.getSelection());
    }

}
