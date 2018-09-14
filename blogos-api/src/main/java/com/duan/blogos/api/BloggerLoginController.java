package com.duan.blogos.api;

import com.duan.blogos.api.blogger.BaseBloggerController;
import com.duan.blogos.service.dto.blogger.BloggerAccountDTO;
import com.duan.blogos.service.exception.CodeMessage;
import com.duan.blogos.service.exception.ResultUtil;
import com.duan.blogos.service.restful.ResultBean;
import com.duan.blogos.service.service.blogger.BloggerAccountService;
import com.duan.blogos.util.common.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.RequestContext;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.math.BigInteger;
import java.security.NoSuchAlgorithmException;

/**
 * Created on 2018/1/11.
 * 博主登录
 * <p>
 * 1 用户名登录
 * 2 电话号码登录
 *
 * @author DuanJiaNing
 */
@RestController
@RequestMapping("/blogger/login")
public class BloggerLoginController extends BaseBloggerController {

    @Autowired
    private BloggerAccountService accountService;

    @RequestMapping(value = "/way=name", method = RequestMethod.POST)
    public ResultBean loginWithUserName(HttpServletRequest request,
                                        @RequestParam("username") String userName,
                                        @RequestParam("password") String password) throws NoSuchAlgorithmException {
        // update 使用shiro

        BloggerAccountDTO account = accountService.getAccount(userName);

        // 用户不存在
        if (account == null) {
            throw ResultUtil.failException(CodeMessage.BLOGGER_UNKNOWN_BLOGGER);
        }

        // 密码错误
        if (!account.getPassword().equals(new BigInteger(StringUtils.toSha(password)).toString())) {
            throw ResultUtil.failException(CodeMessage.BLOGGER_PASSWORD_INCORRECT);
        }

        HttpSession session = request.getSession();
        session.setAttribute(bloggerProperties.getSessionNameOfBloggerId(), account.getId());
        session.setAttribute(bloggerProperties.getSessionNameOfBloggerName(), account.getUsername());
        session.setAttribute(bloggerProperties.getSessionBloggerLoginSignal(), "login");

        // 成功登录
        return new ResultBean<>("");
    }

    @RequestMapping(value = "/way=phone", method = RequestMethod.POST)
    public ResultBean loginWithPhoneNumber(HttpServletRequest request,
                                           @RequestParam("phone") String phone) {

        handlePhoneCheck(phone, request);

        BloggerAccountDTO account = accountService.getAccountByPhone(phone);
        if (account == null) return new ResultBean<>("", ResultBean.FAIL);

        HttpSession session = request.getSession();
        session.setAttribute(bloggerProperties.getSessionNameOfBloggerId(), account.getId());
        session.setAttribute(bloggerProperties.getSessionNameOfBloggerName(), account.getUsername());
        session.setAttribute(bloggerProperties.getSessionBloggerLoginSignal(), "login");

        // 成功登录
        return new ResultBean<>(account.getUsername());
    }

    private void handlePhoneCheck(String phone, HttpServletRequest request) {
        RequestContext context = new RequestContext(request);
        if (phone != null && !StringUtils.isPhone(phone))
            throw ResultUtil.failException(CodeMessage.COMMON_PARAMETER_FORMAT_ILLEGAL);

    }
}