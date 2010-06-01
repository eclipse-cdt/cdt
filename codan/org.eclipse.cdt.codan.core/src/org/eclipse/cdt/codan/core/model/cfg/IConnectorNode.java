package org.eclipse.cdt.codan.core.model.cfg;

/**
 * Connector node has multiple incoming branches and single outgoing.
 * Incoming nodes are usually instance of {@link IJumpNode}
 */
public interface IConnectorNode extends IBasicBlock, ISingleOutgoing {
	/**
	 * @return true if one of the incoming arcs is backward arc
	 */
	boolean hasBackwardIncoming();
}
