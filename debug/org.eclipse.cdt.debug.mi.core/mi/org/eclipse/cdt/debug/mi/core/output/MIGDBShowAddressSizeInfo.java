/*
 * Created on Jun 7, 2004
 *
 * To change the template for this generated file go to
 * Window&gt;Preferences&gt;Java&gt;Code Generation&gt;Code and Comments
 */
package org.eclipse.cdt.debug.mi.core.output;

/**
 * @author root
 *
 * To change the template for this generated type comment go to
 * Window&gt;Preferences&gt;Java&gt;Code Generation&gt;Code and Comments
 */
public class MIGDBShowAddressSizeInfo extends MIGDBShowInfo {

	public MIGDBShowAddressSizeInfo(MIOutput o) {
		super(o);
	}

	public int getAddressSize()
	{
		return Integer.parseInt(getValue()); 
	}
}
