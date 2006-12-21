/*******************************************************************************
 * Copyright (c) 2006 Wind River Systems, Inc. and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Eclipse Public License v1.0 
 * which accompanies this distribution, and is available at 
 * http://www.eclipse.org/legal/epl-v10.html 
 * 
 * Contributors: 
 * Uwe Stieber (Wind River) - initial API and implementation
 *******************************************************************************/
package org.eclipse.rse.tests.internal;

import java.io.BufferedOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Properties;

import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Platform;
import org.eclipse.rse.tests.core.IRSETestLogCollectorDelegate;

/**
 * Default implementation of a test log collector delegate. Collects the
 * main log files like the Eclipse platforms .log and other default information.
 */
public class RSEDefaultTestLogCollectorDelegate implements IRSETestLogCollectorDelegate {
	private final List locationsToDispose = new ArrayList();
	
	/* (non-Javadoc)
	 * @see org.eclipse.rse.tests.core.IRSETestLogCollectorDelegate#dispose()
	 */
	public synchronized void dispose() {
		if (!locationsToDispose.isEmpty()) {
			Iterator iterator = locationsToDispose.iterator();
			while (iterator.hasNext()) {
				Object element = iterator.next();
				if (element instanceof IPath) {
					IPath path = (IPath)element;
					if (path.toFile().exists()) path.toFile().delete();
				}
			}
		}
		locationsToDispose.clear();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.rse.tests.core.IRSETestLogCollectorDelegate#getAbsoluteLogFileLocations()
	 */
	public synchronized IPath[] getAbsoluteLogFileLocations() {
		List locations = new ArrayList();
		locationsToDispose.clear();
		
		internalCollectEclipsePlatformLog(locations);
		internalCollectJavaSystemProperties(locations);
		
		return (IPath[])locations.toArray(new IPath[locations.size()]);
	}

	/**
	 * Lookup the Eclipse platform log (System property osgi.logfile or
	 * <workspace_root>/.metadata/.log).
	 * 
	 * @param locations The list of collected log file locations to add the found location to. Must be not <code>null</code>.
	 */
	private void internalCollectEclipsePlatformLog(final List locations) {
		assert locations != null;
		
		// Try the OSGi framework system property first.
		String osgi_logfile = System.getProperty("osgi.logfile", null); //$NON-NLS-1$
		IPath osgi_logfile_path = osgi_logfile != null ? new Path(osgi_logfile) : null;
		if (osgi_logfile_path == null || !osgi_logfile_path.toFile().canRead()) {
			// If we cannot get the log file via OSGi, fallback to the well known Eclipse
			// platform log location.
			IWorkspaceRoot root = ResourcesPlugin.getWorkspace().getRoot();
			IPath platformLog = root.getLocation().append(".metadata").append(".log"); //$NON-NLS-1$ //$NON-NLS-2$
			if (platformLog.toFile().canRead()) locations.add(platformLog);
		} else {
			// Directly use the log file path as given from the OSGi framework
			locations.add(osgi_logfile_path);
		}
	}
	
	/**
	 * Dumps the current values of all set Java system properties into
	 * a temporary file.
	 * 
	 * @param locations The list of collected log file locations to add the temp file location to. Must be not <code>null</code>.
	 */
	private void internalCollectJavaSystemProperties(final List locations) {
		// Dump the Java system properties into a temporary file.
		String tmpdir = System.getProperty("java.io.tmpdir"); //$NON-NLS-1$
		if (tmpdir != null) {
			IPath tmpdirPath = new Path(tmpdir);
			if (tmpdirPath.toFile().canWrite() && tmpdirPath.toFile().isDirectory()) {
				tmpdirPath = tmpdirPath.append("java_system_properties.txt"); //$NON-NLS-1$
				if (tmpdirPath.toFile().exists()) tmpdirPath.toFile().delete();
				
				BufferedOutputStream stream = null;
				try {
					if (tmpdirPath.toFile().createNewFile()) {
						// remember that we created a temporaryvfile (which will be deleted within the dispose() method).
						locationsToDispose.add(tmpdirPath);
						
						StringBuffer buffer = new StringBuffer();
						buffer.append("#\n"); //$NON-NLS-1$
						buffer.append("# Generated at " + DateFormat.getDateTimeInstance(DateFormat.LONG, DateFormat.LONG, Locale.getDefault()).format(new Date(System.currentTimeMillis())) + "\n"); //$NON-NLS-1$ //$NON-NLS-2$
						buffer.append("#\n\n"); //$NON-NLS-1$
						
						Properties properties = System.getProperties();
						
						// For a better overview within the resulting file, we sort
						// the property keys first.
						Enumeration names = properties.propertyNames();
						List propertyKeys = new ArrayList();
						while (names.hasMoreElements()) {
							propertyKeys.add(names.nextElement());
						}
						Collections.sort(propertyKeys);
						
						Iterator iterator = propertyKeys.iterator();
						while (iterator.hasNext()) {
							String propertyKey = (String)iterator.next();
							String propertyValue = properties.getProperty(propertyKey, ""); //$NON-NLS-1$
							buffer.append(propertyKey + "=" + propertyValue + "\n"); //$NON-NLS-1$ //$NON-NLS-2$
						}

						stream = new BufferedOutputStream(new FileOutputStream(tmpdirPath.toFile()));
						stream.write(buffer.toString().getBytes());
						
						// If we reach this point, we can add the temporary created file
						// to the returned locations.
						locations.add(tmpdirPath);
					}
				} catch (IOException e) {
					if (Platform.inDebugMode()) e.printStackTrace();
				} finally {
					 try { if (stream != null) stream.close(); } catch (IOException e) { if (Platform.inDebugMode()) e.printStackTrace(); }
				}
			}
		}
	}
}
