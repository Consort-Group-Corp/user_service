package uz.consortgroup.user_service.cache;

import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Component;
import uz.consortgroup.user_service.entity.VerificationCode;
import uz.consortgroup.user_service.entity.cacheEntity.VerificationCodeCacheEntity;
import uz.consortgroup.user_service.mapper.VerificationCodeCacheMapper;
import uz.consortgroup.user_service.repository.VerificationCodeRepository;
import uz.consortgroup.user_service.service.VerificationCodeCacheService;

import java.util.List;

@Component
public class VerificationCodesCache extends AbstractCacheWarmup<VerificationCode, VerificationCodeCacheEntity> {
    private static final String VERIFICATION_CODES_CACHE = "verificationCodesCache";

    private final VerificationCodeCacheService verificationCodeCacheService;
    private final VerificationCodeRepository verificationCodeRepository;
    private final VerificationCodeCacheMapper verificationCodeCacheMapper;

    public VerificationCodesCache(ThreadPoolTaskExecutor taskExecutor,
                                  VerificationCodeCacheService verificationCodeCacheService,
                                  VerificationCodeRepository verificationCodeRepository,
                                  VerificationCodeCacheMapper verificationCodeCacheMapper) {
        super(taskExecutor);
        this.verificationCodeCacheService = verificationCodeCacheService;
        this.verificationCodeRepository = verificationCodeRepository;
        this.verificationCodeCacheMapper = verificationCodeCacheMapper;
    }

    @Override
    protected List<VerificationCode> fetchBatch(Long lastId, int batchSize) {
        return verificationCodeRepository.findCodesBatch(lastId, batchSize);
    }

    @Override
    protected Long getLastId(List<VerificationCode> entities) {
        return entities.get(entities.size() - 1).getId();
    }

    @Override
    protected VerificationCodeCacheEntity mapToCacheEntity(VerificationCode entity) {
        return verificationCodeCacheMapper.toVerificationCodeCacheEntity(entity);
    }

    @Override
    protected void saveCache(List<VerificationCodeCacheEntity> cacheEntities) {
        verificationCodeCacheService.saveVerificationCodes(cacheEntities);
    }

    @Override
    protected String getCacheName() {
        return VERIFICATION_CODES_CACHE;
    }
}
