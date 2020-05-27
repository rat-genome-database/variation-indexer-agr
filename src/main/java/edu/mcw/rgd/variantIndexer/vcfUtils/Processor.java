package edu.mcw.rgd.variantIndexer.vcfUtils;

import com.fasterxml.jackson.core.JsonProcessingException;
import edu.mcw.rgd.variantIndexer.dao.IndexDao;
import edu.mcw.rgd.variantIndexer.model.IndexObject;
import htsjdk.tribble.index.Index;
import htsjdk.variant.variantcontext.Allele;
import htsjdk.variant.variantcontext.CommonInfo;
import htsjdk.variant.variantcontext.VariantContext;

import java.util.List;

public class Processor implements Runnable {
    private VariantContext ctx;
    VCFUtils utils=new VCFUtils();
    IndexDao dao=new IndexDao();
    public Processor(VariantContext ctx){ this.ctx=ctx;}
    @Override
    public void run() {
        CommonInfo info=ctx.getCommonInfo();
        Allele refNuc=ctx.getReference();
        List<Allele> alleles=ctx.getAlleles();
        List<Allele> alternateAlleles=ctx.getAlternateAlleles();
        List<String> evidence=(utils.mapEvidence(ctx));
        List<String> clinicalSignificance=(utils.mapClinicalSignificance(ctx));
        StringBuilder alleleString=new StringBuilder();
        boolean first=true;
        for(Allele allele: alleles){
            if(first) {
                alleleString.append((allele.getBaseString()));
                first=false;
            }else{
                alleleString.append(",").append(allele.getBaseString());
            }
        }
        int index=0;
        for(Allele a: alternateAlleles){
            IndexObject object = new IndexObject();
            object.setId(ctx.getID());
            object.setChromosome(ctx.getContig());
            object.setStartPos(ctx.getStart());
            if(a.compareTo(refNuc) < 0){
                continue;
            }
            int endPos=0;
            String variantType= (String) info.getAttribute("TSA");
            if(ctx.isSNP()){
                endPos = ctx.getStart() + 1;
            } // insertions
            if (ctx.isSimpleInsertion()) {
                endPos = ctx.getStart();
                System.out.println("INSERTION");
            }
            // deletions
            else if (ctx.isSimpleDeletion()) {
                endPos = ctx.getStart() + refNuc.getDisplayString().length();
                System.out.println("Deletion");
            } else {
                System.out.println("Unexpected var type");
            }
            if (!utils.alleleIsValid(ctx.getReference().getBaseString())) {
                //   System.out.println(" *** Ref Nucleotides must be A,C,G,T,N");
                continue;
            }
            if (!utils.alleleIsValid(a.getBaseString())) {
                //     System.out.println(" *** Var Nucleotides must be A,C,G,T,N");
                continue;
            }

            object.setRefNuc(refNuc.getBaseString());
            object.setVarNuc(a.getBaseString());
            object.setEndPos(endPos);
            object.setVariantType((variantType));
            try {
                object.setConsequences(utils.getConsequences(ctx,index,a.getBaseString()));
            } catch (JsonProcessingException e) {
                e.printStackTrace();
            }
            object.setEvidence(evidence);
            object.setClinicalSignificance(clinicalSignificance);
            if(ctx.getAttribute("MA")!=null)
            object.setMA(ctx.getAttribute("MA").toString());
            if(ctx.getAttribute("MAF")!=null)
            object.setMAF(Double.parseDouble( ctx.getAttribute("MAF").toString()));
            if(ctx.getAttribute("MAC")!=null)
            object.setMAC(Integer.parseInt( ctx.getAttribute("MAC").toString()));
            if( ctx.getAttribute("RefPep")!=null)
            object.setRefPep((String) ctx.getAttribute("RefPep"));
            if( ctx.getAttribute("AA")!=null)
            object.setAa((String) ctx.getAttribute("AA"));
            if( ctx.getAttribute("QUAL")!=null)
            object.setQual((String) ctx.getAttribute("QUAL"));
            //  System.out.println("FILTER: "+ ctx.getAttribute("FILTER") +"\t"+ ctx.getFilters().toString());
            index=index+1;
            dao.index(object);
        }


    }
}
