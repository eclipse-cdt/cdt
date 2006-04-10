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

package org.eclipse.rse.shells.ui;
import org.eclipse.rse.core.SystemBasePlugin;
import org.eclipse.rse.core.SystemPlugin;
import org.eclipse.rse.model.IHost;
import org.eclipse.rse.model.ISubSystemFactoryCategories;
import org.eclipse.rse.model.ISystemResourceChangeEvent;
import org.eclipse.rse.model.ISystemResourceChangeEvents;
import org.eclipse.rse.subsystems.files.core.model.ISystemRemoteCommand;
import org.eclipse.rse.subsystems.shells.core.subsystems.IRemoteCmdSubSystem;
import org.eclipse.rse.ui.SystemWidgetHelpers;
import org.eclipse.rse.ui.widgets.SystemHostCombo;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;


/**
 * A composite encapsulating the GUI widgets for a command console. Used in the CommandView view
 * but can also be instantiated and used anywhere.
 */
public class SystemRemoteCommandEntryForm extends Composite 
                                          implements org.eclipse.rse.model.ISystemResourceChangeListener
{
	private SystemHostCombo sysConnCombo;
	private IRemoteCmdSubSystem[] subSystems = null;
	private int subSystemIndex = -1;
	
	private Combo cmdCombo;
	private Combo subSysCombo;
	private Button runButton;

	public SystemRemoteCommandEntryForm( Composite parent, int style )
	{
		super(parent, style);
		
		GridLayout gridLayout = new GridLayout();
		gridLayout.numColumns = 4;
		setLayout(gridLayout);
		GridData gridData = new GridData();
		gridData.horizontalAlignment = GridData.FILL;
		gridData.grabExcessHorizontalSpace = true;
	    gridData.grabExcessVerticalSpace = false;
		setLayoutData(gridData);
		
		sysConnCombo = new SystemHostCombo(this, SWT.NULL, null, false, ISubSystemFactoryCategories.SUBSYSTEM_CATEGORY_CMDS, false);
		sysConnCombo.setWidthHint(100);
		sysConnCombo.addSelectionListener( new SelectionAdapter() 
		{
			public void widgetSelected(SelectionEvent event) 
			{
				sysConnChanged();
			};
		});
		sysConnCombo.listenToConnectionEvents(true);
		
		subSysCombo = SystemWidgetHelpers.createReadonlyCombo(this, new Listener() {
			public void handleEvent(Event e) {
				subSysChanged();
			}
		}, ShellResources.RESID_COMMANDSVIEW_SUBSYSCOMBO_TOOLTIP);
		((GridData)subSysCombo.getLayoutData()).widthHint = 100;
		
		cmdCombo = SystemWidgetHelpers.createCombo(this, new Listener() {
			public void handleEvent(Event e) {
				commandChanged();
			}
		}, ShellResources.RESID_COMMANDSVIEW_CMDCOMBO_TOOLTIP);
		cmdCombo.addModifyListener(new ModifyListener() {
				public void modifyText(ModifyEvent e) {
					checkRunButtonEnablement();
				}
			}
		);		
		((GridData)cmdCombo.getLayoutData()).widthHint = 250;
		cmdCombo.addSelectionListener(new SelectionAdapter() {
			public void widgetDefaultSelected( SelectionEvent e )
			{
				handleCmdComboEnter();
			}
		} );
		
		runButton = SystemWidgetHelpers.createPushButton(this, new Listener() {
			public void handleEvent(Event e) {
				runCommand();
			}
		}, ShellResources.RESID_COMMANDSVIEW_RUN_LABEL, ShellResources.RESID_COMMANDSVIEW_RUN_TOOLTIP);
		//((GridData)runButton.getLayoutData()).widthHint = 30;
		
		pack();
		
		populateSubSysCombo();
		populateCommandCombo();
		checkRunButtonEnablement();
		
		cmdCombo.setFocus();
		
        // ----------------------------------------
		// register with system registry for events
        // ----------------------------------------
		SystemPlugin.getTheSystemRegistry().addSystemResourceChangeListener(this);
	}
	
	private void runCommand()
	{
		String cmd = cmdCombo.getText();
		if ( cmd.length() > 0 )
		{
			IHost sysConn = sysConnCombo.getHost();
			if ( sysConn != null )
			{
				//SubSystem[] cmdSubSystems = SystemPlugin.getDefault().getSystemRegistry().getSubSystemsBySubSystemFactoryCategory(ISubSystemFactoryCategories.SUBSYSTEM_CATEGORY_CMDS, sysConn);
				IRemoteCmdSubSystem[] cmdSubSystems = RemoteCommandHelpers.getCmdSubSystems(sysConn);
				IRemoteCmdSubSystem currSubSystem = null;
				String subSystemName = subSysCombo.getText();
				for (int i = 0; i < cmdSubSystems.length && currSubSystem == null; i++)
				{
					if ( subSystemName.equals(cmdSubSystems[i].getName() ) )
						currSubSystem = cmdSubSystems[i];
				}
				
				if ( currSubSystem != null )
				{
					try
					{
						currSubSystem.runCommand(cmd, getShell(), null);
					}
					catch ( Exception e )
					{
						SystemBasePlugin.logInfo("Exception invoking command "+cmd+" on " + sysConn.getAliasName() );
					}
				}
			}
		}
	}

	/**
	 * Handle a change in the command combo
	 */
	private void commandChanged()
	{
		checkRunButtonEnablement();
	}
		

	/**
	 * Handle a change in the subsystem combo
	 */
	private void subSysChanged()
	{
		subSystemIndex = subSysCombo.getSelectionIndex();
		populateCommandCombo();		
		checkRunButtonEnablement();
	}
		
	/**
	 * Handle a change in the system connection combo
	 */
	private void sysConnChanged()
	{
		populateSubSysCombo();
		populateCommandCombo();
		checkRunButtonEnablement();
	}
		
	/** 
	 * Determine if the Run button should be enabled
	 */
	private void checkRunButtonEnablement()
	{
		if ( sysConnCombo.getText().length() > 0 && subSysCombo.getText().length() > 0 && cmdCombo.getText().trim().length() > 0 )
			runButton.setEnabled(true);
		else
			runButton.setEnabled(false);
	}

	/**
	 * Populate the list command subsystems for the selected connection
	 */
	private void populateSubSysCombo()
	{
		subSysCombo.removeAll();
		IHost sysConn = sysConnCombo.getHost();
		if ( sysConn != null )
		{
			//subSystems = SystemPlugin.getDefault().getSystemRegistry().getSubSystemsBySubSystemFactoryCategory(ISubSystemFactoryCategories.SUBSYSTEM_CATEGORY_CMDS, sysConn);
			subSystems = RemoteCommandHelpers.getCmdSubSystems(sysConn);
			for (int i = 0; i < subSystems.length; i++)
			{
				if ( i == 0 )
					subSystemIndex = 0;					
				subSysCombo.add(subSystems[i].getName());
			}
			subSysCombo.select(0);
		}
	}
	
	/**
	 * Populate the command combo with the history of executed commands on that subsystemlist command subsystems for the selected connection
	 */
	private void populateCommandCombo()
	{
		cmdCombo.removeAll();
		if ( subSystemIndex >= 0 )
		{
			String[] cmds = subSystems[subSystemIndex].getExecutedCommands();
			if ( cmds != null )
				cmdCombo.setItems( cmds );
		}
	}

	/**
	 * This is the method in your class that will be called when a
	 *  system resource changes.  Listen for run commands.
	 * @see ISystemResourceChangeEvent
	 */
    public void systemResourceChanged(ISystemResourceChangeEvent event)
    {
		int type = event.getType();    	   
		Object src = event.getSource();
		Object parent = event.getParent();
		switch ( type )
		{
			case ISystemResourceChangeEvents.EVENT_COMMAND_RUN:
					if ( src instanceof ISystemRemoteCommand )
					{
						if ( subSystemIndex >= 0 && subSystems[subSystemIndex] == ((ISystemRemoteCommand)src).getSubSystem() )
						{
							// use asyncExec to avoid invalid Thread exceptions
							Display.getDefault().asyncExec(new Runnable() {
									public void run() 
									{
										populateCommandCombo();
									}	
								}
							);    
						}						
					}
				break;
			case ISystemResourceChangeEvents.EVENT_COMMAND_MESSAGE:
				break;
			case ISystemResourceChangeEvents.EVENT_COMMAND_HISTORY_UPDATE:
					// use asyncExec to avoid invalid Thread exceptions
					Display.getDefault().asyncExec(new Runnable() {
							public void run() 
							{
								populateCommandCombo();
							}	
						}
					);    
				break;
		}    	
    }

	private void handleCmdComboEnter()
	{
		if ( runButton.isEnabled() )
			runCommand();
	}
}