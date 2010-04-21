package org.eclipse.cdt.codan.core.model.cfg;


/**
 * Start node of the control flow graph. Each graph has only one start node. It
 * has no incoming arcs and one outgoing arc. It also contains iterator for
 * function exit nodes.
 */
public interface IStartNode extends IBasicBlock, ISingleOutgoing {
}
