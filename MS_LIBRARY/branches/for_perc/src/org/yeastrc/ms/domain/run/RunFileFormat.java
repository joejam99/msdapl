/**
 * RunFileFormat.java
 * @author Vagisha Sharma
 * Jul 15, 2008
 * @version 1.0
 */
package org.yeastrc.ms.domain.run;

public enum RunFileFormat {
    
    MS2, CMS2, MZXML, MZDATA, MZML, UNKNOWN;
    
    public static RunFileFormat instance(String extString) {
        if(extString.startsWith("."))
            extString = extString.substring(1);
        if (extString.equalsIgnoreCase(RunFileFormat.MS2.name()))
            return RunFileFormat.MS2;
        if (extString.equalsIgnoreCase(RunFileFormat.CMS2.name()))
            return RunFileFormat.CMS2;
        else if (extString.equals(RunFileFormat.MZXML.name()))
            return RunFileFormat.MZXML;
        else if (extString.equalsIgnoreCase(RunFileFormat.MZDATA.name()))
            return RunFileFormat.MZDATA;
        else if (extString.equalsIgnoreCase(RunFileFormat.MZML.name()))
            return RunFileFormat.MZML;
        else return RunFileFormat.UNKNOWN;
    }
    
    public static boolean isSupportedFormat(String extString) {
        if(RunFileFormat.instance(extString) == RunFileFormat.UNKNOWN)
            return false;
        else
            return true;
    }
}