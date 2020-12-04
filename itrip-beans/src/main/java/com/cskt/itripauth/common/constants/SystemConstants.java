package com.cskt.itripauth.common.constants;

public class SystemConstants {

    /*用户类型常量*/
    public interface UserType {
        public static final Integer REGISTRATION = 0;//注册
        public static final Integer WE_CHAT_LOGIN = 1;//微信登录
        public static final Integer QQ_LOGIN = 2;//QQ登录
        public static final Integer WEI_BO_LOGIN = 3;//微博登录
    }


    /*用户激活状态*/
    public interface UserActiveStatus {
        public static final Integer NOT_ACTIVE = 0;//未激活
        public static final Integer IS_ACTIVE = 1;//已激活
    }

    /*预订订单类型*/
    public interface OrderBookType {
        public static final Integer WEB = 1;//PC端
    }

    /*订单的状态*/
    public interface OrderStatus {
        public static final Integer TO_BE_PAID = 0;//待支付
    }

}
