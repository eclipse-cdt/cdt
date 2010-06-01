package org.eclipse.cdt.codan.core.model.cfg;

/**
 * Start node of the control flow graph. Each graph has only one start node. It
 * has no incoming arcs and one outgoing arc. It also contains iterator for
 * function exit nodes.
 * 
 * @noextend This interface is not intended to be extended by clients.
 * @noimplement This interface is not intended to be implemented by clients.
 */
public interface IStartNode extends IBasicBlock, ISingleOutgoing {
}
