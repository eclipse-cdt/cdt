package org.eclipse.cdt.codan.core.model.cfg;

public interface IConnectorNode extends IBasicBlock, ISingleOutgoing {
	/** Backward connector has incoming node which comes from backward arcs */
	boolean hasBackwardIncoming();
}
