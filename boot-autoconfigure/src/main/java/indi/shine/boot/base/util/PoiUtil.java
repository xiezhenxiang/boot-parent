package indi.shine.boot.base.util;

import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

public class PoiUtil {

    /**
     * 读取excel, 返回list集合
     * @author xiezhenxiang 2019/5/30
     * @param fileIn 文件流
     * @param fileName 文件名
     **/
    public static List<List<String>> readExcel(InputStream fileIn, String fileName) {
        return readExcel(fileIn, fileName, 0);
    }

    /**
     * 读取excel, 返回list集合
     * @author xiezhenxiang 2019/5/30
     * @param fileIn 文件流
     * @param fileName 文件名
     * @param refer 表格列数参照行
     **/
    public static List<List<String>> readExcel(InputStream fileIn, String fileName, int refer) {

        Row row = null;
        List<List<String>> list = null;
        Workbook wb = getWorkBook(fileIn, fileName);
        if (wb != null) {
            //用来存放表中数据
            list = new ArrayList<>();
            //获取第一个sheet
            Sheet sheet = wb.getSheetAt(0);
            //获取最大行数
            int rowNum = sheet.getLastRowNum();
            row = sheet.getRow(refer);
            //获取最大列数
            int colNum = row.getPhysicalNumberOfCells();
            for (int i = 0; i <= rowNum; i ++) {
                List<String> cellList = new ArrayList<>();
                row = sheet.getRow(i);
                if (row != null) {
                    for (int j = 0; j < colNum; j ++) {
                        String value = (String) getCellFormatValue(row.getCell(j));
                        cellList.add(value);
                    }
                } else {
                    for (int j = 0; j < colNum; j ++) {
                        cellList.add("");
                    }
                }
                list.add(cellList);
            }
        }

        return list;
    }

    public static Workbook getWorkBook(InputStream fileIn, String name) {
        try {
            if (name.endsWith(".xls")) {
                return new HSSFWorkbook(fileIn);
            } else if (name.endsWith(".xlsx")) {
                return new XSSFWorkbook(fileIn);
            } else {
                return null;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public static Object getCellFormatValue(Cell cell) {
        cell.setCellType(Cell.CELL_TYPE_STRING);
        return cell.getStringCellValue();
    }

}
