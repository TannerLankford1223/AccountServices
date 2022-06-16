package com.example.accountservices.config;

import org.aspectj.lang.annotation.Pointcut;

// List of Common Join Points for logging events using aspectJ
public class CommonJoinPointConfig {

    @Pointcut("execution(* com.example.accountservices.controller.AccountServiceController.signup(..))")
    public void registerUser(){}

    @Pointcut("execution(* com.example.accountservices.controller.AccountServiceController.changePassword(..))")
    public void changePass(){}

    @Pointcut("execution(* com.example.accountservices.config.CustomAccessDeniedHandler.handle(..))")
    public void accessDenied(){}

    @Pointcut("execution(* com.example.accountservices.controller.AdminController.changeRoles(..))")
    public void userRoles(){}

    @Pointcut("execution(* com.example.accountservices.controller.AdminController.deleteUser(..))")
    public void deleteUser(){}

    @Pointcut("execution(* com.example.accountservices.controller.AdminController.changeAccess(..))")
    public void changeAccess(){}

}
