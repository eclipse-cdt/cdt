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

package org.eclipse.rse.ui;

import java.util.Vector;

import org.eclipse.rse.services.clientserver.messages.SystemMessage;
import org.eclipse.rse.ui.messages.ISystemMessageLine;
import org.eclipse.rse.ui.propertypages.ISystemConnectionWizardErrorUpdater;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Shell;



/**
 * A reusable base form.
 * <p>
 * May be used to populate a dialog or a wizard page or properties page.
 * Often we need to support multiple ways to edit the same thing, and we need to 
 *  abstract out the client area. This base class puts some structure around these
 *  abstractions. Note we don't extend Composite. Rather the subclass will create 
 *  and return the composite in createContents(). This offers us more flexibility
 *  in how/where this is used.
 * <p>
 * For error checking, subclasses should simply call setPageComplete whenever they
 * do error checking (such as in response to an event). This will then call any
 * interested listeners who have registered via {@link #addPageCompleteListener(ISystemPageCompleteListener)}.
 * Error messages should be set via {@link #showErrorMessage(SystemMessage)}. 
 */

public abstract class SystemBaseForm 
	   implements Listener, ISystemConnectionWizardErrorUpdater //, ISystemMessages
{
	
	private ISystemMessageLine msgLine;
	private Shell shell;
	private Object inputObject, outputObject;
	private Vector pageCompleteListeners;
	private boolean complete;
	protected Vector verifyListeners;
	protected boolean alreadyNotified;
	    
	/**
	 * Constructor.
	 * @deprecated You should now use the constructor that takes a shell.
	 * @param msgLine A GUI widget capable of writing error messages to.
	 */
	public SystemBaseForm(ISystemMessageLine msgLine)
	{
		this.msgLine = msgLine;
	}
	/**
	 * Constructor.
	 * @param shell The parent shell.
	 * @param msgLine A GUI widget capable of writing error messages to.
	 */
	public SystemBaseForm(Shell shell, ISystemMessageLine msgLine)
	{
		this.msgLine = msgLine;
		this.shell = shell;
	}
		
	/**
	 * Often the message line is null at the time of instantiation, so we have to call this after
	 *  it is created.
	 */
	public void setMessageLine(ISystemMessageLine msgLine)
	{
		this.msgLine = msgLine;
	}
	/**
	 * Return the message line as set via setMessageLine
	 */
	public ISystemMessageLine getMessageLine()
	{
		return msgLine;
	}
	/**
	 * Occassionally we don't know the shell at constructor time, so we need to be able to set it later
	 */
	public void setShell(Shell shell)
	{
		this.shell = shell;
	}
	/**
	 * Return the shell as set via setShell(Shell)
	 */
	public Shell getShell()
	{
		return shell;
	}
	/**
	 * Set the input object. This is usually set to the current selection, from where
	 *   the dialog/page is launched. This matches similar inputObject support in the
	 *   RSE classes for dialogs and wizards.<br>
	 * This is usually set by the using dialog/pane, and queried by this object.
	 */
	public void setInputObject(Object inputObject)
	{
		this.inputObject = inputObject;
	}
	/**
	 * Return the input object as set by {@link #setInputObject(Object)}.
	 */
	protected Object getInputObject()
	{
		return inputObject;
	}
	/**
	 * Set the output object. This is usually set by this object, and is subsequently 
	 *   queried by the using dialog/page.
	 */
	protected void setOutputObject(Object outputObject)
	{
		this.outputObject = outputObject;
	}
	/**
	 * Return the output object as set by {@link #setOutputObject(Object)}.
	 */
	public Object getOutputObject()
	{
		return outputObject;
	}
            
	/**
	 * Default implementation to satisfy Listener interface. Does nothing.
	 */
	public void handleEvent(Event evt) {}
	
	/**
	 * Register an interest in knowing whenever {@link #setPageComplete(boolean)} is
	 *  called by subclass code.
	 */	
	public void addPageCompleteListener(ISystemPageCompleteListener l)
	{
		if (pageCompleteListeners == null)
			pageCompleteListeners = new Vector();
		pageCompleteListeners.add(l);
	}
	/**
	 * De-register a page complete listener.
	 */
	public void removePageCompleteListener(ISystemPageCompleteListener l)
	{
		if (pageCompleteListeners != null)
			pageCompleteListeners.remove(l);
	}
     
	/**
	 * The completeness of the page has changed.
	 * We direct it to the Apply button versus just the OK button
	 * @see {@link #addPageCompleteListener(ISystemPageCompleteListener)}
	 */
	protected void setPageComplete(boolean complete)
	{
		this.complete = complete;
		if (pageCompleteListeners != null)
		{
			for (int idx=0; idx<pageCompleteListeners.size(); idx++)
			{
				ISystemPageCompleteListener l = (ISystemPageCompleteListener)pageCompleteListeners.elementAt(idx);
				l.setPageComplete(complete);
			}
		}
	}
	/**
	 * Return the current completeness value, as last set by {@link #setPageComplete(boolean)}.
	 */
	public boolean isPageComplete()
	{
		return complete; 
	}

	/**
	 * CreateContents is the one method that must be overridden from the parent class.
	 * In this method, we populate an SWT container with widgets and return the container
	 *  to the caller (JFace). This is used as the contents of this page.
	 * @param parent The parent composite
	 */
	public abstract Control createContents(Composite parent);
	
	
	
	// -----------------
	// HELPER METHODS...
	// -----------------

	/**
	 * Display error message or clear error message (if passed null)
	 */
	protected void showErrorMessage(SystemMessage msg)
	{
		if (msgLine != null)
		{
		  if (msg != null)
			msgLine.setErrorMessage(msg);
		  else
			msgLine.clearErrorMessage();
		}
	}

	/**
	 * Add a separator line. This is a physically visible line.
	 */
	protected Label addSeparatorLine(Composite parent, int nbrColumns)
	{
		Label separator = new Label(parent, SWT.SEPARATOR | SWT.HORIZONTAL);	
		GridData data = new GridData();
		data.horizontalSpan = nbrColumns;
		data.horizontalAlignment = GridData.FILL;
		separator.setLayoutData(data);
		return separator;		
	}
	/**
	 * Add a spacer line
	 */
	protected Label addFillerLine(Composite parent, int nbrColumns)
	{
		Label filler = new Label(parent, SWT.LEFT);	
		GridData data = new GridData();
		data.horizontalSpan = nbrColumns;
		data.horizontalAlignment = GridData.FILL;
		filler.setLayoutData(data);	
		return filler;	
	}
	/**
	 * Add a spacer line that grows in height to absorb extra space
	 */
	protected Label addGrowableFillerLine(Composite parent, int nbrColumns)
	{
		Label filler = new Label(parent, SWT.LEFT);	
		GridData data = new GridData();
		data.horizontalSpan = nbrColumns;
		data.horizontalAlignment = GridData.FILL;
		data.verticalAlignment = GridData.FILL;
		data.grabExcessVerticalSpace = true;
		filler.setLayoutData(data);		
		return filler;	
	}
	
	/**
	 * Register an interest in knowing whenever the form is
	 * verified and error messages are updated.
	 */	
	public void addVerifyListener(ISystemVerifyListener l)
	{
		if (verifyListeners == null)
			verifyListeners = new Vector();
		verifyListeners.add(l);
	}
	
	protected void notifyVerifyListeners()
	{
		if (alreadyNotified == true) 
			return; // prevent recursion
		if (verifyListeners == null) return;
		alreadyNotified = true;
		for (int i = 0; i < verifyListeners.size(); i++)
		{
			((ISystemVerifyListener)verifyListeners.get(i)).handleVerifyComplete();
		}
		alreadyNotified = false;
	}
	
	
	/**
	 * De-register a verify listener.
	 */
	public void removePageCompleteListener(ISystemVerifyListener l)
	{
		if (verifyListeners != null)
			verifyListeners.remove(l);
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.rse.core.ui.propertypages.ISystemConnectionWizardErrorUpdatingPropertyPage#getTheErrorMessage()
	 */
	public String getTheErrorMessage() 
	{
		return null;
	}

}