package com.team.araq.pay;

import com.team.araq.user.SiteUser;
import com.team.araq.user.UserService;
import jakarta.persistence.criteria.*;
import lombok.RequiredArgsConstructor;
import org.json.JSONObject;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import javax.net.ssl.HttpsURLConnection;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.URL;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class PaymentService {

    private final PaymentRepository paymentRepository;

    private final UserService userService;

    private String impKey = "4323773555508561";

    private String impSecret = "y46aKg8JTOLBqyKtcM04uAnLPeJ4PfrIEiHIP1vymIVaK91zGwQNJibao4Dw2mOxAlmvCiv37Yq1ueSc";

    private Specification<Payment> search(String kw) {
        return new Specification<Payment>() {
            @Override
            public Predicate toPredicate(Root<Payment> payment, CriteriaQuery<?> query, CriteriaBuilder criteriaBuilder) {
                query.distinct(true);
                Join<Payment, SiteUser> u1 = payment.join("user", JoinType.LEFT);
                return criteriaBuilder.or(criteriaBuilder.like(payment.get("orderNum"), "%" + kw + "%"),
                        criteriaBuilder.like(u1.get("nickName"), "%" + kw + "%"),
                        criteriaBuilder.like(u1.get("username"), "%" + kw + "%"));
            }
        };
    }

    private String getToken() throws Exception {
        URL url = new URL("https://api.iamport.kr/users/getToken");
        HttpsURLConnection conn = (HttpsURLConnection) url.openConnection();

        conn.setRequestMethod("POST");
        conn.setRequestProperty("Content-type", "application/json");
        conn.setDoOutput(true);

        String payload = "{\"imp_key\":\"" + impKey + "\", \"imp_secret\":\"" + impSecret + "\"}";
        try (OutputStream os = conn.getOutputStream()) {
            byte[] input = payload.getBytes("utf-8");
            os.write(input, 0, input.length);
        }

        StringBuilder response = new StringBuilder();
        try (BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream(), "utf-8"))) {
            String responseLine;
            while ((responseLine = br.readLine()) != null) {
                response.append(responseLine.trim());
            }
        }

        JSONObject jsonObject = new JSONObject(response.toString());
        return jsonObject.getJSONObject("response").getString("access_token");
    }

    public JSONObject getInfo(String impUid) throws Exception {
        HttpsURLConnection conn = (HttpsURLConnection) new URL("https://api.iamport.kr/payments/" + impUid).openConnection();
        conn.setRequestMethod("GET");
        conn.setRequestProperty("Authorization", getToken());

        BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream(), "utf-8"));
        JSONObject response = new JSONObject(br.readLine()).getJSONObject("response");
        br.close();

        return response;
    }

    public Page<Payment> getList(int page, String kw) {
        List<Sort.Order> sort = new ArrayList<>();
        sort.add(Sort.Order.desc("date"));
        Pageable pageable = PageRequest.of(page, 10, Sort.by(sort));
        Specification<Payment> specification = search(kw);
        return this.paymentRepository.findAll(specification, pageable);
    }

    public Payment getPayment(String impUid) {
        Optional<Payment> payment = this.paymentRepository.findByImpUid(impUid);
        if (payment.isPresent()) return payment.get();
        else throw new RuntimeException("그런 결제 없습니다.");
    }

    public void savePayment(PaymentDTO paymentDTO) {
        Payment pay = new Payment();
        pay.setDate(paymentDTO.getDate());
        pay.setUser(this.userService.getByUsername(paymentDTO.getUsername()));
        pay.setAmount(paymentDTO.getAmount());
        pay.setMethod(paymentDTO.getMethod());
        pay.setOrderNum(paymentDTO.getOrderNum());
        pay.setDate(LocalDateTime.now());
        pay.setCard(paymentDTO.getCard());
        pay.setImpUid(paymentDTO.getImpUid());
        pay.setStatus(paymentDTO.getStatus());
        pay.setPg(paymentDTO.getPg());
        pay.setBubble(paymentDTO.getBubble());
        this.paymentRepository.save(pay);
    }

    public String cancelPayment(String impUid) throws Exception {
        URL url = new URL("https://api.iamport.kr/payments/cancel");
        HttpsURLConnection conn = (HttpsURLConnection) url.openConnection();
        conn.setRequestMethod("POST");
        conn.setRequestProperty("Content-type", "application/json");
        conn.setRequestProperty("Authorization", getToken());
        conn.setDoOutput(true);
        String payload = "{\"imp_uid\": \"" + impUid + "\"}";
        try (OutputStream os = conn.getOutputStream()) {
            byte[] input = payload.getBytes("utf-8");
            os.write(input, 0, input.length);
        }
        StringBuilder response = new StringBuilder();
        try (BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream(), "utf-8"))) {
            String responseLine;
            while ((responseLine = br.readLine()) != null) {
                response.append(responseLine.trim());
            }
        }
        return response.toString();
    }

    public void updatePayment(Payment payment, String impUid) throws Exception {
        JSONObject object = getInfo(impUid);
        String status = object.getString("status");
        payment.setStatus(status);
        payment.setCancelDate(LocalDateTime.now());
        this.paymentRepository.save(payment);
    }

    public List<Payment> getListByUser(SiteUser user) {
        return this.paymentRepository.findTop3ByUserOrderByDateDesc(user);
    }
}