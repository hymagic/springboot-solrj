package cn.eakay.springboot.biz;

import cn.eakay.springboot.client.Eakay;
import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.apache.solr.client.solrj.response.UpdateResponse;

import java.util.List;

/**
 * Created by magic~ on 2018/6/5.
 */
public interface SolrJService
{


    public UpdateResponse addIndex(List<Eakay> eakays, String collection);

    public UpdateResponse delAllIndex(String collection);

    public QueryResponse query(String collection, SolrQuery solrQuery);
}
