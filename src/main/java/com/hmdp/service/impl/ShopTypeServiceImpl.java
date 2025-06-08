package com.hmdp.service.impl;

import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.hmdp.dto.Result;
import com.hmdp.entity.ShopType;
import com.hmdp.mapper.ShopTypeMapper;
import com.hmdp.service.IShopTypeService;
import com.hmdp.utils.RedisConstants;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * <p>
 *  服务实现类
 * </p>
 *
 * @author 虎哥
 * @since 2021-12-22
 */
@Service
public class ShopTypeServiceImpl extends ServiceImpl<ShopTypeMapper, ShopType> implements IShopTypeService {

    @Resource
    private StringRedisTemplate stringRedisTemplate;

    @Override
    public Result queryTypeList() {
        String cacheListKey = RedisConstants.CACHE_SHOP_TYPE_LIST;

        // 1. 先查Redis缓存
        Long size = stringRedisTemplate.opsForList().size(cacheListKey);
        List<ShopType> shopTypes = new ArrayList<>();

        if (size != null && size > 0) {
            // 存在缓存，直接返回
            List<String> cacheQueryTypeList = stringRedisTemplate.opsForList().range(cacheListKey, 0, size - 1);
            if (CollectionUtil.isNotEmpty(cacheQueryTypeList)) {
                shopTypes = cacheQueryTypeList.stream()
                        .map(x -> JSONUtil.toBean(x, ShopType.class))
                        .collect(Collectors.toList());
                return Result.ok(shopTypes);
            }
        }

        // 2. 没有缓存则查询数据库
        shopTypes = query().orderByAsc("sort").list();

        // 3. 数据库无数据时，防止穿透，缓存空值一段时间
        if (CollectionUtil.isEmpty(shopTypes)) {
            stringRedisTemplate.opsForValue().set(cacheListKey, JSONUtil.toJsonStr(new ArrayList<>()), 5, TimeUnit.MINUTES);
            return Result.ok(shopTypes);
        }

        // 4. 构建缓存，并解决缓存雪崩问题，增加随机过期时间
        List<String> redisShopTypeList = shopTypes.stream()
                .map(JSONUtil::toJsonStr)
                .collect(Collectors.toList());

        stringRedisTemplate.opsForList().rightPushAll(cacheListKey, redisShopTypeList);

        // 设置过期时间加上随机值，避免缓存同时失效导致雪崩
        int expireTime = 2 * 60 + new Random().nextInt(60); // 2小时 + 随机分钟
        stringRedisTemplate.expire(cacheListKey, expireTime, TimeUnit.MINUTES);

        return Result.ok(shopTypes);
    }
}
