package edu.mcw.rgd.variantIndexer.service;

import edu.mcw.rgd.variantIndexer.Manager;
import org.apache.http.HttpHost;
import org.apache.http.client.config.RequestConfig;
import org.apache.log4j.Logger;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestClientBuilder;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.common.settings.Settings;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

/**
 * Created by jthota on 11/15/2019.
 */
public class ESClient {
    private static Logger log=Logger.getLogger(Manager.class);
    private static RestHighLevelClient client = null;
    private ESClient(){}
    public void init(){
        System.out.println("Initializing Elasticsearch Client...");
        client=getInstance();
    }
    public static void destroy() throws IOException {
        System.out.println("destroying Elasticsearh Client...");
        if(client!=null) {
            try{
                client.close();
                client = null;
            }catch (Exception e){
                log.info(e);
            }

        }
    }

    public static RestHighLevelClient getClient() {
        return getInstance();
    }

    public static void setClient(RestHighLevelClient client) {
        ESClient.client = client;
    }

    public static RestHighLevelClient getInstance() {

        if(client==null){
              try(InputStream input= new FileInputStream("C:/Apps/elasticsearchProps.properties")){
          //  try(InputStream input= new FileInputStream("/data/properties/elasticsearchProps.properties")){
                Properties props= new Properties();
                props.load(input);
                String VARIANTS_HOST= (String) props.get("VARIANTS_HOST");
                System.out.println("HOST: "+ VARIANTS_HOST);
                int port=Integer.parseInt((String) props.get("PORT"));
                client = new RestHighLevelClient(
                        RestClient.builder(
                                new HttpHost(VARIANTS_HOST, port, "http")

                        ).setRequestConfigCallback(new RestClientBuilder.RequestConfigCallback(){

                            @Override
                            public RequestConfig.Builder customizeRequestConfig(RequestConfig.Builder requestConfigBuilder) {
                                return requestConfigBuilder
                                        .setConnectTimeout(5000)
                                        .setSocketTimeout(120000);
                            }
                        })

                );
                input.close();
            }catch (Exception e){
                e.printStackTrace();
            }

        }

        return client;
    }

}
