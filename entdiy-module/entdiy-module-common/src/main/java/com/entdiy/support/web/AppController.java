/**
 * Copyright © 2015 - 2017 EntDIY JavaEE Development Framework
 *
 * Site: https://www.entdiy.com, E-Mail: xautlx@hotmail.com
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.entdiy.support.web;

import com.entdiy.core.web.view.OperationResult;
import com.entdiy.security.AuthContextHolder;
import com.entdiy.security.admin.AdminFormAuthenticationFilter;
import com.google.common.collect.Maps;
import org.apache.shiro.authc.AuthenticationException;
import org.apache.shiro.web.filter.authc.FormAuthenticationFilter;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletRequest;
import java.util.Map;

@Controller
public class AppController {

    /**
     * <h3>APP接口: 登录。</h3>
     * <p>
     * <p>
     * 业务输入参数列表：
     * <ul>
     * <li><b>username</b> 账号</li>
     * <li><b>password</b> 密码</li>
     * <li><b>uuid</b> 设备或应用唯一标识</li>
     * </ul>
     * </p>
     * <p>
     * <p>
     * 业务输出参数列表：
     * <ul>
     * <li><b>token</b> 本次登录的随机令牌Token，目前设定半年有效期。APP取到此token值后存储在本应用持久化，在后续访问或下次重开应用时把此token以HTTP Header形式附在Request信息中：ACCESS-TOKEN={token}</li>
     * </ul>
     * </p>
     *
     * @return {@link OperationResult} 通用标准结构
     */
    @RequestMapping(value = "/app/login", method = RequestMethod.POST)
    @ResponseBody
    public OperationResult appLogin(HttpServletRequest request, Model model) {
        //获取认证异常的类名
        AuthenticationException ae = (AuthenticationException) request.getAttribute(FormAuthenticationFilter.DEFAULT_ERROR_KEY_ATTRIBUTE_NAME);
        if (ae == null) {
            return OperationResult.buildSuccessResult(AuthContextHolder.getDefaultAuthUserDetails());
        } else {
            OperationResult result = OperationResult.buildFailureResult(ae.getMessage());
            Boolean captchaRequired = (Boolean) request.getAttribute(AdminFormAuthenticationFilter.KEY_AUTH_CAPTCHA_REQUIRED);
            Map<String, Object> datas = Maps.newHashMap();
            datas.put("captchaRequired", captchaRequired);
            datas.put("captchaImageUrl", request.getContextPath() + "/pub/jcaptcha.servlet");
            result.setData(datas);
            return result;
        }
    }
}
