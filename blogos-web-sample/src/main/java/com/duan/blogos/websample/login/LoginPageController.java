package com.duan.blogos.websample.login;

import com.duan.blogos.service.dto.blogger.BloggerBriefDTO;
import com.duan.blogos.service.properties.WebsiteProperties;
import com.duan.blogos.service.service.website.WebSiteStatisticsService;
import com.duan.blogos.util.common.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;

import java.util.List;

/**
 * Created on 2018/2/8.
 *
 * @author DuanJiaNing
 */
@Controller
@RequestMapping("/login")
public class LoginPageController {

    @Autowired
    private WebSiteStatisticsService webSiteStatisticsService;

    @Autowired
    private WebsiteProperties websiteProperties;

    @RequestMapping
    public ModelAndView loginPage(@RequestParam(value = "activeBloggerCount", required = false) Integer activeCount) {
        ModelAndView mv = new ModelAndView();
        mv.setViewName("blogger/login");

        if (activeCount == null || activeCount < 0)
            activeCount = websiteProperties.getWebsiteActiveBloggerCount();

        List<BloggerBriefDTO> dtos = webSiteStatisticsService.listActiveBlogger(activeCount);
        if (!CollectionUtils.isEmpty(dtos)) {
            mv.addObject("briefs", dtos);
        }

        return mv;
    }
}