package com.yeahmobi.yscheduler.web.controller.login;

import java.io.IOException;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.Validate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.ModelAndView;

import com.alibaba.fastjson.JSON;
import com.yeahmobi.yscheduler.common.Constants;
import com.yeahmobi.yscheduler.common.PasswordEncoder;
import com.yeahmobi.yscheduler.model.User;
import com.yeahmobi.yscheduler.model.common.UserContext;
import com.yeahmobi.yscheduler.model.service.UserService;
import com.yeahmobi.yscheduler.web.common.SigninUtils;
import com.yeahmobi.yscheduler.web.common.UserContextUtils;
import com.yeahmobi.yscheduler.web.controller.AbstractController;

@Controller
@RequestMapping(value = { LoginController.SCREEN_NAME })
public class LoginController extends AbstractController {

    public static final String  SCREEN_NAME = "login";
    private static final Logger LOGGER      = LoggerFactory.getLogger(LoginController.class);
    @Autowired
    private UserService         userService;

    @RequestMapping(method = RequestMethod.GET)
    public ModelAndView index() {
        Map<String, Object> map = new HashMap<String, Object>();

        return screen(map, SCREEN_NAME);
    }

    @RequestMapping(method = RequestMethod.POST, produces = "application/json; charset=utf-8")
    @ResponseBody
    public Object login(HttpServletResponse resp, String username, String password, String returnUrl) {

        Map<String, Object> map = new HashMap<String, Object>();
        try {
            Validate.notEmpty(username, "用户名不能为空");
            Validate.isTrue(!username.contains("@"), "登录时请去掉邮箱后缀");
            Validate.notEmpty(password, "密码不能为空");
            boolean success = false;
            User user = null;

            try {
                user = this.userService.get(username);
            } catch (IllegalArgumentException e) {
                // Ignore
            }
            String encodedPassword = PasswordEncoder.encode(password);
            try {
                // 先使用api登录
                success = SigninUtils.signin(username, password);
                Validate.isTrue(success, "api验证:用户名或密码不正确");
                if (user == null) {
                    // 本地不存在该用户时，新建用户
                    user = new User();
                    user.setName(username);
                    user.setPassword(encodedPassword);
                    user.setEmail(username + Constants.USER_EMAIL_suffix);
                    this.userService.add(user);
                } else {
                    // 密码改变时更新数据库
                    String oldPassword = user.getPassword();
                    if ((oldPassword == null) || !oldPassword.equals(encodedPassword)) {
                        user.setPassword(encodedPassword);
                        this.userService.update(user);
                    }
                }

            } catch (Exception e) {
                // 当signin api不可用的时候看数据库中的用户名密码
                LOGGER.error(e.getMessage());
                if (user != null) {
                    String oldPassword = user.getPassword();
                    Validate.isTrue(oldPassword.equals(encodedPassword), "密码不正确");
                } else {
                    throw new IllegalArgumentException("用户不存在");
                }
            }

            Calendar cal = Calendar.getInstance();
            cal.add(Calendar.DATE, 1);
            UserContextUtils.setContext(resp,
                                        new UserContext(user.getId(), user.getName(), user.getPassword(), cal.getTime()));

            if (StringUtils.isNotBlank(returnUrl)) {
                map.put("returnUrl", returnUrl);
            }
            map.put("success", true);

        } catch (IllegalArgumentException e) {
            map.put("success", false);
            map.put("errorMsg", e.getMessage());
        } catch (Exception e) {
            map.put("success", false);
            map.put("errorMsg", e.getMessage());
            LOGGER.error(e.getMessage(), e);
        }
        return JSON.toJSONString(map);

    }

    @RequestMapping(value = { "logout" }, method = RequestMethod.GET)
    @ResponseBody
    public void logout(HttpServletRequest req, HttpServletResponse resp, String username, String password)
                                                                                                          throws IOException {
        UserContextUtils.removeContext(resp);
        resp.sendRedirect(req.getContextPath() + "/login");
    }
}
