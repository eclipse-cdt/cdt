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

package org.eclipse.rse.internal.subsystems.shells.subsystems;

import org.eclipse.rse.core.subsystems.ISubSystem;
import org.eclipse.rse.subsystems.files.core.model.ISystemRemoteCommand;
import org.eclipse.rse.subsystems.files.core.model.ISystemRemoteCommandMessage;



/**
 * A remote command with the command string, messages retrieved, and possibly additional information
 */
public class SystemRemoteCommand implements ISystemRemoteCommand {



	private String cmd = null;
	private String info = null;
	private Object object = null;
	private ISystemRemoteCommandMessage[] msgs = null;
	private ISubSystem subSys;
	
	
	public SystemRemoteCommand(String cmd, String[] msgsText)
	{
		this(cmd, msgsText, null, null);
	}	

	public SystemRemoteCommand(String cmd, String[] msgsText, ISubSystem subSys)
	{
		this(cmd, msgsText, null, subSys);
	}	
	
	public SystemRemoteCommand(String cmd, String[] msgsText, String[] msgsHelp)
	{
		this(cmd, msgsText, msgsHelp, null);
	}
	
	public SystemRemoteCommand(String cmd, String[] msgsText, String[] msgsHelp, ISubSystem subSys)
	{
		this.cmd = cmd;
		this.subSys = subSys;
		if ( msgsText != null )
		{
			this.msgs = new SystemRemoteCommandMessage[ msgsText.length ];
			for ( int i = 0; i < msgsText.length; i++)
			{
				String msgHelp = null;
				if ( msgsHelp != null && msgsHelp[i] != null )
					msgHelp = msgsHelp[i];
				this.msgs[i] = new SystemRemoteCommandMessage(msgsText[i], msgHelp );
			}
		}	
	}
	
	
	public SystemRemoteCommand(String cmd, ISystemRemoteCommandMessage[] msgs)
	{
		this(cmd, msgs, null);
	}
	
	public SystemRemoteCommand(String cmd, ISystemRemoteCommandMessage[] msgs, ISubSystem subSys)
	{
		this.cmd = cmd;
		this.msgs = msgs;
		this.subSys = subSys;
	}
	

	/**
	 * @return command string
	 */	
	public String getCommand()
	{
		return cmd;
	}
	
	/**
	 * @return any additional information that has been set for the command
	 */
	public String getInfo()
	{
		return info;
	}
	
	/**
	 * @return any additional object that has been set for the command
	 */
	public Object getObject()
	{
		return object;
	}
	
	/**
	 * @return the subsystem the command is run on
	 */
	public ISubSystem getSubSystem()
	{
		return subSys;
	}
	
	/**
	 * @return the messages retrieved by running the command
	 */
	public ISystemRemoteCommandMessage[] getMessages()
	{
		return msgs;
	}
	
	public void setSubSystem(ISubSystem sys)
	{
		subSys = sys;
	}
	
	public void setInfo(String info)
	{
		this.info = info;
	}
	
	public void setObject(Object object)
	{
		this.object = object;
	}
	
	public String toString()
	{
		StringBuffer text = new StringBuffer();
		text.append( cmd + '\n');
		for ( int j = 0; j < msgs.length; j++)
		{
			text.append( msgs[j].getMessageText() + '\n' );
			String msgHelp = msgs[j].getMessageHelp();
			if ( msgHelp != null )
				text.append( msgHelp + '\n' );
		}
		return text.toString();		
	}
	
}