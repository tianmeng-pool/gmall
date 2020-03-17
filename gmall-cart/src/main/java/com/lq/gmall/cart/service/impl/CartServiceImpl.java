package com.lq.gmall.cart.service.impl;

import com.alibaba.dubbo.config.annotation.Reference;
import com.alibaba.dubbo.config.annotation.Service;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.TypeReference;
import com.lq.gmall.cart.component.CartComponent;
import com.lq.gmall.cart.config.ThreadPoolConfig;
import com.lq.gmall.cart.service.CartService;
import com.lq.gmall.cart.vo.Cart;
import com.lq.gmall.cart.vo.CartItem;
import com.lq.gmall.cart.vo.CartResponse;
import com.lq.gmall.cart.vo.UserCartKey;
import com.lq.gmall.constant.CartConstant;
import com.lq.gmall.constant.SysConstant;
import com.lq.gmall.pms.entity.Product;
import com.lq.gmall.pms.entity.SkuStock;
import com.lq.gmall.pms.service.ProductService;
import com.lq.gmall.pms.service.SkuStockService;
import com.lq.gmall.ums.entity.Member;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RMap;
import org.redisson.api.RedissonClient;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * @author tianmeng
 * @date 2020/3/11
 */
@Service
@Component
@Slf4j
public class CartServiceImpl implements CartService {

    @Autowired
    private StringRedisTemplate redisTemplate;

    @Autowired
    private CartComponent cartComponent;

    @Autowired
    private RedissonClient redissonClient;

    @Reference
    private SkuStockService skuStockService;

    @Reference
    private ProductService productService;

    @Qualifier(value = "mainThreadPoolExecutor")
    @Autowired
    private ThreadPoolExecutor threadPool;

    /**
     * 修改购物车数量
     * @param skuId
     * @param num
     * @param cartKey
     * @param accessToken
     * @return
     */
    @Override
    public CartResponse updateCartItem(Long skuId, Integer num, String cartKey, String accessToken) {
        CartResponse cartResponse = new CartResponse();
        Member member = cartComponent.getMember(accessToken);

        String finalCartKey = "";

        //用户登录，用user购物车
        if (member != null) {
            finalCartKey = CartConstant.USER_CART_KEY_PREFIX + member.getId();
            CartItem cartItem = getCartItem(skuId, num, finalCartKey);
            cartResponse.setCartItem(cartItem);
            return cartResponse;
        }

        //用户没有登录，用离线购物车
        if (!StringUtils.isEmpty(cartKey)) {
            finalCartKey = CartConstant.TEMP_CART_KEY_PREFIX + cartKey;
            CartItem cartItem = getCartItem(skuId, num, finalCartKey);
            cartResponse.setCartItem(cartItem);
            return cartResponse;
        }

        //以上情况都没有，给用户一个离线购物车
        String newCartKey = UUID.randomUUID().toString().replace("-","");
        finalCartKey = CartConstant.TEMP_CART_KEY_PREFIX + newCartKey;
        CartItem cartItem = getCartItem(skuId, num, finalCartKey);
        cartResponse.setCartItem(cartItem);
        return cartResponse;

    }

    /**
     * 获取购物车的所有数据
     * @param cartKey
     * @param accessToken
     * @return
     */
    @Override
    public CartResponse cartList(String cartKey, String accessToken) {
        CartResponse cartResponse = new CartResponse();
        UserCartKey userCartKey = cartComponent.getCartKey(accessToken, cartKey);
        Cart cart = new Cart();
        List<CartItem> cartItems = new ArrayList<>();
        //查询用户购物车的时候需要判断购物车是否需要合并
        if (userCartKey.isLogin()) {
            //用户登录了，合并购物车
            mergeCart(userCartKey.getUserId(),cartKey);
        }
        String finalCartKey = userCartKey.getFinalCartKey();
        //给购物车设置一个过期时间(自动续期)
        redisTemplate.expire(finalCartKey,30L, TimeUnit.DAYS);
        RMap<String, String> map = redissonClient.getMap(finalCartKey);
        //map不为空才为list中添加数据
        if (map != null && !map.isEmpty()) {
            //遍历map，获取每一个CartItem
            map.entrySet().forEach((item) -> {
                //获取每一个map的value，买一个value就是一个Cartitem
                if (!item.getKey().equalsIgnoreCase(CartConstant.CART_CHECKED_KEY)) {
                    String value = item.getValue();
                    CartItem cartItem = JSON.parseObject(value, CartItem.class);
                    cartItems.add(cartItem);
                }

            });
            cart.setCartItems(cartItems);
        } else {
            //用户没有购物车，新建一个购物车
            cartResponse.setCartKey(userCartKey.getTempCartKey());
        }

        cartResponse.setCart(cart);
        return cartResponse;
    }

