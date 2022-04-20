package com.itheima.reggie.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.itheima.reggie.entity.Category;


public interface CategoryService extends IService<Category> {
    //自定义移除方法
    public void remove(Long id);
}
