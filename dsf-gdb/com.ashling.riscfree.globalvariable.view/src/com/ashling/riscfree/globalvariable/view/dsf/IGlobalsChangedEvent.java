/**
 *
 */
package com.ashling.riscfree.globalvariable.view.dsf;

import org.eclipse.cdt.dsf.datamodel.IDMEvent;
import org.eclipse.cdt.dsf.debug.service.IRunControl.IExecutionDMContext;

/**
 * @author vinod.appu
 *
 *@implNote Event represents global variables added/removed
 */
public interface IGlobalsChangedEvent extends IDMEvent<IExecutionDMContext> {

}
