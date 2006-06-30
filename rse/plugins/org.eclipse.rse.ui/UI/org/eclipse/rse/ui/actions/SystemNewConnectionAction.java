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

package org.eclipse.rse.ui.actions;
import org.eclipse.jface.viewers.ISelectionProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.wizard.IWizard;
import org.eclipse.rse.core.IRSESystemType;
import org.eclipse.rse.core.RSECorePlugin;
import org.eclipse.rse.core.subsystems.ISubSystem;
import org.eclipse.rse.filters.ISystemFilterPoolReference;
import org.eclipse.rse.filters.ISystemFilterReference;
import org.eclipse.rse.filters.ISystemFilterStringReference;
import org.eclipse.rse.model.IHost;
import org.eclipse.rse.ui.ISystemContextMenuConstants;
import org.eclipse.rse.ui.ISystemIconConstants;
import org.eclipse.rse.ui.RSEUIPlugin;
import org.eclipse.rse.ui.SystemResources;
import org.eclipse.rse.ui.wizards.RSENewConnectionWizard;
import org.eclipse.swt.widgets.Shell;


/**
 * The action that displays the New Connection wizard
 */
public class SystemNewConnectionAction extends SystemBaseWizardAction 
                                 
{
	
	private IHost currConn;
	private boolean fromPopupMenu = true;
	private ISelectionProvider sp = null;
    private String[] restrictSystemTypesTo;    
    private String defaultConnectionName, defaultHostName;
    	
	/**
	 * Constructor for SystemNewConnectionAction
	 * @param shell The parent shell to host the new wizard
	 * @param fromPopupMenu true if being launched from the remote systems explorer view directly,
	 *                      false if being launched from a dialog
	 * @param sp The selection provider that will supply the selection via getSelection, if 
	 *            fromPopupMenu is false
	 */
	public SystemNewConnectionAction(Shell shell, boolean fromPopupMenu, ISelectionProvider sp) 
	{
		this(shell, fromPopupMenu, true, sp);
	}
	/**
	 * Constructor for SystemNewConnectionAction when you don't want the icon. 
	 * @param shell The parent shell to host the new wizard
	 * @param fromPopupMenu true if being launched from the remote systems explorer view directly,
	 *                      false if being launched from a dialog
	 * @param wantIcon true if you want the icon to show beside the action, false if not
	 * @param sp The selection provider that will supply the selection via getSelection, if 
	 *            fromPopupMenu is false
	 */
	public SystemNewConnectionAction(Shell shell, boolean fromPopupMenu, boolean wantIcon, ISelectionProvider sp) 
	{
		this(shell, SystemResources.ACTION_NEWCONN_LABEL, SystemResources.ACTION_NEWCONN_TOOLTIP, fromPopupMenu, wantIcon, sp);
	}
	/**
	 * Constructor for SystemNewConnectionAction when you possibly don't want the icon, and want to 
	 * supply your own label. This is the "full" flavoured constructor!
	 * 
	 * @param shell The parent shell to host the new wizard
     * @param label The label for the action
     * @param tooltip the tooltip for the action
	 * @param fromPopupMenu true if being launched from the remote systems explorer view directly,
	 *                      false if being launched from a dialog
	 * @param wantIcon true if you want the icon to show beside the action, false if not
	 * @param sp The selection provider that will supply the selection via getSelection, if 
	 *            fromPopupMenu is false
	 */
	public SystemNewConnectionAction(Shell shell, String label, String tooltip, 
	                                 boolean fromPopupMenu, boolean wantIcon, ISelectionProvider sp) 
	{
		super(label, tooltip, 
		      wantIcon ? RSEUIPlugin.getDefault().getImageDescriptor(ISystemIconConstants.ICON_SYSTEM_NEWCONNECTION_ID) : null ,
		      shell);
		setContextMenuGroup(ISystemContextMenuConstants.GROUP_NEW);
		this.fromPopupMenu = fromPopupMenu;
		this.sp = sp;
		setHelp(RSEUIPlugin.HELPPREFIX+"actn0000");
	}

	/**
	 * The default processing for the run method calls createDialog, which
	 *  in turn calls this method to return an instance of our wizard.
	 * <p>
	 * Our default implementation is to return SystemNewConnectionWizard.
	 */
	protected IWizard createWizard()
	{
		RSENewConnectionWizard newConnWizard = new RSENewConnectionWizard();
		if (!fromPopupMenu && (sp!=null))
		{
		  setSelection(sp.getSelection());
		}		

		// newConnWizard.setCurrentlySelectedConnection(currConn);
		if (restrictSystemTypesTo != null) {
			
			IRSESystemType[] systemTypes = new IRSESystemType[restrictSystemTypesTo.length];
			for (int i = 0; i < restrictSystemTypesTo.length; i++) {
				systemTypes[i] = RSECorePlugin.getDefault().getRegistry().getSystemType(restrictSystemTypesTo[i]);
			}
			
			newConnWizard.restrictToSystemTypes(systemTypes);
		}
		
		// if (defaultHostName != null)
		  // newConnWizard.setHostName(defaultHostName);
		// if (defaultConnectionName != null)
		  // newConnWizard.setConnectionName(defaultConnectionName);
		return newConnWizard;		
	}

    /**
     * Set the current selected connection so we can better intelligently set
     *  the defaults.
     */
    public void setCurrentConnection(IHost conn)
    {
    	this.currConn = conn;
    }	
    /**
     * Call this to restrict the system type that the user is allowed to choose
     */
    public void restrictSystemType(String systemType)
    {
    	restrictSystemTypesTo = new String[1];
    	restrictSystemTypesTo[0] = systemType;
    }	
    /**
     * Call this to restrict the system types that the user is allowed to choose
     */
    public void restrictSystemTypes(String[] systemTypes)
    {
    	this.restrictSystemTypesTo = systemTypes;
    }	
	/**
	 * Preset the connection name
	 */
	public void setConnectionName(String name)
	{
		defaultConnectionName = name;
	}
	/**
	 * Preset the host name
	 */
	public void setHostName(String name)
	{
		defaultHostName = name;
	}
    
	/**
	 * Override of parent method so we can deduce currently selected connection (direct or indirect if child object selected).
	 */
	public boolean updateSelection(IStructuredSelection selection)
	{
		boolean enable = super.updateSelection(selection);
		if (enable)
		{
			Object firstSelection = getFirstSelection();
			IHost conn = null;
			if (firstSelection != null)
			{
			   if (firstSelection instanceof IHost)
			     conn = (IHost)firstSelection;
			   else if (firstSelection instanceof ISubSystem)
			     conn = ((ISubSystem)firstSelection).getHost();
			   else if (firstSelection instanceof ISystemFilterPoolReference)
			   {
			   	 ISystemFilterPoolReference sfpr = (ISystemFilterPoolReference)firstSelection;
			   	 ISubSystem ss = (ISubSystem)sfpr.getProvider();
			     conn = ss.getHost();
			   }
			   else if (firstSelection instanceof ISystemFilterReference)
			   {
			   	 ISystemFilterReference sfr = (ISystemFilterReference)firstSelection;
			   	 ISubSystem ss = (ISubSystem)sfr.getProvider();
			     conn = ss.getHost();
			   }
			   else if (firstSelection instanceof ISystemFilterStringReference)
			   {
			   	 ISystemFilterStringReference sfsr = (ISystemFilterStringReference)firstSelection;
			   	 ISubSystem ss = (ISubSystem)sfsr.getProvider();
			     conn = ss.getHost();
			   }
			}
			setCurrentConnection(conn);
		}
		return enable;
	}
}