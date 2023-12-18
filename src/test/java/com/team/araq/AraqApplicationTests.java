package com.team.araq;

import com.team.araq.user.UserService;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

@SpringBootTest
class AraqApplicationTests {
	@Autowired
	UserService userService;

    @Test
	void contextLoads() throws IOException {
		FileInputStream file = new FileInputStream(new File("dummy.xlsx"));
		Workbook workbook = new XSSFWorkbook(file);

		Sheet sheet = workbook.getSheetAt(0);
		for (Row row : sheet) {
			if (row.getRowNum() == 0) continue;

			userService.createTmp(row.getCell(0).getStringCellValue(),
					String.valueOf((int)row.getCell(1).getNumericCellValue()),
					row.getCell(2).getStringCellValue(),
					row.getCell(3).getStringCellValue(),
					row.getCell(4).getStringCellValue());
		}
	}

}
