package com.itheima.reggie.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.itheima.reggie.common.BaseContext;
import com.itheima.reggie.common.R;
import com.itheima.reggie.entity.ShoppingCart;
import com.itheima.reggie.service.ShoppingCartService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@RestController
@RequestMapping("/shoppingCart")
public class ShoppingCartController {
    @Autowired
    private ShoppingCartService shoppingCartService;

    /**
     * 添加购物车
     *
     * @param shoppingCart
     * @return
     */
    @PostMapping("/add")
    public R<ShoppingCart> add(@RequestBody ShoppingCart shoppingCart) {
        log.info("购物车数据:{}", shoppingCart);
        //设置用户id 指定当前是哪个用户的购物车数据
        Long userId = BaseContext.getCurrentId(); //也可以用session获取
        shoppingCart.setUserId(userId);

        LambdaQueryWrapper<ShoppingCart> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(ShoppingCart::getUserId, userId);

        Long dishId = shoppingCart.getDishId();
        if (dishId != null) {
            //添加的是菜品
            queryWrapper.eq(ShoppingCart::getDishId, dishId);
        } else {
            //添加的是套餐
            queryWrapper.eq(ShoppingCart::getSetmealId, shoppingCart.getSetmealId());
        }

        //查看当前套餐或菜品是否在购物车中
        //select *from shopping_cart where user_id=? and dish_id/setmeal_id=?;
        ShoppingCart cart = shoppingCartService.getOne(queryWrapper);

        //若已存在，在原来的基础上加1
        if (cart != null) {
            Integer number = cart.getNumber();
            cart.setNumber(number + 1);
            shoppingCartService.updateById(cart);
        } else {
            //不存在，则添加到购物车中，数量默认1
            shoppingCart.setNumber(1);
            shoppingCart.setCreateTime(LocalDateTime.now());
            shoppingCartService.save(shoppingCart);
            cart = shoppingCart;
        }
        return R.success(cart);
    }

    /**
     * 减少购物车
     * @param shoppingCart
     * @return
     */
    @PostMapping("/sub")
    public R<ShoppingCart> sub(@RequestBody ShoppingCart shoppingCart) {

        log.info("购物车数据:{}", shoppingCart);

        //获取当前用户id
        Long currentId = BaseContext.getCurrentId();
        shoppingCart.setUserId(currentId);//设置当前购物车的所属用户

        LambdaQueryWrapper<ShoppingCart> queryWrapper = new LambdaQueryWrapper<>();

        //查询当前购物车的所属用户 where user_id = ?
        queryWrapper.eq(ShoppingCart::getUserId, currentId);
        Long dishId = shoppingCart.getDishId();//获取菜品ID

        //如果不为空 则操作的是菜品dish_id  （条件查询）
        if (dishId != null) {
            //where dish_id = ?
            queryWrapper.eq(ShoppingCart::getDishId, dishId);
        } else {
            //操作的是套餐  setmeal_id
            //where setmeal_id = ?
            queryWrapper.eq(ShoppingCart::getSetmealId, shoppingCart.getSetmealId());
        }
        //获取当前购物车对象
        //select *from shopping_cart where user_id=? and dish_id=?/ setmeal_id=?;
        ShoppingCart shoppingCart1 = shoppingCartService.getOne(queryWrapper); //一条数据
        Integer number = shoppingCart1.getNumber(); //当前购物车对象中属性 数量 的值

        if (number == 0) {
            shoppingCartService.removeById(shoppingCart1.getId());
//            shoppingCart1 = shoppingCart;
//            return R.success(shoppingCart);
        }else {
            shoppingCart1.setNumber(number - 1);//减1
            shoppingCartService.updateById(shoppingCart1);//提交修改
            shoppingCart1 = shoppingCart;
        }
        return R.success(shoppingCart1);

    }

    /**
     * 查看购物车
     *
     * @return
     */
    @GetMapping("/list")
    public R<List<ShoppingCart>> list() {
        log.info("查看购物车...");
        LambdaQueryWrapper<ShoppingCart> queryWrapper = new LambdaQueryWrapper<>();
        //where user_id = ?
        queryWrapper.eq(ShoppingCart::getUserId, BaseContext.getCurrentId());
        //order by create_time
        queryWrapper.orderByAsc(ShoppingCart::getCreateTime);
        //select *from shopping_cart where user_id=? order by create_time asc;
        List<ShoppingCart> list = shoppingCartService.list(queryWrapper); //获取某个用户的购物车数据
        return R.success(list);
    }

    @DeleteMapping("/clean")
    public R<String> delete() {
        //delete from shopping_cart where user_id=?
        LambdaQueryWrapper<ShoppingCart> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(ShoppingCart::getUserId, BaseContext.getCurrentId());
        shoppingCartService.remove(queryWrapper);
        return R.success("清空购物车成功...");
    }
}
