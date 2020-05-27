package edu.mcw.rgd.variantIndexer;

import edu.mcw.rgd.variantIndexer.model.*;
import edu.mcw.rgd.variantIndexer.service.ESClient;
import edu.mcw.rgd.variantIndexer.service.IndexAdmin;

import edu.mcw.rgd.variantIndexer.vcfUtils.VCFUtils;
import org.apache.log4j.Logger;
import org.elasticsearch.action.admin.cluster.health.ClusterHealthRequest;
import org.elasticsearch.action.admin.cluster.health.ClusterHealthResponse;
import org.elasticsearch.action.admin.indices.alias.IndicesAliasesRequest;

import org.elasticsearch.action.support.master.AcknowledgedResponse;
import org.elasticsearch.client.RequestOptions;

import org.springframework.beans.factory.support.DefaultListableBeanFactory;
import org.springframework.beans.factory.xml.XmlBeanDefinitionReader;
import org.springframework.core.io.FileSystemResource;


import java.util.*;
import java.util.Map;

/**
 * Created by jthota on 11/14/2019.
 */

public class Manager {
    private String version;
    private RgdIndex rgdIndex;
    private static List environments;
    private IndexAdmin admin;
    private int mapKey;
    private int speciesTypeKey;
    private String chromosome;
    private String fileName;
    private String command;     //update or reindex
    private String process;     // transcripts or variants
    private String env;     // dev or test or prod
    private String storeType; //loading to es or db

    VCFUtils utils=new VCFUtils();

    static Logger log= Logger.getLogger(Manager.class);

    public static void main(String[] args) throws Exception {

       DefaultListableBeanFactory bf= new DefaultListableBeanFactory();
       new XmlBeanDefinitionReader(bf) .loadBeanDefinitions(new FileSystemResource("properties/AppConfigure.xml"));
       Manager manager= (Manager) bf.getBean("manager");

       log.info(manager.version);
      ESClient es= (ESClient) bf.getBean("client");
       manager.rgdIndex= (RgdIndex) bf.getBean("rgdIndex");
      try{

            List<String> indices= new ArrayList<>();
            manager.command=args[0];
            manager.env=args[1];
            manager.process=args[2];
            manager.speciesTypeKey=Integer.parseInt(args[3]);
            manager.mapKey=Integer.parseInt(args[4]);
            manager.chromosome=args[5];
            manager.storeType=args[6];
            if(manager.mapKey==17){
                manager.fileName=args[7];

            }
            String species= "human";
            String index=manager.process+"_"+species+manager.mapKey;

            if (environments.contains(manager.env)) {
                manager.rgdIndex.setIndex(index +"_"+manager.env);
                indices.add(index+"_"+manager.env + "1");
                indices.add(index + "_"+manager.env + "2");
                manager.rgdIndex.setIndices(indices);
            }

                manager.run(args);


        }catch (Exception e){
           if(es!=null)
                ESClient.destroy();
            e.printStackTrace();
        }
        if(es!=null)
            ESClient.destroy();


    }

    public void run(String[] args) throws Exception {
        long start = System.currentTimeMillis();
       this.setIndex();
      //  utils.parse(fileName);

        utils.parseBySamTools(fileName);

     String clusterStatus = this.getClusterHealth(RgdIndex.getNewAlias());
        if (!clusterStatus.equalsIgnoreCase("ok")) {
            System.out.println(clusterStatus + ", refusing to continue with operations");
           log.info(clusterStatus + ", refusing to continue with operations");
        } else {
            if(command.equalsIgnoreCase("reindex")) {
                System.out.println("CLUSTER STATUR:"+ clusterStatus+". Switching Alias...");
                log.info("CLUSTER STATUR:"+ clusterStatus+". Switching Alias...");
                switchAlias();
            }
        }
        long end = System.currentTimeMillis();
        System.out.println(" - " + Utils.formatElapsedTime(start, end));
        log.info(" - " + Utils.formatElapsedTime(start, end));
        System.out.println("CLIENT IS CLOSED");
    }
    public void setIndex() throws Exception {
        String species= "human";
        if(command.equalsIgnoreCase("reindex"))
            admin.createIndex("", species);
        else  if(command.equalsIgnoreCase("update"))
            admin.updateIndex();
    }

