package com.team.araq.sms;

import net.nurigo.java_sdk.api.Message;
import net.nurigo.java_sdk.exceptions.CoolsmsException;
import org.json.simple.JSONObject;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Random;

@Service
public class SmsService {
    private String api_key = "NCSKYFP3SY1QDLVF";
    private String api_secret = "XAJQ2DL64BS9PYK0GRYVFJDEICKU2BRL";

    public void sendSms(String phoneNum, String verificationCode) {
        Message coolsms = new Message(api_key, api_secret);
        HashMap<String, String> params = new HashMap<String, String>();

        params.put("to", phoneNum);
        params.put("from", "01081261630");
        params.put("type", "SMS");
        params.put("text", "araQ 인증번호는" + "[" + verificationCode + "]" + "입니다.");

        try {
            JSONObject obj = (JSONObject) coolsms.send(params);
        } catch (CoolsmsException e) {
            e.printStackTrace();
        }
    }

    public String createRandomNum() {
        Random rand = new Random();
        String randomNum = "";
        for (int i = 0; i < 6; i++) {
            String random = Integer.toString(rand.nextInt(10));
            randomNum += random;
        }
        System.out.println(randomNum);
        return randomNum;
    }
}
