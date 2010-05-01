package org.eclipse.cdt.codan.core.model.cfg;

/**
 * Connector node has multiple incoming branches and single outgoing
 */
public interface IConnectorNode extends IBasicBlock, ISingleOutgoing {
	/** Backward connector has incoming node which comes from backward arcs */
	boolean hasBackwardIncoming();
}