    public String getClusterHealth(String index) throws Exception {

        ClusterHealthRequest request = new ClusterHealthRequest(index);
        ClusterHealthResponse response = ESClient.getClient().cluster().health(request, RequestOptions.DEFAULT);
        System.out.println(response.getStatus().name());
   //     log.info("CLUSTER STATE: " + response.getStatus().name());
        if (response.isTimedOut()) {
            return   "cluster state is " + response.getStatus().name();
        }

        return "OK";
    }
    public boolean switchAlias() throws Exception {
        System.out.println("NEEW ALIAS: " + RgdIndex.getNewAlias() + " || OLD ALIAS:" + RgdIndex.getOldAlias());
        IndicesAliasesRequest request = new IndicesAliasesRequest();


        if (RgdIndex.getOldAlias() != null) {

            IndicesAliasesRequest.AliasActions removeAliasAction =
                    new IndicesAliasesRequest.AliasActions(IndicesAliasesRequest.AliasActions.Type.REMOVE)
                            .index(RgdIndex.getOldAlias())
                            .alias(rgdIndex.getIndex());
            IndicesAliasesRequest.AliasActions addAliasAction =
                    new IndicesAliasesRequest.AliasActions(IndicesAliasesRequest.AliasActions.Type.ADD)
                            .index(RgdIndex.getNewAlias())
                            .alias(rgdIndex.getIndex());
            request.addAliasAction(removeAliasAction);
            request.addAliasAction(addAliasAction);
        //    log.info("Switched from " + RgdIndex.getOldAlias() + " to  " + RgdIndex.getNewAlias());

        }else{
            IndicesAliasesRequest.AliasActions addAliasAction =
                    new IndicesAliasesRequest.AliasActions(IndicesAliasesRequest.AliasActions.Type.ADD)
                            .index(RgdIndex.getNewAlias())
                            .alias(rgdIndex.getIndex());
            request.addAliasAction(addAliasAction);
        //    log.info(rgdIndex.getIndex() + " pointed to " + RgdIndex.getNewAlias());
        }
        AcknowledgedResponse indicesAliasesResponse =
                ESClient.getClient().indices().updateAliases(request, RequestOptions.DEFAULT);

        return  true;

    }





    public void setVersion(String version) {
        this.version = version;
    }

    public String getVersion() {
        return version;
    }

    public RgdIndex getRgdIndex() {
        return rgdIndex;
    }

    public void setRgdIndex(RgdIndex rgdIndex) {
        this.rgdIndex = rgdIndex;
    }

    public void setEnvironments(List environments) {
        this.environments = environments;
    }

    public List getEnvironments() {
        return environments;
    }

    public void setAdmin(IndexAdmin admin) {
        this.admin = admin;
    }

    public IndexAdmin getAdmin() {
        return admin;
    }

    public int getMapKey() {
        return mapKey;
    }

    public void setMapKey(int mapKey) {
        this.mapKey = mapKey;
    }

    public int getSpeciesTypeKey() {
        return speciesTypeKey;
    }

    public void setSpeciesTypeKey(int speciesTypeKey) {
        this.speciesTypeKey = speciesTypeKey;
    }

    public String getChromosome() {
        return chromosome;
    }

    public void setChromosome(String chromosome) {
        this.chromosome = chromosome;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public String getCommand() {
        return command;
    }

    public void setCommand(String command) {
        this.command = command;
    }

    public String getProcess() {
        return process;
    }

    public void setProcess(String process) {
        this.process = process;
    }

    public String getEnv() {
        return env;
    }

    public void setEnv(String env) {
        this.env = env;
    }

    public static Logger getLog() {
        return log;
    }

    public static void setLog(Logger log) {
        Manager.log = log;
    }

}