/*******************************************************************************
 * Copyright (c) 2006, 2007 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * David Dykstal (IBM) - initial contribution.
 *******************************************************************************/
package org.eclipse.rse.internal.tests.framework.scripting;

import java.net.MalformedURLException;
import java.net.URL;
import org.eclipse.core.runtime.ILog;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Plugin;
import org.eclipse.core.runtime.Status;
import org.eclipse.rse.internal.tests.framework.TestFrameworkPlugin;
import org.osgi.framework.Bundle;

/**
 * Provides a context in which a script may be run.  The steps of the script will
 * ask the context to accomplish things.  Think of a ScriptContext the platform 
 * on which Steps express themselves to the outside world.
 */ 
public abstract class ScriptContext {
 	
 	private boolean failed = false;
 	private URL home;
 	
 	/**
 	 * Create a new ScriptContext.  
 	 * @param home the location relative to which all resource names are resolved.
 	 */
 	public ScriptContext(URL home) {
 		this.home = home;
 	}
 	
 	/**
 	 * @param resourceName the resource name relative to the location specified by 
 	 * the home for this Context.
 	 * @return a new URL for this specific resource or null if no URL can be formed.
 	 */
 	public final URL getResourceURL(String resourceName) {
 		URL result = null;
 		try {
			result = new URL(home, resourceName);
		} catch (MalformedURLException e) {
			setFailing(true);
			Plugin plugin = TestFrameworkPlugin.getDefault();
			Bundle bundle = plugin.getBundle();
			String pluginId = bundle.getSymbolicName();
			ILog log = plugin.getLog();
			IStatus status = new Status(IStatus.ERROR, pluginId, IStatus.OK, "bad resource name in script", e); //$NON-NLS-1$
			log.log(status);
		}
		return result;
 	}
	
	/**
	 * @return true if the Context has received an indication to fail from the environment.
	 */
	public final boolean getFailed() {
		return failed;
	}
	
	/**
	 * Indicate that the script has failed.
	 * @param flag true if the test has failed
	 */
	public final void setFailing(boolean flag) {
		failed = flag;
	}

	/**
	 * A pause operation will stop and wait for a "continue" or "fail" indication
	 * from the environment.
	 * @param text the message to print on the environment during the pause
	 */
	public abstract void pause(String text);

	/**
	 * A show operation will resolve a name to an image and show that image
	 * in the current environment.
	 * @param imageName the name of the image to resolve and show.
	 */
	public abstract void show(String imageName);

	/**
	 * A tell operation will show a string in the environment.
	 * @param text the String to show.
	 */
	public abstract void tell(String text);
	
}
