package org.eclipse.cdt.core.model;

import org.eclipse.core.runtime.IAdaptable;

/**
 * Additions to the <code>ICElement</code> hierarchy provided by
 * contributed languages.
 * 
 * Contributed elements are required to be adaptable to an
 * <code>ImageDescriptor</code>.
 * 
 * @author Jeff Overbey
 * @see ICElement
 * @see IAdaptable
 */
public interface IContributedCElement extends ICElement {
}
