package com.kong.config;

import com.kong.utils.CommunityConstant;
import com.kong.utils.CommunityUtil;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.builders.WebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.access.AccessDeniedHandler;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Locale;

@Configuration
public class SecurityConfig extends WebSecurityConfigurerAdapter implements CommunityConstant {

    @Override
    public void configure(WebSecurity web) throws Exception {
        web.ignoring().antMatchers("/resources/**");
    }

    @Override
    protected void configure(HttpSecurity http) throws Exception {
        // 授权
        http.authorizeRequests()
                .antMatchers( // 需要登录才能访问的功能
                        "/user/setting", // 用户设置
                        "/user/upload", // 用户上传头像
                        "/discuss/add", // 发帖
                        "/comment/add/**", // 添加评论
                        "/letter/**", // 私信
                        "/notice/**", // 通知
                        "/like", // 点赞
                        "/follow", // 关注
                        "/unfollow" //取消关注
                )
                .hasAnyAuthority(
                        AUTHORITY_USER,
                        AUTHORITY_ADMIN,
                        AUTHORITY_MODERATOR
                )
                .antMatchers(
                        "/discuss/top", // 置顶
                        "/discuss/wonderful" // 加精
                )
                .hasAnyAuthority(
                        AUTHORITY_MODERATOR // 版主
                )
                .antMatchers(
                        "/discuss/delete",
                        "/data/**"// 删除
                )
                .hasAnyAuthority(
                        AUTHORITY_ADMIN // 管理员
                )
                .anyRequest().permitAll()
                .and().csrf().disable();

        // 权限不够时的处理
        http.exceptionHandling()
                .authenticationEntryPoint(new AuthenticationEntryPoint() {
                    // 当检测到没有登录时的处理
                    @Override
                    public void commence(HttpServletRequest request, HttpServletResponse response, AuthenticationException authException) throws IOException, ServletException {
                        // 判断是不是异步请求
                        String xRequestedWith = request.getHeader("x-requested-with");
                        if ("XMLHttpRequest".equals(xRequestedWith)) { // 异步请求, 响应json字符串
                            response.setContentType("application/plain;charset=utf-8");
                            PrintWriter write = response.getWriter();
                            write.write(CommunityUtil.getJsonString(403, "还没有登录！"));
                        } else { // 返回html
                            response.sendRedirect(request.getContextPath() + "/login");
                        }
                    }
                })
                .accessDeniedHandler(new AccessDeniedHandler() {
                    // 权限不足时的处理
                    @Override
                    public void handle(HttpServletRequest request, HttpServletResponse response, AccessDeniedException accessDeniedException) throws IOException, ServletException {
                        String xRequestedWith = request.getHeader("x-requested-with");
                        if ("XMLHttpRequest".equals(xRequestedWith)) { // 异步请求, 返回json字符串
                            response.setContentType("application/plain;charset=utf-8");
                            PrintWriter write = response.getWriter();
                            write.write(CommunityUtil.getJsonString(403, "你没有访问此功能的权限！"));
                        } else { // 返回html
                            response.sendRedirect(request.getContextPath() + "/denied");
                        }
                    }
                });

        // Security底层默认会拦截/logout请求，进行退出处理
        // 覆盖它默认的逻辑，才能执行我们自己的退出代码
        http.logout().logoutUrl("/securitylogout"); // 覆盖它本身的 /logout请求
    }
}
