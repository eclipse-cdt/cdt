package org.eclipse.cdt.codan.core.model.cfg;

/**
 * Node with one incoming arc
 * 
 * @noextend This interface is not intended to be extended by clients.
 */
public interface ISingleIncoming {
	/**
	 * @return single incoming node
	 */
	IBasicBlock getIncoming();
}
