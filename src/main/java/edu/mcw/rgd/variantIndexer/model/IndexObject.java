package edu.mcw.rgd.variantIndexer.model;

import java.util.List;

public class IndexObject {

    private String id;
    private String chromosome;
    private int startPos;
    private int endPos;
    private String refNuc;
    private String varNuc;
    private String qual;
    private String variantType;
    private String filter;
    private String refPep;
    private String aa; // ancestral allele
    private String MA; //minor allele
    private double MAF;    // minor allele frequency
    private int MAC;    // minor allele count
    private List<String> evidence;
    private List<String> clinicalSignificance;
    private List<TranscriptFeature> consequences;

    public String getMA() {
        return MA;
    }

    public void setMA(String MA) {
        this.MA = MA;
    }

    public String getVariantType() {
        return variantType;
    }

    public void setVariantType(String variantType) {
        this.variantType = variantType;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getChromosome() {
        return chromosome;
    }

    public void setChromosome(String chromosome) {
        this.chromosome = chromosome;
    }

    public int getStartPos() {
        return startPos;
    }

    public void setStartPos(int startPos) {
        this.startPos = startPos;
    }

    public String getRefNuc() {
        return refNuc;
    }

    public void setRefNuc(String refNuc) {
        this.refNuc = refNuc;
    }

    public String getVarNuc() {
        return varNuc;
    }

    public void setVarNuc(String varNuc) {
        this.varNuc = varNuc;
    }

    public String getQual() {
        return qual;
    }

    public void setQual(String qual) {
        this.qual = qual;
    }

    public String getFilter() {
        return filter;
    }

    public void setFilter(String filter) {
        this.filter = filter;
    }

    public List<TranscriptFeature> getConsequences() {
        return consequences;
    }

    public void setConsequences(List<TranscriptFeature> consequences) {
        this.consequences = consequences;
    }

    public String getRefPep() {
        return refPep;
    }

    public void setRefPep(String refPep) {
        this.refPep = refPep;
    }

    public String getAa() {
        return aa;
    }

    public void setAa(String aa) {
        this.aa = aa;
    }



    public void setEvidence(List<String> evidence) {
        this.evidence = evidence;
    }

    public List<String> getClinicalSignificance() {
        return clinicalSignificance;
    }

    public void setClinicalSignificance(List<String> clinicalSignificance) {
        this.clinicalSignificance = clinicalSignificance;
    }

    public double getMAF() {
        return MAF;
    }

    public void setMAF(double MAF) {
        this.MAF = MAF;
    }

    public int getMAC() {
        return MAC;
    }

    public void setMAC(int MAC) {
        this.MAC = MAC;
    }

    public List<String> getEvidence() {
        return evidence;
    }

    public int getEndPos() {
        return endPos;
    }

    public void setEndPos(int endPos) {
        this.endPos = endPos;
    }
}
