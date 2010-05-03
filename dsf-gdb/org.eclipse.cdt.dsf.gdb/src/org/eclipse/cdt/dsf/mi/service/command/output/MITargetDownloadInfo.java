/*******************************************************************************
 * Copyright (c) 2010 CodeSourcery and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * CodeSourcery - Initial API and implementation
 *******************************************************************************/

package org.eclipse.cdt.dsf.mi.service.command.output;

/**
 * Parsing of GDB/MI "target-download"
 * 
 * Example:
 * -target-download
 * +download,{section=".text",section-size="6668",total-size="9880"}
 * +download,{section=".text",section-sent="512",section-size="6668",
 * total-sent="512",total-size="9880"}
 * +download,{section=".text",section-sent="1024",section-size="6668",
 * total-sent="1024",total-size="9880"}
 * +download,{section=".text",section-sent="1536",section-size="6668",
 * total-sent="1536",total-size="9880"}
 * +download,{section=".text",section-sent="2048",section-size="6668",
 * total-sent="2048",total-size="9880"}
 * +download,{section=".text",section-sent="2560",section-size="6668",
 * total-sent="2560",total-size="9880"}
 * +download,{section=".text",section-sent="3072",section-size="6668",
 * total-sent="3072",total-size="9880"}
 * +download,{section=".text",section-sent="3584",section-size="6668",
 * total-sent="3584",total-size="9880"}
 * +download,{section=".text",section-sent="4096",section-size="6668",
 * total-sent="4096",total-size="9880"}
 * +download,{section=".text",section-sent="4608",section-size="6668",
 * total-sent="4608",total-size="9880"}
 * +download,{section=".text",section-sent="5120",section-size="6668",
 * total-sent="5120",total-size="9880"}
 * +download,{section=".text",section-sent="5632",section-size="6668",
 * total-sent="5632",total-size="9880"}
 * +download,{section=".text",section-sent="6144",section-size="6668",
 * total-sent="6144",total-size="9880"}
 * +download,{section=".text",section-sent="6656",section-size="6668",
 * total-sent="6656",total-size="9880"}
 * +download,{section=".init",section-size="28",total-size="9880"}
 * +download,{section=".fini",section-size="28",total-size="9880"}
 * +download,{section=".data",section-size="3156",total-size="9880"}
 * +download,{section=".data",section-sent="512",section-size="3156",
 * total-sent="7236",total-size="9880"}
 * +download,{section=".data",section-sent="1024",section-size="3156",
 * total-sent="7748",total-size="9880"}
 * +download,{section=".data",section-sent="1536",section-size="3156",
 * total-sent="8260",total-size="9880"}
 * +download,{section=".data",section-sent="2048",section-size="3156",
 * total-sent="8772",total-size="9880"}
 * +download,{section=".data",section-sent="2560",section-size="3156",
 * total-sent="9284",total-size="9880"}
 * +download,{section=".data",section-sent="3072",section-size="3156",
 * total-sent="9796",total-size="9880"}
 * ^done,address="0x10004",load-size="9880",transfer-rate="6586",
 * write-rate="429"
 * 
 * @since 3.0
 */
public class MITargetDownloadInfo extends MIInfo {

    private String fAddress = ""; //$NON-NLS-1$
    private long fLoadSize = 0;
    private long fTransferRate = 0;
    private long fWriteRate = 0;
    
    public MITargetDownloadInfo( MIOutput record ) {
        super( record );
        parse();
    }

    public String getAddress() {
        return fAddress;
    }
    
    public long getLoadSize() {
        return fLoadSize;
    }
    
    public long getTransferRate() {
        return fTransferRate;
    }
    
    public long getWriteRate() {
        return fWriteRate;
    }
    
    private void parse() {
        if ( isDone() ) {
            MIOutput out = getMIOutput();
            MIResultRecord rr = out.getMIResultRecord();
            if ( rr != null ) {
                MIResult[] results = rr.getMIResults();
                for( int i = 0; i < results.length; i++ ) {
                    String var = results[i].getVariable();
                    MIValue value = results[i].getMIValue();
                    String str = ""; //$NON-NLS-1$
                    if ( value != null && value instanceof MIConst ) {
                        str = ((MIConst)value).getCString().trim();
                    }
                    if ( var.equals( "address" ) ) { //$NON-NLS-1$
                        fAddress = str;
                    }
                    else if ( var.equals( "load-size" ) ) { //$NON-NLS-1$
                        try {
                            fLoadSize = Long.parseLong( str );
                        }
                        catch( NumberFormatException e ) {
                        }
                    }
                    else if ( var.equals( "transfer-rate" ) ) { //$NON-NLS-1$
                        try {
                            fTransferRate = Long.parseLong( str );
                        }
                        catch( NumberFormatException e ) {
                        }
                    }
                    else if ( var.equals( "write-rate" ) ) { //$NON-NLS-1$
                        try {
                            fWriteRate = Long.parseLong( str );
                        }
                        catch( NumberFormatException e ) {
                        }
                    }
                }
            }
        }
    }
}
