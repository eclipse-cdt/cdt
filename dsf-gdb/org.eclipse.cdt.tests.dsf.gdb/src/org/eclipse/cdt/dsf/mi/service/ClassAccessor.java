/*******************************************************************************
 * Copyright (c) 2009, 2010 Wind River Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Wind River Systems - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.dsf.mi.service;

import java.util.Map;

import org.eclipse.cdt.dsf.debug.service.IExpressions.IExpressionDMContext;
import org.eclipse.cdt.dsf.debug.service.IExpressions.IExpressionDMData;
import org.eclipse.cdt.dsf.debug.service.IExpressions.IExpressionDMData.BasicType;
import org.eclipse.cdt.dsf.debug.service.IRegisters.IRegisterDMContext;
import org.eclipse.cdt.dsf.mi.service.MIExpressions.ExpressionDMData;
import org.eclipse.cdt.dsf.mi.service.MIExpressions.MIExpressionDMC;

public class ClassAccessor {

	public static class MIExpressionDMCAccessor {
		private MIExpressionDMC miExprDmc;
		
		public MIExpressionDMCAccessor(IExpressionDMContext dmc) {
			miExprDmc = (MIExpressionDMC) dmc;
		}

        @Override
        public boolean equals(Object other) {
            return miExprDmc.equals(other);
        }
        
        @Override
        public int hashCode() {
            return miExprDmc.hashCode();
        }
        
        @Override
        public String toString() {
            return miExprDmc.toString(); 
        }
        
        public String getExpression() {
            return miExprDmc.getExpression();
        }
        
		public String getRelativeExpression() {
			return miExprDmc.getRelativeExpression();
		}
	}
	
	public static class ExpressionDMDataAccessor {
		private ExpressionDMData miExprData;
		
		public ExpressionDMDataAccessor(IExpressionDMData data) {
			miExprData = (ExpressionDMData) data;
		}

		public BasicType getBasicType() {
		    return miExprData.getBasicType();
		}
		
		public String getEncoding() {
			return miExprData.getEncoding();
		}

		public Map<String, Integer> getEnumerations() {
			return miExprData.getEnumerations();
		}

		public String getName() {
			return miExprData.getName();
		}

		public IRegisterDMContext getRegister() {
			return miExprData.getRegister();
		}

		public String getStringValue() {
			return miExprData.getStringValue();
		}

		public String getTypeId() {
			return miExprData.getTypeId();
		}

		public String getTypeName() {
			return miExprData.getTypeName();
		}

		public int getNumChildren() {
			return miExprData.getNumChildren();	
		}
		
		public boolean isEditable() {
			return miExprData.isEditable();
		}
		
		@Override
		public boolean equals(Object other) {
			return miExprData.equals(other);
		}

		@Override
		public int hashCode() {
			return miExprData.hashCode();

		}

		@Override
		public String toString() {
			return miExprData.toString();

		}
	}
}
