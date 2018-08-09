package com.c.s.g.devpractice.cmm.util;


import java.io.Closeable;
import java.io.File;
import java.io.Flushable;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.math.BigDecimal;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.TimeZone;

import org.apache.commons.beanutils.MethodUtils;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class CmmUtil {

    /**
     * <pre>
     * 날짜포맷 변환
     * </pre>
     * @date : 2017. 2. 3.
     * @author : cho.s.g
     * 
     * @param fmt
     * @param dateVal
     * @return
     */
    public static String getDateString( String fmt, Object dateVal ) {
        return new SimpleDateFormat( fmt, Locale.KOREA ).format( dateVal );
    }

    /**
     * <pre>
     * "," 문자포함 split
     * </pre>
     * 
     * @date : 2016. 11. 16.
     * @author : cho.s.g
     * 
     * @param codeStr
     * @return
     */
    public static Object[] splitCmmCode( Object codeObj ) {
        List<String> result = new ArrayList<String>();
        StringBuffer sb = new StringBuffer();
        for ( String c : getNvlB( codeObj ).split( "" ) ) {
            if ( ",".equals( c ) ) {
                if ( sb.indexOf( "(" ) != -1 && sb.indexOf( ")" ) != -1 ) {
                    result.add( sb.toString() );
                    sb.setLength( 0 );
                } else if ( sb.indexOf( "(" ) != -1 ) {
                    sb.append( c );
                } else {
                    result.add( sb.toString() );
                    sb.setLength( 0 );
                }
            } else {
                sb.append( c );
            }
        }
        result.add( sb.toString() );
        return result.toArray();
    }

    /**
     * <pre>
     * Null Check
     * null -> 0
     * </pre>
     * 
     * @date : 2016. 10. 7.
     * @author : cho.s.g
     * 
     * @param data
     * @return
     */
    public static BigDecimal getNvl( Object data ) {
        BigDecimal returnObj;
        if ( data instanceof String || data instanceof Number ) {
            String strValue = getNvlB( data ).replaceAll( "[^0-9]", "" );
            if ( "".equals( strValue ) ) {
                strValue = "0";
            }
            returnObj = new BigDecimal( String.valueOf( strValue ) );
        } else {
            returnObj = new BigDecimal( 0 );
        }
        return returnObj;
    }

    /**
     * <pre>
     * Null Check
     * null -> ""
     * </pre>
     * 
     * @date : 2016. 10. 7.
     * @author : cho.s.g
     * 
     * @param data
     * @return
     */
    public static String getNvlB( Object data ) {
        String returnObj;
        if ( data instanceof String ) {
            returnObj = (String) data;
        } else if ( data instanceof Number ) {
            returnObj = String.valueOf( data );
        } else {
            returnObj = "";
        }
        return returnObj.trim();
    }

    /**
     * <pre>
     * 월주차 획득 ISO-8601
     * </pre>
     * 
     * @date : 2016. 11. 25.
     * @author : cho.s.g
     * 
     * @param fmt
     * @param date
     * @return week : 주차, sdate 주차시작일, edate 주차마지막일
     */
    public static Map<String, String> getDateToMonthWeek( String fmt, String date ) {

        Map<String, String> resultMap = new HashMap<String, String>();
        Calendar calendar = new GregorianCalendar();
        calendar.setFirstDayOfWeek( Calendar.MONDAY );
        calendar.setMinimalDaysInFirstWeek( 4 );
        calendar.setTimeZone( TimeZone.getTimeZone( "Asia/Seoul" ) );

        // calendar.get(idx)
        // 1 연
        // 2 월
        // 3 주차(연)
        // 4 주차(월)
        // 5 일(월)
        // 6 일(연)
        // 7 요일
        // 8 월
        // 9
        try {
            calendar.setTime( new SimpleDateFormat( fmt, Locale.KOREA ).parse( date ) );
            int year = calendar.get( 1 );
            int month = calendar.get( 2 );
            int yearWeek = calendar.get( 3 );
            int mWeek = calendar.get( 4 );

            month += 1; // 월이 0~11..

            if ( mWeek == 0 ) {
                if ( month == 1 ) {
                    year -= 1;
                    month = 12;
                } else {
                    month -= 1;
                }

                calendar.setTime( new SimpleDateFormat( "yyyyMMdd", Locale.KOREA ).parse( year + "" + month + "31" ) );
                mWeek = calendar.get( 4 );
            }
            resultMap.put( "weekNm", year + "" + ( month < 10 ? "0" + month : month ) + "-W" + mWeek );
            calendar.set( 7, 2 );
            resultMap.put( "sdate", new SimpleDateFormat( fmt, Locale.KOREA ).format( calendar.getTime() ) );
            calendar.set( 7, 1 );
            resultMap.put( "edate", new SimpleDateFormat( fmt, Locale.KOREA ).format( calendar.getTime() ) );
        } catch ( ParseException e ) {
            log.error( "{}", e );
        }
        return resultMap;
    }

    /**
     * <pre>
     * Object close
     * </pre>
     * 
     * @date : 2016. 10. 31.
     * @author : cho.s.g
     * 
     * @param obj
     * @throws Throwable
     */
    public static void objectCloser( Object obj ) {
        try {
            if ( obj instanceof Closeable ) {
                if ( obj instanceof Flushable ) {
                    ( (Flushable) obj ).flush();
                }
                ( (Closeable) obj ).close();
            } else if ( obj instanceof AutoCloseable ) {
                ( (AutoCloseable) obj ).close();
            } else {
                log.error( "[objectCloser] :: can not close object" );
            }
        } catch ( Exception e ) {
            log.error( "[objectCloser] :: object close fail : " + e );
        }
    }

    /**
     * <pre>
     * 날짜포맷처리 메소드
     * </pre>
     * 
     * @date : 2017. 1. 5.
     * @author : cho.s.g
     * 
     * @param dateStr
     * @param insFmtStr
     * @param rtnFmtStr
     * @return
     */
    public static String convertDateToStr( String dateStr, String insFmtStr, String rtnFmtStr ) {
        String result = null;
        try {
            result = new SimpleDateFormat( rtnFmtStr, Locale.KOREA ).format( new SimpleDateFormat( insFmtStr, Locale.KOREA ).parse( dateStr ) );
        } catch ( ParseException e ) {
            log.error( "convert date error : {}", e );
        }
        return result;
    }

    /**
     * <pre>
     * Timestamp 기반 이름 생성 메소드
     * </pre>
     * 
     * @date : 2016. 11. 1.
     * @author : cho.s.g
     * 
     * @return
     * @throws Throwable
     */
    public static String genTSName( String baseName, String type ) {
        return baseName + new SimpleDateFormat( "yyyyMMdd-HHmmssSSS", Locale.KOREA ).format( System.currentTimeMillis() ) + "." + type;
    }

    /**
     * <pre>
     * vo 객체의 필드값을 map으로 추출
     * </pre>
     * 
     * @date : 2017. 1. 3.
     * @author : cho.s.g
     * 
     * @param voObj
     * @param argsParam
     * @return
     */
    public static Map<String, Object> convertVoToMap( Object voObj, Map<String, Object> argsParam ) {
        Class<?> clazz = voObj.getClass();
        for ( Field f : clazz.getDeclaredFields() ) {
            for ( Method method : clazz.getMethods() ) {
                String methodNm = method.getName();
                if ( methodNm.indexOf( "get" ) != -1 ) {
                    if ( method.getName().replace( "get", "" ).equalsIgnoreCase( f.getName() ) ) {
                        Object result;
                        try {
                            result = MethodUtils.invokeMethod( voObj, method.getName(), null );
                            if ( result instanceof Object ) {
                                argsParam.put( f.getName(), result );
                            }
                        } catch ( NoSuchMethodException e ) {
                            // TODO Auto-generated catch block
                            log.error("convertVoToMap error : {}", e);
                        } catch ( IllegalAccessException e ) {
                            // TODO Auto-generated catch block
                            log.error("convertVoToMap error : {}", e);
                        } catch ( InvocationTargetException e ) {
                            // TODO Auto-generated catch block
                            log.error("convertVoToMap error : {}", e);
                        }
                    }
                }
            }
        }
        return argsParam;
    }

    /**
     * <pre>
     * 날짜포맷 검증
     * </pre>
     * 
     * @date : 2017. 1. 6.
     * @author : cho.s.g
     * 
     * @param chkFmtstr
     * @param dateStr
     * @return
     */
    public static boolean chkDateFormat( String chkFmtstr, String dateStr ) {
        boolean chkResult = true;
        try {
            SimpleDateFormat fmt = new SimpleDateFormat();
            fmt.applyPattern( chkFmtstr );
            fmt.parse( dateStr );
        } catch ( ParseException e ) {
            chkResult = false;
        }
        return chkResult;
    }

    /**
     * <pre>
     * 파일 경로 생성 및 권한 설정
     * </pre>
     * @date : 2017. 2. 14.
     * @author : cho.s.g
     * 
     * @param chkDir
     * @return
     */
    public static boolean chkMkdirs( String dirPath ) {
        File chkDir = new File( dirPath );
        boolean result = false;
        if ( !chkDir.exists() ) {
            chkDir.mkdirs();
            chkDir.setExecutable( false, true );
            chkDir.setReadable( true );
            chkDir.setWritable( false, true );
            result = true;
        } else {
            result = true;
        }
        return result;
    }

}
