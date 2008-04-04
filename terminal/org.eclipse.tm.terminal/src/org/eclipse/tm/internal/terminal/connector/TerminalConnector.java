package org.eclipse.tm.internal.terminal.connector;

import java.io.OutputStream;

import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.Platform;
import org.eclipse.tm.internal.terminal.control.impl.TerminalMessages;
import org.eclipse.tm.internal.terminal.provisional.api.ISettingsPage;
import org.eclipse.tm.internal.terminal.provisional.api.ISettingsStore;
import org.eclipse.tm.internal.terminal.provisional.api.ITerminalConnector;
import org.eclipse.tm.internal.terminal.provisional.api.ITerminalControl;
import org.eclipse.tm.internal.terminal.provisional.api.Logger;
import org.eclipse.tm.internal.terminal.provisional.api.TerminalState;
import org.eclipse.tm.internal.terminal.provisional.api.provider.TerminalConnectorImpl;

/**
 * A placeholder for the ITerminalConnector. It gets initialized when
 * the real connector is needed. 
 * The following methods can be called without initializing
 * the contributed class: {@link #getId()}, {@link #getName()},
 * {@link #getSettingsSummary()},{@link #load(ISettingsStore)},
 * {@link #setTerminalSize(int, int)}, {@link #save(ISettingsStore)},
 * {@link #getAdapter(Class)}
 *
 */
public class TerminalConnector implements ITerminalConnector {
	/**
	 * Creates an instance of TerminalConnectorImpl. This is
	 * used to lazily load classed defined in extensions.
	 */
	public interface Factory {
		/**
		 * @return an Connector
		 * @throws Exception
		 */
		TerminalConnectorImpl makeConnector() throws Exception;
	}
	/**
	 * 
	 */
	private final TerminalConnector.Factory fTerminalConnectorFactory;
	/**
	 * The (display) name of the TerminalConnector
	 */
	private final String fName;
	/**
	 * The unique id the connector
	 */
	private final String fId;
	/**
	 * The connector
	 */
	private TerminalConnectorImpl fConnector;
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
	/**
	 * @param terminalConnectorFactory
	 * @param id
	 * @param name
	 */
	public TerminalConnector(TerminalConnector.Factory terminalConnectorFactory, String id, String name) {
		fTerminalConnectorFactory = terminalConnectorFactory;
		fId = id;
		fName = name;
	}
	public String getInitializationErrorMessage() {
		getConnectorImpl();
		if(fException!=null)
			return fException.getLocalizedMessage();
		return null;
	}
	public String getId() {
		return fId;
	}
	public String getName() {
		return fName;
	}
	private TerminalConnectorImpl getConnectorImpl() {
		if(!isInitialized()) {
			try {
				fConnector=fTerminalConnectorFactory.makeConnector();
				fConnector.initialize();
			} catch (Exception e) {
				fException=e;
				fConnector=new TerminalConnectorImpl(){
					public void connect(ITerminalControl control) {
						control.setState(TerminalState.CLOSED);
						control.setMsg(getInitializationErrorMessage());
					}
					public void disconnect() {
					}
					public OutputStream getOutputStream() {
						return null;
					}
					public String getSettingsSummary() {
						return null;
					}
					public void load(ISettingsStore store) {
					}
					public ISettingsPage makeSettingsPage() {
						return null;
					}
					public void save(ISettingsStore store) {
					}};
				// that's the place where we log the exception
				Logger.logException(e);
			}
			if(fConnector!=null && fStore!=null)
				fConnector.load(fStore);
		}
		return fConnector;
	}
	
	public boolean isInitialized() {
		return fConnector!=null || fException!=null;
	}
	public void connect(ITerminalControl control) {
		getConnectorImpl().connect(control);
	}
	public void disconnect() {
		getConnectorImpl().disconnect();
	}
	public OutputStream getTerminalToRemoteStream() {
		return getConnectorImpl().getOutputStream();
	}
	public String getSettingsSummary() {
		if(fConnector!=null)
			return getConnectorImpl().getSettingsSummary();
		else
			return TerminalMessages.NotInitialized; 
	}
	public boolean isLocalEcho() {
		return getConnectorImpl().isLocalEcho();
	}
	public void load(ISettingsStore store) {
		if(fConnector==null) {
			fStore=store;
		} else {
			getConnectorImpl().load(store);
		}
	}
	public ISettingsPage makeSettingsPage() {
		return getConnectorImpl().makeSettingsPage();
	}
	public void save(ISettingsStore store) {
		// no need to save the settings: it cannot have changed
		// because we are not initialized....
		if(fConnector!=null)
			getConnectorImpl().save(store);
	}
	public void setTerminalSize(int newWidth, int newHeight) {
		// we assume that setTerminalSize is called also after
		// the terminal has been initialized. Else we would have to cache
		// the values....
		if(fConnector!=null) {
			fConnector.setTerminalSize(newWidth, newHeight);
		}
	}
	public Object getAdapter(Class adapter) {
		TerminalConnectorImpl connector=null;
		if(isInitialized())
			connector=getConnectorImpl();
		// if we cannot create the connector then we cannot adapt...
		if(connector!=null) {
			// maybe the connector is adaptable
			if(connector instanceof IAdaptable) {
				Object result =((IAdaptable)connector).getAdapter(adapter);
				// Not sure if the next block is needed....
				if(result==null)
					//defer to the platform
					result= Platform.getAdapterManager().getAdapter(connector, adapter);
				if(result!=null)
					return result;
			}
			// maybe the real adapter is what we need....
			if(adapter.isInstance(connector))
				return connector;
		}
		// maybe we have to be adapted....
		return Platform.getAdapterManager().getAdapter(this, adapter);
	}
}