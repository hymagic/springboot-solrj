package cn.eakay.springboot.test.solrj;

import cn.eakay.springboot.biz.SolrJService;
import cn.eakay.springboot.client.Eakay;
import cn.eakay.springboot.test.BaseTest;
import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.response.*;
import org.apache.solr.client.solrj.response.FacetField.Count;
import org.apache.solr.common.SolrDocument;
import org.apache.solr.common.SolrDocumentList;
import org.apache.solr.common.params.FacetParams;
import org.apache.solr.common.params.GroupParams;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Created by magic~ on 2018/6/7.
 */
public class TestSolrj extends BaseTest
{
    private Logger logger = LoggerFactory.getLogger(this.getClass());
    @Autowired
    private SolrJService solrJService;

    @Test
    public void testAddIndex()
    {

        List<Eakay> eakays = new ArrayList<>();
        for (int i = 0; i <= 1000; i++)
        {
            Eakay eakay = new Eakay();
            eakay.setId(Long.valueOf(i));
            eakay.setName("中国" + i);
            if (i % 2 == 0)
            {
                eakay.setCategory("北京");
            } else
            {
                eakay.setCategory("湖北");
            }
            eakay.setDescription("我爱中华人民共和国" + i);
            eakay.setWeight(Float.valueOf(i));
            eakays.add(eakay);
        }


        UpdateResponse updateResponse = solrJService.addIndex(eakays, "eakay");

        logger.info(updateResponse.getResponseHeader().toString());
    }

    @Test
    public void delAllIndex()
    {

        UpdateResponse updateResponse = solrJService.delAllIndex("eakay");

        logger.info(updateResponse.getResponseHeader().toString());
    }

    @Test
    public void testQuery()
    {
        SolrQuery query = new SolrQuery();
        query.setQuery("+description:(中华 OR 哎哎哎)");
        //指定返回域
        query.setParam("fl", "id", "category","description");

        query.setStart(0); //索引

        query.setRows(20); //每页显示20条

        //高亮
        query.setHighlight(true);
        query.addHighlightField("description");
        query.setHighlightSimplePre("<b>");
        query.setHighlightSimplePost("</b>");

        logger.info("query :{}", query.toString());

        QueryResponse solrResponse = solrJService.query("eakay", query);

        if (solrResponse != null)
        {
            logger.info("response header:{}", solrResponse.getHeader().toString());

            logger.info("response results:{}", solrResponse.getResults().toString());

            logger.info("response Highlighting:{}",solrResponse.getHighlighting());


            SolrDocumentList result = solrResponse.getResults();

            /** ----------Highlight处理结果相关 ------------ **/
            for (SolrDocument doc : result) {
                Map<String, Map<String, List<String>>> highlighting = solrResponse.getHighlighting();
                List<String> list = highlighting.get(doc.get("id")).get("description");
                if (list != null) {
                    doc.setField("description", list.get(0));
                }
            }


            logger.info("response results:{}", result);

        }


    }




