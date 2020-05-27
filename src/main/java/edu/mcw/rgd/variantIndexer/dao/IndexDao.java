package edu.mcw.rgd.variantIndexer.dao;

import com.fasterxml.jackson.databind.ObjectMapper;
import edu.mcw.rgd.variantIndexer.model.IndexObject;
import edu.mcw.rgd.variantIndexer.model.RgdIndex;
import edu.mcw.rgd.variantIndexer.service.ESClient;
import org.elasticsearch.action.bulk.BulkRequest;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.common.xcontent.XContentType;

import java.io.IOException;

public class IndexDao {
    public void index(IndexObject object){
        ObjectMapper mapper=new ObjectMapper();
        try {
            ObjectMapper map=new ObjectMapper();
            byte[] json = new byte[0];
            json =  map.writeValueAsBytes(object);
            IndexRequest request=new IndexRequest(RgdIndex.getNewAlias()).source(json, XContentType.JSON);
            ESClient.getClient().index(request, RequestOptions.DEFAULT);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


}
