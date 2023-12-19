package com.team.araq;

import com.team.araq.user.SiteUser;
import com.team.araq.user.UserRepository;
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
    UserRepository userRepository;

    @Test
    void contextLoads() throws IOException {

        // 관리자 계정 생성
        // this.userService.createAdmin();

        FileInputStream file = new FileInputStream(new File("dummy.xlsx"));

        Workbook workbook = new XSSFWorkbook(file);
        Sheet sheet = workbook.getSheetAt(0);

        for (Row row : sheet) {
            if (row.getRowNum() == 0) continue;

            SiteUser user = new SiteUser();
            user.setNickName(row.getCell(0).getStringCellValue());
            user.setAge(String.valueOf(row.getCell(1).getNumericCellValue()));
            user.setPhoneNum(row.getCell(2).getStringCellValue());
            user.setAddress(row.getCell(3).getStringCellValue());
            user.setGender(row.getCell(4).getStringCellValue());
            user.setUsername(row.getCell(0).getStringCellValue());

            userRepository.save(user);
        }

        file.close();
    }
}
