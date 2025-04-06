package uz.consortgroup.userservice.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;
import uz.consortgroup.userservice.entity.VerificationCode;
import uz.consortgroup.userservice.entity.cacheEntity.VerificationCodeCacheEntity;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface VerificationCodeCacheMapper {
    VerificationCodeCacheEntity toVerificationCodeCacheEntity(VerificationCode verificationCode);
    VerificationCode toVerificationCode(VerificationCodeCacheEntity verificationCodeCacheEntity);
}
