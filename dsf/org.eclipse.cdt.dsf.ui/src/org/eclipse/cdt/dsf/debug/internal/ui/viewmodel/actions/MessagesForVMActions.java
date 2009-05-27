/*******************************************************************************
 *  Copyright (c) 2006, 2009 IBM Corporation and others.
 *  All rights reserved. This program and the accompanying materials
 *  are made available under the terms of the Eclipse Public License v1.0
 *  which accompanies this distribution, and is available at
 *  http://www.eclipse.org/legal/epl-v10.html
 * 
 *  Contributors:
 *     Wind River Systems, Inc. - initial API and implementation
 *******************************************************************************/

package org.eclipse.cdt.dsf.debug.internal.ui.viewmodel.actions;

import org.eclipse.osgi.util.NLS;

public class MessagesForVMActions extends NLS {
	
    private static final String BUNDLE_NAME = "org.eclipse.cdt.dsf.debug.internal.ui.viewmodel.actions.messages"; //$NON-NLS-1$

    public static String RetargetDebugContextAction_ErrorDialog_title;
    public static String RetargetDebugContextAction_ErrorDialog_message;
    
    public static String UpdatePoliciesContribution_EmptyPoliciesList_label;
    public static String UpdateScopesContribution_EmptyScopesList_label;

    static {
        // initialize resource bundle
        NLS.initializeMessages(BUNDLE_NAME, MessagesForVMActions.class);
    }

    private MessagesForVMActions() {}
}
