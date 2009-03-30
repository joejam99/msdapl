/**
 * Ms2FileScan.java
 * @author Vagisha Sharma
 * Jun 16, 2008
 * @version 1.0
 */
package org.yeastrc.ms.parser.ms2File;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;

import org.yeastrc.ms.domain.run.DataConversionType;
import org.yeastrc.ms.domain.run.ms2file.MS2NameValuePair;
import org.yeastrc.ms.domain.run.ms2file.MS2ScanIn;
import org.yeastrc.ms.domain.run.ms2file.MS2ScanCharge;
import org.yeastrc.ms.domain.run.ms2file.impl.ScanChargeBean;
import org.yeastrc.ms.util.PeakStringBuilder;

/**
 * 
 */
public class Scan implements MS2ScanIn {

    public static final String PRECURSOR_SCAN = "PrecursorScan"; // precursor scan number
    public static final String ACTIVATION_TYPE = "ActivationType";
    public static final String RET_TIME = "RetTime";
    public static final String RT_TIME = "RTime";

    private int startScan = -1;
    private int endScan = -1;
    private int precursorScanNum = -1;

    private BigDecimal precursorMz;
    private BigDecimal retentionTime;
    private String activationType;

    private DataConversionType dataConversionType = DataConversionType.UNKNOWN;
    
    private List<String[]> peakList;

    private List<MS2ScanCharge> chargeStates;

    private List<MS2NameValuePair> analysisItems;


    public Scan() {
        chargeStates = new ArrayList<MS2ScanCharge>();
        analysisItems = new ArrayList<MS2NameValuePair>();
        peakList = new ArrayList<String[]>();
    }

//    public boolean isValid() {
//        return peakList.size() > 0 && chargeStates.size() > 0;
//    }

    @Override
    public List<MS2NameValuePair> getChargeIndependentAnalysisList() {
        return this.analysisItems;
    }

    public void addAnalysisItem(String label, String value) {
        if (label == null)   return;
        analysisItems.add(new HeaderItem(label, value));
        if (label.equalsIgnoreCase(RET_TIME))
            setRetentionTime(value);
        else if (label.equalsIgnoreCase(RT_TIME))
            setRetentionTime(value);
        else if (label.equalsIgnoreCase(PRECURSOR_SCAN))
            setPrecursorScanNum(value);
        else if (label.equalsIgnoreCase(ACTIVATION_TYPE))
            setFragmentationType(value);
    }

    public BigDecimal getRetentionTime() {
        return retentionTime;
    }
    private void setRetentionTime(String rt) {
        if (rt == null) return;
        try {
            this.retentionTime = new BigDecimal(rt);
        }
        catch(NumberFormatException e) {
            this.retentionTime = null;
        }
    }

    public int getPrecursorScanNum() {
        return precursorScanNum;
    }
    private void setPrecursorScanNum(String num) {
        if (num == null)    return;
        try {
            this.precursorScanNum = Integer.parseInt(num);
        }
        catch(NumberFormatException e) {
            this.precursorScanNum = -1;
        }
    }

    public List<MS2ScanCharge> getScanChargeList() {
        return this.chargeStates;
    }
    public void addChargeState(ScanChargeBean chargeState) {
        chargeStates.add(chargeState);
    }

    public int getStartScanNum() {
        return startScan;
    }
    public void setStartScan(int startScan) {
        this.startScan = startScan;
    }

    public int getEndScanNum() {
        return this.endScan;
    }
    public void setEndScan(int endScan) {
        this.endScan = endScan;
    }

    public BigDecimal getPrecursorMz() {
        return precursorMz;
    }
    public void setPrecursorMz(String precursorMz) {
        this.precursorMz = new BigDecimal(precursorMz);
    }


    public Iterator<String[]> peakIterator() {
        return peakList.iterator();
    }
    public void addPeak(String mz, String rt) {
        peakList.add(new String[]{mz, rt});
    }

    public String getFragmentationType() {
        return activationType;
    }

    /**
     * The database (msScan table) currently supports a 3 character value for 
     * fragmentationType. We should get a SQL exception if actType is more than 3 characters long.
     */
    private void setFragmentationType(String actType) {
        this.activationType = actType;
    }

    
    // In MS2 files there is a file header if the data is centroided.  We will need to set it 
    // for each scan.
    public void setDataConversionType(DataConversionType convType) {
        this.dataConversionType = convType;
    }
    
    @Override
    public DataConversionType getDataConversionType() {
        return this.dataConversionType;
    }
    
    public int getPeakCount() {
        return peakList.size();
    }
    
    public int getMsLevel() {
        return 2;
    }

    public String toString() {
        StringBuilder buf = new StringBuilder();
        buf.append("S\t");
        buf.append(startScan);
//        buf.append(String.format("%06d", startScan));
        buf.append("\t");
        buf.append(endScan);
//        buf.append(String.format("%06d", endScan));
        if (precursorMz != null) {
            buf.append("\t");
            String premz = precursorMz.toString();
            premz = PeakStringBuilder.trimTrailingZerosKeepDecimalPoint(premz);
            buf.append(premz);
        }
        buf.append("\n");
        // charge independent analysis
        for (MS2NameValuePair item: analysisItems) {
            buf.append("I\t");
            buf.append(item.getName());
            if (item.getValue() != null) {
                buf.append("\t");
                buf.append(item.getValue());
            }
            buf.append("\n");
        }
        // charge states along with their charge dependent analysis
        Collections.sort(chargeStates, new Comparator<MS2ScanCharge>() {
            @Override
            public int compare(MS2ScanCharge o1, MS2ScanCharge o2) {
                return Integer.valueOf(o1.getCharge()).compareTo(o2.getCharge());
            }});
        
        for (MS2ScanCharge charge: chargeStates) {
            buf.append(charge.toString());
            buf.append("\n");
        }
        
        // peak data
        for (String[] peak: peakList){
            String mass = PeakStringBuilder.trimTrailingZerosKeepDecimalPoint(peak[0]);
            String inten = PeakStringBuilder.trimTrailingZerosKeepDecimalPoint(peak[1]);
            buf.append(mass);
            buf.append("\t");
            buf.append(inten);
            buf.append("\n");
        }
        buf.deleteCharAt(buf.length() - 1); // remove last new-line character
        return buf.toString();
    }
}