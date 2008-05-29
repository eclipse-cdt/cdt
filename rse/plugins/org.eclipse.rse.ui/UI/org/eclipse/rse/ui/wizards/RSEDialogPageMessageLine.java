/*******************************************************************************
 * Copyright (c) 2007, 2008 Wind River Systems, Inc. and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Eclipse Public License v1.0 
 * which accompanies this distribution, and is available at 
 * http://www.eclipse.org/legal/epl-v10.html 
 * 
 * Contributors: 
 * Uwe Stieber (Wind River) - initial API and implementation.
 *******************************************************************************/
package org.eclipse.rse.ui.wizards;

import org.eclipse.jface.dialogs.DialogPage;
import org.eclipse.jface.dialogs.IMessageProvider;
import org.eclipse.rse.services.clientserver.messages.SystemMessage;
import org.eclipse.rse.ui.messages.ISystemMessageLine;

/**
 * Message line interface implementation which forwards the calls
 * to the associated parent dialog page.
 * 
 * @noextend This class is not intended to be subclassed by clients.
 */
public class RSEDialogPageMessageLine implements ISystemMessageLine {
	private final DialogPage page;
	private SystemMessage errorSystemMessage;
	
	/**
	 * Constructor.
	 * 
	 * @param dialogPage The parent dialog page. Must be not <code>null</code>.
	 */
	public RSEDialogPageMessageLine(DialogPage dialogPage) {
		assert dialogPage != null;
		page = dialogPage;
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.rse.ui.messages.ISystemMessageLine#clearErrorMessage()
	 */
	public void clearErrorMessage() {
		assert page != null;
		if (page.getErrorMessage() != null) page.setErrorMessage(null);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.rse.ui.messages.ISystemMessageLine#clearMessage()
	 */
	public void clearMessage() {
		assert page != null;
		page.setMessage(null, IMessageProvider.NONE);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.rse.ui.messages.ISystemMessageLine#getErrorMessage()
	 */
	public String getErrorMessage() {
		assert page != null;
		return page.getErrorMessage();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.rse.ui.messages.ISystemMessageLine#getMessage()
	 */
	public String getMessage() {
		assert page != null;
		return page.getMessage();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.rse.ui.messages.ISystemMessageLine#getSystemErrorMessage()
	 */
	public SystemMessage getSystemErrorMessage() {
		return errorSystemMessage;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.rse.ui.messages.ISystemMessageLine#setErrorMessage(java.lang.String)
	 */
	public void setErrorMessage(String message) {
		assert page != null;
		page.setErrorMessage(message);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.rse.ui.messages.ISystemMessageLine#setErrorMessage(org.eclipse.rse.services.clientserver.messages.SystemMessage)
	 */
	public void setErrorMessage(SystemMessage message) {
		errorSystemMessage = message;
		if (errorSystemMessage != null) setErrorMessage(errorSystemMessage.getLevelOneText());
	}

	/* (non-Javadoc)
	 * @see org.eclipse.rse.ui.messages.ISystemMessageLine#setErrorMessage(java.lang.Throwable)
	 */
	public void setErrorMessage(Throwable exception) {
		if (exception != null) setErrorMessage(exception.getLocalizedMessage());
	}

	/* (non-Javadoc)
	 * @see org.eclipse.rse.ui.messages.ISystemMessageLine#setMessage(java.lang.String)
	 */
	public void setMessage(String message) {
		assert page != null;
		page.setMessage(message, IMessageProvider.INFORMATION);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.rse.ui.messages.ISystemMessageLine#setMessage(org.eclipse.rse.services.clientserver.messages.SystemMessage)
	 */
	public void setMessage(SystemMessage message) {
		if (message != null) setMessage(message.getLevelOneText());
	}

}
