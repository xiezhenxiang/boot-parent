package indi.fly.boot.base.util;

import org.apache.commons.io.FileUtils;
import org.apache.poi.hssf.usermodel.*;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.DateUtil;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.*;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

public class PoiUtil {
    public static List<List<String>> importXls(InputStream fileIn, String name) {
        return importXls(fileIn, name, 0);
    }


    public static List<List<String>> importXls(InputStream fileIn, String name, int x) {
        Workbook wb = null;
        Sheet sheet = null;
        org.apache.poi.ss.usermodel.Row row = null;
        List<List<String>> list = null;
        String cellData = null;
        wb = readExcel(fileIn, name);
        if (wb != null) {
            //用来存放表中数据
            list = new ArrayList<>();
            //获取第一个sheet
            sheet = wb.getSheetAt(0);
            //获取最大行数
            int rownum = sheet.getLastRowNum();
            //获取第一行
            row = sheet.getRow(x);
            //获取最大列数
            int colnum = row.getPhysicalNumberOfCells();
            for (int i = 0; i <= rownum; i++) {
                List<String> cellList = new ArrayList<>();
                row = sheet.getRow(i);
                if (row != null) {
                    for (int j = 0; j < colnum; j++) {
                        cellData = (String) getCellFormatValue(row.getCell(j));
                        cellList.add(cellData);
                    }
                } else {
                    for (int j = 0; j < colnum; j++) {
                        cellList.add("");
                    }
                }
                list.add(cellList);
            }
        }

        return list;
    }

    //读取excel
    public static Workbook readExcel(InputStream fileIn, String name) {
        Workbook wb = null;
        InputStream is = fileIn;
        try {
            if (name.endsWith(".xls")) {
                return wb = new HSSFWorkbook(is);
            } else if (name.endsWith(".xlsx")) {
                return wb = new XSSFWorkbook(is);
            } else {
                return wb = null;
            }

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return wb;
    }

    public static Object getCellFormatValue(Cell cell) {
        Object cellValue = null;
        if (cell != null) {
            //判断cell类型
            switch (cell.getCellType()) {
                case Cell.CELL_TYPE_NUMERIC: {
                    //判断cell是否为日期格式
                    if (DateUtil.isCellDateFormatted(cell)) {
                        //转换为日期格式YYYY-mm-dd
                        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
                        cellValue = sdf.format(HSSFDateUtil.getJavaDate(cell.getNumericCellValue()));
                    } else {
                        //数字
                        double dob = cell.getNumericCellValue();
                        if (TypeCheckUtil.whitDecimalPointIntCheck(dob)) {
                            cellValue = (int) dob + "";
                        } else {
                            cellValue = String.valueOf(dob);
                        }
                    }
                    break;
                }
                case Cell.CELL_TYPE_FORMULA: {
                    //判断cell是否为日期格式
                    if (DateUtil.isCellDateFormatted(cell)) {
                        //转换为日期格式YYYY-mm-dd
                        cellValue = cell.getDateCellValue();
                    } else {
                        //数字
                        cellValue = String.valueOf(cell.getNumericCellValue());
                    }
                    break;
                }
                case Cell.CELL_TYPE_STRING: {
                    cellValue = cell.getRichStringCellValue().getString();
                    break;
                }
                default:
                    cellValue = "";
            }
        } else {
            cellValue = "";
        }
        return cellValue;
    }

    public static Boolean outPutXls(List<List<String>> datas, String errPath) {
        List<String> title = datas.get(0);

        // 第一步，创建一个HSSFWorkbook，对应一个Excel文件
        HSSFWorkbook wb = new HSSFWorkbook();

        // 第二步，在workbook中添加一个sheet,对应Excel文件中的sheet
        HSSFSheet sheet = wb.createSheet();

        // 第三步，在sheet中添加表头第0行,注意老版本poi对Excel的行数列数有限制
        HSSFRow row = sheet.createRow(0);

        // 第四步，创建单元格，并设置值表头 设置表头居中
        HSSFCellStyle style = wb.createCellStyle();
        style.setAlignment(HSSFCellStyle.ALIGN_CENTER); // 创建一个居中格式

        //声明列对象
        HSSFCell cell = null;

        //创建标题
        for (int i = 0; i < title.size(); i++) {
            cell = row.createCell(i);
            cell.setCellValue(title.get(i));
            cell.setCellStyle(style);
        }

        //创建内容
        for (int i = 1; i < datas.size(); i++) {
            row = sheet.createRow(i);
            List<String> data = datas.get(i);
            for (int j = 0; j < data.size(); j++) {
                //将内容按顺序赋给对应的列对象
                row.createCell(j).setCellValue(data.get(j));
            }
        }
        //创建excel文件
        File file = new File(errPath);
        try {
            file.createNewFile();
            //将excel写入
            FileOutputStream stream = FileUtils.openOutputStream(file);
            wb.write(stream);
            stream.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return true;

    }


}
