package com.team.araq;

import jakarta.servlet.RequestDispatcher;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.boot.web.servlet.error.ErrorController;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
public class CustomErrorController implements ErrorController {
    @RequestMapping("/error")
    public String handleError(HttpServletRequest request, Model model) {
        Object status = request.getAttribute(RequestDispatcher.ERROR_STATUS_CODE);
        Object message = request.getAttribute(RequestDispatcher.ERROR_MESSAGE);

        if (status != null) {
            int statusCode = Integer.parseInt(status.toString());
            if (message != null) {
                model.addAttribute("errorMessage", message.toString());
            }
            switch (statusCode) {
                case 400:
                    return "error/error_400";
                case 403:
                    return "error/error_403";
                case 404:
                    return "error/error_404";
                case 500:
                    return "error/error_500";
                default:
                    return "error/default";
            }
        }
        return "error/default";
    }
}
