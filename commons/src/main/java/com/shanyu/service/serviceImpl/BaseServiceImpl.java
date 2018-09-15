package com.shanyu.service.serviceImpl;

import com.shanyu.mapper.BaseMapper;
import com.shanyu.service.BaseService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

public class BaseServiceImpl<M extends BaseMapper<T>,T> implements BaseService<T> {
    @Autowired
    protected M baseMapper;

    @Transactional
    public int deleteByPrimaryKey(Integer id){return baseMapper.deleteByPrimaryKey(id);}

    @Transactional
    public int insert(T record){return baseMapper.insert(record);}

    @Transactional
    public int insertSelective(T record){return baseMapper.insertSelective(record);}

    public T selectByPrimaryKey(Integer id){return baseMapper.selectByPrimaryKey(id);}

    @Transactional
    public int updateByPrimaryKeySelective(T record){return baseMapper.updateByPrimaryKeySelective(record);}

    @Transactional
    public int updateByPrimaryKey(T record){return baseMapper.updateByPrimaryKey(record);}
}
