package uz.consortgroup.userservice.service.one_id;

import uz.consortgroup.core.api.v1.dto.oneid.OneIdProfile;
import uz.consortgroup.core.api.v1.dto.oneid.OneIdTokenResponse;
import uz.consortgroup.core.api.v1.dto.user.auth.JwtResponse;
import uz.consortgroup.userservice.entity.User;

public interface OneIdService {
    String buildAuthUrl();
    OneIdTokenResponse exchangeCodeForTokens(String code);
    OneIdProfile fetchProfile(String accessToken);
    User processUserFromOneId(OneIdTokenResponse tokens, OneIdProfile profile);
    JwtResponse authorizeViaOneId(String code);
}
