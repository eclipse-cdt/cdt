/*******************************************************************************
 * Copyright (c) 2008, 2016 Stefan Sprenger and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Stefan Sprenger
 *******************************************************************************/

package org.eclipse.cdt.dsf.mi.service.command.output;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * 
 * Analyzes if the gdb debug sources are valid files
 * or add corrupt files to pathList
 * 
 * @since 5.0
 */
public class MIFileListExecSourceFilesInfo extends MIInfo {

    private ArrayList<String> pathList;

    public MIFileListExecSourceFilesInfo(MIOutput rr) {
        super(rr);
        parse();
    }
    
    private void parse() {
        pathList = new ArrayList<String>();

        if (isDone()) {
            MIOutput out = getMIOutput();
            MIResultRecord rr = out.getMIResultRecord();
            if (rr != null) {
                MIResult[] results =  rr.getMIResults();
                for (int i = 0; i < results.length; i++) {
                    String var = results[i].getVariable();
                    if (var.equals("files")) { //$NON-NLS-1$
                        MIValue value = results[i].getMIValue();
                        if (value instanceof MIList) {
                        	
                        	System.out.println(value);
                            parseResult((MIList) value);
                        }
                    }
                }
            }
        }
    }

    /*
     * 
     * If a gdb returned file does not match the criterias absolute,exists,isFile
     * it will be added to a pathList
     * */
    private void parseResult(MIList list) {
        MIValue[] values = list.getMIValues();
        if (values != null && values.length > 0) {
            for (int i = 0; i < values.length; i++) {
                if (values[i] instanceof MITuple) {
                	String fullName = ((MITuple) values[i]).getField("fullname").toString(); //$NON-NLS-1$
                	File fileCheck = new File(fullName);
                	if(!fileCheck.isAbsolute() || !fileCheck.exists() || !fileCheck.isFile()){
                		pathList.add(fullName);
                	}
                }
            }
        }
    }
    
    public List<String> getPathList(){
    	return pathList;
    }
}
