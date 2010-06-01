package org.eclipse.cdt.codan.core.model.cfg;

/**
 * Connector node has multiple incoming branches and single outgoing.
 * Incoming nodes are usually instance of {@link IJumpNode}
 * 
 * @noextend This interface is not intended to be extended by clients.
 * @noimplement This interface is not intended to be implemented by clients.
 */
public interface IConnectorNode extends IBasicBlock, ISingleOutgoing {
	/**
	 * @return true if one of the incoming arcs is backward arc
	 */
	boolean hasBackwardIncoming();
}
