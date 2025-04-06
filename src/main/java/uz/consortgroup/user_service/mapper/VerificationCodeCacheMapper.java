package uz.consortgroup.user_service.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;
import uz.consortgroup.user_service.entity.VerificationCode;
import uz.consortgroup.user_service.entity.cacheEntity.VerificationCodeCacheEntity;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface VerificationCodeCacheMapper {
    VerificationCodeCacheEntity toVerificationCodeCacheEntity(VerificationCode verificationCode);
    VerificationCode toVerificationCode(VerificationCodeCacheEntity verificationCodeCacheEntity);
}
