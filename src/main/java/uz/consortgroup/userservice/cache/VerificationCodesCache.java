package uz.consortgroup.userservice.cache;

import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Component;
import uz.consortgroup.userservice.entity.VerificationCode;
import uz.consortgroup.userservice.entity.cacheEntity.VerificationCodeCacheEntity;
import uz.consortgroup.userservice.mapper.VerificationCodeCacheMapper;
import uz.consortgroup.userservice.repository.VerificationCodeRepository;
import uz.consortgroup.userservice.service.cache.VerificationCodeCacheServiceImpl;

import java.util.List;
import java.util.UUID;

@Component
public class VerificationCodesCache extends AbstractCacheWarmup<VerificationCode, VerificationCodeCacheEntity> {
    private static final String VERIFICATION_CODES_CACHE = "verificationCodesCache";

    private final VerificationCodeCacheServiceImpl verificationCodeCacheServiceImpl;
    private final VerificationCodeRepository verificationCodeRepository;
    private final VerificationCodeCacheMapper verificationCodeCacheMapper;

    public VerificationCodesCache(ThreadPoolTaskExecutor taskExecutor,
                                  VerificationCodeCacheServiceImpl verificationCodeCacheServiceImpl,
                                  VerificationCodeRepository verificationCodeRepository,
                                  VerificationCodeCacheMapper verificationCodeCacheMapper) {
        super(taskExecutor);
        this.verificationCodeCacheServiceImpl = verificationCodeCacheServiceImpl;
        this.verificationCodeRepository = verificationCodeRepository;
        this.verificationCodeCacheMapper = verificationCodeCacheMapper;
    }

    @Override
    protected List<VerificationCode> fetchBatch(UUID lastId, int batchSize) {
        return verificationCodeRepository.findCodesBatch(lastId, batchSize);
    }

    @Override
    protected UUID getLastId(List<VerificationCode> entities) {
        return entities.get(entities.size() - 1).getId();
    }

    @Override
    protected VerificationCodeCacheEntity mapToCacheEntity(VerificationCode entity) {
        return verificationCodeCacheMapper.toVerificationCodeCacheEntity(entity);
    }

    @Override
    protected void saveCache(List<VerificationCodeCacheEntity> cacheEntities) {
        verificationCodeCacheServiceImpl.saveVerificationCodes(cacheEntities);
    }

    @Override
    protected String getCacheName() {
        return VERIFICATION_CODES_CACHE;
    }
}
