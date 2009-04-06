/**
 * SORT_BY.java
 * @author Vagisha Sharma
 * Apr 6, 2009
 * @version 1.0
 */
package org.yeastrc.ms.domain.search;


public enum SORT_BY {

    ID("ID"), 
    SCAN("Scan"), 
    CHARGE("Charge"), 
    MASS("Obs. Mass"), 
    RT("RT"), 
    PEPTIDE("Peptide");

    private String displayName;

    private SORT_BY(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }

    public static SORT_BY getSortByForName(String name) {
        if(name == null)
            return null;
        else if (name.equalsIgnoreCase(ID.name())) return ID;
        else if (name.equalsIgnoreCase(SCAN.name())) return SCAN;
        else if (name.equalsIgnoreCase(CHARGE.name())) return CHARGE;
        else if (name.equalsIgnoreCase(MASS.name())) return MASS;
        else if (name.equalsIgnoreCase(RT.name())) return RT;
        else if (name.equalsIgnoreCase(PEPTIDE.name())) return PEPTIDE;
        else    return null;
    }

    public static SORT_BY defaultSortBy() {
        return ID;
    }
}