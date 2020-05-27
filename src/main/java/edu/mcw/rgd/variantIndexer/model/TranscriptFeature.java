package edu.mcw.rgd.variantIndexer.model;

import java.util.List;

public class TranscriptFeature {
    private String feature;
    private String consequence;
    private String featureType;
    private String allele;
    private String aminoAcids;
    private String sift;
    private Polyphen polyphen;
    private String varPep;
    private List<VariantEffect> variantEffects;

    public String getVarPep() {
        return varPep;
    }

    public void setVarPep(String varPep) {
        this.varPep = varPep;
    }

    public String getFeature() {
        return feature;
    }

    public void setFeature(String feature) {
        this.feature = feature;
    }

    public String getAllele() {
        return allele;
    }

    public void setAllele(String allele) {
        this.allele = allele;
    }

    public String getAminoAcids() {
        return aminoAcids;
    }

    public void setAminoAcids(String aminoAcids) {
        this.aminoAcids = aminoAcids;
    }

    public String getSift() {
        return sift;
    }

    public void setSift(String sift) {
        this.sift = sift;
    }

    public Polyphen getPolyphen() {
        return polyphen;
    }

    public void setPolyphen(Polyphen polyphen) {
        this.polyphen = polyphen;
    }

    public String getConsequence() {
        return consequence;
    }

    public void setConsequence(String consequence) {
        this.consequence = consequence;
    }

    public String getFeatureType() {
        return featureType;
    }

    public void setFeatureType(String featureType) {
        this.featureType = featureType;
    }

    public List<VariantEffect> getVariantEffects() {
        return variantEffects;
    }

    public void setVariantEffects(List<VariantEffect> variantEffects) {
        this.variantEffects = variantEffects;
    }
}