    /**
     * 删除某个购物项
     * @param skuId
     * @param cartKey
     * @param accessToken
     * @return
     */
    @Override
    public CartResponse delCartItem(Long skuId, String cartKey, String accessToken) {
        CartResponse cartResponse = new CartResponse();
        UserCartKey userCartKey = cartComponent.getCartKey(accessToken, cartKey);
        String finalCartKey = userCartKey.getFinalCartKey();
        RMap<String, String> map = redissonClient.getMap(finalCartKey);
        //从map中移除指定的key
        map.remove(skuId.toString());
        //删除成功，给前端返回整个购物车
        cartResponse.setCart(cartList(cartKey,accessToken).getCart());
        return cartResponse;
    }

    /**
     * 清空整个购物车
     * @param cartKey
     * @param accessToken
     * @return
     */
    @Override
    public CartResponse cartClear(String cartKey, String accessToken) {
        UserCartKey userCartKey = cartComponent.getCartKey(accessToken, cartKey);
        String finalCartKey = userCartKey.getFinalCartKey();
        RMap<String, String> map = redissonClient.getMap(finalCartKey);
        //清空map里面的所有数据
        map.clear();
        CartResponse cartResponse = new CartResponse();
        return cartResponse;
    }

    /**
     * 购物车选中状态:选中/不选中
     * @param skuIds
     * @param ops
     * @param cartKey
     * @param accessToken
     * @return
     */
    @Override
    public CartResponse checkCartItems(String skuIds, Integer ops, String cartKey, String accessToken) {
        List<Long> skuIdsList = new ArrayList<>();
        UserCartKey userCartKey = cartComponent.getCartKey(accessToken, cartKey);
        String finalCartKey = userCartKey.getFinalCartKey();
        RMap<String, String> map = redissonClient.getMap(finalCartKey);
        boolean check = ops == 1 ? true : false;
        //修改购物车状态
        if (!StringUtils.isEmpty(skuIds)) {
            String[] ids = skuIds.split(",");
            for (String id : ids) {
                Long valueOf = Long.valueOf(id);
                skuIdsList.add(valueOf);
                //找到每个skuId对应的购物车中的json，把状态改为ops所对应的值
                if (map != null && !map.isEmpty()) {
                    String jsonString = map.get(id);
                    //把json数据转换为CartItem
                    CartItem cartItem = JSON.parseObject(jsonString, CartItem.class);
                    cartItem.setCheck(check);
                    //覆盖原来的json数据
                    map.put(id, JSON.toJSONString(cartItem));
                }
            }
        }

        //修改checked集合的状态
        //2.为了快速找到那个被选中的，我们单独维护了数组，数组在map中用的key 是checked,只是set最好
        String jsonCartItem = map.get(CartConstant.CART_CHECKED_KEY);
        Set<Long> set = JSON.parseObject(jsonCartItem, new TypeReference<Set<Long>>(){});
        //防止空指针
        if (set == null && set.isEmpty()) {
            set = new HashSet<>();
        }
        if (check) {
            set.addAll(skuIdsList);
            log.info("被选中的商品:{}",set);
        } else {
            set.removeAll(skuIdsList);
            log.info("被移除不选中的商品:{}",set);
        }
        //重新保存被选中的商品
        map.put(CartConstant.CART_CHECKED_KEY,JSON.toJSONString(set));

        //返回整个购物车
        CartResponse cartResponse = cartList(cartKey, accessToken);

        return cartResponse;
    }

    private CartItem getCartItem(Long skuId, Integer num, String finalCartKey) {
        RMap<String, String> map = redissonClient.getMap(finalCartKey);
        String cartItemJson = map.get(skuId.toString());
        CartItem cartItem = JSON.parseObject(cartItemJson, CartItem.class);
        cartItem.setCount(num);
        String jsonString = JSON.toJSONString(cartItem);
        map.put(skuId.toString(),jsonString);
        return cartItem;
    }

