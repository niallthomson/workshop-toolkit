package com.devcodes.workshopkit.web;

import com.devcodes.workshopkit.config.WorkshopConfig;
import com.devcodes.workshopkit.util.ILandingRedirectProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import javax.servlet.http.HttpServletRequest;

@Controller
public class HomeController {

    @Autowired
    private WorkshopConfig workshopConfig;

    @Autowired
    private ILandingRedirectProvider redirectProvider;

    @GetMapping("/")
    public String index(Model model) {
        return authPage(model, false);
    }

    @PostMapping("/")
    public String landing(@RequestParam("accessCode") String accessCode, Model model) {
        if(!workshopConfig.getAccessCode().equals(accessCode)) {
            return authPage(model, true);
        }

        return "redirect:"+redirectProvider.getRedirectUrl();
    }

    @GetMapping("/info")
    public String infoGet(Model model) {
        return authPage(model, false);
    }

    @PostMapping("/info")
    public String infoPost(@RequestParam("accessCode") String accessCode, Model model, HttpServletRequest request) {
        if(!workshopConfig.getAccessCode().equals(accessCode)) {
            return authPage(model, true);
        }

        model.addAttribute("name", workshopConfig.getName());
        model.addAttribute("accessCode", this.workshopConfig.getAccessCode());
        model.addAttribute("publicUrl", getPublicUrl(request));

        return "info";
    }

    public String authPage(Model model, boolean failed) {
        model.addAttribute("name", workshopConfig.getName());

        if(failed) {
            model.addAttribute("error", "Wrong access code");
        }

        return "auth";
    }

    public String getPublicUrl(HttpServletRequest request) {
        String forwardedProto = request.getHeader("X-Forwarded-Proto");
        String forwardedPort = request.getHeader("X-Forwarded-Port");

        String proto = forwardedProto == null ? ""+request.getScheme() : forwardedProto;
        String port = forwardedPort == null ? ""+request.getServerPort() : forwardedPort;

        if(port.equals("80") && proto.equals("http") || port.equals("443") && proto.equals("https")) {
            port = "";
        }
        else {
            port = ":"+port;
        }

        return new StringBuilder().append(proto).append("://").append(request.getServerName()).append(port).toString();
    }
}
