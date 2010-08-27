/*******************************************************************************
 * Copyright (c) 2010 CodeSourcery and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Vladimir Prus (CodeSourcery) - Initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.dsf.mi.service.command.output;

import java.util.ArrayList;
import java.util.List;

/** 
 * Example output is:
 * 
 *     (gdb) -list-features
 *     ^done,result=["feature1","feature2"]
 * @since 4.0
 */
public class MIListFeaturesInfo extends MIInfo {

	private List<String> fFeatures = new ArrayList<String>();

	public MIListFeaturesInfo(MIOutput out) {
		super(out);
		parse();
	}

	public List<String> getFeatures() {
		return fFeatures;
	}

	private void parse() {
		if (isDone()) {
			MIOutput out = getMIOutput();
			MIResultRecord rr = out.getMIResultRecord();
			if (rr != null) {
				MIResult[] results = rr.getMIResults();
				for (int i = 0; i < results.length; i++) {
					String var = results[i].getVariable();
					if (var.equals("features")) { //$NON-NLS-1$
						MIValue val = results[i].getMIValue();
						if (val instanceof MIList) {
							MIList list = (MIList) val;
							for (int j = 0; j < list.getMIValues().length; ++j) {
								MIValue feature = list.getMIValues()[j];
								if (feature instanceof MIConst)
									fFeatures.add(((MIConst) feature).getString());
							}
						}
					}
				}
			}
		}
	}
}
