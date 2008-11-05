package edu.uwpr.protinfer.infer;


public interface PeptideSpectrumMatch <T extends SpectrumMatch> {

    public abstract PeptideHit getPeptideHit();
    
    public abstract T getSpectrumMatch();
    
    public abstract SearchSource getSearchSource();
    
    public abstract int getScanNumber();
    
    public abstract int getCharge();
    
    /**
     * Return the sequence of the peptide without any modifications.
     * @return
     */
    public abstract String getPeptideSequence();

    public abstract boolean isDecoyMatch();
    
    public abstract boolean isTargetMatch();
}