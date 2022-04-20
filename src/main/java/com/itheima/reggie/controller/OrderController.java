package com.itheima.reggie.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.itheima.reggie.common.R;
import com.itheima.reggie.entity.Orders;
import com.itheima.reggie.service.OrderService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@RestController
@RequestMapping("/order")
public class OrderController {
    @Autowired
    private OrderService orderService;

    @PostMapping("/submit")
    public R<String> submit(@RequestBody Orders orders) {
        log.info("订单数据:{}", orders);
        orderService.submit(orders);
        return R.success("下单成功...");
    }

    /**
     * 订单分页条件查询
     * @param page  当前页
     * @param pageSize 每页显示数
     * @param number 订单号
     * @return
     */
    @GetMapping("/page")
    public R<Page> page(int page, int pageSize,String number) {
        Page<Orders> pageInfo = new Page<>(page, pageSize);
        LambdaQueryWrapper<Orders> queryWrapper = new LambdaQueryWrapper<>();

        queryWrapper.like(number!=null,Orders::getNumber,number);
        queryWrapper.orderByAsc(Orders::getOrderTime);

        //分页查询
        orderService.page(pageInfo, queryWrapper);

        //查询所有订单 orders表
        List<Orders> ordersList = orderService.list(queryWrapper);
        ordersList = ordersList.stream().map((item)->{
            String consignee = item.getConsignee();
            item.setUserName(consignee);
            return item;
        }).collect(Collectors.toList());

        pageInfo.setRecords(ordersList);
        return R.success(pageInfo);
    }

    /**
     * 修改订单状态:3(派送订单) 4(派送完成)
     * @param orders
     * @return
     */
    @PutMapping
    public R<String> changeStatus(@RequestBody Orders orders){
        log.info("订单状态:{}",orders);
        orderService.updateById(orders);//修改订单状态
        return R.success("修改订单成功...");
    }
}