 /*   facet.prefix  –   限制constaints的前缀

    facet.mincount=0 –  限制constants count的最小返回值，默认为0

    facet.sort=count –  排序的方式，根据count或者index

    facet.offset=0  –   表示在当前排序情况下的偏移，可以做分页

    facet.limit=100 –  constraints返回的数目

    facet.missing=false –  是否返回没有值的field

    facet.date –  Deprecated, use facet.range

    facet.query*/
    @Test
    public void testFacet()
    {
        SolrQuery query = new SolrQuery();
        query.setQuery("*:*");
        //指定返回域
        query.setParam("fl", "id", "category");

        query.setStart(0); //索引

        query.setRows(20); //每页显示20条

        //设置开启facet
        query.setFacet(true);

        // 设置返回的数据中每个分组的数据最小值，比如设置为1，则统计数量最小为1，不然不显示
        //query.setFacetMinCount(1);

        // 限制facet返回的数量
        //query.setFacetLimit(10);
         //设置需要facet的字段
        query.addFacetField("category");
        // query.addFacetField("category","name"); //设置需要facet的字段

        query.addFacetQuery("weight:[0 TO 200]"); //设置facetQuery

        query.addFacetQuery("weight:[200 TO 800]");

        query.addFacetQuery("weight:[800 TO *]");


        query.addNumericRangeFacet("weight",0,1000,100);
      //  query.addFacetPivotField();//二维

        query.setFacetMissing(false); //不统计null的值

        query.setFacetSort(FacetParams.FACET_SORT_COUNT);//排序


        logger.info("query :{}", query.toString());

        QueryResponse solrResponse = solrJService.query("eakay", query);

        if (solrResponse != null)
        {
            logger.info("response header:{}", solrResponse.getHeader().toString());

            logger.info("response results:{}", solrResponse.getResults().toString());

            logger.info("response facetRanges:{}", solrResponse.getFacetRanges().toString());

            List<RangeFacet> rangeFacets =solrResponse.getFacetRanges();


            for (RangeFacet ff : rangeFacets)
            {



                List<RangeFacet.Count> counts = ff.getCounts();

                for (RangeFacet.Count count : counts)
                {

                    logger.info("name=" + count.getValue() + "\tcount=" + count.getCount());

                }


            }

            logger.info("response facetQuery:{}", solrResponse.getFacetQuery().toString());


            Map<String, Integer> facetQuery = solrResponse.getFacetQuery();

            for (Map.Entry<String, Integer> map : facetQuery.entrySet()) {

                logger.info("facetQuery:"+map.getKey() + ":" + map.getValue());

            }
            logger.info("response facetFields:{}", solrResponse.getFacetFields().toString());

            List<FacetField> facetFields = solrResponse.getFacetFields();

            for (FacetField ff : facetFields)
            {

                logger.info(ff.getName() + ":" + ff.getValueCount());

                List<Count> counts = ff.getValues();

                for (Count count : counts)
                {

                    logger.info("name=" + count.getName() + "\tcount=" + count.getCount());

                }

            }


        }


    }


    @Test
    public void testGroup()
    {
        SolrQuery query = new SolrQuery();
        query.setQuery("category:湖北");
        //指定返回域
        query.setParam("fl", "id", "category");

        query.setStart(0); //索引

        query.setRows(20); //每页显示20条
        //query.set("df", "item_keywords"); //默认域

        query.setParam(GroupParams.GROUP, true);

        query.setParam(GroupParams.GROUP_FIELD, "category");

      //  query.setParam(GroupParams.GROUP_QUERY, "weight:[500 TO 800]","weight:[800 TO *]");

        query.setParam(GroupParams.GROUP_LIMIT, "5");

        logger.info("query :{}", query.toString());

        QueryResponse solrResponse = solrJService.query("eakay", query);

        if (solrResponse != null)
        {
            GroupResponse groupResponse = solrResponse.getGroupResponse();

            if (groupResponse != null) {

                List<GroupCommand> groupList = groupResponse.getValues();

                for (GroupCommand groupCommand : groupList) {

                    List<Group> groups = groupCommand.getValues();

                    for (Group group : groups) {

                        System.out.println(group.getGroupValue() + ":" + group.getResult().getNumFound());

                    }

                }

            }
        }

    }



    @Test
    public void testSuggest()
    {

        SolrQuery query = new SolrQuery();
        query.set("qt","/suggest");
        query.set("suggest",true);
        //手动创建索引
       // query.set("suggest.build",true);
        query.set("suggest.dictionary","mySuggester");
        query.set("suggest.q","中");

        query.set("suggest.count",10);



        logger.info("query :{}", query.toString());

        QueryResponse solrResponse = solrJService.query("eakay", query);

        if (solrResponse != null)
        {


            logger.info("response SuggesterResponse:{}",solrResponse.getSuggesterResponse());

            SuggesterResponse suggesterRes = solrResponse.getSuggesterResponse();
            if(suggesterRes!=null)
            {
                Map<String, List<String>> suggestedTerms = suggesterRes.getSuggestedTerms();

                for (Map.Entry<String, List<String>>  entry : suggestedTerms.entrySet())
                {
                    logger.info("response results:{}={}", entry.getKey(),entry.getValue());
                }


            }
        }


    }

}
