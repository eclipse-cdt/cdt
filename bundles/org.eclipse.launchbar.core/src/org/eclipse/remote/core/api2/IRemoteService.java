package org.eclipse.remote.core.api2;

/**
 * The root interface for a service provided by a remote services provider.
 */
public interface IRemoteService {

	/**
	 * Return the remote services provider object.
	 * 
	 * @return remote services provider.
	 */
	IRemoteServices getRemoteServices();

}
