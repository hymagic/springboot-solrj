package cn.eakay.springboot.biz.impl;

import cn.eakay.springboot.biz.SolrJService;
import cn.eakay.springboot.client.Eakay;
import org.apache.solr.client.solrj.SolrClient;
import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.apache.solr.client.solrj.response.UpdateResponse;
import org.apache.solr.common.SolrInputDocument;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by magic~ on 2018/6/5.
 */
@Service
public class SolrJServiceImpl implements SolrJService
{

    private Logger logger = LoggerFactory.getLogger(this.getClass());
    @Autowired
    private SolrClient solrClient;


    public UpdateResponse addIndex(List<Eakay> eakays,String collection)
    {


        List<SolrInputDocument>  documents=new ArrayList<>();

        for (Eakay eakay:eakays)
        {
            SolrInputDocument document=new SolrInputDocument();
            document.addField("id",eakay.getId());
            document.addField("name",eakay.getName());
            document.addField("description",eakay.getDescription());
            document.addField("category",eakay.getCategory());
            document.addField("weight",eakay.getWeight());
            documents.add(document);
        }

        try
        {
            solrClient.add(collection,documents);
            UpdateResponse response=   solrClient.commit(collection);
            return response;
        } catch (SolrServerException e)
        {
            logger.error("addIndex SolrServerException ",e);
        } catch (IOException e)
        {
            logger.error("addIndex IOException ",e);
        }

        return null;
    }

    public UpdateResponse delAllIndex(String collection)
    {
        try
        {
             solrClient.deleteByQuery(collection,"*:*");
            return solrClient.commit(collection);
        } catch (SolrServerException e)
        {
            logger.error("delAllIndex SolrServerException ",e);
        } catch (IOException e)
        {
            logger.error("delAllIndex IOException ",e);
        }

        return null;
    }

    public QueryResponse query(String collection, SolrQuery solrQuery)
    {
        try
        {
          return  solrClient.query(collection,solrQuery);

        } catch (SolrServerException e)
        {
            logger.error("query SolrServerException ",e);
        } catch (IOException e)
        {
            logger.error("query IOException ",e);
        }

        return null;
    }

}
