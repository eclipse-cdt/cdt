/*******************************************************************************
 * Copyright (c) 2006, 2007 Wind River Systems, Inc. and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Eclipse Public License v1.0 
 * which accompanies this distribution, and is available at 
 * http://www.eclipse.org/legal/epl-v10.html 
 * 
 * Contributors: 
 * Michael Scharf (Wind River) - initial API and implementation
 * Martin Oberhuber (Wind River) - fixed copyright headers and beautified
 *******************************************************************************/
package org.eclipse.tm.internal.terminal.provisional.api;

import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.RegistryFactory;
import org.eclipse.core.runtime.Status;
import org.eclipse.tm.internal.terminal.control.impl.TerminalPlugin;

/**
 * A factory to get {@link ITerminalConnector} instances.
 * 
 * @author Michael Scharf
 *
 * <p>
 * <strong>EXPERIMENTAL</strong>. This class or interface has been added as
 * part of a work in progress. There is no guarantee that this API will
 * work or that it will remain the same. Please do not use this API without
 * consulting with the <a href="http://www.eclipse.org/dsdp/tm/">Target Management</a> team.
 * </p>
 */
public class TerminalConnectorExtension {
	/**
	 * A placeholder for the ITerminalConnector. It gets initialized when
	 * the real connector is needed. 
	 * The following methods can be called without initializing
	 * the contributed class: {@link #getId()}, {@link #getName()},
	 * {@link #getSettingsSummary()},{@link #load(ISettingsStore)},
	 * {@link #setTerminalSize(int, int)}, {@link #save(ISettingsStore)}
	 *
	 */
	static private class TerminalConnectorProxy implements ITerminalConnector {
		/**
		 * The connector
		 */
		private ITerminalConnector fConnector;
		/**
		 * The plugin contribution, needed for lazy initialization
		 * of {@link #fConnector}
		 */
		private final IConfigurationElement fConfig;
		/**
		 * If the initialization of the class specified in the extension fails,
		 * this variable contains the error
		 */
		private Exception fException;
		/**
		 * The store might be set before the real connector is initialized.
		 * This keeps the value until the connector is created.
		 */
		private ISettingsStore fStore;

		TerminalConnectorProxy(IConfigurationElement config) {
			fConfig=config;
		}
		public String getId() {
			String id = fConfig.getAttribute("id"); //$NON-NLS-1$
			if(id==null || id.length()==0)
				id=fConfig.getAttribute("class"); //$NON-NLS-1$
			return id;
		}
		public String getName() {
			String name= fConfig.getAttribute("name"); //$NON-NLS-1$
			if(name==null || name.length()==0) {
				name=getId();
			}
			return name;
		}
		private ITerminalConnector getConnector() {
			if(!isInitialized()) {
				try {
					fConnector=createConnector(fConfig);
					if(fConnector==null || !fConnector.isInstalled())
						// TODO MSA externalize
						throw new RuntimeException(getName()+ " is not properly installed!"); //$NON-NLS-1$
				} catch (Exception e) {
					fException=e;
				}
				if(fConnector!=null && fStore!=null)
					fConnector.load(fStore);
			}
			if(fException!=null)
				throw new RuntimeException(fException);
			return fConnector;
		}
		private boolean isInitialized() {
			return fConnector!=null || fException!=null;
		}
		public void connect(ITerminalControl control) {
			getConnector().connect(control);
		}
		public void disconnect() {
			getConnector().disconnect();
		}
		public OutputStream getOutputStream() {
			return getConnector().getOutputStream();
		}
		public String getSettingsSummary() {
			if(fConnector!=null)
				return getConnector().getSettingsSummary();
			else
				// TODO: see TerminalView.getSettingsSummary
				return "?"; //$NON-NLS-1$
		}
		public boolean isInstalled() {
			if(isInitialized() && fConnector==null)
				return false;
			return getConnector().isInstalled();
		}
		public boolean isLocalEcho() {
			return getConnector().isLocalEcho();
		}
		public void load(ISettingsStore store) {
			if(fConnector==null) {
				fStore=store;
			} else {
				getConnector().load(store);
			}
		}
		public ISettingsPage makeSettingsPage() {
			return getConnector().makeSettingsPage();
		}
		public void save(ISettingsStore store) {
			// no need to save the settings: it cannot have changed
			// because we are not initialized....
			if(!isInstalled())
				getConnector().save(store);
		}
		public void setTerminalSize(int newWidth, int newHeight) {
			// we assume that setTerminalSize is called also after
			// the terminal has been initialized. Else we would have to cache
			// the values....
			if(fConnector!=null) {
				fConnector.setTerminalSize(newWidth, newHeight);
			}
		}
	}
	/**
	 * @return null or a new connector created from the extension
	 */
	static private ITerminalConnector createConnector(IConfigurationElement config) throws Exception {
		try {
			Object obj=config.createExecutableExtension("class"); //$NON-NLS-1$
			if(obj instanceof ITerminalConnector) {
				ITerminalConnector conn=(ITerminalConnector) obj;
				if(conn.isInstalled())
					return conn;
			}
		} catch (Exception e) {
			log(e);
			throw e;
		}
		return null;
	}
	/**
	 * @return a new list of ITerminalConnectors. 
	 */
	public static ITerminalConnector[] getTerminalConnectors() {
		IConfigurationElement[] config=RegistryFactory.getRegistry().getConfigurationElementsFor("org.eclipse.tm.terminal.terminalConnector"); //$NON-NLS-1$
		List result=new ArrayList();
		for (int i = 0; i < config.length; i++) {
			result.add(new TerminalConnectorProxy(config[i]));
		}
		return (ITerminalConnector[]) result.toArray(new ITerminalConnector[result.size()]);
	}

	private static void log(Throwable e) {
		TerminalPlugin.getDefault().getLog().log(new Status(IStatus.ERROR, TerminalPlugin.PLUGIN_ID, IStatus.OK, e.getMessage(), e));
	}
}
