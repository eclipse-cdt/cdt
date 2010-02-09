/*******************************************************************************
 * Copyright (c) 2010 Wind River Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Wind River Systems - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.dsf.gdb.service.macos;

import org.eclipse.cdt.dsf.concurrent.DataRequestMonitor;
import org.eclipse.cdt.dsf.mi.service.MIVariableManager;
import org.eclipse.cdt.dsf.mi.service.command.commands.macos.MacOSMIVarUpdate;
import org.eclipse.cdt.dsf.mi.service.command.output.MIVarChange;
import org.eclipse.cdt.dsf.mi.service.command.output.macos.MacOSMIVarUpdateInfo;
import org.eclipse.cdt.dsf.service.DsfServicesTracker;
import org.eclipse.cdt.dsf.service.DsfSession;

/**
 * Specific VariableManager for MacOS
 *   
 * @since 3.0
 */
public class MacOSGDBVariableManager extends MIVariableManager {

    public MacOSGDBVariableManager(DsfSession session, DsfServicesTracker tracker) {
        super(session, tracker);
    }

    @Override
    protected MIRootVariableObject createRootVariableObject(VariableObjectId id) {
    	return new MacOSGDBRootVariableObject(id);
    }
    
	private class MacOSGDBRootVariableObject extends MIRootVariableObject {

		public MacOSGDBRootVariableObject(VariableObjectId id) {
			super(id);
		}

		@Override
		public void update(final DataRequestMonitor<Boolean> rm) {

			if (isOutOfScope()) {
		    	rm.setData(false);
				rm.done();
			} else if (currentState != STATE_READY) {
				// Object is not fully created or is being updated
				// so add RequestMonitor to pending queue
				updatesPending.add(rm);
			} else if (getOutOfDate() == false) {
				rm.setData(false);
				rm.done();
			} else {
				// Object needs to be updated in the back-end
				currentState = STATE_UPDATING;

				// In GDB, var-update will only report a change if -var-evaluate-expression has
				// changed -- in the current format--.  This means that situations like
				// double z = 1.2;
				// z = 1.4;
				// Will not report a change if the format is anything else than natural.
				// This is because 1.2 and 1.4 are both printed as 1, 0x1, etc
				// Since we cache the values of every format, we must know if -any- format has
				// changed, not just the current one.
				// To solve this, we always do an update in the natural format; I am not aware
				// of any case where the natural format would stay the same, but another format
				// would change.  However, since a var-update update all children as well,
			    // we must make sure these children are also in the natural format
				// The simplest way to do this is that whenever we change the format
				// of a variable object, we immediately set it back to natural with a second
				// var-set-format command.  This is done in the getValue() method
				getCommandControl().queueCommand(
						new MacOSMIVarUpdate(getRootToUpdate().getControlDMContext(), getGdbName()),
						new DataRequestMonitor<MacOSMIVarUpdateInfo>(getSession().getExecutor(), rm) {
							@Override
							protected void handleCompleted() {
								currentState = STATE_READY;
								
								if (isSuccess()) {
									setOutOfDate(false);

									MIVarChange[] changes = getData().getMIVarChanges();
									if (changes.length > 0 && changes[0].isInScope() == false) {
										// Object is out-of-scope
										outOfScope = true;
										
										// We can delete this root in GDB right away.  This is safe, even
									 	// if the root has children, because they are also out-of-scope.
										// We -must- also remove this entry from our LRU.  If we don't
										// we can end-up with a race condition that create this object
										// twice, or have an infinite loop while never re-creating the object.
										// The can happen if we update a child first then we request 
										// the root later,
										getLRUCache().remove(getInternalId());

										rm.setData(true);
										rm.done();
									} else {
										// The root object is now up-to-date, we must parse the changes, if any.
										processChanges(changes);

										// We only mark this root as updated in our list if it is in-scope.
										// For out-of-scope object, we don't ever need to re-update them so
										// we don't need to add them to this list.
										rootVariableUpdated(MacOSGDBRootVariableObject.this);

										rm.setData(false);
										rm.done();
									}

									while (updatesPending.size() > 0) {
										DataRequestMonitor<Boolean> pendingRm = updatesPending.poll();
										pendingRm.setData(false);
										pendingRm.done();
									}
								} else {
									// We were not able to update for some reason
									rm.setData(false);
									rm.done();

									while (updatesPending.size() > 0) {
										DataRequestMonitor<Boolean> pendingRm = updatesPending.poll();
										pendingRm.setStatus(getStatus());
										pendingRm.done();
									}
								}
							}
						});
		    }
		}
	}
}
