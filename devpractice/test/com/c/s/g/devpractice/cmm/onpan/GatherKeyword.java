package com.c.s.g.devpractice.cmm.onpan;

import static com.c.s.g.devpractice.cmm.util.CmmUtil.getNvlB;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.filefilter.EmptyFileFilter;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import com.c.s.g.devpractice.cmm.util.CmmUtil;
import com.google.common.collect.Lists;

import lombok.extern.slf4j.Slf4j;

/**
 * <pre>
 * gx2abs.appDev.onlineshoppingmall.searchkeyword 
 *    |_ GatherKeyword.java
 * 
 * SELECT DISTINCT KEYWORD
                , PC_MSRCH_NUM
                , MBL_MSRCH_NUM
                , PC_MAVGCLK_NUM
                , MBL_MAVGCLK_NUM
                , PC_MAVGCLK_PER
                , MBL_MAVGCLK_PER
        FROM KEYTEMP01
       WHERE     (PC_MAVGCLK_PER > 1 OR MBL_MAVGCLK_PER > 1)
             AND (KEYWORD NOT LIKE '%업체%')
             AND (   KEYWORD LIKE '%창문%'
                  OR KEYWORD LIKE '%유리%'
                  OR KEYWORD LIKE '%닦이%')
             AND GROUPNM = 'transparent-clothing-cover'
    ORDER BY MBL_MAVGCLK_NUM DESC, MBL_MSRCH_NUM DESC
    
    
 * 
 * DROP TABLE KEYTEMP01 CASCADE CONSTRAINTS;

    CREATE TABLE KEYTEMP01
    (
      GROUPNM          VARCHAR2(200 BYTE),
      KEYWORD          VARCHAR2(300 BYTE),
      PC_MSRCH_NUM     NUMBER,
      MBL_MSRCH_NUM    NUMBER,
      PC_MAVGCLK_NUM   BINARY_DOUBLE,
      MBL_MAVGCLK_NUM  BINARY_DOUBLE,
      PC_MAVGCLK_PER   BINARY_DOUBLE,
      MBL_MAVGCLK_PER  BINARY_DOUBLE,
      INS_DATE         CHAR(14 CHAR)
    )
    
 * </pre>
 * @date : 2017. 4. 12. 오후 6:31:12
 * @version : 
 * @author : cho.s.g
 * @history : 
 *	-----------------------------------------------------------------------
 *	변경일				작성자						변경내용  
 *	----------- ------------------- ---------------------------------------
 *	2017. 4. 12.		cho.s.g				최초 작성 
 *	-----------------------------------------------------------------------
 */
@Slf4j
public class GatherKeyword {

    public static void main( String[] args ) {
        // TODO Auto-generated method stub
        GatherKeyword test = new GatherKeyword();
        String keyName = "kitchenpaper-rollholder";
        test.insertTempdata( keyName, "C:/Users/ykiki/Gx2abs/99.temp/keywordfile/" + keyName );
    }

    public void insertTempdata( String grpName, String filePath ) {
        Connection conn = null;
        Statement st = null;
        try {
            Class.forName( "oracle.jdbc.driver.OracleDriver" );
            conn = DriverManager.getConnection( "jdbc:oracle:thin:@localhost:1521:orcl", "wanikiki", "apxkf10" );
            conn.setAutoCommit(false);
            
//            String jdbcUrl = "jdbc:mysql://localhost:3306/gx2absdb"
//                    + "?useSSL=false&characterEncoding=utf8&useUnicode=true&useJDBCCompliantTimezoneShift=true&useLegacyDatetimeCode=false&serverTimezone=UTC";
//            Class.forName( "com.mysql.jdbc.Driver" );
//            conn = DriverManager.getConnection( jdbcUrl, "gx2abs", "apxkf" );
            
            st = conn.createStatement( ResultSet.TYPE_SCROLL_SENSITIVE, ResultSet.CONCUR_UPDATABLE );
            for ( File file : FileUtils.listFiles( new File( filePath ), EmptyFileFilter.NOT_EMPTY, EmptyFileFilter.NOT_EMPTY ) ) {
                log.debug( "{}", file.getName() );
                List<List<String>> rowDataList = extractionExcelData( file );
                StringBuffer sbSql = new StringBuffer();
                for ( List<String> row : rowDataList ) {
                    sbSql.setLength( 0 );
                    sbSql.append( "INSERT INTO KEYTEMP01 VALUES ( '" + grpName + "', '" );
                    for ( int x = 0; x < row.size() - 2; x++ ) {
                        if ( x == 0 ) {
                            sbSql.append( getNvlB( row.get( x ) ) ).append( "'" );
                        } else {
                            sbSql.append( "," ).append( row.get( x ) );
                        }
                    }
                    sbSql.append( ", '" ).append( new SimpleDateFormat( "yyyyMMddHHmmss", Locale.KOREA ).format( System.currentTimeMillis() ) );
                    sbSql.append( "') " );
                    
//                    log.debug( "{}", sbSql.toString() );
                    
                    st.executeUpdate( sbSql.toString() );
                    conn.commit();
                }
            }

        } catch ( Exception e ) {
            // TODO Auto-generated catch block
            log.error( "{}", e );
        } finally {
            CmmUtil.objectCloser( st );
            CmmUtil.objectCloser( conn );
        }

    }

    public List<List<String>> extractionExcelData( File excelFile ) {
        FileInputStream fis = null;
        XSSFWorkbook wBook = null;
        List<List<String>> rowDataList = Lists.newArrayList();

        try {
            fis = new FileInputStream( excelFile );
            wBook = new XSSFWorkbook( fis );

            XSSFSheet worksheet = wBook.getSheetAt( 0 );

            int rowIdx = 1, lastCellnum = worksheet.getRow( 0 ).getLastCellNum();
            List<String> rowData;

            while ( rowIdx < worksheet.getPhysicalNumberOfRows() ) {
                //행을읽는다
                XSSFRow row = worksheet.getRow( rowIdx );
                rowData = Lists.newArrayList();
                for ( int x = 0; x < lastCellnum; x++ ) {
                    if ( x > 0 && x < lastCellnum - 1 ) {
                        rowData.add( getNvlB( row.getCell( x ).getStringCellValue() ).replaceAll( "[^0-9\\.]", "" ) );
                    } else {
                        rowData.add( getNvlB( row.getCell( x ).getStringCellValue() ) );
                    }
                }
                rowDataList.add( Lists.newArrayList( rowData ) );
                rowData.clear();
                rowIdx++;
            }
        } catch ( IOException e ) {
            // TODO: handle exception
            log.error( "{}", e );
        } finally {
            CmmUtil.objectCloser( fis );
            CmmUtil.objectCloser( wBook );
        }

        return rowDataList;
    }

}
