package org.eclipse.cdt.make.core.scannerconfig;

/**
 * Profile scope enum
 * 
 * @author vhirsl
 */
public class ScannerConfigScope {
    public static final ScannerConfigScope PROJECT_SCOPE = new ScannerConfigScope("project"); //$NON-NLS-1$
    public static final ScannerConfigScope FILE_SCOPE = new ScannerConfigScope("file"); //$NON-NLS-1$
    
    public String toString() {
        return scope;
    }
    
    private String scope;
    private ScannerConfigScope(String scope) {
        this.scope = scope;
    }
    /* (non-Javadoc)
     * @see java.lang.Object#equals(java.lang.Object)
     */
    public boolean equals(Object arg0) {
        if (arg0 == null) return false;
        if (arg0 == this) return true;
        if (arg0 instanceof ScannerConfigScope) return scope.equals(((ScannerConfigScope)arg0).scope);
        return false;
    }
    /* (non-Javadoc)
     * @see java.lang.Object#hashCode()
     */
    public int hashCode() {
        return scope.hashCode();
    }
}