    @Override
    public CartResponse add(Long skuId,Integer num, String cartKey, String accessToken) {
        //0、根据传来的accessToke查询用户的基本信息
        Member member = cartComponent.getMember(accessToken);

        //合并购物车
        if (member != null && !StringUtils.isEmpty(cartKey)) {
            //先去合并购物车
            mergeCart(member.getId(),cartKey);
        }

        String finalCartKey = "";
        //1、用户登录了，cartKey有值，就使用它的在线购物车:cart:user:1
        if (member != null) {
            //用户登录
            finalCartKey = CartConstant.USER_CART_KEY_PREFIX + member.getId();
            /**
             * 1、根据skuId找到sku的真正信息
             * 2.给指定的购物车添加记录
             *      如果有了这个skuid就只是count的增加
             */
            CartItem cartItem = addItemToCart(skuId,num,finalCartKey);
            CartResponse cartResponse = new CartResponse();
            cartResponse.setCartItem(cartItem);
            return cartResponse;
        }

        //2、用户没有登录，cartKey没有值，就使用它的离线购物车；cart:temp:cartKey
        if (!StringUtils.isEmpty(cartKey)) {

            finalCartKey = CartConstant.TEMP_CART_KEY_PREFIX + cartKey;

            CartItem cartItem = addItemToCart(skuId,num,finalCartKey);
            CartResponse cartResponse = new CartResponse();
            cartResponse.setCartItem(cartItem);
            return cartResponse;
        }

        //3、以上两种情况都没有，则给用户分配一个临时购物车
        String newCartKey = UUID.randomUUID().toString().replace("-","");
        finalCartKey = CartConstant.TEMP_CART_KEY_PREFIX + newCartKey;

        CartItem cartItem = addItemToCart(skuId,num,finalCartKey);
        CartResponse cartResponse = new CartResponse();
        cartResponse.setCartItem(cartItem);
        //设置临时用户的cartKey
        cartResponse.setCartKey(newCartKey);

        return cartResponse;
    }

    /**
     * 合并购物车
     * @param id
     * @param cartKey
     */
    private void mergeCart(Long id, String cartKey) {

        //老购物车的cartKey
        String oldCart = CartConstant.TEMP_CART_KEY_PREFIX + cartKey;
        //用户购物车的cartKey
        String userCart = CartConstant.USER_CART_KEY_PREFIX + id.toString();

        RMap<String, String> map = redissonClient.getMap(oldCart);
        //map不为null，而且里面有数据才合并购物车
        if (map != null && !map.isEmpty()) {
            map.entrySet().forEach((item)->{
                String key = item.getKey();
                if (!key.equalsIgnoreCase(CartConstant.CART_CHECKED_KEY)) {
                    String value = item.getValue();

                    CartItem cartItem = JSON.parseObject(value, CartItem.class);
                    addItemToCart(Long.valueOf(key),cartItem.getCount(),userCart);
                }
            });

            //移除老购物车中的数据
            map.clear();
        }

    }

    /**
     * 给指定购物车添加商品
     * @param skuId
     * @param finalCartKey
     * @return
     */
    @SneakyThrows
    private CartItem addItemToCart(Long skuId,Integer num, String finalCartKey) {

        CartItem newCartItem = new CartItem();

        //0、根据skuId去数据库查询最新的商品信息
        /**
         * 1.只接受上一步的结果
         * thenAccept(r){
         *  r:上一步的结果
         * }
         * 2.thenApply(r){
         *     r:把上一步的结果拿来进行修改再返回
         * }
         * 3.thenAccept(){} 上一次结果1s+本次结果2s=3s
         * 4.thenAcceptAsync(){}上一次1s+异步2s=最多等2s
         */
        CompletableFuture<SkuStock> skuFutur = CompletableFuture.supplyAsync(() -> {
            //1s
            SkuStock skuStock = skuStockService.getById(skuId);
            return skuStock;
        }, threadPool).thenApplyAsync((stock) -> {
            //2s
            //根据商品id查询该商品
            Product product = productService.getById(stock.getProductId());
            //拿到上一步的结果，把结果封装到newCart中
            BeanUtils.copyProperties(stock, newCartItem);
            newCartItem.setSkuId(stock.getId());
            newCartItem.setName(product.getName());
            newCartItem.setCount(num);
            return stock;
        }).whenComplete((r, e) -> {
            log.info("sku信息为:{}", r);
            log.error("异步执行时发生异常，异常为:{}", e.getMessage());
        });

        /**
         * 购物车集合k[skuId]  v[购物项]是str(json)
         * k[checked]  v[1,2,3]
         */
        RMap<String, String> map = redissonClient.getMap(finalCartKey);
        //获取购物车中这个skuId对应的购物项
        String itemJson = map.get(skuId.toString());

        skuFutur.get();//在线等结果
        //检查购物车中是否存在这个购物项
        if (!StringUtils.isEmpty(itemJson)) {
            //只是购物项数量的叠加，购物车老item获取到数量，给新的cartitem里面添加信息
            CartItem oldItem = JSON.parseObject(itemJson, CartItem.class);
            Integer count = oldItem.getCount();
            newCartItem.setCount(count+newCartItem.getCount());
            String jsonString = JSON.toJSONString(newCartItem);
            //新数据覆盖老数据
            map.put(skuId.toString(),jsonString);
        } {
            //新增购物项
            String jsonString = JSON.toJSONString(newCartItem);
            map.put(skuId.toString(),jsonString);
        }

        return newCartItem;
    }
}
