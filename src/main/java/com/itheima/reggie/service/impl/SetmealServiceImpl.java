package com.itheima.reggie.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.itheima.reggie.common.CustomException;
import com.itheima.reggie.dto.SetmealDto;
import com.itheima.reggie.entity.Setmeal;
import com.itheima.reggie.entity.SetmealDish;
import com.itheima.reggie.mapper.SetmealMapper;
import com.itheima.reggie.service.SetmealDishService;
import com.itheima.reggie.service.SetmealService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class SetmealServiceImpl extends ServiceImpl<SetmealMapper, Setmeal> implements SetmealService {

    @Autowired
    private SetmealDishService setmealDishService;
    /**
     * 新增套餐 同时保存套餐和菜品的关联关系
     */
    @Transactional
    @Override
    public void saveWithDish(SetmealDto setmealDto) {
        //保存套餐的基本信息 操作setmeal表 执行insert操作
        this.save(setmealDto);//setmealDto继承了setmeal
        //
        List<SetmealDish> setmealDishes = setmealDto.getSetmealDishes();
        setmealDishes = setmealDishes.stream().map((item)->{
            item.setSetmealId(setmealDto.getId());
            return item;
        }).collect(Collectors.toList());
        //保存套餐和菜品的关联信息 操作setmeal_dish  执行insert操作
        setmealDishService.saveBatch(setmealDishes);
    }

    /**
     * 删除套餐，同时需要删除套餐和菜品的关联数据
     * @param ids
     */
    @Override
    public void deleteWithDish(List<Long> ids) {
        //查询套餐状态 停售状态才能删除 status=0
        LambdaQueryWrapper<Setmeal> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.in(Setmeal::getId,ids);
        queryWrapper.eq(Setmeal::getStatus,1);
        int count = this.count(queryWrapper);
        if (count>0){
            //若不能删除  抛出业务异常
            throw new CustomException("套餐正在售卖中,无法删除...");
        }
        //若可以删除 先删除套餐表中的数据--setmeal表
        this.removeByIds(ids);
        //删除关系表中的数据  setmeal_dish
        LambdaQueryWrapper<SetmealDish> dishLambdaQueryWrapper = new LambdaQueryWrapper<>();
        dishLambdaQueryWrapper.in(SetmealDish::getSetmealId,ids); //根据setmeal_id删除
        setmealDishService.remove(dishLambdaQueryWrapper);
    }


    @Override
    public void stopSale(List<Long> ids) {
        LambdaQueryWrapper<Setmeal> queryWrapper = new LambdaQueryWrapper<>();
        //select * from setmeal where id in (...);
        queryWrapper.in(Setmeal::getId,ids);
        List<Setmeal> list = this.list(queryWrapper);
        list = list.stream().map((item)->{
            item.setStatus(0); //1起售 0停售
            return item;
        }).collect(Collectors.toList());

        this.updateBatchById(list);
    }

    /**
     * 批量起售
     * @param ids
     */
    @Override
    public void startSale(List<Long> ids) {
        LambdaQueryWrapper<Setmeal> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.in(Setmeal::getId,ids);
        List<Setmeal> setmealList = this.list(queryWrapper);
        setmealList = setmealList.stream().map((item)->{
            item.setStatus(1);
            return item;
        }).collect(Collectors.toList());
        this.updateBatchById(setmealList);
    }
}
