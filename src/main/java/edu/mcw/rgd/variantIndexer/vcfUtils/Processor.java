package edu.mcw.rgd.variantIndexer.vcfUtils;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import edu.mcw.rgd.variantIndexer.dao.IndexDao;
import edu.mcw.rgd.variantIndexer.model.IndexObject;
import edu.mcw.rgd.variantIndexer.model.RgdIndex;
import edu.mcw.rgd.variantIndexer.service.ESClient;
import htsjdk.variant.variantcontext.Allele;
import htsjdk.variant.variantcontext.CommonInfo;
import htsjdk.variant.variantcontext.VariantContext;
import org.elasticsearch.action.bulk.BackoffPolicy;
import org.elasticsearch.action.bulk.BulkProcessor;
import org.elasticsearch.action.bulk.BulkRequest;
import org.elasticsearch.action.bulk.BulkResponse;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.common.unit.ByteSizeUnit;
import org.elasticsearch.common.unit.ByteSizeValue;
import org.elasticsearch.common.unit.TimeValue;
import org.elasticsearch.common.xcontent.XContentType;

import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class Processor implements Runnable {
    private List<VariantContext> ctxs;
    VCFUtils utils=new VCFUtils();
    IndexDao dao=new IndexDao();
    public Processor(List<VariantContext> ctxs){ this.ctxs=ctxs;}
    @Override
    public void run() {
        BulkProcessor.Listener listener = new BulkProcessor.Listener() {
            @Override
            public void beforeBulk(long executionId, BulkRequest request) {
                //        System.out.println("ACTIONS: "+request.numberOfActions());
            }

            @Override
            public void afterBulk(long executionId, BulkRequest request,
                                  BulkResponse response) {
                //     System.out.println("in process...");
            }

            @Override
            public void afterBulk(long executionId, BulkRequest request,
                                  Throwable failure) {

            }
        };
        BulkProcessor bulkProcessor = BulkProcessor.builder(
                (request, bulkListener) ->
                        ESClient.getClient().bulkAsync(request, RequestOptions.DEFAULT, bulkListener),
                listener)
                .setBulkActions(10000)
                .setBulkSize(new ByteSizeValue(5, ByteSizeUnit.MB))
                .setFlushInterval(TimeValue.timeValueSeconds(5))
                .setConcurrentRequests(1)
                .setBackoffPolicy(
                        BackoffPolicy.exponentialBackoff(TimeValue.timeValueMillis(100), 3))
                .build();
        for(VariantContext ctx:ctxs) {
            CommonInfo info = ctx.getCommonInfo();
            Allele refNuc = ctx.getReference();
            List<Allele> alleles = ctx.getAlleles();
            List<Allele> alternateAlleles = ctx.getAlternateAlleles();
            List<String> evidence = (utils.mapEvidence(ctx));
            List<String> clinicalSignificance = (utils.mapClinicalSignificance(ctx));
            StringBuilder alleleString = new StringBuilder();
            boolean first = true;
            for (Allele allele : alleles) {
                if (first) {
                    alleleString.append((allele.getBaseString()));
                    first = false;
                } else {
                    alleleString.append(",").append(allele.getBaseString());
                }
            }
            int index = 0;
            for (Allele a : alternateAlleles) {
                IndexObject object = new IndexObject();
                object.setId(ctx.getID());
                object.setChromosome(ctx.getContig());
                object.setStartPos(ctx.getStart());
                if (a.compareTo(refNuc) < 0) {
                    continue;
                }
                int endPos = 0;
                String variantType = (String) info.getAttribute("TSA");
                if (ctx.isSNP()) {
                    endPos = ctx.getStart() + 1;
                } // insertions
                if (ctx.isSimpleInsertion()) {
                    endPos = ctx.getStart();
                    //  System.out.println("INSERTION");
                }
                // deletions
                else if (ctx.isSimpleDeletion()) {
                    endPos = ctx.getStart() + refNuc.getDisplayString().length();
                    //  System.out.println("Deletion");
                } else {
                    //   System.out.println("Unexpected var type");
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
                    object.setConsequences(utils.getConsequences(ctx, index, a.getBaseString()));
                } catch (JsonProcessingException e) {
                    e.printStackTrace();
                }
                object.setEvidence(evidence);
                object.setClinicalSignificance(clinicalSignificance);
                if (ctx.getAttribute("MA") != null)
                    object.setMA(ctx.getAttribute("MA").toString());
                if (ctx.getAttribute("MAF") != null)
                    object.setMAF(Double.parseDouble(ctx.getAttribute("MAF").toString()));
                if (ctx.getAttribute("MAC") != null)
                    object.setMAC(Integer.parseInt(ctx.getAttribute("MAC").toString()));
                if (ctx.getAttribute("RefPep") != null)
                    object.setRefPep((String) ctx.getAttribute("RefPep"));
                if (ctx.getAttribute("AA") != null)
                    object.setAa((String) ctx.getAttribute("AA"));
                if (ctx.getAttribute("QUAL") != null)
                    object.setQual((String) ctx.getAttribute("QUAL"));
                //  System.out.println("FILTER: "+ ctx.getAttribute("FILTER") +"\t"+ ctx.getFilters().toString());
                index = index + 1;
               // dao.index(object);
                try {
                    ObjectMapper mapper=new ObjectMapper();
                    String json =  mapper.writeValueAsString(object);
                    bulkProcessor.add(new IndexRequest(RgdIndex.getNewAlias()).source(json, XContentType.JSON));
                } catch (JsonProcessingException e) {
                    e.printStackTrace();
                }
            }


        }
        try {
            bulkProcessor.awaitClose(10, TimeUnit.MINUTES);
            bulkProcessor.close();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }finally {
            bulkProcessor.close();
        }
        System.out.println("***********"+Thread.currentThread().getName()+ "\tEND ...."+"\t"+ new Date()+"*********");

    }
}
