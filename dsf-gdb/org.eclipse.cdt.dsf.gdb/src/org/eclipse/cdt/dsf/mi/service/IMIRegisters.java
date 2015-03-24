/*******************************************************************************
 * Copyright (c) 2015 Mentor Graphics and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Mentor Graphics - Initial API and implementation
 *******************************************************************************/

package org.eclipse.cdt.dsf.mi.service;

import org.eclipse.cdt.dsf.debug.service.IRegisters;
import org.eclipse.cdt.dsf.debug.service.IRunControl.IContainerDMContext;
import org.eclipse.cdt.dsf.debug.service.IStack.IFrameDMContext;

/**
 * @since 4.7
 */
public interface IMIRegisters extends IRegisters {

    public interface IMIRegisterGroupDMContext extends IRegisterGroupDMContext {
        public int getGroupNo();
        public String getName();
		public void setName(String groupName);
    }

    public interface IMIRegisterDMContext extends IRegisterDMContext {
        public int getRegNo();
        public String getName();
    }

    public IMIRegisterGroupDMContext createRegisterGroupDMC(IContainerDMContext contDmc, int groupNo, String groupName);

    public IMIRegisterDMContext createRegisterDMC(IMIRegisterGroupDMContext groupDmc, int regNo, String regName);

    public IMIRegisterDMContext createRegisterDMC(IMIRegisterGroupDMContext groupDmc, IFrameDMContext frameDmc, int regNo, String regName);

    public String getRootRegisterGroupName();

    public String getRootRegisterGroupDescription();
}
