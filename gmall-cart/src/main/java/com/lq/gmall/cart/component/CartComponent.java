package com.lq.gmall.cart.component;

import com.alibaba.fastjson.JSON;
import com.lq.gmall.cart.vo.UserCartKey;
import com.lq.gmall.constant.CartConstant;
import com.lq.gmall.constant.SysConstant;
import com.lq.gmall.ums.entity.Member;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.util.UUID;

/**
 * @author tianmeng
 * @date 2020/3/11
 */
@Component
public class CartComponent {

    @Autowired
    private StringRedisTemplate redisTemplate;

    public Member getMember(String accessToken){
        String jsonMember = redisTemplate.opsForValue().get(SysConstant.LOGIN_MEMBER +accessToken);
        System.out.println("jsonMember:"+jsonMember);
        return JSON.parseObject(jsonMember, Member.class);
    }

    public UserCartKey getCartKey(String accessToken, String cartKey){
        UserCartKey userCartKey = new UserCartKey();
        Member member = null;
        if(!StringUtils.isEmpty(accessToken)){
            member = getMember(accessToken);
        }

        if(member!=null){
            //获取到了在线用户；用户登录用这个
            userCartKey.setLogin(true);
            userCartKey.setUserId(member.getId());
            userCartKey.setFinalCartKey(CartConstant.USER_CART_KEY_PREFIX+member.getId());
            return userCartKey;
        }else if(!StringUtils.isEmpty(cartKey)){
            //用户有临时的用这个
            userCartKey.setLogin(false);
            userCartKey.setFinalCartKey(CartConstant.TEMP_CART_KEY_PREFIX+cartKey);
            return userCartKey;
        }else {
            //用户既没有登录也没有零时购物车
            String replace = UUID.randomUUID().toString().replace("-", "");
            userCartKey.setLogin(false);
            userCartKey.setFinalCartKey(CartConstant.TEMP_CART_KEY_PREFIX+replace);
            userCartKey.setTempCartKey(replace);

            return userCartKey;
        }

    }

}
