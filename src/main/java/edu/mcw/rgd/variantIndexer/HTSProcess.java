package edu.mcw.rgd.variantIndexer;

import edu.mcw.rgd.variantIndexer.dao.IndexDao;
import edu.mcw.rgd.variantIndexer.model.IndexObject;
import htsjdk.samtools.util.CloseableIterator;
import htsjdk.variant.variantcontext.CommonInfo;
import htsjdk.variant.variantcontext.VariantContext;
import htsjdk.variant.vcf.VCFFileReader;

import java.io.File;

/**
 * Created by jthota on 12/19/2019.
 */

/**
 * HTSProcess is a High Sequence Troughput processing pipeline, reads VCF file
 * and processes the variants.
 */
public class HTSProcess {

    public static void main(String[] args){
        IndexDao dao=new IndexDao();
        VCFFileReader r= new VCFFileReader(new File(args[0]), false);
        CloseableIterator<VariantContext> t=r.iterator();
        System.out.println("ID"+"\t"+"CHR"+"\t"+"START_POS"+"\t"+"REFNUC"+
                "\t"+"VARNUC"+"\t"+"ALLELES"+"\t"+ "QUAL");
        while(t.hasNext()){
            VariantContext ctx=t.next();
            CommonInfo info=ctx.getCommonInfo();
         //   if(ctx.getStart()==50697687) {
            if(ctx.getStart()==29130209){
                IndexObject object=new IndexObject();
                 object.setId(ctx.getID()) ;
                 object.setChromosome(ctx.getContig());
                 object.setStartPos( ctx.getStart() );
                    dao.index(object);
              /*   object.setRefNuc(ctx.getReference().toString());
                 object.setAa(ctx.getAlternateAlleles().toString());
                 object.setVarNuc(ctx.getAlleles()) ;
                        "\nTSA:" + info.getAttribute("TSA") +
                        "\nSOURCE: "+ ctx.getSource()+
                        "\nSAMPLE NAMES: "+ctx.getSampleNames()+
                        "\nGENOTYPES:"+ ctx.getGenotypes()+
                        "\nHET COUNT:"+ctx.getHetCount()+
                        "\nHOM VAR COUNT:"+ctx.getHomVarCount()+
                        "\nSIFT: " + info.getAttribute("Sift")+
                        "\nVE:" + info.getAttribute("VE")+
                        "\nPOLYPHEN: "+ info.getAttribute("Polyphen")+
                        "\nCSQ: "+info.getAttribute("CSQ")+
                        "\nVARPEP: "+ info.getAttribute("VarPep")+
                        "\nREFPEP: "+ info.getAttribute("RefPep")+
                        //***********EVIDENCE*******************/
                 /*       "\nE_FREQ: "+ info.getAttribute("E_Freq")+
                        "\nETOPMED: "+ info.getAttribute("E_TOPMed")+
                        "\nE_GNOMAD: "+info.getAttribute("E_gnomAD")+
                        "\nE_1000G: "+info.getAttribute("E_1000G")+
                        "\ndbSNP_153: "+ info.getAttribute("dbSNP_153")+
                        "\nCOSMIC_90: "+ info.getAttribute("COSMIC_90")+
                        "\nClinVar_201912:"+ info.getAttribute("ClinVar_201912")+
                        "\nHGMD-PUBLIC_20194:"+ info.getAttribute("HGMD-PUBLIC_20194")+
                        "\nESP_20141103:"+ info.getAttribute("ESP_20141103")+
                        "\nE_Cited: "+ info.getAttribute("E_Cited")+
                        "\nE_Multiple_observations: "+ info.getAttribute("E_Multiple_observations")+
                        "\nE_Freq: "+ info.getAttribute("E_Freq")+
                        "\nE_Hapmap: "+ info.getAttribute("E_Hapmap")+
                        "\nE_Phenotype_or_Disease:"+ info.getAttribute("E_Phenotype_or_Disease")+
                        "\nE_ESP:"+ info   .getAttribute("E_ESP")+
                        "\nE_ExAC:"+info.getAttribute("E_ExAC")+

                        //**************clinical significance *****************/
                    /*    "\nCLIN_risk_factor:"+info.getAttribute("CLIN_risk_factor")+
                        "\nCLIN_protective:"+ info.getAttribute("CLIN_protective")+
                        "\nCLIN_confers_sensitivity: "+ info.getAttribute("CLIN_confers_sensitivity")+
                        "\nCLIN_other: "+ info.getAttribute("CLIN_other")+
                        "\nCLIN_drug_response:"+ info.getAttribute("CLIN_drug_response")+
                        "\nCLIN_uncertain_significance: "+ info.getAttribute("CLIN_uncertain_significance")+
                        "\nCLIN_benign: "+ info.getAttribute("CLIN_benign")+
                        "\nCLIN_likely_pathogenic: "+ info  .getAttribute("CLIN_likely_pathogenic")+
                        "\nCLIN_pathogenic:"+info.getAttribute("CLIN_pathogenic")+
                        "\nCLIN_likely_benign: "+ info.getAttribute("CLIN_likely_benign")+
                        "\nCLIN_histocompatibility:"+ info.getAttribute("CLIN_histocompatibility")+
                        "\nCLIN_not_provided: "+ info.getAttribute("CLIN_not_provided")+
                        "\nCLIN_association: "+ info.getAttribute("CLIN_association")+
                        //*************************MINOR ALLELE****************/
                    /*    "\nMA: "+ info.getAttribute("MA")+
                        "\nMAF: "+info.getAttribute("MAF")+
                        "\nMAC: "+ info.getAttribute("MAC")+
                        "\nATTRRIBUTES: "+ info.getAttributes().keySet()*/

            }

        }
        t.close();
        r.close();
        System.out.println("Done!!");
    }

}
