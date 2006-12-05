/********************************************************************************
 * Copyright (c) 2002, 2006 IBM Corporation. All rights reserved.
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
package org.eclipse.rse.files.ui.internal.search;

import org.eclipse.rse.files.ui.widgets.SystemSelectRemoteFileOrFolderForm;
import org.eclipse.rse.subsystems.files.core.model.ISystemFileRemoteTypes;
import org.eclipse.rse.ui.messages.ISystemMessageLine;
import org.eclipse.rse.ui.view.SystemSelectRemoteObjectAPIProviderImpl;

/**
 * The selection form to use is search selection dialogs.
 */
public class SystemSearchRemoteFolderForm extends SystemSelectRemoteFileOrFolderForm {

	/**
	 * Constructor.
	 * @param msgLine the message line.
	 * @param caller the caller of the contructor for callbacks.
	 */
	public SystemSearchRemoteFolderForm(ISystemMessageLine msgLine, Object caller) {
		super(msgLine, caller, false);
	}

	/**
	 * Returns an instance of the search input provider {@link SystemSearchRemoteObjectAPIProvider}
	 * @see org.eclipse.rse.files.ui.widgets.SystemSelectRemoteFileOrFolderForm#getInputProvider()
	 */
	protected SystemSelectRemoteObjectAPIProviderImpl getInputProvider() {
		
		if (inputProvider == null) {
		    // create the input provider that drives the contents of the tree
			inputProvider = new SystemSearchRemoteObjectAPIProvider(null, ISystemFileRemoteTypes.TYPECATEGORY, true, null); // show new connection prompt, no system type restrictions
		}
		
		return inputProvider;
	}
}
