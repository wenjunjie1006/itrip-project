package com.cskt.itripauth.service.impl;

import org.springframework.stereotype.Service;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.cskt.entity.HotelOrder;
import com.cskt.mapper.HotelOrderMapper;
import com.cskt.itripauth.service.HotelOrderService;
@Service
public class HotelOrderServiceImpl extends ServiceImpl<HotelOrderMapper, HotelOrder> implements HotelOrderService{

}
