package com.hmdp.controller;


import cn.hutool.core.bean.BeanUtil;
import com.hmdp.dto.LoginFormDTO;
import com.hmdp.dto.Result;
import com.hmdp.dto.UserDTO;
import com.hmdp.entity.User;
import com.hmdp.entity.UserInfo;
import com.hmdp.service.IUserInfoService;
import com.hmdp.service.IUserService;
import com.hmdp.service.impl.UserServiceImpl;
import com.hmdp.utils.RegexUtils;
import com.hmdp.utils.UserHolder;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import com.hmdp.utils.RedisConstants;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpSession;
import javax.validation.Valid;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.servlet.http.HttpServletRequest;

/**
 * <p>
 * 前端控制器
 * </p>
 *
 * @author 虎哥
 */
@Slf4j
@RestController
@RequestMapping("/user")
public class UserController {

    @Resource
    private IUserService userService;

    @Resource
    private IUserInfoService userInfoService;

    @Autowired
    private StringRedisTemplate stringRedisTemplate;

    /**
     * 发送手机验证码
     * user/code
     */
    @PostMapping("code")
    public Result sendCode(@RequestParam("phone") @NotBlank(message = "手机号不能为空") String phone, HttpSession session) {
        // 发送短信验证码并保存验证码
        return userService.sendCode2(phone, session);
    }

//    @PostMapping("code1")
//    public Result sendCode(@RequestParam("phone") @NotBlank(message = "手机号不能为空") String phone,HttpSession session){
//
//        return userService.sendCode3(phone,session);
//    }

    /**
     * 登录功能
     * @param loginForm 登录参数，包含手机号、验证码；或者手机号、密码
     */
    @PostMapping("/login")
    public Result login(@RequestBody LoginFormDTO loginForm, HttpSession session){
        if (loginForm == null || loginForm.getPhone() == null || loginForm.getPhone().trim().isEmpty()) {
            return Result.fail("手机号不能为空");
        }
        if ((loginForm.getCode() == null || loginForm.getCode().trim().isEmpty()) &&
            (loginForm.getPassword() == null || loginForm.getPassword().trim().isEmpty())) {
            return Result.fail("验证码和密码不能同时为空");
        }
        // 实现登录功能
        return userService.login2(loginForm, session);
    }


    /**
     * 登出功能
     * @return 无
     */
    @PostMapping("/logout")
    /**
 * 用户登出功能
 * 通过删除Redis中的用户信息以及清理ThreadLocal中的用户信息来实现用户登出
 *
 * @param request HTTP请求对象，用于获取请求头中的token
 * @return 返回一个Result对象，表示登出操作的结果
 */
public Result logout(HttpServletRequest request){
    // 新增代码：完善登出功能
    String token = request.getHeader("authorization");
    if (token != null && !token.isEmpty()) {
        String key = RedisConstants.LOGIN_USER_KEY + token;
        stringRedisTemplate.delete(key);
    }
    UserHolder.removeUser(); // 清理ThreadLocal
    return Result.ok("退出登录成功");
}


    @GetMapping("/me")
    public Result me(){
        // 获取当前登录的用户并返回
        UserDTO user = UserHolder.getUser();
        return Result.ok(user);
    }

    @GetMapping("/info/{id}")
    public Result info(@PathVariable("id") @NotNull(message = "用户id不能为空") Long userId){
        // 查询详情
        UserInfo info = userInfoService.getById(userId);
        if (info == null) {
            // 没有详情，应该是第一次查看详情
            return Result.ok();
        }
        info.setCreateTime(null);
        info.setUpdateTime(null);
        // 返回
        return Result.ok(info);
    }

    @GetMapping("/{id}")
    public Result queryUserById(@PathVariable("id") @NotNull(message = "用户id不能为空") Long userId){
        // 查询详情
        User user = userService.getById(userId);
        if (user == null) {
            return Result.ok();
        }
        UserDTO userDTO = BeanUtil.copyProperties(user, UserDTO.class);
        // 返回
        return Result.ok(userDTO);
    }

    @PostMapping("/sign")
    public Result sign(){
        return userService.sign();
    }

    @GetMapping("/sign/count")
    public Result signCount(){
        return userService.signCount();
    }
}