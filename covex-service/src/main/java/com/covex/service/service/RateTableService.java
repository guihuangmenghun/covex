package com.covex.service.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.covex.common.exception.BizException;
import com.covex.service.entity.RateTableEntity;
import com.covex.service.entity.RateTableRowEntity;
import com.covex.service.mapper.RateTableMapper;
import com.covex.service.mapper.RateTableRowMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * 费率表服务 — 管理费率表元数据、行数据和 Redis 缓存
 */
@Service
public class RateTableService {

    private static final Logger log = LoggerFactory.getLogger(RateTableService.class);
    private static final String REDIS_KEY_PREFIX = "ins:rate:";
    private static final long REDIS_TTL_HOURS = 24;

    private final RateTableMapper rateTableMapper;
    private final RateTableRowMapper rateTableRowMapper;
    private final RedisTemplate<String, Object> redisTemplate;

    public RateTableService(RateTableMapper rateTableMapper,
                            RateTableRowMapper rateTableRowMapper,
                            RedisTemplate<String, Object> redisTemplate) {
        this.rateTableMapper = rateTableMapper;
        this.rateTableRowMapper = rateTableRowMapper;
        this.redisTemplate = redisTemplate;
    }

    // ========== 元数据 CRUD ==========

    @Transactional
    public RateTableEntity createRateTable(RateTableEntity entity) {
        if (entity.getVersion() == null) {
            entity.setVersion("1.0.0");
        }
        rateTableMapper.insert(entity);
        log.info("Rate table created: id={}, code={}, version={}",
                entity.getId(), entity.getRateTableCode(), entity.getVersion());
        return entity;
    }

    public RateTableEntity getById(Long id) {
        RateTableEntity entity = rateTableMapper.selectById(id);
        if (entity == null) {
            throw new BizException(404, "费率表不存在: " + id);
        }
        return entity;
    }

    public RateTableEntity getByCode(String rateTableCode, String version) {
        LambdaQueryWrapper<RateTableEntity> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(RateTableEntity::getRateTableCode, rateTableCode);
        if (version != null) {
            wrapper.eq(RateTableEntity::getVersion, version);
        }
        RateTableEntity entity = rateTableMapper.selectOne(wrapper);
        if (entity == null) {
            throw new BizException(404, "费率表不存在: " + rateTableCode + " v" + version);
        }
        return entity;
    }

    public List<RateTableEntity> listByProductId(Long productId) {
        LambdaQueryWrapper<RateTableEntity> wrapper = new LambdaQueryWrapper<>();
        if (productId != null) {
            wrapper.eq(RateTableEntity::getProductId, productId);
        }
        wrapper.orderByDesc(RateTableEntity::getCreatedAt);
        return rateTableMapper.selectList(wrapper);
    }

    // ========== 行数据 ==========

    @Transactional
    public void importRows(Long tableId, List<RateTableRowEntity> rows) {
        RateTableEntity table = rateTableMapper.selectById(tableId);
        if (table == null) {
            throw new BizException(404, "费率表不存在: " + tableId);
        }

        int count = 0;
        for (RateTableRowEntity row : rows) {
            row.setRateTableId(tableId);
            rateTableRowMapper.insert(row);
            count++;
        }
        log.info("Rate table rows imported: tableId={}, count={}", tableId, count);
    }

    public List<RateTableRowEntity> listRows(Long tableId) {
        LambdaQueryWrapper<RateTableRowEntity> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(RateTableRowEntity::getRateTableId, tableId);
        return rateTableRowMapper.selectList(wrapper);
    }

    // ========== Redis 缓存 ==========

    /**
     * 加载费率表到 Redis Hash
     * Key: ins:rate:{code}:{version}, Field: dimension_key, Value: rate_value
     */
    @Transactional(readOnly = true)
    public int loadToRedis(String tableCode, String version) {
        RateTableEntity table = getByCode(tableCode, version);
        String redisKey = buildRedisKey(tableCode, version);

        // 清除旧缓存
        redisTemplate.delete(redisKey);

        // 加载所有行
        List<RateTableRowEntity> rows = rateTableRowMapper.selectList(
                new LambdaQueryWrapper<RateTableRowEntity>()
                        .eq(RateTableRowEntity::getRateTableId, table.getId()));

        for (RateTableRowEntity row : rows) {
            redisTemplate.opsForHash().put(redisKey, row.getDimensionKey(),
                    row.getRateValue().toPlainString());
        }

        // 设置 TTL
        redisTemplate.expire(redisKey, REDIS_TTL_HOURS, TimeUnit.HOURS);

        log.info("Rate table loaded to Redis: key={}, rows={}", redisKey, rows.size());
        return rows.size();
    }

    /**
     * 查询费率 — 先查 Redis，miss 则查 DB 并回填
     */
    public BigDecimal queryRate(String tableCode, String version, String dimensionKey) {
        String redisKey = buildRedisKey(tableCode, version);

        // 先查 Redis
        Object cached = redisTemplate.opsForHash().get(redisKey, dimensionKey);
        if (cached != null) {
            log.debug("Rate cache hit: key={}, dimension={}", redisKey, dimensionKey);
            return new BigDecimal(cached.toString());
        }

        // Cache miss → 查 DB
        log.debug("Rate cache miss: key={}, dimension={}", redisKey, dimensionKey);
        RateTableEntity table = getByCode(tableCode, version);
        LambdaQueryWrapper<RateTableRowEntity> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(RateTableRowEntity::getRateTableId, table.getId())
               .eq(RateTableRowEntity::getDimensionKey, dimensionKey);
        RateTableRowEntity row = rateTableRowMapper.selectOne(wrapper);

        if (row == null) {
            throw new BizException(404, "费率不存在: " + tableCode + "/" + dimensionKey);
        }

        // 回填 Redis
        redisTemplate.opsForHash().put(redisKey, dimensionKey, row.getRateValue().toPlainString());
        // 如果没有设置 TTL，设置一个
        if (Boolean.FALSE.equals(redisTemplate.hasKey(redisKey))) {
            redisTemplate.expire(redisKey, REDIS_TTL_HOURS, TimeUnit.HOURS);
        }

        return row.getRateValue();
    }

    /**
     * 清除费率表 Redis 缓存
     */
    public void evictFromRedis(String tableCode, String version) {
        String redisKey = buildRedisKey(tableCode, version);
        redisTemplate.delete(redisKey);
        log.info("Rate table cache evicted: key={}", redisKey);
    }

    private String buildRedisKey(String tableCode, String version) {
        return REDIS_KEY_PREFIX + tableCode + ":" + version;
    }
}
