package org.eclipse.cdt.codan.core.model.cfg;

/**
 * Node with one outgoing arc
 * 
 * @noextend This interface is not intended to be extended by clients.
 */
public interface ISingleOutgoing {
	/**
	 * @return outgoing node
	 */
	IBasicBlock getOutgoing();
}
