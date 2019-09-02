package com.leyou.order.service;

import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.leyou.auth.entity.UserInfo;
import com.leyou.common.pojo.PageResult;
import com.leyou.common.utils.IdWorker;
import com.leyou.order.interceptor.LoginInterceptor;
import com.leyou.order.mapper.OrderDetailMapper;
import com.leyou.order.mapper.OrderMapper;
import com.leyou.order.mapper.OrderStatusMapper;
import com.leyou.order.pojo.Order;
import com.leyou.order.pojo.OrderDetail;
import com.leyou.order.pojo.OrderStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import tk.mybatis.mapper.entity.Example;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Service
public class OrderService {
    
    @Autowired
    private IdWorker idWorker;
    
    @Autowired
    private OrderMapper orderMapper;
    
    @Autowired
    private OrderStatusMapper orderStatusMapper;
    
    @Autowired
    private OrderDetailMapper orderDetailMapper;
    
    private final static Logger logger= LoggerFactory.getLogger(OrderService.class);
    
    /**
     * 创建订单
     * @param order
     * @return
     */
    public Long createOrder(Order order) {
        //1.生成orderId
        long orderId = idWorker.nextId();
        //2.获取登录的用户
        UserInfo userInfo = LoginInterceptor.getLoginUser();
        //3.初始化数据
        order.setBuyerNick(userInfo.getUsername());
        order.setBuyerRate(false);
        order.setCreateTime(new Date());
        order.setOrderId(orderId);
        order.setUserId(userInfo.getId());
        //4.保存数据
        this.orderMapper.insertSelective(order);
    
        //5.保存订单状态
        OrderStatus orderStatus = new OrderStatus();
        orderStatus.setOrderId(orderId);
        orderStatus.setCreateTime(order.getCreateTime());
        //初始状态未未付款：1
        orderStatus.setStatus(1);
        //6.保存数据
        this.orderStatusMapper.insertSelective(orderStatus);
    
        //7.在订单详情中添加orderId
        order.getOrderDetails().forEach(orderDetail -> orderDetail.setOrderId(orderId));
        //8.保存订单详情，使用批量插入功能
        this.orderDetailMapper.insertList(order.getOrderDetails());
        //9.减库存
            //--待补充
        
        logger.debug("生成订单，订单编号：{}，用户id：{}", orderId, userInfo.getId());
        return orderId;
    }
    
    /**
     * 根据订单号查询订单
     * @param id
     * @return
     */
    public Order queryOrderById(Long id) {
        //1.查询订单
        Order order = this.orderMapper.selectByPrimaryKey(id);
        //2.查询订单详情
        Example example = new Example(OrderDetail.class);
        example.createCriteria().andEqualTo("orderId",id);
        List<OrderDetail> orderDetail = this.orderDetailMapper.selectByExample(example);
        //3.查询订单状态
        OrderStatus orderStatus = this.orderStatusMapper.selectByPrimaryKey(order.getOrderId());
        //4.order对象填充订单详情
        order.setOrderDetails(orderDetail);
        //5.order对象设置订单状态
        order.setStatus(orderStatus.getStatus());
        //6.返回order
        return order;
    }
    
    /**
     * 分页查询订单
     * @param page
     * @param rows
     * @param status
     * @return
     */
    public PageResult<Order> queryUserOrderList(Integer page, Integer rows, Integer status) {
        PageHelper.startPage(page, rows);
        //2.获取登录用户
        UserInfo userInfo = LoginInterceptor.getLoginUser();
        //3.查询
        Page<Order> pageInfo = (Page<Order>) this.orderMapper.queryOrderList(userInfo.getId(), status);
        //4.填充orderDetail
        List<Order> orderList = pageInfo.getResult();
        orderList.forEach(order -> {
            Example example = new Example(OrderDetail.class);
            example.createCriteria().andEqualTo("orderId",order.getOrderId());
            List<OrderDetail> orderDetailList = this.orderDetailMapper.selectByExample(example);
            order.setOrderDetails(orderDetailList);
        });
        return new PageResult<Order>(pageInfo.getTotal(),pageInfo.getPages(), orderList);
    }
    
    /**
     * 更新
     * @param id
     * @param status
     * @return
     */
    public Boolean updateOrderStatus(Long id, Integer status) {
        OrderStatus orderStatus = new OrderStatus();
        orderStatus.setOrderId(id);
        orderStatus.setStatus(status);
        //1.根据状态判断要修改的时间
        switch (status){
            case 2:
                //2.付款时间
                orderStatus.setPaymentTime(new Date());
                break;
            case 3:
                //3.发货时间
                orderStatus.setConsignTime(new Date());
                break;
            case 4:
                //4.确认收货，订单结束
                orderStatus.setEndTime(new Date());
                break;
            case 5:
                //5.交易失败，订单关闭
                orderStatus.setCloseTime(new Date());
                break;
            case 6:
                //6.评价时间
                orderStatus.setCommentTime(new Date());
                break;
        
            default:
                return null;
        }
        int count = this.orderStatusMapper.updateByPrimaryKeySelective(orderStatus);
        return count == 1;
    }
    
    /**
     * 根据订单号查询商品id
     * @param id
     * @return
     */
    public List<Long> querySkuIdByOrderId(Long id) {
        Example example = new Example(OrderDetail.class);
        example.createCriteria().andEqualTo("orderId",id);
        List<OrderDetail> orderDetailList = this.orderDetailMapper.selectByExample(example);
        List<Long> ids = new ArrayList<>();
        orderDetailList.forEach(orderDetail -> ids.add(orderDetail.getSkuId()));
        return ids;
    }

}
