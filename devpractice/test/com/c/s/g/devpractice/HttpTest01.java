package com.c.s.g.devpractice;

import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.http.client.fluent.Content;
import org.apache.http.client.fluent.Form;
import org.apache.http.client.fluent.Request;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.json.JsonSimpleJsonParser;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * <pre>
 * com.c.s.g.devpractice 
 *    |_ HttpTest01.java
 * 
 * 
 * 
 * </pre>
 * 
 * @author : jadetech01
 * @version : 1.0
 * @since : 2018. 8. 7.
 *
 */
public class HttpTest01 {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    public static void main(String[] args) {
//        HttpTest01 test = new HttpTest01();
//        test.setProductMap();
        try {
            Document doc = Jsoup.connect("http://onpan3.getmall.kr/front/productdetail.php?productcode=001001001000000156").get();
            for(Object obj1 : doc.select(".basic_select option[value]").toArray()) {
                ObjectMapper mapper =  new ObjectMapper();
                JsonSimpleJsonParser parser = new JsonSimpleJsonParser();
                Content cntn = Request.Post("http://onpan3.getmall.kr/templet/option/opt_combination_info.php").bodyForm(Form.form().add("productcode", "001001001000000156").add("type", "check").add("opt_combi", Jsoup.parse(String.valueOf(obj1)).select("option").val()).build()).execute().returnContent();
                Map<String, String> mp = mapper.readValue(cntn.asString(), new TypeReference<Map<String, String>>(){});
                for(Map.Entry<String, String> en : mp.entrySet()) {
                    System.out.println(en.getKey() + " : " + en.getValue());
                }
                
            }
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    public void setProductMap() {
        Map<String, String> prodMp = new LinkedHashMap<>(); //제품정보 
        for(int x = 0; x < getMaxPagenum(); x++) {
            if(x % 5 == 0) {
                System.out.println("pageNum : " + (x+1));
            }
            try {
                Document doc = Jsoup.connect("http://onpan3.getmall.kr/front/codesearch.popup.php?gotopage=" + (x+1)).get();
                for (Object obj1 : doc.select("table[bgcolor] tr[onmouseover]").toArray()) {
                    if (String.valueOf(obj1).split("td").length > 3) {
                        String prodNm = String.valueOf(obj1).split("td")[3].replaceAll(">([^<]+)<.+", "$1");
                        String prodCode = String.valueOf(obj1).split("td")[3].replaceAll(".+\\<span[^>]+>([^<]+).+", "$1");
                        if(prodMp.containsKey(prodCode)) {
                            System.out.println("dupl prod code : " + prodCode);
                        } else {
                            prodMp.put(prodCode, prodNm);
                        }
                    }
                }
            } catch (Exception e) {
                // TODO: handle exception
                e.printStackTrace();
            }
        }

        for(Map.Entry<String, String> en : prodMp.entrySet()) {
            System.out.println(en.getKey() + " :: " + en.getValue());
        }
    }

    public int getMaxPagenum() {
        int maxNum = 0;
        try {
            Document doc = Jsoup.connect("http://onpan3.getmall.kr/front/codesearch.popup.php?gotopage=1").get();
            for (Object obj : doc.select("a[href]").toArray()) {
                if (String.valueOf(obj).indexOf("GoPage") != -1) {
                    maxNum = Math.max(maxNum, Integer.parseInt(String.valueOf(obj).replaceAll(".+GoPage\\([0-9]+,([0-9]+)\\).+", "$1")));
                }
            }
        } catch (Exception e) {
            // TODO: handle exception
            e.printStackTrace();
        }
        return maxNum;
    }
}
