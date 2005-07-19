/*******************************************************************************
 * Copyright (c) 2005 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     QNX Software Systems - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.debug.testplugin;

import java.io.File;
import java.io.IOException;

import org.eclipse.cdt.core.model.CModelException;
import org.eclipse.cdt.core.model.IBinary;
import org.eclipse.cdt.core.model.ICProject;
import org.eclipse.cdt.debug.core.cdi.ICDISession;
import org.eclipse.cdt.debug.mi.core.MIException;
import org.eclipse.cdt.debug.mi.core.MIPlugin;
import org.eclipse.cdt.debug.mi.core.command.MIVersion;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Platform;


/**
 * Helper methods to set up a Debug session.
 */
public class CDebugHelper {
	
	

	/**
	 * Creates a ICDISession.
	 */	
	public static ICDISession createSession(String exe) throws IOException, MIException  {
		MIPlugin mi;
        ICDISession session;
        String os = System.getProperty("os.name");
        String exename;
        mi=MIPlugin.getDefault();
        
        exename=org.eclipse.core.runtime.Platform.getPlugin("org.eclipse.cdt.debug.ui.tests").find(new Path("/")).getFile();
        exename+="core/org/eclipse/cdt/debug/core/tests/resources/";
        os=os.toLowerCase();
        /* We need to get the correct executable to execute
         */
        if (os.indexOf("windows")!=-1)
            exename+="win/"+ exe +".exe";
        else if (os.indexOf("qnx")!=-1) 
            exename+="qnx/" + exe;
        else if (os.indexOf("linux")!=-1)
            exename+="linux/"+exe;
        else if (os.indexOf("sol")!=-1) 
            exename+="sol/" + exe;
        else
           return(null);
        session=mi.createCSession(null, MIVersion.MI1, new File(exename), new File("."), null, null);
		return(session);
	}
	/**
	 * Creates a ICDISession.
	 */	
	public static ICDISession createSession(String exe, ICProject project) throws IOException, MIException, CModelException  {
		MIPlugin mi;
		String  workspacePath= Platform.getLocation().toOSString();
		ICDISession session;
		mi=MIPlugin.getDefault();
		
		IBinary bins[] = project.getBinaryContainer().getBinaries();
		if (bins.length!=1) {
			//SHOULD NOT HAPPEN
			return(null);        
		}
		
		session=mi.createCSession(null, MIVersion.MI1, new File(workspacePath +bins[0].getPath().toOSString()), new File("."), null, null);
		return(session);
	}
	

}

