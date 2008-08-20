/**
 * MsResultDynamicTerminalModDb.java
 * @author Vagisha Sharma
 * Aug 19, 2008
 * @version 1.0
 */
package org.yeastrc.ms.domain.search;

/**
 * 
 */
public interface MsResultDynamicTerminalModDb extends MsTerminalModification {

    /**
     * @return database id of the modification which appears in the peptide sequence of the result
     */
    public abstract int getModificationId();
    
    /**
     * @return database id of the result which has this modification
     */
    public abstract int getResultId();
}
