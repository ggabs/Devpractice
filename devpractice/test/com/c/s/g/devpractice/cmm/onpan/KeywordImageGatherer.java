package com.c.s.g.devpractice.cmm.onpan;


import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLDecoder;
import java.net.URLEncoder;

import javax.net.ssl.HttpsURLConnection;

import org.apache.commons.io.FileUtils;

import com.c.s.g.devpractice.cmm.util.CmmUtil;

import lombok.extern.slf4j.Slf4j;


/**
 * <pre>
 * gx2abs.appDev.test.img 
 *    |_ KeywordImageGatherer.java
 * 
 * 
 * 
 * </pre>
 * @date : 2017. 4. 18. 오후 1:39:01
 * @version : 
 * @author : cho.s.g
 * @history : 
 *	-----------------------------------------------------------------------
 *	변경일				작성자						변경내용  
 *	----------- ------------------- ---------------------------------------
 *	2017. 4. 18.		cho.s.g				최초 작성 ㅐㅑ
 *	-----------------------------------------------------------------------
 */
@Slf4j
public class KeywordImageGatherer {

    public static void main( String[] args ) {
        String[] keywordList = {
                "베드민턴","공격형배드민턴라켓","배드민터라켓","배드민턴","배드민턴라켓","배드민턴라켓종류","배드민턴라켓추천","배드민턴몰","배드민턴세트","배드민턴쇼핑몰","배드민턴용품","배드민턴용품점","배드민턴채","배드민턴채추천","배드민턴체","배트민턴라켓","베드민턴라켓","베드민턴마켓","베드민턴채","입문용배드민턴라켓","입문자용배드민턴라켓"
        };
        
        //        String imgUrl = "http://search.de.phinf.net/common/?src=http%3A%2F%2Fadimg.search.naver.net%2Fo%2Fncc_shopping_201703%2F31%2Fnad-a001-02-000000010923768_a70aa73b-5f89-4cf1-844c-f4f61f751077.png&type=w&size=200";
        //        try {
        //            System.out.println( URLDecoder.decode( imgUrl.replaceAll( ".+data-original=\"([^\"]+)\".+", "$1" ), "UTF-8" ).replaceAll( ".+/", "" ).replaceAll( ".+\\.([a-zA-Z]+)[\\?\\&].+", "$1" ) );
        //        } catch ( UnsupportedEncodingException e ) {
        //            // TODO Auto-generated catch block
        //            e.printStackTrace();
        //        }

        //        try {
        //            System.out.println( URLEncoder.encode( "PVC옷커버", "UTF-8" ) );
        //        } catch ( UnsupportedEncodingException e ) {
        //            // TODO Auto-generated catch block
        //            e.printStackTrace();
        //        }

        KeywordImageGatherer test = new KeywordImageGatherer();
        for ( String keyword : keywordList ) {
            test.getKeywordImg( keyword );
        }

        //        test.test();
    }

    private String strBasePath = "C:/Users/ykiki/Gx2abs/99.temp/keywordimagegatherer/배드민턴라켓/";

    public void test() {

        URL url;
        try {

            File page = new File( strBasePath + CmmUtil.genTSName( "pagesource-", "txt" ) );

            String https_url = "https://search.shopping.naver.com/search/all.nhn?query=PVC%EC%98%B7%EC%BB%A4%EB%B2%84&cat_id=&frm=NVSHATC";
            url = new URL( https_url );
            HttpsURLConnection con = (HttpsURLConnection) url.openConnection();
            FileUtils.copyInputStreamToFile( con.getInputStream(), page );

        } catch ( MalformedURLException e ) {
            e.printStackTrace();
        } catch ( IOException e ) {
            e.printStackTrace();
        }

    }

    public void getKeywordImg( String keyword ) {
        BufferedReader br = null;
        String lineStr, strImgUrl;
        File page = new File( strBasePath + CmmUtil.genTSName( "pagesource-", "txt" ) );
        File destPath = new File( strBasePath + keyword );
        try {
            log.debug( "{}", keyword );
            String https_url = "https://search.shopping.naver.com/search/all.nhn?query=" + URLEncoder.encode( keyword, "UTF-8" )
                    + "&cat_id=&frm=NVSHATC";
            URL url = new URL( https_url );

            HttpsURLConnection con = (HttpsURLConnection) url.openConnection();
            FileUtils.copyInputStreamToFile( con.getInputStream(), page );
            //            FileUtils.copyURLToFile( new URL( "http://shopping.naver.com/search/all.nhn?query=" + keyword + "&cat_id=&frm=NVSHATC" ), page );
            br = new BufferedReader( new FileReader( page ) );
            destPath = new File( strBasePath + keyword );
            if ( !destPath.exists() ) {
                destPath.mkdirs();
            } else {
                FileUtils.cleanDirectory( destPath );
            }
            while ( ( lineStr = br.readLine() ) != null ) {
                if ( lineStr.indexOf( "_productLazyImg" ) != -1 && lineStr.indexOf( "placeholder.png" ) == -1 ) {
                    strImgUrl = lineStr.replaceAll( ".+data-original=\"([^\"]+)\".+", "$1" );
                    if ( strImgUrl.indexOf( "src=" ) != -1 ) {
                        strImgUrl = URLDecoder.decode( strImgUrl, "UTF-8" );
                    }
                    getImageFromUrl( strImgUrl, destPath );
                }
            }
        } catch ( Exception e ) {
            log.error( "{}", e );
        } finally {
            try {
                FileUtils.forceDeleteOnExit( page );
            } catch ( IOException e ) {
                // TODO Auto-generated catch block
                log.error( "{}", e );
            }
            CmmUtil.objectCloser( br );
        }
    }

    private String getImageFromUrl( String imgUrl, File destPath ) {
        String resultFullPath = "";
        try {
            resultFullPath = destPath.getAbsolutePath() + File.separator
                    + CmmUtil.genTSName( "item-", imgUrl.replaceAll( ".+/", "" ).replaceAll( ".+\\.([a-zA-Z]+)[\\?\\&].+", "$1" ) );
            FileUtils.copyURLToFile( new URL( imgUrl ), new File( resultFullPath ), 20000, 20000 );
        } catch ( IOException e ) {
            // TODO Auto-generated catch block
            log.error( "{}", e );
            resultFullPath = "";
        }
        return resultFullPath;
    }

}
