package edu.mcw.rgd.variantIndexer.vcfUtils;


import com.fasterxml.jackson.core.JsonProcessingException;

import edu.mcw.rgd.variantIndexer.dao.IndexDao;
import edu.mcw.rgd.variantIndexer.model.*;
import htsjdk.samtools.util.CloseableIterator;
import htsjdk.variant.variantcontext.Allele;
import htsjdk.variant.variantcontext.CommonInfo;
import htsjdk.variant.variantcontext.VariantContext;
import htsjdk.variant.vcf.VCFFileReader;

import java.io.*;


import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.zip.GZIPInputStream;

public class VCFUtils {

    public void parse(String fileName) throws Exception {
        System.out.println("FILE NAME:" + fileName);
        File file = new File(fileName);
        BufferedReader reader;
        if (file.getName().endsWith(".txt.gz") || file.getName().endsWith(".vcf.gz")) {
            reader = new BufferedReader(new InputStreamReader(new GZIPInputStream(new FileInputStream(file))));
        } else {
            // System.out.println("FILE: "+ file);
            reader = new BufferedReader(new FileReader(file));
        }
        String line;
        int lineCount = 0;
        String[] header = null;

        File out = new File("data/outFile.txt");
        FileWriter writer = new FileWriter(out);
        //    ExecutorService executor= new MyThreadPoolExecutor(10,10,0L, TimeUnit.MILLISECONDS, new LinkedBlockingQueue<>());
        while ((line = reader.readLine()) != null) {
            // skip comment line
            if (line.startsWith("#")) {
                header = line.substring(1).split("[\\t]", -1);
                //  writer.write(line +"\n");
            } else {
                if (line.contains("E_Phenotype_or_Disease")) {
                    // writer.write(line+"\n");
                    System.out.println(line);
                    lineCount = lineCount + 1;
                }
                  /*  if(lineCount>1){
                        break;
                    }*/

            }
        }
        writer.close();
        reader.close();
    }
    public void parseBySamTools(String fileName) throws JsonProcessingException {
        IndexDao dao = new IndexDao();

        VCFFileReader r = new VCFFileReader(new File(fileName), false);
        CloseableIterator<VariantContext> t = r.iterator();
        ExecutorService executor= new MyThreadPoolExecutor(10,10,0L, TimeUnit.MILLISECONDS, new LinkedBlockingQueue<>());
        while (t.hasNext()) {
            VariantContext ctx = t.next();
            Runnable workerThread=new Processor(ctx);
            workerThread.run();
        /*    CommonInfo info = ctx.getCommonInfo();
            //   if(ctx.getStart()==50697687) {
      //      if (ctx.getStart() == 29130209) {

                Allele refNuc=ctx.getReference();
                List<Allele> alleles=ctx.getAlleles();
                List<Allele> alternateAlleles=ctx.getAlternateAlleles();
                List<String> evidence=(mapEvidence(ctx));
                List<String> clinicalSignificance=(mapClinicalSignificance(ctx));
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
                   if (!alleleIsValid(ctx.getReference().getBaseString())) {
                        //   System.out.println(" *** Ref Nucleotides must be A,C,G,T,N");
                        continue;
                    }
                    if (!alleleIsValid(a.getBaseString())) {
                        //     System.out.println(" *** Var Nucleotides must be A,C,G,T,N");
                        continue;
                    }

                    object.setRefNuc(refNuc.getBaseString());
                    object.setVarNuc(a.getBaseString());
                    object.setEndPos(endPos);
                    object.setVariantType((variantType));
                    object.setConsequences(getConsequences(ctx,index,a.getBaseString()));
                    object.setEvidence(evidence);
                    object.setClinicalSignificance(clinicalSignificance);
                    object.setMA(ctx.getAttribute("MA").toString());
                    object.setMAF(Double.parseDouble( ctx.getAttribute("MAF").toString()));
                    object.setMAC(Integer.parseInt( ctx.getAttribute("MAC").toString()));
                    object.setRefPep((String) ctx.getAttribute("RefPep"));
                    object.setAa((String) ctx.getAttribute("AA"));
                    object.setQual((String) ctx.getAttribute("QUAL"));
                  //  System.out.println("FILTER: "+ ctx.getAttribute("FILTER") +"\t"+ ctx.getFilters().toString());
                    index=index+1;
                    dao.index(object);
                }



             //   break;
          //  }*/
        }
        executor.shutdown();
        while (!executor.isTerminated()) {}
        r.close();
    }
    public List<String> mapEvidence(VariantContext ctx){
        CommonInfo info=ctx.getCommonInfo();
        List<String> evidences=new ArrayList<>();
        for (Map.Entry e:Evidence.emap.entrySet() ) {
            String key= (String) e.getKey();
            String value=(String) e.getValue();
            if(info.getAttribute(key)!=null) {
                boolean evidence = (boolean) info.getAttribute(key);
                if (evidence) {
                    evidences.add(value);
                }
            }
        }
        return evidences;
    }
    public List<String> mapClinicalSignificance(VariantContext ctx){
        CommonInfo info=ctx.getCommonInfo();
        List<String> significance=new ArrayList<>();
        for (Map.Entry e:ClinicalSig.csmap.entrySet() ) {
            String key= (String) e.getKey();
            String value=(String) e.getValue();
            if(info.getAttribute(key)!=null) {
                boolean flag = (boolean) info.getAttribute(key);
                if (flag) {
                    significance.add(value);
                }
            }
        }
        return significance;
    }
    public List<TranscriptFeature> getConsequences(VariantContext ctx, int index, String varNuc) throws JsonProcessingException {
        List<TranscriptFeature> features = new ArrayList<>();
        if(ctx.getAttribute("CSQ")!=null) {
            List<String> objects = Arrays.asList(ctx.getAttribute("CSQ").toString().split(","));

            Map<String, Polyphen> polyphen = mapPolyphen(ctx, index);
            Map<String, String> varPep = mapVarPep(ctx);
            Map<String, List<VariantEffect>> veffects = mapVE(ctx, index);

            for (String obj : objects) {
                String[] tokens = obj.toString().split("\\|");
                String feature = new String();
                String allele = new String();
                try {
                    allele = tokens[0];
                } catch (Exception e) {
                }
                if (allele.equalsIgnoreCase(varNuc)) {
                    TranscriptFeature f = new TranscriptFeature();
                    f.setAllele(allele);
                    try {
                        String consequence = tokens[1];
                        f.setConsequence(consequence);
                    } catch (Exception e) {
                    }
                    try {
                        String featureType = tokens[2];
                        f.setFeatureType(featureType);
                    } catch (Exception e) {
                        //   e.printStackTrace();
                    }
                    try {
                        feature = tokens[3];
                        f.setFeature(feature);
                    } catch (Exception e) {
                        //  e.printStackTrace();
                    }
                    try {
                        String aminoAcids = tokens[4];
                        f.setAminoAcids(aminoAcids);
                    } catch (Exception e) {
                        // e.printStackTrace();
                    }
                    try {
                        String sift = tokens[5];
                        f.setSift(sift);
                    } catch (Exception e) {
                        //e.printStackTrace();
                    }

                    f.setPolyphen(polyphen.get(feature));
                    f.setVarPep(varPep.get(feature));
                    f.setVariantEffects(veffects.get(feature));

                    features.add(f);
                }
            }
        }
        return features;
    }
    public Map<String, List<VariantEffect>> mapVE(VariantContext ctx, int index){
        Map<String, List<VariantEffect>> veMap=new HashMap<>();
        if(ctx.getAttribute("VE")!=null) {
            List<String> objects = Arrays.asList(ctx.getAttribute("VE").toString().split(","));
            List<String> ids=new ArrayList<>();
            for (String obj : objects) {
             //   System.out.println(obj.toString() + "\n");
                String[] tokens = obj.toString().split("\\|");

                    try {
                        if (Integer.parseInt(tokens[1]) == index) {
                        String id = tokens[3];
                        List<VariantEffect> ves = new ArrayList<>();
                        if (!ids.contains(id)) {

                            ids.add(id);
                            VariantEffect ve = new VariantEffect();
                            ve.setConsequence(tokens[0]);
                            ve.setFeatureType(tokens[2]);
                            ves.add(ve);
                            veMap.put(id, ves);
                        } else {
                            ves = veMap.get(id);
                            if (ves == null) {
                                ves = new ArrayList<>();
                            }
                            VariantEffect ve = new VariantEffect();
                            ve.setConsequence(tokens[0]);
                            ve.setFeatureType(tokens[2]);
                            ves.add(ve);
                            veMap.put(id, ves);
                        }
                    }
                    } catch (Exception e) {
                    }

            }
        }
        return veMap;
    }
    public Map<String, Polyphen> mapPolyphen(VariantContext ctx, int index){
        Map<String, Polyphen> polyphenMap=new HashMap<>();
        if(ctx.getAttribute("Polyphen")!=null) {
            List<String> objects = Arrays.asList(ctx.getAttribute("Polyphen").toString().split(","));


            for (String obj : objects) {
                Polyphen p = new Polyphen();
                //.out.println(obj.toString() + "\n");
                String[] tokens = obj.toString().split("\\|");
                try {
                    p.setPrediction(tokens[1]);
                    p.setValue(tokens[2]);
                    polyphenMap.put(tokens[3], p);
                } catch (Exception e) {
                }
            }
        }
        return polyphenMap;
    }
    public Map<String, String> mapVarPep(VariantContext ctx){
        Map<String, String> varPep=new HashMap<>();
        if(ctx.getAttribute("VarPep")!=null) {
            List<String> objects = Arrays.asList(ctx.getAttribute("VarPep").toString().split(","));
            for (String obj : objects) {
           //     System.out.println(obj.toString() + "\n");
                String[] tokens = obj.toString().split("\\|");
                try {
                    varPep.put(tokens[2], tokens[1]);
                } catch (Exception e) {
                }
            }
        }
        return varPep;
    }
    public  boolean alleleIsValid(String allele) {
        for( int i=0; i<allele.length(); i++ ) {
            char c = allele.charAt(i);
            if( c=='A' || c=='C' || c=='G' || c=='T' || c=='N' || c=='-' )
                continue;
            return false;
        }
        return true;
    }
}

