/********************************************************************************
 * Copyright (c) 2006 IBM Corporation. All rights reserved.
 * This program and the accompanying materials are made available under the terms
 * of the Eclipse Public License v1.0 which accompanies this distribution, and is 
 * available at http://www.eclipse.org/legal/epl-v10.html
 * 
 * Initial Contributors:
 * The following IBM employees contributed to the Remote System Explorer
 * component that contains this file: David McKnight, Kushal Munir, 
 * Michael Berger, David Dykstal, Phil Coulthard, Don Yantzi, Eric Simpson, 
 * Emily Bruner, Mazen Faraj, Adrian Storisteanu, Li Ding, and Kent Hawley.
 * 
 * Contributors:
 * {Name} (company) - description of contribution.
 ********************************************************************************/

package org.eclipse.rse.ui.view.search;

import org.eclipse.rse.ui.actions.SystemCopyToClipboardAction;
import org.eclipse.rse.ui.view.ISystemViewElementAdapter;
import org.eclipse.swt.dnd.Clipboard;
import org.eclipse.swt.widgets.Shell;


/**
 * Action that copies objects selected in Remote Search view to clipboard.
 */
public class SystemSearchCopyToClipboardAction extends SystemCopyToClipboardAction {

    /**
     * Constructor.
     * @param shell the shell.
     * @param clipboard the system clipboard.
     */
    public SystemSearchCopyToClipboardAction(Shell shell, Clipboard clipboard) {
        super(shell, clipboard);
    }
    
    /**
     * Returns the string "\t" if the object is a remote search result, otherwise returns the super class
     * implementation.
     * @see org.eclipse.rse.ui.actions.SystemCopyToClipboardAction#getTextTransferPrepend(java.lang.Object, org.eclipse.rse.ui.view.ISystemViewElementAdapter)
     */
    protected String getTextTransferPrepend(Object obj, ISystemViewElementAdapter adapter) {
        /** shouldn't be coupled with search (files ui)
        if (adapter instanceof SystemViewRemoteSearchResultAdapter) 
        {
            return "\t";
        }
        else
        	**/
        {
            return super.getTextTransferPrepend(obj, adapter);
        }
    }
}